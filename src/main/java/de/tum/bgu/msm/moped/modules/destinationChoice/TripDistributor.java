package de.tum.bgu.msm.moped.modules.destinationChoice;

import cern.colt.list.tfloat.FloatArrayList;
import cern.colt.list.tint.IntArrayList;
import cern.colt.map.tfloat.OpenIntFloatHashMap;
import cern.colt.matrix.tfloat.impl.DenseLargeFloatMatrix2D;
import cern.jet.math.tfloat.FloatFunctions;
import cern.jet.stat.tfloat.FloatDescriptive;
import com.google.common.collect.Iterables;
import com.google.common.math.LongMath;
import com.google.common.util.concurrent.AtomicDouble;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.MopedZone;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.data.SuperPAZ;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import de.tum.bgu.msm.moped.util.concurrent.ConcurrentExecutor;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Geometry;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.contrib.dvrp.router.DistanceAsTravelDisutility;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.router.*;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.utils.geometry.CoordUtils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public abstract class TripDistributor {

    private static final Logger logger = Logger.getLogger(TripDistributor.class);
    public final static AtomicDouble FAILEDTRIPS = new AtomicDouble(0.0);
    protected final DataSet dataSet;
    protected final Purpose purpose;
    private DenseLargeFloatMatrix2D tripDistribution;
    protected Map<Integer, Float> destinationUtility = new HashMap<>();
    protected OpenIntFloatHashMap destinationUtilityPAZ = new OpenIntFloatHashMap();
    private Map<Integer, Double> sumExpUtilityList;

//    private final Map<Purpose, Float> coefNoCarByPurpose = new HashMap<Purpose, Float>(){{
//        put(Purpose.HBW, -1.588032f);
//        put(Purpose.HBSHOP, -1.83f);
//        put(Purpose.HBREC, -2.232597f);
//        put(Purpose.HBOTH, -2.616255f);
//        put(Purpose.NHBW, -2.540999f);
//        put(Purpose.NHBNW, -2.639274f);
//    }};
//
//    private final Map<Purpose, Double> coefHasCarByPurpose = new HashMap<Purpose, Double>(){{
//        put(Purpose.HBW, -1.857719);
//        put(Purpose.HBSHOP, -1.586985);
//        put(Purpose.HBREC, -2.232597);
//        put(Purpose.HBOTH, -2.616255);
//        put(Purpose.NHBW, -2.540999);
//        put(Purpose.NHBNW, -2.639274);
//    }};

    private final Map<Purpose, Float> coefByPurpose = new HashMap<Purpose, Float>(){{
        put(Purpose.HBW, -1.536f);
        put(Purpose.HBSHOP, -2.182f);
        put(Purpose.HBREC, -2.321f);
        put(Purpose.HBOTH, -2.217f);
        put(Purpose.NHBW, -1.883f);
        put(Purpose.NHBNW, -2.141f);
    }};

    private final Map<Purpose, Float> coefByPurposeCross = new HashMap<Purpose, Float>(){{
        put(Purpose.HBW, -0.321f);
        put(Purpose.HBSHOP, -0.279f);
        put(Purpose.HBREC, -0.568f);
        put(Purpose.HBOTH, -0.828f);
        put(Purpose.NHBW, -0.718f);
        put(Purpose.NHBNW, -1.361f);
    }};

    private final Map<Purpose, Float> coefByPurposePAZ = new HashMap<Purpose, Float>(){{
        put(Purpose.HBW, -1.348f);
        put(Purpose.HBSHOP, -2.149f);
        put(Purpose.HBREC, -1.818f);
        put(Purpose.HBOTH, -2.260f);
        put(Purpose.NHBW, -3.218f);
        put(Purpose.NHBNW, -2.496f);
    }};

    private final Map<Purpose, Float> coefByPurposePAZIntercept = new HashMap<Purpose, Float>(){{
        put(Purpose.HBW, 2.051f);
        put(Purpose.HBSHOP, 0.691f);
        put(Purpose.HBREC, 2.511f);
        put(Purpose.HBOTH, 3.005f);
        put(Purpose.NHBW, 0.855f);
        put(Purpose.NHBNW, 1.800f);
    }};

    private final Map<Purpose, Float> coefByPurposePAZInteraction = new HashMap<Purpose, Float>(){{
        put(Purpose.HBW, 0.f);
        put(Purpose.HBSHOP, 0.f);
        put(Purpose.HBREC, -0.891f);
        put(Purpose.HBOTH, -0.718f);
        put(Purpose.NHBW, 1.128f);
        put(Purpose.NHBNW, 0.995f);
    }};

    private final float intercept = 0.f;

    public TripDistributor(DataSet dataSet, Purpose purpose) {
        this.dataSet = dataSet;
        this.purpose = purpose;
    }

    public void run (){
        calculateDestinationUtility();
        logger.info("utility: " + destinationUtility.size());
        calculateDestinationUtilityPAZ();
        logger.info("utility: " + destinationUtilityPAZ.size());
        String output = Resources.INSTANCE.getString(Properties.BASE) + Resources.INSTANCE.getString(Properties.OUTPUT_OD)+"_"+purpose.toString()+".csv";
        twoStageTripDistributor(Resources.INSTANCE.getString(Properties.MATSIMNETWORK),output);

        //For test
//        String output_superPAZ = Resources.INSTANCE.getString(Properties.BASE) + Resources.INSTANCE.getString(Properties.OUTPUT_OD)+"_superPAZ_"+purpose.toString()+".csv";
//        String output_PAZ = Resources.INSTANCE.getString(Properties.BASE) + Resources.INSTANCE.getString(Properties.OUTPUT_OD)+"_PAZ_"+purpose.toString()+".csv";
//
//        twoStageTripDistributor_OHAS(Resources.INSTANCE.getString(Properties.MATSIMNETWORK),output_superPAZ,output_PAZ);
//

        //For test end
        logger.warn(FAILEDTRIPS + " trips can not be distributed to paz!");
    }

//    public void run () {
//        int origins = dataSet.getOriginPAZs().size();
//        int destinations = dataSet.getDestinationSuperPAZs().size();
//        calculateDestinationUtility();
//        tripDistribution = new DenseLargeFloatMatrix2D(origins, destinations);
//        sumExpUtilityList = new HashMap<>();
//        calculateUtilityCalculator(coefNoCarByPurpose.get(purpose),coefInteractionByPurpose.get(purpose));
//        distributeNoCarTrips();
//        dataSet.addDistributionNoCar(tripDistribution, purpose);
//
//        tripDistribution = new DenseLargeFloatMatrix2D(origins, destinations);
//        sumExpUtilityList = new HashMap<>();
//        calculateUtilityCalculator(coefHasCarByPurpose.get(purpose),coefInteractionByPurpose.get(purpose));
//        distributeHasCarTrips();
//        dataSet.addDistributionHasCar(tripDistribution, purpose);
//        //distributeTripsToPAZ(coefByPurpose.get(purpose));
//    }


    //Read impedance from csv file
    private void calculateUtilityCalculator(double coef, double interaction) {
        for (MopedZone origin : dataSet.getOriginPAZs().values()) {
            double sumExpUtility = 0.0;
            float expUtility;
            for (SuperPAZ destination : dataSet.getDestinationSuperPAZs().values()){
                float distance = dataSet.getImpedance().get(origin.getSuperPAZId(),destination.getIndex());
                if ( distance == 0.f){
                    continue;
                }

                double utilitySum = coef * distance + destinationUtility.get(destination.getIndex())+interaction*distance*destination.getRetail();

                expUtility = (float) Math.exp(utilitySum);
                tripDistribution.setQuick(origin.getIndex(), destination.getIndex(), expUtility);
                sumExpUtility += expUtility;
            }
            sumExpUtilityList.put(origin.getIndex(), sumExpUtility);
        }
    }

    private void distributeNoCarTrips() {
        float distributions;
        double probability;

        for (MopedZone origin: dataSet.getOriginPAZs().values()) {
            for (SuperPAZ destination : dataSet.getDestinationSuperPAZs().values()){
                float distance = dataSet.getImpedance().get(origin.getSuperPAZId(),destination.getIndex());
                if ( distance == 0.f){
                    continue;
                }
                if (sumExpUtilityList.get(origin.getIndex()) == 0.0){
                    distributions = 0.0f;
                }else{
                    probability = tripDistribution.get(origin.getIndex(),destination.getIndex())/sumExpUtilityList.get(origin.getIndex());
                    distributions =  (float)probability * origin.getTotalWalkTripsNoCarByPurpose().get(purpose);
                }
                tripDistribution.setQuick(origin.getIndex(),destination.getIndex(),distributions);
            }
        }

    }

    private void distributeHasCarTrips() {
        float distributions;
        double probability;

        for (MopedZone origin: dataSet.getOriginPAZs().values()) {
            for (SuperPAZ destination : dataSet.getDestinationSuperPAZs().values()){
                float distance = dataSet.getImpedance().get(origin.getSuperPAZId(),destination.getIndex());
                if ( distance == 0.f){
                    continue;
                }
                if (sumExpUtilityList.get(origin.getIndex()) == 0.0){
                    distributions = 0.0f;
                }else{
                    probability = tripDistribution.get(origin.getIndex(),destination.getIndex())/sumExpUtilityList.get(origin.getIndex());
                    distributions =  (float)probability * origin.getTotalWalkTripsHasCarByPurpose().get(purpose);
                }
                tripDistribution.setQuick(origin.getIndex(),destination.getIndex(),distributions);
            }
        }

    }

    //    //Read impedance from omx matrices
//    private void calculateUtilityCalculator(double coef) {
//        for (Zone origin : dataSet.getOriginPAZs().values()) {
//            double sumExpUtility = 0.0;
//            float expUtility;
//            Map<Integer, Short> impedance = dataSet.getSuperPAZ(origin.getSuperPAZId()).getImpedanceToSuperPAZs();
//            for (int superPAZID : impedance.keySet()) {
//                int index = dataSet.getSuperPAZ(superPAZID).getIndex();
//                if ( destinationUtility.get(index) == null){
//                    continue;
//                }
//                double utilitySum = coef * impedance.get(superPAZID) + destinationUtility.get(index);
//                System.out.println(utilitySum);
//                expUtility = (float) Math.exp(utilitySum);
//                tripDistribution.setQuick(origin.getIndex(), index, expUtility);
//                sumExpUtility += expUtility;
//            }
//            sumExpUtilityList.put(origin.getIndex(), sumExpUtility);
//        }
//
//    }

    //    private void distributeTrips() {
//        float distributions;
//        double probability;
//
//        for (Zone origin: dataSet.getOriginPAZs().values()) {
//            Map<Integer, Short> impedance = dataSet.getSuperPAZ(origin.getSuperPAZId()).getImpedanceToSuperPAZs();
//            for (int superPAZID : impedance.keySet()) {
//                int index = dataSet.getSuperPAZ(superPAZID).getIndex();
//                if ( destinationUtility.get(index) == null){
//                    continue;
//                }
//                if (sumExpUtilityList.get(origin.getIndex()) == 0.0){
//                    distributions = 0.0f;
//                }else{
//                    probability = tripDistribution.get(origin.getIndex(),index)/sumExpUtilityList.get(origin.getIndex());
//                    distributions =  (float)probability * origin.getTotalWalkTripsByPurpose().get(purpose);
//                }
//                tripDistribution.setQuick(origin.getIndex(),index,distributions);
//            }
//        }
//
//    }

//    private void distributeTripsToPAZ(double coef) {
//        for(MopedZone origin : dataSet.getOriginPAZs().values()){
//            for(SuperPAZ superPAZ: dataSet.getDestinationSuperPAZs().values()) {
//                float sumExpUtility = 0;
//                Map<Integer, Double> destinationUtilityPAZ = calculateDestinationUtilityPAZ(superPAZ);
//
//                for (MopedZone paz : superPAZ.getPazs().values()) {
//                    double impedance = dataSet.getPAZImpedance().getQuick(origin.getZoneId(),paz.getZoneId());
//                    float expUtility = (float) Math.exp(coef*impedance + destinationUtilityPAZ.get(paz.getZoneId()));
//                    origin.getDistribution().put(paz.getZoneId(),expUtility);
//                    sumExpUtility += expUtility;
//                }
//
//                for (MopedZone paz : superPAZ.getPazs().values()) {
//                    double probability = origin.getDistribution().get(paz.getZoneId()) / sumExpUtility;
//                    float distributions = (float) probability * dataSet.getDistributionsByPurpose().get(purpose).get(origin.getIndex(),superPAZ.getIndex());
//                    origin.getDistribution().put(paz.getZoneId(),distributions);
//                }
//            }
//        }
//    }

    private void twoStageTripDistributor (String networkFile, String output) {

        Map<Integer, MopedZone> zonesHasWalkTrips = dataSet.getOriginPAZs().entrySet().stream().filter(entry -> entry.getValue().getTotalWalkTripsByPurpose().get(purpose)>0.0).collect(Collectors.toConcurrentMap(e->e.getKey(),e->e.getValue()));

        final int partitionSize = (int) ((double) zonesHasWalkTrips.keySet().size() / Runtime.getRuntime().availableProcessors()) + 1;
        Iterable<List<MopedZone>> partitions = Iterables.partition(zonesHasWalkTrips.values(), partitionSize);
        logger.info("total original zone: " + dataSet.getOriginPAZs().size());
        logger.info("total zone has walk trips: " + zonesHasWalkTrips.size());

        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(networkFile);

        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Runtime.getRuntime().availableProcessors());

        AtomicInteger counter = new AtomicInteger();
        FastMultiNodeDijkstraFactory fastMultiNodeDijkstraFactory = new FastMultiNodeDijkstraFactory(true);
        StringBuilder header = new StringBuilder();
        header.append("origin,destination,count,distance");
        header.append('\n');
        try {
            writeToFile(output,header.toString());
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
                        //float accessDistance = (float) (CoordUtils.calcEuclideanDistance(originCoord,originNode.getCoord()));
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

                        StringBuilder distributionList = new StringBuilder();
                        for (SuperPAZ destination : destinationZones) {
                            Coord destinationCoord = CoordUtils.createCoord(((Geometry) (destination.getShapeFeature().getDefaultGeometry())).getCentroid().getCoordinate());
                            Node destinationNode = NetworkUtils.getNearestNode(network, destinationCoord);
                            //float egressDistance = (float) (CoordUtils.calcEuclideanDistance(destinationCoord,destinationNode.getCoord()));
                            LeastCostPathCalculator.Path path = pathCalculator.constructPath(originNode, destinationNode, 8 * 3600);
                            //float travelDistance = (float) ((path.travelTime + accessDistance + egressDistance)/1000.0f);
                            float travelDistance = (float) (path.travelTime);
                            if (travelDistance > 4.8){
                                continue;
                            }

                            int crossMotorway = 0;
                            if(destination.getBlock()==origin.getBlock()){
                                crossMotorway = 1;
                            }


                            float utility = crossMotorway * coefByPurposeCross.get(purpose) + travelDistance * coefByPurpose.get(purpose) + destinationUtility.get(destination.getIndex());

                            if (Float.isInfinite(utility) || Float.isNaN(utility)) {
                                throw new RuntimeException(utility + " utility calculated! Please check calculation!" +
                                        " Origin: " + origin.getZoneId() + " | Destination: " + destination.getSuperPAZId() + " | Distance: "
                                        + travelDistance + " | attraction: " + destinationUtility.get(destination.getIndex()));
                            }
                            utilityList.put(destination.getSuperPAZId(), (float) Math.exp(utility));
                        }
                        //utilityList = topN(utilityList, 10);
                        float sum = FloatDescriptive.sum(utilityList.values());
                        float totalWalkTrip = origin.getTotalWalkTripsByPurpose().get(purpose);
                        utilityList.assign(FloatFunctions.mult(totalWalkTrip/sum));

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

                                Coord destinationPAZCoord = CoordUtils.createCoord(((Geometry) (dataSet.getZone(paz).getShapeFeature().getDefaultGeometry())).getCentroid().getCoordinate());
                                Node destinationPAZNode = NetworkUtils.getNearestNode(network, destinationPAZCoord);
                                float egressDistance = (float) (CoordUtils.calcEuclideanDistance(destinationPAZCoord,destinationPAZNode.getCoord()));
                                float travelDistancePAZ = (float) ((subPathCalculator.constructPath(originNode, destinationPAZNode, 8 * 3600).travelTime + egressDistance)/ 1000.0f);
                                if(travelDistancePAZ > 4.8){
                                    continue;
                                }


                                float utilityPAZ;
                                int originPAZ = 0;
                                int originSuperpaz = 0;
                                if(origin.getZoneId()==paz){
                                    originPAZ = 1;
                                }

                                if(origin.getSuperPAZId()==destination.getSuperPAZId()){
                                    originSuperpaz = 1;
                                }

                                utilityPAZ = originPAZ * coefByPurposePAZIntercept.get(purpose) +
                                        originSuperpaz * travelDistancePAZ * coefByPurposePAZInteraction.get(purpose) +
                                        travelDistancePAZ * coefByPurposePAZ.get(purpose) + destinationUtilityPAZ.get(paz);


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

                            for (int paz : utilityListPAZ.keys().elements()) {
                                float distributionPAZ = utilityListPAZ.get(paz)*(utilityList.get(destination.getSuperPAZId())/sumPAZ);
                                distributionList.append(origin.getZoneId());
                                distributionList.append(',');
                                distributionList.append(paz);
                                distributionList.append(',');
                                distributionList.append(distributionPAZ);
                                distributionList.append(',');
                                distributionList.append((short)Math.round(impedanceListPAZ.get(paz)*1000));
                                distributionList.append('\n');
                            }

                        }
                        counterr++;
                        try {
                            writeToFile(output,distributionList.toString());
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

    private void twoStageTripDistributor_OHAS (String networkFile, String output_superPAZ, String output_PAZ) {
        Map<Integer, MopedZone> zonesHasWalkTrips = dataSet.getOriginPAZs().entrySet().stream().filter(entry -> entry.getValue().getTotalWalkTripsByPurpose().get(purpose)>0.0).collect(Collectors.toConcurrentMap(e->e.getKey(),e->e.getValue()));

        final int partitionSize = (int) ((double) zonesHasWalkTrips.keySet().size() / Runtime.getRuntime().availableProcessors()) + 1;
        Iterable<List<MopedZone>> partitions = Iterables.partition(zonesHasWalkTrips.values(), partitionSize);
        logger.info("total original zone: " + dataSet.getOriginPAZs().size());
        logger.info("total zone has walk trips: " + zonesHasWalkTrips.size());

        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(networkFile);

        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Runtime.getRuntime().availableProcessors());

        AtomicInteger counter = new AtomicInteger();
        FastMultiNodeDijkstraFactory fastMultiNodeDijkstraFactory = new FastMultiNodeDijkstraFactory(true);

        StringBuilder header_superPAZ = new StringBuilder();
        header_superPAZ.append("origin_paz,destination_superPAZ,prob,dist");
        header_superPAZ.append('\n');
        try {
            writeToFile(output_superPAZ,header_superPAZ.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        StringBuilder header_PAZ = new StringBuilder();
        header_PAZ.append("origin_paz,destination_superPAZ,destination_PAZ,prob,dist");
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
                            Coord destinationCoord = CoordUtils.createCoord(((Geometry) (destination.getShapeFeature().getDefaultGeometry())).getCentroid().getCoordinate());
                            Node destinationNode = NetworkUtils.getNearestNode(network, destinationCoord);
                            float egressDistance = (float) (CoordUtils.calcEuclideanDistance(destinationCoord,destinationNode.getCoord()));
                            float travelDistance = (float) ((pathCalculator.constructPath(originNode, destinationNode, 8 * 3600).travelTime + accessDistance + egressDistance)/1000.0f);
                            if (travelDistance > 4.8){
                                continue;
                            }
                            float utility = travelDistance * coefByPurpose.get(purpose) + destinationUtility.get(destination.getIndex());
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

                                Coord destinationPAZCoord = CoordUtils.createCoord(((Geometry) (dataSet.getZone(paz).getShapeFeature().getDefaultGeometry())).getCentroid().getCoordinate());
                                Node destinationPAZNode = NetworkUtils.getNearestNode(network, destinationPAZCoord);
                                float egressDistance = (float) (CoordUtils.calcEuclideanDistance(destinationPAZCoord,destinationPAZNode.getCoord()));
                                float travelDistancePAZ = (float) ((subPathCalculator.constructPath(originNode, destinationPAZNode, 8 * 3600).travelTime + accessDistance + egressDistance)/ 1000.0f);
                                if(travelDistancePAZ > 4.8){
                                    continue;
                                }
                                float utilityPAZ;
                                if(origin.getZoneId()==paz){
                                    utilityPAZ = travelDistancePAZ * coefByPurposePAZ.get(purpose) + destinationUtilityPAZ.get(paz);

                                }else{
                                    utilityPAZ = travelDistancePAZ * coefByPurposePAZ.get(purpose) + destinationUtilityPAZ.get(paz);

                                }
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
                            writeToFile(output_PAZ,distributionList_superPAZ.toString());
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
