package de.tum.bgu.msm.moped.modules.destinationChoice;

import cern.colt.list.tfloat.FloatArrayList;
import cern.colt.list.tint.IntArrayList;
import cern.colt.map.tfloat.OpenIntFloatHashMap;
import cern.jet.math.tfloat.FloatFunctions;
import cern.jet.stat.tfloat.FloatDescriptive;
import com.google.common.collect.Iterables;
import com.google.common.math.LongMath;
import com.google.common.util.concurrent.AtomicDouble;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.MopedZone;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.data.SuperPAZ;
import de.tum.bgu.msm.moped.io.input.readers.OHASTripReader;
import de.tum.bgu.msm.moped.io.input.readers.SuperPAZAttributesReader;
import de.tum.bgu.msm.moped.io.input.readers.ZoneAttributesReader;
import de.tum.bgu.msm.moped.io.input.readers.ZonesReader;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import de.tum.bgu.msm.moped.util.MoPeDUtil;
import de.tum.bgu.msm.moped.util.concurrent.ConcurrentExecutor;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Geometry;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.api.core.v01.population.Person;
import org.matsim.contrib.dvrp.router.DistanceAsTravelDisutility;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.router.*;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.router.util.TravelDisutility;
import org.matsim.core.router.util.TravelTime;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.vehicles.Vehicle;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public abstract class TripDistributor_Validation {

    private static final Logger logger = Logger.getLogger(TripDistributor_Validation.class);
    public final static Boolean ACCESSEGRESS = Boolean.TRUE;
    public final static AtomicDouble FAILEDTRIPS = new AtomicDouble(0.0);
    protected final DataSet dataSet;
    protected final Purpose purpose;
    protected Map<Integer, Float> destinationUtility = new HashMap<>();
    protected OpenIntFloatHashMap destinationUtilityPAZ = new OpenIntFloatHashMap();
    private final Map<Purpose, Float> coefNoCarByPurpose = new HashMap<Purpose, Float>(){{
        put(Purpose.HBW, -1.588032f);
        put(Purpose.HBSHOP, -2.127f);//method 2: -2.281f;method 3: -2.127f
        put(Purpose.HBREC, -2.232597f);
        put(Purpose.HBOTH, -2.616255f);
        put(Purpose.NHBW, -2.540999f);
        put(Purpose.NHBNW, -2.639274f);
    }};

    //private final double intercept = -0.761;//-0.261;

    public TripDistributor_Validation(DataSet dataSet, Purpose purpose) {
        this.dataSet = dataSet;
        this.purpose = purpose;
    }

    public void run (){
        logger.info("  Reading input data for MoPeD");
        new ZonesReader(dataSet).read();
        logger.info(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
        new ZoneAttributesReader(dataSet).read();
        logger.info(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
        new SuperPAZAttributesReader(dataSet).read();
        logger.info(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
        new OHASTripReader(dataSet).read();
        logger.info("  Done reading input data!");

        calculateDestinationUtility();
        logger.info("utility: " + destinationUtility.size());
        calculateDestinationUtilityPAZ();
        logger.info("utility: " + destinationUtilityPAZ.size());


        //String output_superPAZ = Resources.INSTANCE.getString(Properties.BASE) + Resources.INSTANCE.getString(Properties.OUTPUT_OD)+"_superPAZ_"+purpose.toString()+".csv";
        //String output_PAZ = Resources.INSTANCE.getString(Properties.BASE) + Resources.INSTANCE.getString(Properties.OUTPUT_OD)+"_PAZ_"+purpose.toString()+".csv";
        //twoStageTripDistributor_OHAS(Resources.INSTANCE.getString(Properties.MATSIMNETWORK),output_superPAZ,output_PAZ);

        //For monte carlo test
        //String output_montecarlo = Resources.INSTANCE.getString(Properties.BASE) + Resources.INSTANCE.getString(Properties.OUTPUT_OD)+"_MonteCarlo_"+purpose.toString()+".csv";
        //twoStageTripDistributor_OHAS_MonteCarlo(Resources.INSTANCE.getString(Properties.MATSIMNETWORK),output_montecarlo);
        //For monte carlo test end

        String output_distance = Resources.INSTANCE.getString(Properties.BASE) + Resources.INSTANCE.getString(Properties.OUTPUT_OD)+"_impedance_OHAS_crossMotorway.csv";
        distanceGenerator_OHAS(Resources.INSTANCE.getString(Properties.MATSIMNETWORK),output_distance);
        logger.warn(FAILEDTRIPS + " trips can not be distributed to paz!");
    }

    private void twoStageTripDistributor_OHAS (String networkFile, String output_superPAZ, String output_PAZ) {
        final int partitionSize = (int) ((double) dataSet.getOriginPAZs().keySet().size() / Runtime.getRuntime().availableProcessors()) + 1;
        Iterable<List<MopedZone>> partitions = Iterables.partition(dataSet.getOriginPAZs().values(), partitionSize);
        logger.info("total zones: " + dataSet.getZones().size());
        logger.info(dataSet.getOriginPAZs().keySet().size() + " zones, " + Runtime.getRuntime().availableProcessors() + " processors, with " + partitionSize);

        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(networkFile);

        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Runtime.getRuntime().availableProcessors());

        AtomicInteger counter = new AtomicInteger();
        FastMultiNodeDijkstraFactory fastMultiNodeDijkstraFactory = new FastMultiNodeDijkstraFactory(true);

        StringBuilder header_superPAZ = new StringBuilder();
        header_superPAZ.append("origin_paz,destination_superPAZ,prob_superPAZ,dist");
        header_superPAZ.append('\n');
        try {
            writeToFile(output_superPAZ,header_superPAZ.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        StringBuilder header_PAZ = new StringBuilder();
        header_PAZ.append("origin_paz,destination_superPAZ,destination_PAZ,prob_PAZ,dist");
        header_PAZ.append('\n');
        try {
            writeToFile(output_PAZ,header_PAZ.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for (final List<MopedZone> partition : partitions) {
            MultiNodePathCalculator pathCalculator = (MultiNodePathCalculator) fastMultiNodeDijkstraFactory.createPathCalculator(network, new DistanceAsTravelDisutility(), (link, v, person, vehicle) -> link.getLength());
            MultiNodePathCalculator subPathCalculator = (MultiNodePathCalculator) fastMultiNodeDijkstraFactory.createPathCalculator(network, new DistanceAsTravelDisutility(), (link, v, person, vehicle) -> link.getLength());

            executor.addTaskToQueue(() -> {
                try {
                    int id = counter.incrementAndGet();
                    int counterr = 0;

                    for (MopedZone origin : partition) {
                        if(LongMath.isPowerOfTwo(counterr)) {
                            logger.info(counterr + " in " + id);
                        };

                        //generate toNodes vector (toSuperPAZ) and calculate Least Cost Path to all destination nodes
                        Set<InitialNode> toNodes = new HashSet<>();
                        Coord originCoord = CoordUtils.createCoord(((Geometry)(origin.getShapeFeature().getDefaultGeometry())).getCentroid().getCoordinate());
                        Node originNode = NetworkUtils.getNearestNode(network, originCoord);
                        float accessDistance = (float) (CoordUtils.calcEuclideanDistance(originCoord,originNode.getCoord()));
                        Collection<SuperPAZ> destinationZones = dataSet.getSuperPAZSearchTree().getDisk(originCoord.getX(),originCoord.getY(),Double.parseDouble(Resources.INSTANCE.getString(Properties.DESTSEARCHDISTANCE)));

                        for(SuperPAZ destination : destinationZones){
                            Coord destinationCoord = CoordUtils.createCoord(((Geometry)(destination.getShapeFeature().getDefaultGeometry())).getCentroid().getCoordinate());
                            Node destinationNode = NetworkUtils.getNearestNode(network, destinationCoord);
                            toNodes.add(new InitialNode(destinationNode, 0., 0.));
                        }

                        ImaginaryNode aggregatedToNodes = MultiNodeDijkstra.createImaginaryNode(toNodes);
                        pathCalculator.calcLeastCostPath(originNode, aggregatedToNodes, 8*3600, null, null);

                        //calculate utility for superPAZ
                        OpenIntFloatHashMap utilityList = new OpenIntFloatHashMap();
                        OpenIntFloatHashMap impedanceList = new OpenIntFloatHashMap();

                        StringBuilder distributionList_superPAZ = new StringBuilder();
                        StringBuilder distributionList_PAZ = new StringBuilder();
                        for (SuperPAZ destination : destinationZones) {
                            float travelDistance = 0.0f;
                            if(destination.getSuperPAZId()==origin.getSuperPAZId()){
                                travelDistance = 0.282f;//half of the superpaz diagonal
                            }else {
                                Coord destinationCoord = CoordUtils.createCoord(((Geometry) (destination.getShapeFeature().getDefaultGeometry())).getCentroid().getCoordinate());
                                Node destinationNode = NetworkUtils.getNearestNode(network, destinationCoord);
                                float egressDistance = (float) (CoordUtils.calcEuclideanDistance(destinationCoord, destinationNode.getCoord()));
                                LeastCostPathCalculator.Path path = pathCalculator.constructPath(originNode, destinationNode, 8 * 3600);
                                if (path == null) {
                                    continue;
                                }

                                if(ACCESSEGRESS){
                                    if(accessDistance>path.travelTime){
                                        travelDistance = travelDistance + accessDistance;
                                    }

                                    if(egressDistance>path.travelTime){
                                        travelDistance = travelDistance + egressDistance;
                                    }
                                    travelDistance = (float) ((travelDistance + path.travelTime) / 1000.0f);
                                }else{
                                    travelDistance = (float) ((path.travelTime) / 1000.0f);
                                }

                                if (travelDistance > 6) {
                                    continue;
                                }
                            }
                            float utility = travelDistance * coefNoCarByPurpose.get(purpose) + destinationUtility.get(destination.getIndex());
                            if (Float.isInfinite(utility) || Float.isNaN(utility)) {
                                throw new RuntimeException(utility + " utility calculated! Please check calculation!" +
                                        " Origin: " + origin.getZoneId() + " | Destination: " + destination.getSuperPAZId() + " | Distance: "
                                        + travelDistance + " | attraction: " + destinationUtility.get(destination.getIndex()));
                            }
                            utilityList.put(destination.getSuperPAZId(), (float) Math.exp(utility));
                            impedanceList.put(destination.getSuperPAZId(),travelDistance);

                        }
                        //utilityList = topN(utilityList, 25);
                        float sum = FloatDescriptive.sum(utilityList.values());
                        utilityList.assign(FloatFunctions.div(sum));

                        for ( int destinationId : utilityList.keys().elements()) {
                            SuperPAZ destination = dataSet.getSuperPAZ(destinationId);
                            //generate toNodes vector (toPAZ) and calculate Least Cost Path to all destination nodes
                            OpenIntFloatHashMap utilityListPAZ = new OpenIntFloatHashMap();
                            OpenIntFloatHashMap impedanceListPAZ = new OpenIntFloatHashMap();

                            Set<InitialNode> toPAZs = new HashSet<>();
                            for (int paz : destination.getPazs().toArray()) {
                                Coord destinationPAZCoord = CoordUtils.createCoord(((Geometry) (dataSet.getZone(paz).getShapeFeature().getDefaultGeometry())).getCentroid().getCoordinate());
                                Node destinationPAZNode = NetworkUtils.getNearestNode(network, destinationPAZCoord);
                                toPAZs.add(new InitialNode(destinationPAZNode, 0., 0.));
                            }

                            ImaginaryNode aggregatedToPAZs = MultiNodeDijkstra.createImaginaryNode(toPAZs);
                            subPathCalculator.calcLeastCostPath(originNode, aggregatedToPAZs, 8 * 3600, null, null);

                            //calculate utility for PAZ
                            for (int paz : destination.getPazs().toArray()) {
                                float travelDistancePAZ = 0.0f;
                                if(paz == origin.getZoneId()){
                                    travelDistancePAZ = 0.056f;//half of the paz diagonal dist
                                }else {
                                    Coord destinationPAZCoord = CoordUtils.createCoord(((Geometry) (dataSet.getZone(paz).getShapeFeature().getDefaultGeometry())).getCentroid().getCoordinate());
                                    Node destinationPAZNode = NetworkUtils.getNearestNode(network, destinationPAZCoord);
                                    float egressDistance = (float) (CoordUtils.calcEuclideanDistance(destinationPAZCoord, destinationPAZNode.getCoord()));
                                    LeastCostPathCalculator.Path path = subPathCalculator.constructPath(originNode, destinationPAZNode, 8 * 3600);
                                    if (path == null) {
                                        continue;
                                    }

                                    if(ACCESSEGRESS){
                                        if(accessDistance>path.travelTime){
                                            travelDistancePAZ = travelDistancePAZ + accessDistance;
                                        }

                                        if(egressDistance>path.travelTime){
                                            travelDistancePAZ = travelDistancePAZ + egressDistance;
                                        }
                                        travelDistancePAZ = (float) ((travelDistancePAZ + path.travelTime) / 1000.0f);
                                    }else{
                                        travelDistancePAZ = (float) ((path.travelTime) / 1000.0f);
                                    }

                                    if (travelDistancePAZ > 6) {
                                        continue;
                                    }
                                }
                                float utilityPAZ = travelDistancePAZ * coefNoCarByPurpose.get(purpose) + destinationUtilityPAZ.get(paz);
                                //float utilityPAZ = destinationUtilityPAZ.get(paz);

                                if (Float.isInfinite(utilityPAZ) || Float.isNaN(utilityPAZ)) {
                                    throw new RuntimeException(utilityPAZ + " utility calculated! Please check calculation!" +
                                            " Origin PAZ: " + origin.getZoneId() + " | Destination PAZ: " + paz + " | Distance: "
                                            + travelDistancePAZ + " | attraction: " + destinationUtilityPAZ.get(paz));
                                }
                                float expUtility = (float) Math.exp(utilityPAZ);
                                if(expUtility != 0.0f) {
                                    utilityListPAZ.put(paz, expUtility);
                                    impedanceListPAZ.put(paz, travelDistancePAZ);
                                }
                            }

                            float sumPAZ = FloatDescriptive.sum(utilityListPAZ.values());
                            if (sumPAZ == 0.0f) {
                                FAILEDTRIPS.getAndAdd(utilityList.get(destination.getSuperPAZId()));
                                continue;
                            }

                            utilityListPAZ.assign(FloatFunctions.div(sumPAZ));


                            for (int paz : utilityListPAZ.keys().elements()) {
                                distributionList_PAZ.append(origin.getZoneId());
                                distributionList_PAZ.append(',');
                                distributionList_PAZ.append(destinationId);
                                distributionList_PAZ.append(',');
                                distributionList_PAZ.append(paz);
                                distributionList_PAZ.append(',');
                                distributionList_PAZ.append(utilityListPAZ.get(paz));
                                distributionList_PAZ.append(',');
                                distributionList_PAZ.append((short)Math.round(impedanceListPAZ.get(paz)*1000));
                                distributionList_PAZ.append('\n');
                            }

                            distributionList_superPAZ.append(origin.getZoneId());
                            distributionList_superPAZ.append(',');
                            distributionList_superPAZ.append(destinationId);
                            distributionList_superPAZ.append(',');
                            distributionList_superPAZ.append(utilityList.get(destinationId));
                            distributionList_superPAZ.append(',');
                            distributionList_superPAZ.append((short)Math.round(impedanceList.get(destinationId)*1000));
                            distributionList_superPAZ.append('\n');
                        }
                        counterr++;
                        try {
                            writeToFile(output_PAZ,distributionList_PAZ.toString());
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        try {
                            writeToFile(output_superPAZ,distributionList_superPAZ.toString());
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.warn(e.getLocalizedMessage());
                    throw new RuntimeException(e);
                }
                return null;
            });
        }
        executor.execute();
    }


    private void twoStageTripDistributor_OHAS_MonteCarlo (String networkFile, String output_montecarlo) {
        final int partitionSize = (int) ((double) dataSet.getOhasTripMap().keySet().size() / Runtime.getRuntime().availableProcessors()) + 1;
        Iterable<List<Integer>> partitions = Iterables.partition(dataSet.getOhasTripMap().keySet(), partitionSize);
        logger.info("total zones: " + dataSet.getZones().size());
        logger.info(dataSet.getOhasTripMap().keySet().size() + " zones, " + Runtime.getRuntime().availableProcessors() + " processors, with " + partitionSize);

        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(networkFile);

        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Runtime.getRuntime().availableProcessors());

        AtomicInteger counter = new AtomicInteger();
        FastMultiNodeDijkstraFactory fastMultiNodeDijkstraFactory = new FastMultiNodeDijkstraFactory(true);


        StringBuilder header_PAZ = new StringBuilder();
        header_PAZ.append("tripId,origin_paz,destination_superPAZ,destination_PAZ,dist");
        header_PAZ.append('\n');
        try {
            writeToFile(output_montecarlo,header_PAZ.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for (final List<Integer> partition : partitions) {
            MultiNodePathCalculator pathCalculator = (MultiNodePathCalculator) fastMultiNodeDijkstraFactory.createPathCalculator(network, new DistanceAsTravelDisutility(), (link, v, person, vehicle) -> link.getLength());
            MultiNodePathCalculator subPathCalculator = (MultiNodePathCalculator) fastMultiNodeDijkstraFactory.createPathCalculator(network, new DistanceAsTravelDisutility(), (link, v, person, vehicle) -> link.getLength());

            executor.addTaskToQueue(() -> {
                try {
                    int id = counter.incrementAndGet();
                    int counterr = 0;

                    for (int tripid : partition) {
                        MopedZone origin = dataSet.getOhasTripMap().get(tripid);
                        if(LongMath.isPowerOfTwo(counterr)) {
                            logger.info(counterr + " in " + id);
                        };

                        //generate toNodes vector (toSuperPAZ) and calculate Least Cost Path to all destination nodes
                        Set<InitialNode> toNodes = new HashSet<>();
                        Coord originCoord = CoordUtils.createCoord(((Geometry)(origin.getShapeFeature().getDefaultGeometry())).getCentroid().getCoordinate());
                        Node originNode = NetworkUtils.getNearestNode(network, originCoord);
                        float accessDistance = (float) (CoordUtils.calcEuclideanDistance(originCoord,originNode.getCoord()));
                        Collection<SuperPAZ> destinationZones = dataSet.getSuperPAZSearchTree().getDisk(originCoord.getX(),originCoord.getY(),Double.parseDouble(Resources.INSTANCE.getString(Properties.DESTSEARCHDISTANCE)));

                        for(SuperPAZ destination : destinationZones){
                            Coord destinationCoord = CoordUtils.createCoord(((Geometry)(destination.getShapeFeature().getDefaultGeometry())).getCentroid().getCoordinate());
                            Node destinationNode = NetworkUtils.getNearestNode(network, destinationCoord);
                            toNodes.add(new InitialNode(destinationNode, 0., 0.));
                        }

                        ImaginaryNode aggregatedToNodes = MultiNodeDijkstra.createImaginaryNode(toNodes);
                        pathCalculator.calcLeastCostPath(originNode, aggregatedToNodes, 8*3600, null, null);

                        //calculate utility for superPAZ
                        OpenIntFloatHashMap utilityList = new OpenIntFloatHashMap();
                        OpenIntFloatHashMap impedanceList = new OpenIntFloatHashMap();

                        StringBuilder distributionList_PAZ = new StringBuilder();
                        for (SuperPAZ destination : destinationZones) {
                            float travelDistance = 0.0f;
                            Coord destinationCoord = CoordUtils.createCoord(((Geometry) (destination.getShapeFeature().getDefaultGeometry())).getCentroid().getCoordinate());
                            Node destinationNode = NetworkUtils.getNearestNode(network, destinationCoord);
                            float egressDistance = (float) (CoordUtils.calcEuclideanDistance(destinationCoord,destinationNode.getCoord()));
                            LeastCostPathCalculator.Path path = pathCalculator.constructPath(originNode, destinationNode, 8 * 3600);
                            if(path == null){
                               continue;
                            }

                            if(ACCESSEGRESS){
                                if(accessDistance>path.travelTime){
                                    travelDistance = travelDistance + accessDistance;
                                }

                                if(egressDistance>path.travelTime){
                                    travelDistance = travelDistance + egressDistance;
                                }
                                travelDistance = (float) ((travelDistance + path.travelTime) / 1000.0f);
                            }else{
                                travelDistance = (float) ((path.travelTime) / 1000.0f);
                            }

                            if (travelDistance > 4.8){
                                continue;
                            }
                            float utility = travelDistance * coefNoCarByPurpose.get(purpose) + destinationUtility.get(destination.getIndex());
                            if (Float.isInfinite(utility) || Float.isNaN(utility)) {
                                throw new RuntimeException(utility + " utility calculated! Please check calculation!" +
                                        " Origin: " + origin.getZoneId() + " | Destination: " + destination.getSuperPAZId() + " | Distance: "
                                        + travelDistance + " | attraction: " + destinationUtility.get(destination.getIndex()));
                            }
                            utilityList.put(destination.getSuperPAZId(), (float) Math.exp(utility));
                            impedanceList.put(destination.getSuperPAZId(),travelDistance);

                        }
                        //utilityList = topN(utilityList, 25);
                        float sum = FloatDescriptive.sum(utilityList.values());
                        if (sum == 0.0f) {
                            FAILEDTRIPS.getAndAdd(1);
                            continue;
                        }
                        final int selectedSuperPAZId = MoPeDUtil.select(utilityList, MoPeDUtil.getRandomObject());

                        SuperPAZ destination = dataSet.getSuperPAZ(selectedSuperPAZId);
                        //generate toNodes vector (toPAZ) and calculate Least Cost Path to all destination nodes
                        OpenIntFloatHashMap utilityListPAZ = new OpenIntFloatHashMap();
                        OpenIntFloatHashMap impedanceListPAZ = new OpenIntFloatHashMap();

                        Set<InitialNode> toPAZs = new HashSet<>();
                        for (int paz : destination.getPazs().toArray()) {
                                Coord destinationPAZCoord = CoordUtils.createCoord(((Geometry) (dataSet.getZone(paz).getShapeFeature().getDefaultGeometry())).getCentroid().getCoordinate());
                                Node destinationPAZNode = NetworkUtils.getNearestNode(network, destinationPAZCoord);
                                toPAZs.add(new InitialNode(destinationPAZNode, 0., 0.));
                        }

                        ImaginaryNode aggregatedToPAZs = MultiNodeDijkstra.createImaginaryNode(toPAZs);
                        subPathCalculator.calcLeastCostPath(originNode, aggregatedToPAZs, 8 * 3600, null, null);

                        //calculate utility for PAZ
                        for (int paz : destination.getPazs().toArray()) {
                            float travelDistancePAZ = 0.0f;
                                Coord destinationPAZCoord = CoordUtils.createCoord(((Geometry) (dataSet.getZone(paz).getShapeFeature().getDefaultGeometry())).getCentroid().getCoordinate());
                                Node destinationPAZNode = NetworkUtils.getNearestNode(network, destinationPAZCoord);
                                float egressDistance = (float) (CoordUtils.calcEuclideanDistance(destinationPAZCoord,destinationPAZNode.getCoord()));
                                LeastCostPathCalculator.Path path = subPathCalculator.constructPath(originNode, destinationPAZNode, 8 * 3600);
                                if(path == null){
                                    continue;
                                }

                            if(ACCESSEGRESS){
                                if(accessDistance>path.travelTime){
                                    travelDistancePAZ = travelDistancePAZ + accessDistance;
                                }

                                if(egressDistance>path.travelTime){
                                    travelDistancePAZ = travelDistancePAZ + egressDistance;
                                }
                                travelDistancePAZ = (float) ((travelDistancePAZ + path.travelTime) / 1000.0f);
                            }else{
                                travelDistancePAZ = (float) ((path.travelTime) / 1000.0f);
                            }

                                if(travelDistancePAZ > 4.8){
                                    continue;
                                }

                                float utilityPAZ = travelDistancePAZ * coefNoCarByPurpose.get(purpose) + destinationUtilityPAZ.get(paz);
                                //float utilityPAZ = destinationUtilityPAZ.get(paz);

                                if (Float.isInfinite(utilityPAZ) || Float.isNaN(utilityPAZ)) {
                                    throw new RuntimeException(utilityPAZ + " utility calculated! Please check calculation!" +
                                            " Origin PAZ: " + origin.getZoneId() + " | Destination PAZ: " + paz + " | Distance: "
                                            + travelDistancePAZ + " | attraction: " + destinationUtilityPAZ.get(paz));
                                }
                                float expUtility = (float) Math.exp(utilityPAZ);
                                if(expUtility != 0.0f) {
                                    utilityListPAZ.put(paz, expUtility);
                                    impedanceListPAZ.put(paz, travelDistancePAZ);
                                }
                        }


                        float sumPAZ = FloatDescriptive.sum(utilityListPAZ.values());
                        if (sumPAZ == 0.0f) {
                                FAILEDTRIPS.getAndAdd(utilityList.get(destination.getSuperPAZId()));
                                continue;
                        }

                        final int selectedPAZId = MoPeDUtil.select(utilityListPAZ, MoPeDUtil.getRandomObject());
                        counterr++;
                        distributionList_PAZ.append(tripid);
                        distributionList_PAZ.append(',');
                        distributionList_PAZ.append(origin.getZoneId());
                        distributionList_PAZ.append(',');
                        distributionList_PAZ.append(selectedSuperPAZId);
                        distributionList_PAZ.append(',');
                        distributionList_PAZ.append(selectedPAZId);
                        distributionList_PAZ.append(',');
                        distributionList_PAZ.append((short)Math.round(impedanceListPAZ.get(selectedPAZId)*1000));
                        distributionList_PAZ.append('\n');


                        try {
                            writeToFile(output_montecarlo,distributionList_PAZ.toString());
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.warn(e.getLocalizedMessage());
                    throw new RuntimeException(e);
                }
                return null;
            });
        }
        executor.execute();
    }


    private void distanceGenerator_OHAS (String networkFile, String output_distance) {
        Set<MopedZone> zones = new HashSet<>();
        for(MopedZone zone : dataSet.getOhasTripMap().values()){
            if(!zones.contains(zone)){
               zones.add(zone);
            }
        }

        final int partitionSize = (int) ((double) zones.size() / Runtime.getRuntime().availableProcessors()) + 1;
        Iterable<List<MopedZone>> partitions = Iterables.partition(zones, partitionSize);
        logger.info("total OHAS trips: " + dataSet.getOhasTripMap().size());
        logger.info("total zones: " + dataSet.getZones().size());
        logger.info(zones.size() + " OHAS zones, " + Runtime.getRuntime().availableProcessors() + " processors, with " + partitionSize);

        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(networkFile);

        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Runtime.getRuntime().availableProcessors());

        AtomicInteger counter = new AtomicInteger();
        FastMultiNodeDijkstraFactory fastMultiNodeDijkstraFactory = new FastMultiNodeDijkstraFactory(true);


        StringBuilder header_PAZ = new StringBuilder();
        header_PAZ.append("origin_paz,destination_superPAZ,dist,highCarDist,lowCarDist,noCarDist,primaryDist,secondaryDist,trunkDist,tertiaryDist,residentialDist,living_streetDist,pedestrianDist,unclassifiedDist,accessDist,egressDist,totalNodes,crossMotorway");
        header_PAZ.append('\n');
        try {
            writeToFile(output_distance,header_PAZ.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for (final List<MopedZone> partition : partitions) {
           TravelDisutility disutility = new TravelDisutility() {
                private TravelDisutility delegate = new DistanceAsTravelDisutility();
                @Override
                public double getLinkTravelDisutility(Link link, double time, Person person, Vehicle vehicle) {
                    double baseDisutility = delegate.getLinkTravelDisutility(link, time, person, vehicle);
                    double penalty = getPenalty(link);
                    return baseDisutility*penalty;
                }
                @Override
                public double getLinkMinimumTravelDisutility(Link link) {
                    return delegate.getLinkMinimumTravelDisutility(link);
                }

                private double getPenalty(Link link) {
                    if("primary".equals(link.getAttributes().getAttribute("type"))) {
                        return 2;
                    }
                    return 0;
                }
            };
            logger.info("1");
            TravelTime time = new TravelTime() {
                @Override public double getLinkTravelTime(Link link, double time, Person person, Vehicle vehicle ){
                    if("primary".equals(link.getAttributes().getAttribute("type"))) {
                        return link.getLength()*2;
                    }
                    return link.getLength();
                }
            };
            logger.info("2");

            //MultiNodePathCalculator pathCalculator = (MultiNodePathCalculator) fastMultiNodeDijkstraFactory.createPathCalculator(network, new DistanceAsTravelDisutility(), (link, v, person, vehicle) -> link.getLength());
            MultiNodePathCalculator pathCalculator = (MultiNodePathCalculator) fastMultiNodeDijkstraFactory.createPathCalculator(network, new DistanceAsTravelDisutility(), time);
            logger.info("3");
            executor.addTaskToQueue(() -> {
                try {
                    int id = counter.incrementAndGet();
                    int counterr = 0;

                    for (MopedZone origin : partition) {


                        if(LongMath.isPowerOfTwo(counterr)) {
                            logger.info(counterr + " in " + id);
                        };

                        /*if(origin.isClark()){
                            continue;
                        }*/

                        //generate toNodes vector (toSuperPAZ) and calculate Least Cost Path to all destination nodes
                        Set<InitialNode> toNodes = new HashSet<>();
                        Coord originCoord = CoordUtils.createCoord(((Geometry)(origin.getShapeFeature().getDefaultGeometry())).getCentroid().getCoordinate());
                        Node originNode = NetworkUtils.getNearestNode(network, originCoord);
                        float accessDistance = (float) (CoordUtils.calcEuclideanDistance(originCoord,originNode.getCoord()));
                        Collection<SuperPAZ> destinationZones = dataSet.getSuperPAZSearchTree().getDisk(originCoord.getX(),originCoord.getY(),Double.parseDouble(Resources.INSTANCE.getString(Properties.DESTSEARCHDISTANCE)));

                        for(SuperPAZ destination : destinationZones){
                            if(origin.isClark()&&!destination.isClark()){
                                continue;
                            }
                            Coord destinationCoord = CoordUtils.createCoord(((Geometry)(destination.getShapeFeature().getDefaultGeometry())).getCentroid().getCoordinate());
                            Node destinationNode = NetworkUtils.getNearestNode(network, destinationCoord);
                            toNodes.add(new InitialNode(destinationNode, 0., 0.));
                        }

                        ImaginaryNode aggregatedToNodes = MultiNodeDijkstra.createImaginaryNode(toNodes);
                        pathCalculator.calcLeastCostPath(originNode, aggregatedToNodes, 8*3600, null, null);

                        //calculate utility for superPAZ
                        StringBuilder distanceSuperPAZ = new StringBuilder();
                        for (SuperPAZ destination : destinationZones) {
                            if(origin.isClark()&&!destination.isClark()){
                                continue;
                            }


                            float travelDistance = 0.0f;
                            if(origin.getSuperPAZId()==destination.getSuperPAZId()){
                                travelDistance = 0.134f;
                                distanceSuperPAZ.append(origin.getZoneId());
                                distanceSuperPAZ.append(',');
                                distanceSuperPAZ.append(destination.getSuperPAZId());
                                distanceSuperPAZ.append(',');
                                distanceSuperPAZ.append(travelDistance);
                                distanceSuperPAZ.append(',');
                                distanceSuperPAZ.append(0.0);
                                distanceSuperPAZ.append(',');
                                distanceSuperPAZ.append(travelDistance);
                                distanceSuperPAZ.append(',');
                                distanceSuperPAZ.append(0.0);
                                distanceSuperPAZ.append(',');
                                distanceSuperPAZ.append(0.0);
                                distanceSuperPAZ.append(',');
                                distanceSuperPAZ.append(0.0);
                                distanceSuperPAZ.append(',');
                                distanceSuperPAZ.append(0.0);
                                distanceSuperPAZ.append(',');
                                distanceSuperPAZ.append(0.0);
                                distanceSuperPAZ.append(',');
                                distanceSuperPAZ.append(0.0);
                                distanceSuperPAZ.append(',');
                                distanceSuperPAZ.append(0.0);
                                distanceSuperPAZ.append(',');
                                distanceSuperPAZ.append(0.0);
                                distanceSuperPAZ.append(',');
                                distanceSuperPAZ.append(0.0);
                                distanceSuperPAZ.append(',');
                                distanceSuperPAZ.append(accessDistance/1000.0f);
                                distanceSuperPAZ.append(',');
                                distanceSuperPAZ.append(accessDistance/1000.0f);
                                distanceSuperPAZ.append(',');
                                distanceSuperPAZ.append(0.0);
                                distanceSuperPAZ.append(',');
                                distanceSuperPAZ.append(0.0);
                                distanceSuperPAZ.append('\n');
                                continue;
                            }

                            Coord destinationCoord = CoordUtils.createCoord(((Geometry) (destination.getShapeFeature().getDefaultGeometry())).getCentroid().getCoordinate());
                            Node destinationNode = NetworkUtils.getNearestNode(network, destinationCoord);
                            float egressDistance = (float) (CoordUtils.calcEuclideanDistance(destinationCoord,destinationNode.getCoord()));
                            LeastCostPathCalculator.Path path = pathCalculator.constructPath(originNode, destinationNode, 8 * 3600);
                            if(path == null){
                                //logger.warn("paz: " + origin.getZoneId() + " | superPAZ: " + destination.getSuperPAZId() + " has no path!");
//                                distanceSuperPAZ.append(origin.getZoneId());
//                                distanceSuperPAZ.append(',');
//                                distanceSuperPAZ.append(destination.getSuperPAZId());
//                                distanceSuperPAZ.append(',');
//                                distanceSuperPAZ.append(0.0);
//                                distanceSuperPAZ.append(',');
//                                distanceSuperPAZ.append(0.0);
//                                distanceSuperPAZ.append(',');
//                                distanceSuperPAZ.append(0.0);
//                                distanceSuperPAZ.append(',');
//                                distanceSuperPAZ.append(0.0);
//                                distanceSuperPAZ.append(',');
//                                distanceSuperPAZ.append(accessDistance);
//                                distanceSuperPAZ.append(',');
//                                distanceSuperPAZ.append(egressDistance);
//                                distanceSuperPAZ.append('\n');
                                continue;
                            }



//                            if(ACCESSEGRESS){
//                                if(accessDistance>path.travelTime){
//                                    travelDistance = travelDistance + accessDistance;
//                                }
//
//                                if(egressDistance>path.travelTime){
//                                    travelDistance = travelDistance + egressDistance;
//                                }
//                                travelDistance = (float) ((travelDistance + path.travelTime) / 1000.0f);
//                            }else{
//                                travelDistance = (float) ((path.travelTime) / 1000.0f);
//                            }
//
//                            if (travelDistance > 4.8){
//                                continue;
//                            }

                            Map<String, Double> levels = path.links.stream().collect(Collectors.groupingBy(o -> o.getAttributes().getAttribute("trafficVolumeLevel").toString(), Collectors.summingDouble(Link::getLength)));
                            Map<String, Double> types = path.links.stream().collect(Collectors.groupingBy(o -> o.getAttributes().getAttribute("typeGroup").toString(), Collectors.summingDouble(Link::getLength)));

                            distanceSuperPAZ.append(origin.getZoneId());
                            distanceSuperPAZ.append(',');
                            distanceSuperPAZ.append(destination.getSuperPAZId());
                            distanceSuperPAZ.append(',');
                            distanceSuperPAZ.append((float) ((path.travelTime) / 1000.0f));
                            distanceSuperPAZ.append(',');
                            if(levels.get("high")==null){
                                distanceSuperPAZ.append(0);
                                distanceSuperPAZ.append(',');
                            }else{
                                distanceSuperPAZ.append((levels.get("high")/1000.0f));
                                distanceSuperPAZ.append(',');
                            }
                            if(levels.get("low")==null){
                                distanceSuperPAZ.append(0);
                                distanceSuperPAZ.append(',');
                            }else{
                                distanceSuperPAZ.append((levels.get("low")/1000.0f));
                                distanceSuperPAZ.append(',');
                            }
                            if(levels.get("no")==null){
                                distanceSuperPAZ.append(0);
                                distanceSuperPAZ.append(',');
                            }else{
                                distanceSuperPAZ.append((levels.get("no")/1000.0f));
                                distanceSuperPAZ.append(',');
                            }

                            if(types.get("primary")==null){
                                distanceSuperPAZ.append(0);
                                distanceSuperPAZ.append(',');
                            }else{
                                distanceSuperPAZ.append((types.get("primary")/1000.0f));
                                distanceSuperPAZ.append(',');
                            }

                            if(types.get("secondary")==null){
                                distanceSuperPAZ.append(0);
                                distanceSuperPAZ.append(',');
                            }else{
                                distanceSuperPAZ.append((types.get("secondary")/1000.0f));
                                distanceSuperPAZ.append(',');
                            }

                            if(types.get("trunk")==null){
                                distanceSuperPAZ.append(0);
                                distanceSuperPAZ.append(',');
                            }else{
                                distanceSuperPAZ.append((types.get("trunk")/1000.0f));
                                distanceSuperPAZ.append(',');
                            }

                            if(types.get("tertiary")==null){
                                distanceSuperPAZ.append(0);
                                distanceSuperPAZ.append(',');
                            }else{
                                distanceSuperPAZ.append((types.get("tertiary")/1000.0f));
                                distanceSuperPAZ.append(',');
                            }

                            if(types.get("residential")==null){
                                distanceSuperPAZ.append(0);
                                distanceSuperPAZ.append(',');
                            }else{
                                distanceSuperPAZ.append((types.get("residential")/1000.0f));
                                distanceSuperPAZ.append(',');
                            }

                            if(types.get("living_street")==null){
                                distanceSuperPAZ.append(0);
                                distanceSuperPAZ.append(',');
                            }else{
                                distanceSuperPAZ.append((types.get("living_street")/1000.0f));
                                distanceSuperPAZ.append(',');
                            }

                            if(types.get("pedestrian")==null){
                                distanceSuperPAZ.append(0);
                                distanceSuperPAZ.append(',');
                            }else{
                                distanceSuperPAZ.append((types.get("pedestrian")/1000.0f));
                                distanceSuperPAZ.append(',');
                            }

                            if(types.get("unclassified")==null){
                                distanceSuperPAZ.append(0);
                                distanceSuperPAZ.append(',');
                            }else{
                                distanceSuperPAZ.append((types.get("unclassified")/1000.0f));
                                distanceSuperPAZ.append(',');
                            }

                            distanceSuperPAZ.append(accessDistance/1000.0f);
                            distanceSuperPAZ.append(',');
                            distanceSuperPAZ.append(egressDistance/1000.0f);
                            distanceSuperPAZ.append(',');
                            /*distanceSuperPAZ.append(path.nodes.stream().filter(n->n.getAttributes().getAttribute("Crossing").equals(Boolean.TRUE)).count());
                            distanceSuperPAZ.append(',');*/
                            distanceSuperPAZ.append(path.nodes.size());
                            distanceSuperPAZ.append(',');

                            int crossLinks = 0;
                            for(Link link: path.links){
                                if(link.getAttributes().getAttribute("crossMotorway")!=null){
                                    crossLinks+=1;
                                }
                            }
                            distanceSuperPAZ.append(crossLinks);
                            distanceSuperPAZ.append('\n');

                        }

                        try {
                            writeToFile(output_distance,distanceSuperPAZ.toString());
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                        counterr++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.warn(e.getLocalizedMessage());
                    throw new RuntimeException(e);
                }
                return null;
            });
        }
        executor.execute();
    }


    public static synchronized void writeToFile(String path, String building) throws FileNotFoundException {
        PrintWriter bd = new PrintWriter(new FileOutputStream(path, true));
        bd.write(building);
        bd.close();
    }

    public OpenIntFloatHashMap topN (OpenIntFloatHashMap map, int n){
        if (map.size()<=n){
            return map;
        }
        IntArrayList keyList = new IntArrayList();
        FloatArrayList valueList = new FloatArrayList();
        map.pairsSortedByValue(keyList,valueList);
        int[] topList = keyList.partFromTo(keyList.size()-n,keyList.size()-1).elements();
        OpenIntFloatHashMap topNMap = new OpenIntFloatHashMap();
        for(int i : topList){
            topNMap.put(i,map.get(i));
        }
        return topNMap;
    }

    protected abstract void calculateDestinationUtility();
    protected abstract void calculateDestinationUtilityPAZ();
}
