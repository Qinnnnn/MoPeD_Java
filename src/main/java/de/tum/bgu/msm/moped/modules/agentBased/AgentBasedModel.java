package de.tum.bgu.msm.moped.modules.agentBased;

import cern.colt.map.tdouble.OpenIntDoubleHashMap;
import cern.colt.map.tfloat.OpenIntFloatHashMap;
import cern.colt.matrix.tfloat.FloatMatrix1D;
import cern.colt.matrix.tfloat.impl.DenseLargeFloatMatrix2D;
import cern.jet.math.tdouble.DoubleFunctions;
import cern.jet.math.tfloat.FloatFunctions;
import cern.jet.stat.tdouble.DoubleDescriptive;
import cern.jet.stat.tfloat.FloatDescriptive;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.math.LongMath;
import de.tum.bgu.msm.moped.data.*;
import de.tum.bgu.msm.moped.modules.agentBased.destinationChoice.AgentTripDistribution;
import de.tum.bgu.msm.moped.modules.agentBased.destinationChoice.DestinationUtilityCalculatorImpl;
import de.tum.bgu.msm.moped.modules.agentBased.destinationChoice.DestinationUtilityJSCalculator;
import de.tum.bgu.msm.moped.modules.agentBased.walkModeChoice.ModeChoiceCalculatorImpl;
import de.tum.bgu.msm.moped.modules.agentBased.walkModeChoice.ModeChoiceJSCalculator;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import de.tum.bgu.msm.moped.util.MoPeDUtil;
import de.tum.bgu.msm.moped.util.concurrent.ConcurrentExecutor;
import de.tum.bgu.msm.moped.util.concurrent.RandomizableConcurrentFunction;
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

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static de.tum.bgu.msm.moped.data.Purpose.*;

/**
 * @author Qin
 */
public final class AgentBasedModel {

    public final static AtomicInteger distributedTripsCounter = new AtomicInteger(0);
    public final static AtomicInteger NOOCCUPATIONCOUNTER = new AtomicInteger(0);
    private final static Logger logger = Logger.getLogger(AgentBasedModel.class);
    private final DataSet dataSet;
    private final Random random;
    //private final ModeChoiceJSCalculator modeChoiceJSCalculator;
    //private final DestinationUtilityJSCalculator destinationJSCalculator;
    private final ModeChoiceCalculatorImpl modeChoiceCalculator;
    private final DestinationUtilityCalculatorImpl destinationCalculator;
    public final static AtomicInteger noPriorTripCounter = new AtomicInteger(0);
    private final Map<Purpose, List<Purpose>> priorPurposes = new HashMap<Purpose, List<Purpose>>(){{
        put(NHBO, ImmutableList.of(HBO, HBE, HBS));
        put(NHBW, Collections.singletonList(HBW));
    }};
    public final static AtomicInteger totalProcessedTrip = new AtomicInteger(0);
    private final static AtomicInteger failedTripsCounter = new AtomicInteger(0);

    public AgentBasedModel(DataSet dataSet) {
        this.dataSet = dataSet;
        this.random = new Random();
        //Reader mcReader = new InputStreamReader(this.getClass().getResourceAsStream("ModeChoiceAgent"));
        //modeChoiceJSCalculator = new ModeChoiceJSCalculator(mcReader);
        //Reader dcReader = new InputStreamReader(this.getClass().getResourceAsStream("TripDistribution"));
        //destinationJSCalculator = new DestinationUtilityJSCalculator(dcReader);
        modeChoiceCalculator = new ModeChoiceCalculatorImpl();
        destinationCalculator = new DestinationUtilityCalculatorImpl();
    }

    public void runHomeBased() {
        logger.info("find destination for moped walk trips, calculate distance on the fly...");
        List<MopedTrip> homeBasedTrips = dataSet.getTrips().values().stream().filter(tt->tt.getTripPurpose()!=Purpose.NHBW&&tt.getTripPurpose()!=Purpose.NHBO).collect(Collectors.toList());
        logger.info(homeBasedTrips.size() + " home based trips " );
        logger.warn(homeBasedTrips.stream().filter(tt->tt.getTripOrigin()==null).count() + " home based trips has no origin PAZ!");

        final Map<MopedZone,List<MopedTrip>> tripsByZone = homeBasedTrips.stream().filter(tt->tt.getTripOrigin()!=null).collect(Collectors.groupingBy(tt -> tt.getTripOrigin())).entrySet().stream().filter(entry -> entry.getValue().size()>0).collect(Collectors.toConcurrentMap(e->e.getKey(),e->e.getValue()));
        final int partitionSize = (int) ((double) tripsByZone.keySet().size() / Runtime.getRuntime().availableProcessors()) + 1;
        Iterable<List<MopedZone>> partitions = Iterables.partition(tripsByZone.keySet(), partitionSize);
        logger.info("Partition size: " + partitionSize);
        logger.info("Total moped zones: " + tripsByZone.keySet().size());

        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(Resources.INSTANCE.getString(Properties.MATSIMNETWORK));
        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Runtime.getRuntime().availableProcessors());

        FastMultiNodeDijkstraFactory fastMultiNodeDijkstraFactory = new FastMultiNodeDijkstraFactory(true);
        List<Callable<Void>> homeBasedTasks = new ArrayList<>();
        int partitionId = 0;
        for (final List<MopedZone> partition : partitions) {
            MultiNodePathCalculator pathCalculator = (MultiNodePathCalculator) fastMultiNodeDijkstraFactory.createPathCalculator(network, new DistanceAsTravelDisutility(), (link, v, person, vehicle) -> link.getLength());
            MultiNodePathCalculator subPathCalculator = (MultiNodePathCalculator) fastMultiNodeDijkstraFactory.createPathCalculator(network, new DistanceAsTravelDisutility(), (link, v, person, vehicle) -> link.getLength());
            homeBasedTasks.add(new HomeBasedDistribution(dataSet,partition,pathCalculator,
                    subPathCalculator,network,destinationCalculator,modeChoiceCalculator,tripsByZone, partitionId));
            partitionId++;
        }
        executor.submitTasksAndWaitForCompletion(homeBasedTasks);

        logger.warn(NOOCCUPATIONCOUNTER + " work or education trips have no occupation zone!");
        logger.info(distributedTripsCounter + " walk trips haved been distributed.");
        logger.info(totalProcessedTrip + " walk trips haved been processed.");
    }

    public void runNonHomeBased() {
        logger.info("find destination for moped walk trips, calculate distance on the fly...");
        List<MopedTrip> nonHomeBasedTrips = dataSet.getTrips().values().stream().filter(tt->tt.getTripPurpose().equals(Purpose.NHBW)||tt.getTripPurpose().equals(Purpose.NHBO)).collect(Collectors.toList());

        //assign trip origin
        logger.info("Before origin assignment, there are non home based trips: " + nonHomeBasedTrips.size());
        int i = 0;
        for (MopedHousehold household : dataSet.getHouseholds().values()) {
            if (!household.getNonHomeBasedTrips().isEmpty()) {
                for (MopedTrip trip : household.getNonHomeBasedTrips()) {
                    if(LongMath.isPowerOfTwo(i)) {
                        logger.info(i + " has done origin assignment.");
                    };
                    MopedZone origin = findOrigin(household, trip);
                    if (origin == null) {
                        logger.debug("No origin found for trip" + trip);
                        failedTripsCounter.incrementAndGet();
                        nonHomeBasedTrips.remove(trip);
                        continue;
                    }
                    trip.setTripOrigin(origin);
                    i++;
                }
            }
        }

        logger.info(failedTripsCounter + "failed to find a origin zone because of no prior home based trips");

        logger.info("After origin assignment, there are non home based trips (ready to assgin destination): " + nonHomeBasedTrips.size());

        Map<MopedZone,List<MopedTrip>> tripsByZone = nonHomeBasedTrips.stream().collect(Collectors.groupingBy(tt -> tt.getTripOrigin())).entrySet().stream().filter(entry -> entry.getValue().size()>0).collect(Collectors.toConcurrentMap(e->e.getKey(),e->e.getValue()));
        final int partitionSize = (int) ((double) tripsByZone.keySet().size() / Runtime.getRuntime().availableProcessors()) + 1;
        Iterable<List<MopedZone>> partitions = Iterables.partition(tripsByZone.keySet(), partitionSize);
        logger.info("Partition size: " + partitionSize);
        logger.info("Total moped zones: " + tripsByZone.keySet().size());

        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(Resources.INSTANCE.getString(Properties.MATSIMNETWORK));
        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Runtime.getRuntime().availableProcessors());

        FastMultiNodeDijkstraFactory fastMultiNodeDijkstraFactory = new FastMultiNodeDijkstraFactory(true);
        List<Callable<Void>> nonHomeBasedTasks = new ArrayList<>();
        int partitionId = 0;
        for (final List<MopedZone> partition : partitions) {
            MultiNodePathCalculator pathCalculator = (MultiNodePathCalculator) fastMultiNodeDijkstraFactory.createPathCalculator(network, new DistanceAsTravelDisutility(), (link, v, person, vehicle) -> link.getLength());
            MultiNodePathCalculator subPathCalculator = (MultiNodePathCalculator) fastMultiNodeDijkstraFactory.createPathCalculator(network, new DistanceAsTravelDisutility(), (link, v, person, vehicle) -> link.getLength());
            nonHomeBasedTasks.add(new NonHomeBasedDistribution(dataSet,partition,pathCalculator,
                    subPathCalculator,network,destinationCalculator,modeChoiceCalculator,tripsByZone,partitionId));
            partitionId++;
        }
        executor.submitTasksAndWaitForCompletion(nonHomeBasedTasks);

        logger.info(distributedTripsCounter + " walk trips haved been distributed.");
        logger.info(totalProcessedTrip + " walk trips haved been processed.");
    }

    /*private void nonHomeBasedModel(List<MopedTrip> nonHomeBasedTrips, String networkFile) {
        //assign trip origin
        logger.info("Before origin assignment, there are non home based trips: " + nonHomeBasedTrips.size());
        int counter = 0;
        for (MopedHousehold household : dataSet.getHouseholds().values()) {
            if (!household.getNonHomeBasedTrips().isEmpty()) {
                for (MopedTrip trip : household.getNonHomeBasedTrips()) {
                    if(LongMath.isPowerOfTwo(counter)) {
                        logger.info(counter + " has done origin assignment.");
                    };
                    MopedZone origin = findOrigin(household, trip);
                    if (origin == null) {
                        logger.debug("No origin found for trip" + trip);
                        failedTripsCounter.incrementAndGet();
                        nonHomeBasedTrips.remove(trip);
                        continue;
                    }
                    trip.setTripOrigin(origin);
                    counter++;
                }
            }
        }

        logger.info(failedTripsCounter + "failed to find a origin zone because of no prior home based trips");

        logger.info("After origin assignment, there are non home based trips (ready to assgin destination): " + nonHomeBasedTrips.size());
        //mode choice and destination choice similar with home based trips
        Map<MopedZone,List<MopedTrip>> tripsByZone = nonHomeBasedTrips.stream().collect(Collectors.groupingBy(tt -> tt.getTripOrigin())).entrySet().stream().filter(entry -> entry.getValue().size()>0).collect(Collectors.toConcurrentMap(e->e.getKey(),e->e.getValue()));

        final int partitionSize = (int) ((double) tripsByZone.keySet().size() / Runtime.getRuntime().availableProcessors())+ 1;
        Iterable<List<MopedZone>> partitions = Iterables.partition(tripsByZone.keySet(), partitionSize);

        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(networkFile);
        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Runtime.getRuntime().availableProcessors());
        FastMultiNodeDijkstraFactory fastMultiNodeDijkstraFactory = new FastMultiNodeDijkstraFactory(true);
        for (final List<MopedZone> partition : partitions) {
            MultiNodePathCalculator pathCalculator = (MultiNodePathCalculator)  fastMultiNodeDijkstraFactory.createPathCalculator(network, new DistanceAsTravelDisutility(), (link, v, person, vehicle) -> link.getLength());
            Random rand = new Random(random.nextInt());
            executor.addTaskToQueue(() -> {
                try {
                    for (MopedZone origin : partition) {
                        //prepare utilityMatrices for all purpose
                        Map<Integer,Double> impedenceList = new HashMap<>();
                        EnumMap<Purpose, Map<Integer,Double>> utilityMatrices = new EnumMap<>(Purpose.class);
                        for(Purpose purpose : ImmutableList.of(NHBW, NHBO)){
                            Map<Integer,Double> utility = new HashMap<>();
                            utilityMatrices.put(purpose,utility);
                        }

                        //generate toNodes vector and calculate Least Cost Path to all destination nodes
                        Set<InitialNode> toNodes = new HashSet<>();
                        Coord originCoord = CoordUtils.createCoord(((Geometry)(origin.getShapeFeature().getDefaultGeometry())).getCentroid().getCoordinate());
                        Node originNode = NetworkUtils.getNearestNode(network, originCoord);
                        Collection<MopedZone> destinationZones = dataSet.getZoneSearchTree().getDisk(originCoord.getX(),originCoord.getY(),Resources.INSTANCE.getDouble(Properties.DESTSEARCHDISTANCE));
                        for(MopedZone destination : destinationZones){
                            Coord destinationCoord = CoordUtils.createCoord(((Geometry)(destination.getShapeFeature().getDefaultGeometry())).getCentroid().getCoordinate());
                            Node destinationNode = NetworkUtils.getNearestNode(network, destinationCoord);
                            toNodes.add(new InitialNode(destinationNode, 0., 0.));
                        }

                        ImaginaryNode aggregatedToNodes = MultiNodeDijkstra.createImaginaryNode(toNodes);
                        pathCalculator.calcLeastCostPath(originNode, aggregatedToNodes, 8*3600, null, null);

                        //calculate utilityMatrices for all purpose
                        for (MopedZone destination : destinationZones) {
                            Coord destinationCoord = CoordUtils.createCoord(((Geometry)(destination.getShapeFeature().getDefaultGeometry())).getCentroid().getCoordinate());
                            Node destinationNode = NetworkUtils.getNearestNode(network, destinationCoord);
                            double travelDistance = pathCalculator.constructPath(originNode, destinationNode, 8*3600).travelTime;
                            impedenceList.put(destination.getZoneId(),travelDistance);
                            for (Purpose purpose : ImmutableList.of(NHBW, NHBO)){
                                double utility = destinationCalculator.calculateUtility(purpose,destination.getTotalJobDensity(), destination.getIndustrialJobDensity(),travelDistance);
                                if (Double.isInfinite(utility) || Double.isNaN(utility)) {
                                    throw new RuntimeException(utility + " utility calculated! Please check calculation!" +
                                            " Origin: " + origin + " | Destination: " + destination + " | Distance: "
                                            + dataSet.getPAZImpedance().get(origin.getSuperPAZId(), destination.getSuperPAZId()) +
                                            " | Purpose: " + purpose + " | attraction rate: " + destination.getTotalEmpl());
                                }
                                utilityMatrices.get(purpose).put(destination.getZoneId(),utility);
                            }
                        }

                        //find destination and set trip distance for all trips generated from this zone
                        for (MopedTrip trip : tripsByZone.get(origin)){

                            chooseMode(trip, calculateTripProbabilities(trip),rand);

                            if(trip.isWalkMode()){
                                final int selectedZoneId = MoPeDUtil.select(utilityMatrices.get(trip.getTripPurpose()), rand);
                                trip.setTripDestination(dataSet.getZone(selectedZoneId));
                                trip.setTripDistance(impedenceList.get(selectedZoneId));
                                AgentBasedModel.distributedTripsCounter.incrementAndGet();
                            }
                        }
                    }

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
        }
        executor.execute();
    }
    */


    /*private void nonHomeBasedModel (List<MopedTrip> nonHomeBasedTrips, String networkFile) {
        //assign trip origin
        logger.info("Before origin assignment, there are non home based trips: " + nonHomeBasedTrips.size());
        int i = 0;
        for (MopedHousehold household : dataSet.getHouseholds().values()) {
            if (!household.getNonHomeBasedTrips().isEmpty()) {
                for (MopedTrip trip : household.getNonHomeBasedTrips()) {
                    if(LongMath.isPowerOfTwo(i)) {
                        logger.info(i + " has done origin assignment.");
                    };
                    MopedZone origin = findOrigin(household, trip);
                    if (origin == null) {
                        logger.debug("No origin found for trip" + trip);
                        failedTripsCounter.incrementAndGet();
                        nonHomeBasedTrips.remove(trip);
                        continue;
                    }
                    trip.setTripOrigin(origin);
                    i++;
                }
            }
        }

        logger.info(failedTripsCounter + "failed to find a origin zone because of no prior home based trips");

        logger.info("After origin assignment, there are non home based trips (ready to assgin destination): " + nonHomeBasedTrips.size());

        //Map<MopedZone,List<MopedTrip>> tripsByZone = dataSet.getTrips().values().stream().filter(tt->tt.isWalkMode()).collect(Collectors.groupingBy(tt -> tt.getTripOrigin())).entrySet().stream().filter(entry -> entry.getValue().size()>0).collect(Collectors.toMap(e->e.getKey(),e->e.getValue()));
        Map<MopedZone,List<MopedTrip>> tripsByZone = nonHomeBasedTrips.stream().collect(Collectors.groupingBy(tt -> tt.getTripOrigin())).entrySet().stream().filter(entry -> entry.getValue().size()>0).collect(Collectors.toConcurrentMap(e->e.getKey(),e->e.getValue()));

        final int partitionSize = (int) ((double) tripsByZone.keySet().size() / Runtime.getRuntime().availableProcessors()) + 1;
        Iterable<List<MopedZone>> partitions = Iterables.partition(tripsByZone.keySet(), partitionSize);
        logger.info("Partition size: " + partitionSize);
        logger.info("Total moped zones: " + tripsByZone.keySet().size());

        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(networkFile);
        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Runtime.getRuntime().availableProcessors());

        AtomicInteger counter = new AtomicInteger();
        FastMultiNodeDijkstraFactory fastMultiNodeDijkstraFactory = new FastMultiNodeDijkstraFactory(true);
        for (final List<MopedZone> partition : partitions) {
            MultiNodePathCalculator pathCalculator = (MultiNodePathCalculator) fastMultiNodeDijkstraFactory.createPathCalculator(network, new DistanceAsTravelDisutility(), (link, v, person, vehicle) -> link.getLength());
            MultiNodePathCalculator subPathCalculator = (MultiNodePathCalculator) fastMultiNodeDijkstraFactory.createPathCalculator(network, new DistanceAsTravelDisutility(), (link, v, person, vehicle) -> link.getLength());

            Random rand = new Random(random.nextInt());
            executor.addTaskToQueue(() -> {
                try {
                    int id = counter.incrementAndGet();
                    int counterr = 0;
                    //logger.info("Started partition " + id + " with " + partition.size() + " entries");
                    for (MopedZone origin : partition) {
                        if(LongMath.isPowerOfTwo(counterr)) {
                            logger.info(counterr + " in " + id);
                        };

                        //prepare utilityMatrices for all purpose
                        OpenIntFloatHashMap impedanceListPAZ = new OpenIntFloatHashMap();

                        EnumMap<Purpose, OpenIntFloatHashMap> utilityMatrices = new EnumMap<>(Purpose.class);
                        EnumMap<Purpose, OpenIntFloatHashMap> utilityMatricesPAZ = new EnumMap<>(Purpose.class);

                        for(Purpose purpose : ImmutableList.of(HBW,HBO,HBE,HBS)){
                            OpenIntFloatHashMap utility = new OpenIntFloatHashMap();
                            OpenIntFloatHashMap utilityPAZ = new OpenIntFloatHashMap();

                            utilityMatrices.put(purpose,utility);
                            utilityMatricesPAZ.put(purpose,utilityPAZ);
                        }

                        //generate toNodes vector and calculate Least Cost Path to all destination nodes
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

                        //calculate utilityMatrices for all purpose
                        for (SuperPAZ destination : destinationZones) {
                            Coord destinationCoord = CoordUtils.createCoord(((Geometry)(destination.getShapeFeature().getDefaultGeometry())).getCentroid().getCoordinate());
                            Node destinationNode = NetworkUtils.getNearestNode(network, destinationCoord);
                            LeastCostPathCalculator.Path path = pathCalculator.constructPath(originNode, destinationNode, 8 * 3600);
                            float travelDistance;

                            if(path == null){
                                if(origin.getSuperPAZId()==destination.getSuperPAZId()){
                                    travelDistance = 0.282f;
                                }else{
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

                            for (Purpose purpose : ImmutableList.of(HBW,HBO,HBE,HBS)){
                                double utility = destinationCalculator.calculateUtility(purpose,destination,travelDistance,crossMotorway);
                                if (Double.isInfinite(utility) || Double.isNaN(utility)) {
                                    throw new RuntimeException(utility + " utility calculated! Please check calculation!" +
                                            " Origin: " + origin + " | Destination: " + destination + " | Distance: "
                                            + dataSet.getPAZImpedance().get(origin.getSuperPAZId(), destination.getSuperPAZId()) +
                                            " | Purpose: " + purpose + " | attraction rate: " + destination.getTotalEmpl());
                                }
                                utilityMatrices.get(purpose).put(destination.getSuperPAZId(), (float) utility);
                            }
                        }

                        //calculate utilityMatricesPAZ for all purpose
                        for (Purpose purpose : ImmutableList.of(HBW,HBO,HBE,HBS)){
                            float sum = FloatDescriptive.sum(utilityMatrices.get(purpose).values());
                            utilityMatrices.get(purpose).assign(FloatFunctions.mult((float) (1./sum)));

                            for ( int destinationId : utilityMatrices.get(purpose).keys().elements()) {
                                SuperPAZ destination = dataSet.getSuperPAZ(destinationId);
                                //generate toNodes vector (toPAZ) and calculate Least Cost Path to all destination nodes
                                OpenIntFloatHashMap utilityListPAZ = new OpenIntFloatHashMap();

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
                                    if (path == null) {
                                        if (origin.getZoneId() == paz) {
                                            travelDistancePAZ = 0.056f;
                                        } else {
                                            logger.warn("There is no path from origin paz: " + origin.getZoneId() + " to destination paz: " + paz);
                                            continue;
                                        }
                                    } else {
                                        travelDistancePAZ = (float) (path.travelTime / 1000.0f);
                                    }

                                    if (travelDistancePAZ > 4.8) {
                                        continue;
                                    }



                                    int originPAZ = 0;

                                    if (origin.getZoneId() == paz) {
                                        originPAZ = 1;
                                    }

                                    double utilityPAZ = destinationCalculator.calculateUtility(purpose,dataSet.getZone(paz),travelDistancePAZ,originPAZ);
                                    if (Double.isInfinite(utilityPAZ) || Double.isNaN(utilityPAZ)) {
                                        throw new RuntimeException(utilityPAZ + " utility calculated! Please check calculation!" +
                                                " Origin: " + origin + " | Destination: " + destination + " | Distance: "
                                                + dataSet.getPAZImpedance().get(origin.getSuperPAZId(), destination.getSuperPAZId()) +
                                                " | Purpose: " + purpose + " | attraction rate: " + destination.getTotalEmpl());
                                    }


                                    if (utilityPAZ != 0.0f) {
                                        utilityListPAZ.put(paz, (float) utilityPAZ);
                                        impedanceListPAZ.put(paz, travelDistancePAZ);
                                    }
                                }

                                float sumPAZ = FloatDescriptive.sum(utilityListPAZ.values());
                                if (sumPAZ == 0.0f) {
                                    logger.info("sumPAZ is 0!");
                                    continue;
                                }

                                for (int paz : utilityListPAZ.keys().elements()) {
                                    float probabilityPAZ = utilityListPAZ.get(paz)*(utilityMatrices.get(purpose).get(destination.getSuperPAZId())/sumPAZ);
                                    utilityMatricesPAZ.get(purpose).put(paz,probabilityPAZ);
                                }
                            }
                        }


                        //find destination and set trip distance for all trips generated from this zone
                        for (MopedTrip trip : tripsByZone.get(origin)){
                            totalProcessedTrip.incrementAndGet();
                            if(trip.getTripPurpose().equals(Purpose.HBW)||trip.getTripPurpose().equals(Purpose.HBE)){
                                if(trip.getTripOrigin()==null||trip.getTripDestination()==null){
                                    //logger.warn("trip " + trip.getTripId() + ", purpose " + trip.getTripPurpose()+", has no home or occupation zone" + trip.getTripOrigin() + "," + trip.getTripDestination());
                                    NOOCCUPATIONCOUNTER.incrementAndGet();
                                    continue;
                                }else{
                                    //TODO: allow HBW walk trip allow 4.8km
                                    if (!impedanceListPAZ.containsKey(trip.getTripDestination().getZoneId())){
                                        trip.setWalkMode(Boolean.FALSE);
                                        continue;
                                    }
                                    trip.setTripDistance(impedanceListPAZ.get(trip.getTripDestination().getZoneId()));
                                }
                            }

                            chooseMode(trip, calculateTripProbabilities(trip), rand);

                            if(trip.isWalkMode()){
                                final int selectedZoneId = MoPeDUtil.select(utilityMatricesPAZ.get(trip.getTripPurpose()), rand);
                                trip.setTripDestination(dataSet.getZone(selectedZoneId));
                                trip.setTripDistance(impedanceListPAZ.get(selectedZoneId));
                                AgentBasedModel.distributedTripsCounter.incrementAndGet();
                            }
                        }
                        counterr++;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
        }
        executor.execute();
    }
*/
    private MopedZone findOrigin(MopedHousehold household, MopedTrip trip) {
        final List<MopedZone> possibleBaseZones = new ArrayList<>();

        for (Purpose purpose : priorPurposes.get(trip.getTripPurpose())) {
            for (MopedTrip priorTrip : household.getTripsForPurpose(purpose)) {
                if (priorTrip.getPerson().equals(trip.getPerson())) {
                    possibleBaseZones.add(priorTrip.getTripDestination());
                }
            }
        }
        if (!possibleBaseZones.isEmpty()) {
            return MoPeDUtil.select(random, possibleBaseZones);
        }
        if (trip.getPerson().getOccupation().equals(Occupation.WORKER) &&
                trip.getPerson().getOccupationZone() != null) {
            return trip.getPerson().getOccupationZone();
        }

        //TODO
        noPriorTripCounter.incrementAndGet();
        return null;
    }

    /*private void homeBasedModel (List<MopedTrip> homeBasedTrips, String networkFile) {
        Map<MopedZone,List<MopedTrip>> tripsByZone = homeBasedTrips.stream().collect(Collectors.groupingBy(tt -> tt.getTripOrigin())).entrySet().stream().filter(entry -> entry.getValue().size()>0).collect(Collectors.toConcurrentMap(e->e.getKey(),e->e.getValue()));

        final int partitionSize = (int) ((double) tripsByZone.keySet().size() / Runtime.getRuntime().availableProcessors()) + 1;
        Iterable<List<MopedZone>> partitions = Iterables.partition(tripsByZone.keySet(), partitionSize);
        logger.info("Partition size: " + partitionSize);
        logger.info("Total moped zones: " + tripsByZone.keySet().size());


        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(networkFile);
        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Runtime.getRuntime().availableProcessors());

        AtomicInteger counter = new AtomicInteger();
        FastMultiNodeDijkstraFactory fastMultiNodeDijkstraFactory = new FastMultiNodeDijkstraFactory(true);
        for (final List<MopedZone> partition : partitions) {
            MultiNodePathCalculator pathCalculator = (MultiNodePathCalculator) fastMultiNodeDijkstraFactory.createPathCalculator(network, new DistanceAsTravelDisutility(), (link, v, person, vehicle) -> link.getLength());
            MultiNodePathCalculator subPathCalculator = (MultiNodePathCalculator) fastMultiNodeDijkstraFactory.createPathCalculator(network, new DistanceAsTravelDisutility(), (link, v, person, vehicle) -> link.getLength());

            Random rand = new Random(random.nextInt());
            executor.addTaskToQueue(() -> {
                try {
                    int id = counter.incrementAndGet();
                    int counterr = 0;

                    for (MopedZone origin : partition) {
                        if(LongMath.isPowerOfTwo(counterr)) {
                            logger.info(counterr + " in " + id);
                        };

                        //prepare utilityMatrices for all purpose
                        OpenIntFloatHashMap impedanceListPAZ = new OpenIntFloatHashMap();

                        EnumMap<Purpose, OpenIntFloatHashMap> utilityMatrices = new EnumMap<>(Purpose.class);
                        EnumMap<Purpose, OpenIntFloatHashMap> utilityMatricesPAZ = new EnumMap<>(Purpose.class);

                        for(Purpose purpose : ImmutableList.of(HBW,HBO,HBE,HBS)){
                            OpenIntFloatHashMap utility = new OpenIntFloatHashMap();
                            OpenIntFloatHashMap utilityPAZ = new OpenIntFloatHashMap();

                            utilityMatrices.put(purpose,utility);
                            utilityMatricesPAZ.put(purpose,utilityPAZ);
                        }

                        //generate toNodes vector and calculate Least Cost Path to all destination nodes
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

                        //calculate utilityMatrices for all purpose
                        for (SuperPAZ destination : destinationZones) {
                            Coord destinationCoord = CoordUtils.createCoord(((Geometry)(destination.getShapeFeature().getDefaultGeometry())).getCentroid().getCoordinate());
                            Node destinationNode = NetworkUtils.getNearestNode(network, destinationCoord);
                            LeastCostPathCalculator.Path path = pathCalculator.constructPath(originNode, destinationNode, 8 * 3600);
                            float travelDistance;

                            if(path == null){
                                if(origin.getSuperPAZId()==destination.getSuperPAZId()){
                                    travelDistance = 0.282f;
                                }else{
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

                            for (Purpose purpose : ImmutableList.of(HBW,HBO,HBE,HBS)){
                                double utility = destinationCalculator.calculateUtility(purpose,destination,travelDistance,crossMotorway);
                                if (Double.isInfinite(utility) || Double.isNaN(utility)) {
                                    throw new RuntimeException(utility + " utility calculated! Please check calculation!" +
                                            " Origin: " + origin + " | Destination: " + destination + " | Distance: "
                                            + dataSet.getPAZImpedance().get(origin.getSuperPAZId(), destination.getSuperPAZId()) +
                                            " | Purpose: " + purpose + " | attraction rate: " + destination.getTotalEmpl());
                                }
                                utilityMatrices.get(purpose).put(destination.getSuperPAZId(), (float) utility);
                            }
                        }

                        //calculate utilityMatricesPAZ for all purpose
                        for (Purpose purpose : ImmutableList.of(HBW,HBO,HBE,HBS)){
                            float sum = FloatDescriptive.sum(utilityMatrices.get(purpose).values());
                            utilityMatrices.get(purpose).assign(FloatFunctions.mult((float) (1./sum)));

                            for ( int destinationId : utilityMatrices.get(purpose).keys().elements()) {
                                SuperPAZ destination = dataSet.getSuperPAZ(destinationId);
                                //generate toNodes vector (toPAZ) and calculate Least Cost Path to all destination nodes
                                OpenIntFloatHashMap utilityListPAZ = new OpenIntFloatHashMap();

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
                                    if (path == null) {
                                        if (origin.getZoneId() == paz) {
                                            travelDistancePAZ = 0.056f;
                                        } else {
                                            logger.warn("There is no path from origin paz: " + origin.getZoneId() + " to destination paz: " + paz);
                                            continue;
                                        }
                                    } else {
                                        travelDistancePAZ = (float) (path.travelTime / 1000.0f);
                                    }

                                    if (travelDistancePAZ > 4.8) {
                                        continue;
                                    }



                                    int originPAZ = 0;

                                    if (origin.getZoneId() == paz) {
                                        originPAZ = 1;
                                    }

                                    double utilityPAZ = destinationCalculator.calculateUtility(purpose,dataSet.getZone(paz),travelDistancePAZ,originPAZ);
                                    if (Double.isInfinite(utilityPAZ) || Double.isNaN(utilityPAZ)) {
                                        throw new RuntimeException(utilityPAZ + " utility calculated! Please check calculation!" +
                                                " Origin: " + origin + " | Destination: " + destination + " | Distance: "
                                                + dataSet.getPAZImpedance().get(origin.getSuperPAZId(), destination.getSuperPAZId()) +
                                                " | Purpose: " + purpose + " | attraction rate: " + destination.getTotalEmpl());
                                    }


                                    if (utilityPAZ != 0.0f) {
                                        utilityListPAZ.put(paz, (float) utilityPAZ);
                                        impedanceListPAZ.put(paz, travelDistancePAZ);
                                    }
                                }

                                float sumPAZ = FloatDescriptive.sum(utilityListPAZ.values());
                                if (sumPAZ == 0.0f) {
                                    logger.info("sumPAZ is 0!");
                                    continue;
                                }

                                for (int paz : utilityListPAZ.keys().elements()) {
                                    float probabilityPAZ = utilityListPAZ.get(paz)*(utilityMatrices.get(purpose).get(destination.getSuperPAZId())/sumPAZ);
                                    utilityMatricesPAZ.get(purpose).put(paz,probabilityPAZ);
                                }
                            }
                        }


                        //find destination and set trip distance for all trips generated from this zone
                        for (MopedTrip trip : tripsByZone.get(origin)){
                            totalProcessedTrip.incrementAndGet();
                            if(trip.getTripPurpose().equals(Purpose.HBW)||trip.getTripPurpose().equals(Purpose.HBE)){
                                if(trip.getTripOrigin()==null||trip.getTripDestination()==null){
                                    //logger.warn("trip " + trip.getTripId() + ", purpose " + trip.getTripPurpose()+", has no home or occupation zone" + trip.getTripOrigin() + "," + trip.getTripDestination());
                                    NOOCCUPATIONCOUNTER.incrementAndGet();
                                    continue;
                                }else{
                                    //TODO: allow HBW walk trip allow 4.8km
                                    if (!impedanceListPAZ.containsKey(trip.getTripDestination().getZoneId())){
                                        trip.setWalkMode(Boolean.FALSE);
                                        continue;
                                    }
                                    trip.setTripDistance(impedanceListPAZ.get(trip.getTripDestination().getZoneId()));
                                }
                            }

                            chooseMode(trip, calculateTripProbabilities(trip), rand);

                            if(trip.isWalkMode()){
                                final int selectedZoneId = MoPeDUtil.select(utilityMatricesPAZ.get(trip.getTripPurpose()), rand);
                                trip.setTripDestination(dataSet.getZone(selectedZoneId));
                                trip.setTripDistance(impedanceListPAZ.get(selectedZoneId));
                                AgentBasedModel.distributedTripsCounter.incrementAndGet();
                            }
                        }
                        counterr++;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
        }
        executor.execute();
    }
*/
    /*private double calculateTripProbabilities(MopedTrip trip) {
        return modeChoiceCalculator.calculateProbabilities(trip.getPerson().getMopedHousehold(), trip.getPerson(),trip);
    }

    private void chooseMode(MopedTrip trip, double probabilities, Random rand) {
        double random = rand.nextDouble();
        trip.setWalkMode(random <= probabilities);
    }*/
}
