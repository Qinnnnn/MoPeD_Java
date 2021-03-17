package de.tum.bgu.msm.moped.modules.destinationChoice;

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
    protected Map<Integer, Float> destinationUtility = new HashMap<>();
    protected OpenIntFloatHashMap destinationUtilityPAZ = new OpenIntFloatHashMap();
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
        put(Purpose.HBW, -1.335f);
        put(Purpose.HBSHOP, -2.120f);
        put(Purpose.HBREC, -1.974f);
        put(Purpose.HBOTH, -2.348f);
        put(Purpose.NHBW, -2.894f);
        put(Purpose.NHBNW, -2.163f);
    }};

    private final Map<Purpose, Float> coefByPurposePAZIntercept = new HashMap<Purpose, Float>(){{
        put(Purpose.HBW, 2.068f);
        put(Purpose.HBSHOP, 0.623f);
        put(Purpose.HBREC, 2.704f);
        put(Purpose.HBOTH, 3.162f);
        put(Purpose.NHBW, 0.654f);
        put(Purpose.NHBNW, 1.628f);
    }};

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
        logger.warn(FAILEDTRIPS + " trips can not be distributed to paz!");
    }

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
        header.append("originPAZ,originSuperPAZ,destinationPAZ,destinationSuperPAZ,count,distance");
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
                            LeastCostPathCalculator.Path path = pathCalculator.constructPath(originNode, destinationNode, 8 * 3600);
                            float travelDistance;
                            if(path == null){
                                if(origin.getSuperPAZId()==destination.getSuperPAZId()){
                                    travelDistance = 0.282f;
                                }else{
                                    //logger.warn("There is no path from origin paz: " + origin.getZoneId() + " to destination paz: " + destination.getSuperPAZId());
                                    //might because WASHI county is disconnected
                                    continue;
                                }
                            }else {
                                travelDistance = (float) (path.travelTime / 1000.0f);
                            }

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
                                LeastCostPathCalculator.Path path = subPathCalculator.constructPath(originNode, destinationPAZNode, 8 * 3600);
                                float travelDistancePAZ;
                                if(path==null){
                                    if(origin.getZoneId() == paz){
                                        travelDistancePAZ = 0.056f;
                                    }else{
                                        //logger.warn("There is no path from origin paz: " + origin.getZoneId() + " to destination paz: " + paz);
                                        continue;
                                    }
                                }else{
                                    travelDistancePAZ = (float) (path.travelTime/ 1000.0f);
                                }

                                if(travelDistancePAZ > 4.8){
                                    continue;
                                }


                                float utilityPAZ;
                                int originPAZ = 0;

                                if(origin.getZoneId()==paz){
                                    originPAZ = 1;
                                }


                                utilityPAZ = originPAZ * coefByPurposePAZIntercept.get(purpose) +
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
                                if (origin.isScenarioZone()||dataSet.getZone(paz).isScenarioZone()){
                                    float distributionPAZ = utilityListPAZ.get(paz)*(utilityList.get(destination.getSuperPAZId())/sumPAZ);
                                    distributionList.append(origin.getZoneId());
                                    distributionList.append(',');
                                    distributionList.append(origin.getSuperPAZId());
                                    distributionList.append(',');
                                    distributionList.append(paz);
                                    distributionList.append(',');
                                    distributionList.append(destinationId);
                                    distributionList.append(',');
                                    distributionList.append(distributionPAZ);
                                    distributionList.append(',');
                                    distributionList.append((short)Math.round(impedanceListPAZ.get(paz)*1000));
                                    distributionList.append('\n');
                                }
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

    public static synchronized void writeToFile(String path, String building) throws FileNotFoundException {
        PrintWriter bd = new PrintWriter(new FileOutputStream(path, true));
        bd.write(building);
        bd.close();
    }

    protected abstract void calculateDestinationUtility();
    protected abstract void calculateDestinationUtilityPAZ();
}
