package de.tum.bgu.msm.moped.modules.agentBased;

import cern.colt.map.tfloat.OpenIntFloatHashMap;
import cern.jet.math.tfloat.FloatFunctions;
import cern.jet.stat.tfloat.FloatDescriptive;
import com.google.common.collect.ImmutableList;
import com.google.common.math.LongMath;
import de.tum.bgu.msm.moped.data.*;
import de.tum.bgu.msm.moped.modules.agentBased.destinationChoice.DestinationUtilityCalculatorImpl;
import de.tum.bgu.msm.moped.modules.agentBased.walkModeChoice.ModeChoiceCalculatorImpl;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import de.tum.bgu.msm.moped.util.MoPeDUtil;
import de.tum.bgu.msm.moped.util.concurrent.RandomizableConcurrentFunction;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Geometry;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.router.ImaginaryNode;
import org.matsim.core.router.InitialNode;
import org.matsim.core.router.MultiNodeDijkstra;
import org.matsim.core.router.MultiNodePathCalculator;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.utils.geometry.CoordUtils;

import java.util.*;

import static de.tum.bgu.msm.moped.data.Purpose.*;

/**
 * @author Qin
 */
public final class HomeBasedDistribution extends RandomizableConcurrentFunction<Void> {

    private final static Logger logger = Logger.getLogger(HomeBasedDistribution.class);
    private final int partitionId;
    private List<MopedZone> zoneList;
    private final DataSet dataSet;
    private final MultiNodePathCalculator pathCalculator;
    private final MultiNodePathCalculator subPathCalculator;
    private final Network network;
    private final DestinationUtilityCalculatorImpl destinationCalculator;
    private final ModeChoiceCalculatorImpl modeChoiceCalculator;
    private final Map<MopedZone,List<MopedTrip>> tripsByZone;

    public HomeBasedDistribution(DataSet dataSet, List<MopedZone> zoneList, MultiNodePathCalculator pathCalculator,
                                  MultiNodePathCalculator subPathCalculator, Network network,
                                  DestinationUtilityCalculatorImpl destinationCalculator,
                                  ModeChoiceCalculatorImpl modeChoiceCalculator,
                                  Map<MopedZone,List<MopedTrip>> tripsByZone, int partitionId) {
        super(MoPeDUtil.getRandomObject().nextLong());
        this.zoneList = zoneList;
        this.dataSet = dataSet;
        this.pathCalculator = pathCalculator;
        this.subPathCalculator = subPathCalculator;
        this.network = network;
        this.destinationCalculator = destinationCalculator;
        this.modeChoiceCalculator = modeChoiceCalculator;
        this.tripsByZone = tripsByZone;
        this.partitionId = partitionId;
    }

    @Override
    public Void call() {

        int counter = 0;
        try {
            for (MopedZone origin : zoneList) {

                if(LongMath.isPowerOfTwo(counter)) {
                    logger.info(counter + " in " + partitionId);
                };

                //prepare utilityMatrices for all purpose
                OpenIntFloatHashMap impedanceListPAZ = new OpenIntFloatHashMap();

                EnumMap<Purpose, OpenIntFloatHashMap> utilityMatrices = new EnumMap<>(Purpose.class);
                EnumMap<Purpose, OpenIntFloatHashMap> utilityMatricesPAZ = new EnumMap<>(Purpose.class);

                for(Purpose purpose : ImmutableList.of(HBW,HBE,HBS,HBO)){
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

                    for (Purpose purpose : ImmutableList.of(HBW,HBE,HBS,HBO)){
                        float utility = destinationCalculator.calculateUtility(purpose,destination,travelDistance,crossMotorway);
                        if (Float.isInfinite(utility) || Float.isNaN(utility)) {
                            throw new RuntimeException(utility + " utility calculated! Please check calculation!" +
                                    " Origin: " + origin.getZoneId() + " | Destination: " + destination.getSuperPAZId() + " | Distance: "
                                    + travelDistance + " | Purpose: " + purpose + " | attraction rate: " + destination.getTotalEmpl() +
                                    "Cross motorway: " + crossMotorway + "Cross motorway: " + crossMotorway);
                        }
                        utilityMatrices.get(purpose).put(destination.getSuperPAZId(),utility);
                    }
                }


                for (Purpose purpose : ImmutableList.of(HBW,HBE,HBS,HBO)) {
                    float sum = FloatDescriptive.sum(utilityMatrices.get(purpose).values());
                    utilityMatrices.get(purpose).assign(FloatFunctions.mult((float) (1. / sum)));
                }

                int destinationCounter = 0;
                for ( int destinationId : utilityMatrices.get(HBW).keys().elements()) {
                    SuperPAZ destination = dataSet.getSuperPAZ(destinationId);
                    //generate toNodes vector (toPAZ) and calculate Least Cost Path to all destination nodes
                    Map<Purpose, OpenIntFloatHashMap> utilityListPAZ = new HashMap<>();

                    for(Purpose purpose : ImmutableList.of(HBW,HBE,HBS,HBO)){
                        OpenIntFloatHashMap utilityPAZ = new OpenIntFloatHashMap();
                        utilityListPAZ.put(purpose,utilityPAZ);
                    }

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
                                //logger.warn("There is no path from origin paz: " + origin.getZoneId() + " to destination paz: " + paz);
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

                        for (Purpose purpose : ImmutableList.of(HBW,HBE,HBS,HBO)){
                            float utilityPAZ = destinationCalculator.calculateUtility(purpose,dataSet.getZone(paz),travelDistancePAZ,originPAZ);
                            if (Float.isInfinite(utilityPAZ) || Float.isNaN(utilityPAZ)) {
                                throw new RuntimeException(utilityPAZ + " utility calculated! Please check calculation!" +
                                        " Origin: " + origin + " | Destination: " + destination + " | Distance: "
                                        + travelDistancePAZ +
                                        " | Purpose: " + purpose + " | attraction rate: " + destination.getTotalEmpl());
                            }

                            utilityListPAZ.get(purpose).put(paz, utilityPAZ);
                        }

                        impedanceListPAZ.put(paz, travelDistancePAZ);
                    }

                    for (Purpose purpose : ImmutableList.of(HBW,HBE,HBS,HBO)){
                        float sumPAZ = FloatDescriptive.sum(utilityListPAZ.get(purpose).values());
                        if (sumPAZ == 0.0f) {
                            continue;
                        }

                        for (int paz : utilityListPAZ.get(purpose).keys().elements()) {
                            float probabilityPAZ = utilityListPAZ.get(purpose).get(paz)*(utilityMatrices.get(purpose).get(destination.getSuperPAZId())/sumPAZ);
                            utilityMatricesPAZ.get(purpose).put(paz,probabilityPAZ);
                        }
                    }
                    destinationCounter++;
                }



                //find destination and set trip distance for all trips generated from this zone
                for (MopedTrip trip : tripsByZone.get(origin)){
                    AgentBasedModel.totalProcessedTrip.incrementAndGet();
                    if(trip.getTripPurpose().equals(Purpose.HBW)||trip.getTripPurpose().equals(Purpose.HBE)){
                        if(trip.getTripOrigin()==null||trip.getTripDestination()==null){
                            //logger.warn("trip " + trip.getTripId() + ", purpose " + trip.getTripPurpose()+", has no home or occupation zone" + trip.getTripOrigin() + "," + trip.getTripDestination());
                            AgentBasedModel.NOOCCUPATIONCOUNTER.incrementAndGet();
                            continue;
                        }else{
                            //TODO: allow HBW walk trip allow 4.8km
                            if (!impedanceListPAZ.containsKey(trip.getTripDestination().getZoneId())){
                                trip.setWalkMode(Boolean.FALSE);
                                continue;
                            }else{
                                trip.setTripDistance(impedanceListPAZ.get(trip.getTripDestination().getZoneId()));
                                chooseMode(trip, calculateTripProbabilities(trip), random);
                                continue;
                            }
                        }
                    }


                    chooseMode(trip, calculateTripProbabilities(trip), random);

                    if(trip.isWalkMode()){
                        if(FloatDescriptive.sum(utilityMatricesPAZ.get(trip.getTripPurpose()).values())==0){
                            logger.warn("origin: " + origin.getZoneId() + " Purpose: " + trip.getTripPurpose() + " PAZ utility sum 0");
                            continue;
                        }
                        final int selectedZoneId = MoPeDUtil.select(origin.getZoneId(),utilityMatricesPAZ.get(trip.getTripPurpose()), random);
                        trip.setTripDestination(dataSet.getZone(selectedZoneId));
                        trip.setTripDistance(impedanceListPAZ.get(selectedZoneId));
                        AgentBasedModel.distributedTripsCounter.incrementAndGet();
                    }
                }
                counter++;
            }

        }catch (Exception e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    private double calculateTripProbabilities(MopedTrip trip) {
        return modeChoiceCalculator.calculateProbabilities(trip.getPerson().getMopedHousehold(), trip);
    }

    private void chooseMode(MopedTrip trip, double probabilities, Random rand) {
        double random = rand.nextDouble();
        trip.setWalkMode(random <= probabilities);
    }
}
