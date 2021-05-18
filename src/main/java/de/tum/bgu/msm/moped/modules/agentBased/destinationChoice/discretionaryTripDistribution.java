package de.tum.bgu.msm.moped.modules.agentBased.destinationChoice;

import cern.colt.map.tfloat.OpenIntFloatHashMap;
import cern.jet.math.tfloat.FloatFunctions;
import cern.jet.stat.tfloat.FloatDescriptive;
import com.google.common.math.LongMath;
import de.tum.bgu.msm.moped.data.*;
import de.tum.bgu.msm.moped.modules.agentBased.AgentBasedModel;
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

/**
 * @author Qin
 */
public final class discretionaryTripDistribution extends RandomizableConcurrentFunction<Void> {

    private final static Logger logger = Logger.getLogger(discretionaryTripDistribution.class);
    private final int partitionId;
    private List<MopedZone> zoneList;
    private final DataSet dataSet;
    private final MultiNodePathCalculator pathCalculator;
    private final MultiNodePathCalculator subPathCalculator;
    private final Network network;
    private final DestinationUtilityCalculatorImpl destinationCalculator;
    private final Map<MopedZone,List<MopedTrip>> tripsByZone;
    private final List<Purpose> purposes;

    public discretionaryTripDistribution(DataSet dataSet, List<Purpose> purposes, List<MopedZone> zoneList, MultiNodePathCalculator pathCalculator,
                                         MultiNodePathCalculator subPathCalculator, Network network,
                                         DestinationUtilityCalculatorImpl destinationCalculator,
                                         Map<MopedZone,List<MopedTrip>> tripsByZone, int partitionId) {
        super(MoPeDUtil.getRandomObject().nextLong());
        this.zoneList = zoneList;
        this.dataSet = dataSet;
        this.pathCalculator = pathCalculator;
        this.subPathCalculator = subPathCalculator;
        this.network = network;
        this.destinationCalculator = destinationCalculator;
        this.tripsByZone = tripsByZone;
        this.partitionId = partitionId;
        this.purposes = purposes;
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

                for(Purpose purpose : purposes){
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

                    for (Purpose purpose : purposes){
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


                for (Purpose purpose : purposes) {
                    float sum = FloatDescriptive.sum(utilityMatrices.get(purpose).values());
                    utilityMatrices.get(purpose).assign(FloatFunctions.mult((float) (1. / sum)));
                }

                for ( int destinationId : utilityMatrices.get(purposes.get(0)).keys().elements()) {
                    SuperPAZ destination = dataSet.getSuperPAZ(destinationId);
                    //generate toNodes vector (toPAZ) and calculate Least Cost Path to all destination nodes
                    Map<Purpose, OpenIntFloatHashMap> utilityListPAZ = new HashMap<>();

                    for(Purpose purpose : purposes){
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

                        for (Purpose purpose : purposes){
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

                    for (Purpose purpose : purposes){
                        float sumPAZ = FloatDescriptive.sum(utilityListPAZ.get(purpose).values());
                        if (sumPAZ == 0.0f) {
                            continue;
                        }

                        for (int paz : utilityListPAZ.get(purpose).keys().elements()) {
                            float probabilityPAZ = utilityListPAZ.get(purpose).get(paz)*(utilityMatrices.get(purpose).get(destination.getSuperPAZId())/sumPAZ);
                            utilityMatricesPAZ.get(purpose).put(paz,probabilityPAZ);
                        }
                    }
                }

                //find destination and set trip distance for all trips generated from this zone
                for (MopedTrip trip : tripsByZone.get(origin)){
                    if(FloatDescriptive.sum(utilityMatricesPAZ.get(trip.getTripPurpose()).values())==0){
                        logger.warn("origin: " + origin.getZoneId() + " Purpose: " + trip.getTripPurpose() + " PAZ utility sum 0");
                        continue;
                    }
                    final int selectedZoneId = MoPeDUtil.select(origin.getZoneId(),utilityMatricesPAZ.get(trip.getTripPurpose()), random);
                    trip.setTripDestination(dataSet.getZone(selectedZoneId));
                    trip.setTripDistance(impedanceListPAZ.get(selectedZoneId));
                    AgentBasedModel.distributedTripsCounter.incrementAndGet();
                }
                counter++;
            }

        }catch (Exception e) {
            throw new RuntimeException(e);
        }

        return null;
    }
}
