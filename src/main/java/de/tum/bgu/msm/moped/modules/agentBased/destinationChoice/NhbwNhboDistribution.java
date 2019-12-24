package de.tum.bgu.msm.moped.modules.agentBased.destinationChoice;

import cern.colt.matrix.tfloat.FloatMatrix1D;
import cern.colt.matrix.tfloat.impl.DenseLargeFloatMatrix2D;
import com.google.common.collect.ImmutableList;
import com.google.common.math.LongMath;
import de.tum.bgu.msm.moped.data.*;
import de.tum.bgu.msm.moped.modules.destinationChoice.TripDistribution;
import de.tum.bgu.msm.moped.util.MoPeDUtil;
import de.tum.bgu.msm.moped.util.concurrent.RandomizableConcurrentFunction;
import org.apache.commons.math3.util.FastMath;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.IntStream;

import static de.tum.bgu.msm.moped.data.Purpose.*;


/**
 * @author Nico
 */
public final class NhbwNhboDistribution extends RandomizableConcurrentFunction<Void> {


    private final static Logger logger = Logger.getLogger(HbsHboDistribution.class);

    private final Purpose purpose;
    private final List<Purpose> priorPurposes;
    private final Occupation relatedMitoOccupationStatus;
    private final EnumMap<Purpose, DenseLargeFloatMatrix2D> baseProbabilities;
    private final DataSet dataSet;

    private final Map<Integer, MopedZone> zonesCopy;

    private NhbwNhboDistribution(Purpose purpose, List<Purpose> priorPurposes, Occupation relatedMitoOccupationStatus,
                                 EnumMap<Purpose, DenseLargeFloatMatrix2D> baseProbabilities, DataSet dataSet) {
        super(MoPeDUtil.getRandomObject().nextLong());
        this.purpose = purpose;
        this.priorPurposes = priorPurposes;
        this.relatedMitoOccupationStatus = relatedMitoOccupationStatus;
        this.baseProbabilities = baseProbabilities;
        this.dataSet = dataSet;
        this.zonesCopy = new HashMap<>(dataSet.getZones());
    }

    public static NhbwNhboDistribution nhbw(EnumMap<Purpose, DenseLargeFloatMatrix2D> baseProbabilites, DataSet dataSet) {
        return new NhbwNhboDistribution(Purpose.NHBW, Collections.singletonList(HBW),
                Occupation.WORKER, baseProbabilites, dataSet);
    }

    public static NhbwNhboDistribution nhbo(EnumMap<Purpose, DenseLargeFloatMatrix2D> baseProbabilites, DataSet dataSet) {
        return new NhbwNhboDistribution(Purpose.NHBO, ImmutableList.of(HBO, HBE, HBS),
                null, baseProbabilites, dataSet);
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
                        MopedZone origin = findOrigin(household, trip);
                        if (origin == null) {
                            logger.debug("No origin found for trip" + trip);
                            AgentTripDistribution.failedTripsCounter.incrementAndGet();
                            continue;
                        }
                        trip.setTripOrigin(origin);
                        MopedZone destination = findDestination(trip.getTripOrigin().getSuperPAZId());
                        trip.setTripDestination(destination);
                        if (destination == null) {
                            logger.debug("No destination found for trip" + trip);
                            //TripDistribution.failedTripsCounter.incrementAndGet();
                            continue;
                        }
                        trip.setTripDistance(dataSet.getPAZImpedance().get(trip.getTripOrigin().getSuperPAZId(), trip.getTripDestination().getSuperPAZId()));
                        AgentTripDistribution.distributedTripsCounter.incrementAndGet();
                    }
            } else {
                AgentTripDistribution.failedTripsCounter.incrementAndGet();
            }

            counter++;
        }
        return null;
    }

    private boolean hasTripsForPurpose(MopedHousehold household) {
        return !household.getTripsForPurpose(purpose).isEmpty();
    }

    private MopedZone findOrigin(MopedHousehold household, MopedTrip trip) {
        final List<MopedZone> possibleBaseZones = new ArrayList<>();
        for (Purpose purpose : priorPurposes) {
            for (MopedTrip priorTrip : household.getTripsForPurpose(purpose)) {
                if (priorTrip.getPerson().equals(trip.getPerson())) {
                    possibleBaseZones.add(priorTrip.getTripDestination());
                }
            }
        }
        if (!possibleBaseZones.isEmpty()) {
            return MoPeDUtil.select(random, possibleBaseZones);
        }
        if (trip.getPerson().getOccupation() == relatedMitoOccupationStatus &&
            trip.getPerson().getOccupation() != null) {
            return trip.getPerson().getOccupationZone();
        }

        final Purpose selectedPurpose = MoPeDUtil.select(random, priorPurposes);
        return findRandomOrigin(household, selectedPurpose);
    }

    private MopedZone findDestination(int origin) {
        final FloatMatrix1D row;
        if(purpose.equals(HBE)||purpose.equals(HBW)) {
            row = baseProbabilities.get(HBW).viewRow(origin);
        }else{
            row = baseProbabilities.get(HBO).viewRow(origin);
        }
        int destinationInternalId = MoPeDUtil.select(row.toArray(), random);
        return zonesCopy.get(dataSet.getExternalForInternal(destinationInternalId));
    }

    private MopedZone findRandomOrigin(MopedHousehold household, Purpose priorPurpose) {
        final FloatMatrix1D originProbabilities;
        if(priorPurpose.equals(HBE)||priorPurpose.equals(HBW)) {
            originProbabilities = baseProbabilities.get(HBW).viewRow(household.getHomeZone().getSuperPAZId());
        }else{
            originProbabilities = baseProbabilities.get(HBO).viewRow(household.getHomeZone().getSuperPAZId());
        }
        final int destinationInternalId = MoPeDUtil.select(originProbabilities.toArray(), random);
        return zonesCopy.get(dataSet.getExternalForInternal(destinationInternalId));
    }

}
