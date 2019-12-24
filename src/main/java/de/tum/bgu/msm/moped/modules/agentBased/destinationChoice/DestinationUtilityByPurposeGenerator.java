package de.tum.bgu.msm.moped.modules.agentBased.destinationChoice;

import cern.colt.matrix.tfloat.impl.DenseLargeFloatMatrix2D;
import com.google.common.math.LongMath;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.MopedZone;
import de.tum.bgu.msm.moped.data.Purpose;
import javafx.util.Pair;
import org.apache.log4j.Logger;


import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;
import java.util.concurrent.Callable;

public class DestinationUtilityByPurposeGenerator implements Callable<Pair<Purpose, DenseLargeFloatMatrix2D>> {

    private final static Logger logger = Logger.getLogger(DestinationUtilityByPurposeGenerator.class);

    private final DestinationUtilityJSCalculator calculator;
    private final Purpose purpose;
    private final Map<Integer, MopedZone> zones;
    private final DataSet dataSet;


    DestinationUtilityByPurposeGenerator(Purpose purpose, DataSet dataSet) {
        this.purpose = purpose;
        this.zones = dataSet.getZones();
        this.dataSet = dataSet;
        Reader reader = new InputStreamReader(this.getClass().getResourceAsStream("TripDistribution"));
        calculator = new DestinationUtilityJSCalculator(reader, purpose);
    }

    @Override
    public Pair<Purpose, DenseLargeFloatMatrix2D> call() throws Exception {
        final DenseLargeFloatMatrix2D utilityMatrix = new DenseLargeFloatMatrix2D(zones.values().size()+1, zones.values().size()+1);
        long counter = 0;
        for (MopedZone origin : zones.values()) {
            for (MopedZone destination : zones.values()) {
                if(dataSet.getPAZImpedance().get(origin.getSuperPAZId(), destination.getSuperPAZId())!= 0.0f) {
                    final double utility = calculator.calculateUtility(destination.getTotalJobDensity(), destination.getIndustrialJobDensity(),
                            dataSet.getPAZImpedance().get(origin.getSuperPAZId(), destination.getSuperPAZId()));
                    if (Double.isInfinite(utility) || Double.isNaN(utility)) {
                        throw new RuntimeException(utility + " utility calculated! Please check calculation!" +
                                " Origin: " + origin + " | Destination: " + destination + " | Distance: "
                                + dataSet.getPAZImpedance().get(origin.getSuperPAZId(), destination.getSuperPAZId()) +
                                " | Purpose: " + purpose + " | attraction rate: " + destination.getTotalEmpl());
                    }
                    utilityMatrix.setQuick(origin.getSuperPAZId(), destination.getSuperPAZId(), (float) utility);
                }else{
                    utilityMatrix.setQuick(origin.getSuperPAZId(), destination.getSuperPAZId(), 0.0f);
                }
                if (LongMath.isPowerOfTwo(counter)) {
                    logger.info(counter + " OD pairs done for purpose " + purpose);
                }
                counter++;
            }
        }
        logger.info("Utility matrix for purpose " + purpose + " done.");
        return new Pair<>(purpose, utilityMatrix);
    }
}
