package de.tum.bgu.msm.moped.modules.agentBased;

import cern.colt.map.tdouble.OpenIntDoubleHashMap;
import cern.colt.matrix.tfloat.FloatMatrix1D;
import cern.colt.matrix.tfloat.impl.DenseLargeFloatMatrix2D;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.math.LongMath;
import de.tum.bgu.msm.moped.data.*;
import de.tum.bgu.msm.moped.modules.agentBased.destinationChoice.AgentTripDistribution;
import de.tum.bgu.msm.moped.modules.agentBased.destinationChoice.DestinationUtilityJSCalculator;
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
import org.matsim.core.utils.geometry.CoordUtils;

import java.io.InputStreamReader;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static de.tum.bgu.msm.moped.data.Purpose.*;

/**
 * @author Nico
 */
public final class AgentBasedModel {

    public final static AtomicInteger distributedTripsCounter = new AtomicInteger(0);
    public final static AtomicInteger NOOCCUPATIONCOUNTER = new AtomicInteger(0);
    private final DestinationUtilityJSCalculator destinationCalculator;
    private final static Logger logger = Logger.getLogger(AgentBasedModel.class);
    private final DataSet dataSet;
    private final Random random;
    private final ModeChoiceJSCalculator modeChoiceCalculator;
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
        Reader mcReader = new InputStreamReader(this.getClass().getResourceAsStream("ModeChoiceAgent"));
        modeChoiceCalculator = new ModeChoiceJSCalculator(mcReader);
        Reader dcReader = new InputStreamReader(this.getClass().getResourceAsStream("TripDistribution"));
        destinationCalculator = new DestinationUtilityJSCalculator(dcReader);
    }

    public void run() {
        logger.info("find destination for moped walk trips, calculate distance on the fly...");
        List<MopedTrip> homeBasedTrips = dataSet.getTrips().values().stream().filter(tt->tt.getTripPurpose()!=Purpose.NHBW&&tt.getTripPurpose()!=Purpose.NHBO).collect(Collectors.toList());
        logger.info("there are total home based trips " + homeBasedTrips.size());
        homeBasedModel(homeBasedTrips, Resources.INSTANCE.getString(Properties.MATSIMNETWORK));
        logger.warn(NOOCCUPATIONCOUNTER + " work or education trips have no occupation zone!");
        logger.info(distributedTripsCounter + " walk trips haved been distributed.");
        logger.info(totalProcessedTrip + " walk trips haved been processed.");
    }

    public void runNonHomeBased() {
        logger.info("find destination for moped walk trips, calculate distance on the fly...");
        List<MopedTrip> nonHomeBasedTrips = dataSet.getTrips().values().stream().filter(tt->tt.getTripPurpose().equals(Purpose.NHBW)||tt.getTripPurpose().equals(Purpose.NHBO)).collect(Collectors.toList());
        nonHomeBasedModel(nonHomeBasedTrips, Resources.INSTANCE.getString(Properties.MATSIMNETWORK));
        logger.info(distributedTripsCounter + " walk trips haved been distributed.");
    }

    private void nonHomeBasedModel(List<MopedTrip> nonHomeBasedTrips, String networkFile) {
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

    private void homeBasedModel (List<MopedTrip> homeBasedTrips, String networkFile) {
        //Map<MopedZone,List<MopedTrip>> tripsByZone = dataSet.getTrips().values().stream().filter(tt->tt.isWalkMode()).collect(Collectors.groupingBy(tt -> tt.getTripOrigin())).entrySet().stream().filter(entry -> entry.getValue().size()>0).collect(Collectors.toMap(e->e.getKey(),e->e.getValue()));
        Map<MopedZone,List<MopedTrip>> tripsByZone = homeBasedTrips.stream().collect(Collectors.groupingBy(tt -> tt.getTripOrigin())).entrySet().stream().filter(entry -> entry.getValue().size()>0).collect(Collectors.toConcurrentMap(e->e.getKey(),e->e.getValue()));

        final int partitionSize = (int) ((double) tripsByZone.keySet().size() / Runtime.getRuntime().availableProcessors()) + 1;
        Iterable<List<MopedZone>> partitions = Iterables.partition(tripsByZone.keySet(), partitionSize);

        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(networkFile);
        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Runtime.getRuntime().availableProcessors());

        AtomicInteger counter = new AtomicInteger();
        FastMultiNodeDijkstraFactory fastMultiNodeDijkstraFactory = new FastMultiNodeDijkstraFactory(true);
        for (final List<MopedZone> partition : partitions) {
            MultiNodePathCalculator pathCalculator = (MultiNodePathCalculator) fastMultiNodeDijkstraFactory.createPathCalculator(network, new DistanceAsTravelDisutility(), (link, v, person, vehicle) -> link.getLength());
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
                        OpenIntDoubleHashMap impedenceList = new OpenIntDoubleHashMap();
                        EnumMap<Purpose, OpenIntDoubleHashMap> utilityMatrices = new EnumMap<>(Purpose.class);
                        for(Purpose purpose : ImmutableList.of(HBW,HBO,HBE,HBS)){
                            OpenIntDoubleHashMap utility = new OpenIntDoubleHashMap();
                            utilityMatrices.put(purpose,utility);
                        }

                        //generate toNodes vector and calculate Least Cost Path to all destination nodes
                        Set<InitialNode> toNodes = new HashSet<>();
                        Coord originCoord = CoordUtils.createCoord(((Geometry)(origin.getShapeFeature().getDefaultGeometry())).getCentroid().getCoordinate());
                        Node originNode = NetworkUtils.getNearestNode(network, originCoord);
                        Collection<MopedZone> destinationZones = dataSet.getZoneSearchTree().getDisk(originCoord.getX(),originCoord.getY(),Double.parseDouble(Resources.INSTANCE.getString(Properties.DESTSEARCHDISTANCE)));
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
                            for (Purpose purpose : ImmutableList.of(HBW,HBO,HBE,HBS)){
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
                            totalProcessedTrip.incrementAndGet();
                            if(trip.getTripPurpose().equals(Purpose.HBW)||trip.getTripPurpose().equals(Purpose.HBE)){
                                if(trip.getTripOrigin()==null||trip.getTripDestination()==null){
                                    //logger.warn("trip " + trip.getTripId() + ", purpose " + trip.getTripPurpose()+", has no home or occupation zone" + trip.getTripOrigin() + "," + trip.getTripDestination());
                                    NOOCCUPATIONCOUNTER.incrementAndGet();
                                    continue;
                                }else{
                                    if (!impedenceList.containsKey(trip.getTripDestination().getZoneId())){
                                        trip.setWalkMode(Boolean.FALSE);
                                        continue;
                                    }
                                    trip.setTripDistance(impedenceList.get(trip.getTripDestination().getZoneId()));
                                }
                            }

                            chooseMode(trip, calculateTripProbabilities(trip), rand);

                            if(trip.isWalkMode()){
                                final int selectedZoneId = MoPeDUtil.select(utilityMatrices.get(trip.getTripPurpose()), rand);
                                trip.setTripDestination(dataSet.getZone(selectedZoneId));
                                trip.setTripDistance(impedenceList.get(selectedZoneId));
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

    private double calculateTripProbabilities(MopedTrip trip) {
        return modeChoiceCalculator.calculateProbabilities(trip.getPerson().getMopedHousehold(), trip.getPerson(),trip);
    }

    private void chooseMode(MopedTrip trip, double probabilities, Random rand) {
        double random = rand.nextDouble();
        trip.setWalkMode(random <= probabilities);
    }
}
