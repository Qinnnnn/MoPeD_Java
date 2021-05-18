package de.tum.bgu.msm.moped.modules.agentBased.walkModeChoice;


import com.google.common.collect.Iterables;
import com.google.common.math.LongMath;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.MopedTrip;
import de.tum.bgu.msm.moped.data.Purpose;
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
import org.matsim.core.router.FastDijkstraFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.utils.geometry.CoordUtils;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static de.tum.bgu.msm.moped.data.Purpose.HBE;
import static de.tum.bgu.msm.moped.data.Purpose.HBW;


public class ModeChoice {

    private final static Logger logger = Logger.getLogger(ModeChoice.class);

    private final Map<Purpose, ModeChoiceCalculator> modeChoiceCalculatorByPurpose = new EnumMap<>(Purpose.class);
    protected final DataSet dataSet;
    protected final List<Purpose> purposes;

    public ModeChoice(DataSet dataSet, List<Purpose> purposes) {
        this.dataSet = dataSet;
        this.purposes = purposes;
        for(Purpose purpose : purposes){
            modeChoiceCalculatorByPurpose.put(purpose, new ModeChoiceCalculatorImpl());
        }
    }

    public void registerModeChoiceCalculator(Purpose purpose, ModeChoiceCalculator modeChoiceCalculator) {
        final ModeChoiceCalculator prev = modeChoiceCalculatorByPurpose.put(purpose, modeChoiceCalculator);
        if(prev != null) {
            logger.info("Overwrote mode choice calculator for purpose " + purpose + " with " + modeChoiceCalculator.getClass());
        }
    }

    public void run() {
        logger.info(" Calculating mode choice probabilities for each trip. Modes considered - 1. Auto driver, 2. Auto passenger, 3. Bicycle, 4. Bus, 5. Train, 6. Tram or Metro, 7. Walk ");
        modeChoiceByPurpose();
    }

    private void modeChoiceByPurpose() {
        if(purposes.equals(Purpose.getMandatoryPurposes())){
            buildMatsimDistance();
        }

        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(purposes.size());
        for (Purpose purpose : purposes) {
            executor.addTaskToQueue(new ModeChoiceByPurpose(purpose, dataSet, modeChoiceCalculatorByPurpose.get(purpose)));
        }
        executor.execute();
    }

    private void buildMatsimDistance() {
        List<MopedTrip> tripsForPurpose = dataSet.getTrips().values().stream().filter(tt->tt.getTripPurpose().equals(HBW)||tt.getTripPurpose().equals(HBE)).collect(Collectors.toList());

        final int partitionSize = (int) ((double) tripsForPurpose.size() / Runtime.getRuntime().availableProcessors()) + 1;
        Iterable<List<MopedTrip>> partitions = Iterables.partition(tripsForPurpose, partitionSize);
        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(Resources.INSTANCE.getString(Properties.MATSIMNETWORK));
        logger.info("Partition size: " + partitionSize);
        logger.info("Total trips: " + tripsForPurpose.size());

        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Runtime.getRuntime().availableProcessors());

        AtomicInteger counter = new AtomicInteger();

        for (final List<MopedTrip> partition : partitions) {
            LeastCostPathCalculator dijkstra = new FastDijkstraFactory(false)
                    .createPathCalculator(network, new DistanceAsTravelDisutility(), (link, v, person, vehicle) -> link.getLength());

            executor.addTaskToQueue(() -> {
                try {
                    int id = counter.incrementAndGet();
                    int counterr = 0;
                    for (MopedTrip trip : partition) {

                        if(LongMath.isPowerOfTwo(counterr)) {
                            logger.info(counterr + " in " + id);
                        };

                        if (trip.getTripOrigin() == null||trip.getTripDestination()==null) {
                            trip.setTripDistance(Double.POSITIVE_INFINITY);
                            counterr++;
                            continue;
                        }

                        Coord originCoord = CoordUtils.createCoord(((Geometry)(trip.getTripOrigin().getShapeFeature().getDefaultGeometry())).getCentroid().getCoordinate());
                        Node originNode = NetworkUtils.getNearestNode(network, originCoord);

                        Coord destinationCoord = CoordUtils.createCoord(((Geometry)(trip.getTripDestination().getShapeFeature().getDefaultGeometry())).getCentroid().getCoordinate());
                        Node destinationNode = NetworkUtils.getNearestNode(network, destinationCoord);
                        LeastCostPathCalculator.Path path = dijkstra.calcLeastCostPath(originNode,destinationNode,8 * 3600,null,null);
                        if(path == null){
                            logger.warn("There is no path from origin paz: " + trip.getTripOrigin().getZoneId() + " to destination paz: " + trip.getTripDestination().getZoneId());
                            continue;
                        }
                        trip.setTripDistance(path.travelTime/1000.);

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


    static class ModeChoiceByPurpose extends RandomizableConcurrentFunction<Void> {

        private final Purpose purpose;
        private final DataSet dataSet;
        private final ModeChoiceCalculator modeChoiceCalculator;
        private int countTripsSkipped;

        ModeChoiceByPurpose(Purpose purpose, DataSet dataSet, ModeChoiceCalculator modeChoiceCalculator) {
            super(MoPeDUtil.getRandomObject().nextLong());
            this.purpose = purpose;
            this.dataSet = dataSet;
            this.modeChoiceCalculator = modeChoiceCalculator;
        }

        @Override
        public Void call() {
            countTripsSkipped = 0;
            try {
                List<MopedTrip> tripsForPurpose = dataSet.getTrips().values().stream().filter(tt->tt.getTripPurpose().equals(purpose)).collect(Collectors.toList());
                for (MopedTrip trip : tripsForPurpose) {
                    if(trip.getTripOrigin()==null){
                        countTripsSkipped++;
                        continue;
                    }
                    chooseMode(trip, calculateTripProbabilities(trip));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            logger.info(countTripsSkipped + " trips skipped for " + purpose);
            return null;
        }

        private double calculateTripProbabilities(MopedTrip trip) {
            return modeChoiceCalculator.calculateProbabilities(trip.getPerson().getMopedHousehold(), trip);
        }

        private void chooseMode(MopedTrip trip, double probabilities) {
            trip.setWalkMode(random.nextDouble() <= probabilities);
        }

    }
}
