package de.tum.bgu.msm.moped.modules.agentBased;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import de.tum.bgu.msm.moped.data.*;
import de.tum.bgu.msm.moped.modules.agentBased.destinationChoice.DestinationUtilityCalculatorImpl;
import de.tum.bgu.msm.moped.modules.agentBased.destinationChoice.discretionaryTripDistribution;
import de.tum.bgu.msm.moped.modules.agentBased.walkModeChoice.ModeChoice;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import de.tum.bgu.msm.moped.util.concurrent.ConcurrentExecutor;
import org.apache.log4j.Logger;
import org.matsim.api.core.v01.network.Network;
import org.matsim.contrib.dvrp.router.DistanceAsTravelDisutility;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.router.*;

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
    //private final ModeChoiceCalculatorImpl modeChoiceCalculator;
    private final DestinationUtilityCalculatorImpl destinationCalculator;
    public final static AtomicInteger noPriorTripCounter = new AtomicInteger(0);
    private final Map<Purpose, List<Purpose>> priorPurposes = new HashMap<Purpose, List<Purpose>>(){{
        put(NHBO, ImmutableList.of(HBO, HBE, HBS));
        put(NHBW, Collections.singletonList(HBW));
    }};
    private final static AtomicInteger failedTripsCounter = new AtomicInteger(0);

    public AgentBasedModel(DataSet dataSet) {
        this.dataSet = dataSet;
        this.random = new Random();
        //modeChoiceCalculator = new ModeChoiceCalculatorImpl();
        destinationCalculator = new DestinationUtilityCalculatorImpl();
    }

    public void runMandatoryTrips(List<Purpose> purposes) {
        logger.info("Walk/Non-walk mode choice for mandatory trips...");
        ModeChoice modeChoiceMandatory = new ModeChoice(dataSet,purposes);
        modeChoiceMandatory.run();

    }

    public void runHomeBasedDiscretionaryTrips(List<Purpose> purposes) {
        logger.info("Walk/Non-walk mode choice for home based discretionary trips...");
        Map<Purpose,List<MopedTrip>> tripsByPurpose = dataSet.getTrips().values().stream().collect(Collectors.groupingBy(tt -> tt.getTripPurpose()));
        for(Purpose purpose:tripsByPurpose.keySet()){
            logger.warn(tripsByPurpose.get(purpose).size() + "  trips for purpose " + purpose);

        }


        ModeChoice modeChoiceHomeBasedDiscretionary = new ModeChoice(dataSet,purposes);
        modeChoiceHomeBasedDiscretionary.run();



        logger.info("find destination for moped walk trips, calculate distance on the fly...");
        List<MopedTrip> homeBasedDiscretionaryTrips = dataSet.getTrips().values().stream().filter(tt->Purpose.getHomeBasedDiscretionaryPurposes().contains(tt.getTripPurpose())).collect(Collectors.toList());
        logger.info(homeBasedDiscretionaryTrips.size() + " home based discretionary trips " );
        List<MopedTrip> homeBasedWalkDiscretionaryTrips = homeBasedDiscretionaryTrips.stream().filter(tt->tt.isWalkMode()).collect(Collectors.toList());
        logger.info(homeBasedWalkDiscretionaryTrips.size() + " home based discretionary walk trips " );

        logger.warn(homeBasedWalkDiscretionaryTrips.stream().filter(tt->tt.getTripOrigin()==null).count() + " home based trips has no origin PAZ!");

        final Map<MopedZone,List<MopedTrip>> tripsByZone = homeBasedWalkDiscretionaryTrips.stream().filter(tt->tt.getTripOrigin()!=null).collect(Collectors.groupingBy(tt -> tt.getTripOrigin())).entrySet().stream().filter(entry -> entry.getValue().size()>0).collect(Collectors.toConcurrentMap(e->e.getKey(),e->e.getValue()));
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
            homeBasedTasks.add(new discretionaryTripDistribution(dataSet,Purpose.getHomeBasedDiscretionaryPurposes(),partition,pathCalculator,
                    subPathCalculator,network,destinationCalculator,tripsByZone, partitionId));
            partitionId++;
        }
        executor.submitTasksAndWaitForCompletion(homeBasedTasks);

        logger.info(distributedTripsCounter + " walk trips haved been distributed.");

    }

    public void runNonHomeBasedTrips(List<Purpose> purposes) {
        logger.info("Walk/Non-walk mode choice for non home based trips...");
        ModeChoice modeChoiceNonHomeBased = new ModeChoice(dataSet,purposes);
        modeChoiceNonHomeBased.run();

        logger.info("find destination for moped walk trips, calculate distance on the fly...");
        List<MopedTrip> nonHomeBasedTrips = dataSet.getTrips().values().stream().filter(tt->Purpose.getNonHomeBasedPurposes().contains(tt.getTripPurpose())).collect(Collectors.toList());
        logger.info(nonHomeBasedTrips.size() + " non home based trips " );

        List<MopedTrip> nonHomeBasedWalkTrips = nonHomeBasedTrips.stream().filter(tt->tt.isWalkMode()).collect(Collectors.toList());
        logger.info(nonHomeBasedWalkTrips.size() + " non home based walk trips " );

        logger.warn(nonHomeBasedWalkTrips.stream().filter(tt->tt.getTripOrigin()==null).count() + " non home based walk trips has no origin PAZ!");

        final Map<MopedZone,List<MopedTrip>> tripsByZone = nonHomeBasedWalkTrips.stream().filter(tt->tt.getTripOrigin()!=null).collect(Collectors.groupingBy(tt -> tt.getTripOrigin())).entrySet().stream().filter(entry -> entry.getValue().size()>0).collect(Collectors.toConcurrentMap(e->e.getKey(),e->e.getValue()));
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
            nonHomeBasedTasks.add(new discretionaryTripDistribution(dataSet,Purpose.getNonHomeBasedPurposes(),partition,pathCalculator,
                    subPathCalculator,network,destinationCalculator,tripsByZone,partitionId));
            partitionId++;
        }
        executor.submitTasksAndWaitForCompletion(nonHomeBasedTasks);

        logger.info(distributedTripsCounter + " walk trips haved been distributed.");

    }

}
