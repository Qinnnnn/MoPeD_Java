package de.tum.bgu.msm.moped.modules.agentBased.destinationChoice;

import cern.colt.matrix.tfloat.impl.DenseLargeFloatMatrix2D;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.util.concurrent.ConcurrentExecutor;
import org.apache.log4j.Logger;
import org.matsim.core.utils.collections.Tuple;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Nico
 */
public final class AgentTripDistribution {

    public final static AtomicInteger distributedTripsCounter = new AtomicInteger(0);
    public final static AtomicInteger failedTripsCounter = new AtomicInteger(0);
    public final static AtomicInteger randomOccupationDestinationTrips = new AtomicInteger(0);

    private final EnumMap<Purpose, DenseLargeFloatMatrix2D> utilityMatrices = new EnumMap<>(Purpose.class);

    private final static Logger logger = Logger.getLogger(AgentTripDistribution.class);
    private final DataSet dataSet;

    public AgentTripDistribution(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public void run() {
        logger.info("Building initial destination choice utility matrices...");
        buildMatrices();

        logger.info("Distributing trips for households...");
        distributeTrips();
    }

    private void buildMatrices() {
        List<Callable<Tuple<Purpose,DenseLargeFloatMatrix2D>>> utilityCalcTasks = new ArrayList<>();
        for (Purpose purpose : Purpose.values()) {
            if(purpose.equals(Purpose.HBW)||purpose.equals(Purpose.HBO))
            utilityCalcTasks.add(new DestinationUtilityByPurposeGenerator(purpose, dataSet));
        }
        ConcurrentExecutor<Tuple<Purpose, DenseLargeFloatMatrix2D>> executor = ConcurrentExecutor.fixedPoolService(Purpose.values().length);
        List<Tuple<Purpose,DenseLargeFloatMatrix2D>> results = executor.submitTasksAndWaitForCompletion(utilityCalcTasks);
        for(Tuple<Purpose, DenseLargeFloatMatrix2D> result: results) {
            utilityMatrices.put(result.getFirst(), result.getSecond());
        }
    }

    private void distributeTrips() {
        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Purpose.values().length);
        List<Callable<Void>> homeBasedTasks = new ArrayList<>();
        homeBasedTasks.add(HbsHboDistribution.hbs(utilityMatrices.get(Purpose.HBO), dataSet));
        homeBasedTasks.add(HbsHboDistribution.hbo(utilityMatrices.get(Purpose.HBO), dataSet));
        homeBasedTasks.add(HbeHbwDistribution.hbw(utilityMatrices.get(Purpose.HBW), dataSet));
        homeBasedTasks.add(HbeHbwDistribution.hbe(utilityMatrices.get(Purpose.HBW), dataSet));
        executor.submitTasksAndWaitForCompletion(homeBasedTasks);

        executor = ConcurrentExecutor.fixedPoolService(Purpose.values().length);
        List<Callable<Void>> nonHomeBasedTasks = new ArrayList<>();
        nonHomeBasedTasks.add(NhbwNhboDistribution.nhbw(utilityMatrices, dataSet));
        nonHomeBasedTasks.add(NhbwNhboDistribution.nhbo(utilityMatrices, dataSet));
        executor.submitTasksAndWaitForCompletion(nonHomeBasedTasks);


        logger.info("Distributed: " + distributedTripsCounter + ", failed: " + failedTripsCounter);
        if(randomOccupationDestinationTrips.get() > 0) {
            logger.info("There have been " + randomOccupationDestinationTrips.get() +
                    " HBW or HBE trips not done by a worker or student or missing occupation zone. " +
                    "Picked a destination by random utility instead.");
        }
    }
}
