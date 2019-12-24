package de.tum.bgu.msm.moped.modules.agentBased.destinationChoice;

import cern.colt.matrix.tfloat.FloatMatrix1D;
import cern.colt.matrix.tfloat.impl.DenseLargeFloatMatrix2D;
import com.google.common.math.LongMath;
import de.tum.bgu.msm.moped.data.*;
import de.tum.bgu.msm.moped.util.MoPeDUtil;
import de.tum.bgu.msm.moped.util.concurrent.RandomizableConcurrentFunction;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Nico
 */
public class HbsHboDistribution extends RandomizableConcurrentFunction<Void> {

    private final static Logger logger = Logger.getLogger(HbsHboDistribution.class);
    private final Purpose purpose;
    private final DenseLargeFloatMatrix2D baseProbabilities;
    private final DataSet dataSet;
    private final Map<Integer, MopedZone> zonesCopy;

    private HbsHboDistribution(Purpose purpose, DenseLargeFloatMatrix2D baseProbabilities, DataSet dataSet) {
        super(MoPeDUtil.getRandomObject().nextLong());
        this.dataSet = dataSet;
        this.purpose = purpose;
        this.baseProbabilities = baseProbabilities;
        this.zonesCopy = new HashMap<>(dataSet.getZones());
    }

    public static HbsHboDistribution hbs(DenseLargeFloatMatrix2D baseProbabilities, DataSet dataSet) {
        return new HbsHboDistribution(Purpose.HBS, baseProbabilities, dataSet);
    }

    public static HbsHboDistribution hbo(DenseLargeFloatMatrix2D baseProbabilities, DataSet dataSet) {
        return new HbsHboDistribution(Purpose.HBO, baseProbabilities, dataSet);
    }

    @Override
    public Void call() {
        long counter = 0;
        for (MopedHousehold household : dataSet.getHouseholds().values()) {
            if (LongMath.isPowerOfTwo(counter)) {
                logger.info(counter + " households done for Purpose " + purpose);
            }
            if (hasTripsForPurpose(household)) {
                for (MopedTrip trip : household.getTripsForPurpose(purpose)) {
                    trip.setTripOrigin(household.getHomeZone());
                    if (trip.isWalkMode()) {
                        findDestination(household, trip);
                        trip.setTripDistance(dataSet.getPAZImpedance().get(trip.getTripOrigin().getSuperPAZId(), trip.getTripDestination().getSuperPAZId()));
                        AgentTripDistribution.distributedTripsCounter.incrementAndGet();
                    }
                }
            }
            counter++;
        }
        return null;
    }

    private boolean hasTripsForPurpose(MopedHousehold household) {
        return !household.getTripsForPurpose(purpose).isEmpty();
    }

    private void findDestination(MopedHousehold household, MopedTrip trip) {
        FloatMatrix1D probabilities = baseProbabilities.viewRow(household.getHomeZone().getSuperPAZId());
        final int internalIndex = MoPeDUtil.select(probabilities.toArray(), random, probabilities.zSum());
        final MopedZone destination = zonesCopy.get(dataSet.getExternalForInternal(internalIndex));
        trip.setTripDestination(destination);
    }
}

