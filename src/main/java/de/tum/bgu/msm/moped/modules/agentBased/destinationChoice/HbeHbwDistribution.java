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
public final class HbeHbwDistribution extends RandomizableConcurrentFunction<Void> {

    private final static Logger logger = Logger.getLogger(HbsHboDistribution.class);

    private final Purpose purpose;
    private final Occupation mopedOccupationStatus;
    private final DenseLargeFloatMatrix2D baseProbabilities;

    private final DataSet dataSet;
    private final Map<Integer, MopedZone> zonesCopy;

    private HbeHbwDistribution(Purpose purpose, Occupation mopedOccupationStatus, DenseLargeFloatMatrix2D baseProbabilities, DataSet dataSet) {
        super(MoPeDUtil.getRandomObject().nextLong());
        this.purpose = purpose;
        this.mopedOccupationStatus = mopedOccupationStatus;
        this.baseProbabilities = baseProbabilities;
        this.dataSet = dataSet;
        this.zonesCopy = new HashMap<>(dataSet.getZones());
    }

    public static HbeHbwDistribution hbe(DenseLargeFloatMatrix2D baseprobabilities, DataSet dataSet) {
        return new HbeHbwDistribution(Purpose.HBE, Occupation.STUDENT, baseprobabilities, dataSet);
    }

    public static HbeHbwDistribution hbw(DenseLargeFloatMatrix2D baseprobabilities, DataSet dataSet) {
        return new HbeHbwDistribution(Purpose.HBW, Occupation.WORKER, baseprobabilities, dataSet);
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
                    if(trip.isWalkMode()) {
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

    private void findDestination(MopedHousehold household, MopedTrip trip) {
        if (isFixedByOccupation(trip)) {
            trip.setTripDestination(trip.getPerson().getOccupationZone());
        } else {
            AgentTripDistribution.randomOccupationDestinationTrips.incrementAndGet();
            FloatMatrix1D probabilities = baseProbabilities.viewRow(household.getHomeZone().getSuperPAZId());
            final int internalIndex = MoPeDUtil.select(probabilities.toArray(), random, probabilities.zSum());
            final MopedZone destination = zonesCopy.get(dataSet.getExternalForInternal(internalIndex));
            trip.setTripDestination(destination);
        }
    }

    private boolean hasTripsForPurpose(MopedHousehold household) {
        return !household.getTripsForPurpose(purpose).isEmpty();
    }

    private boolean isFixedByOccupation(MopedTrip trip) {
        if (trip.getPerson().getOccupation() == mopedOccupationStatus) {
            return trip.getPerson().getOccupation() != null;
        }
        return false;
    }
}
