package de.tum.bgu.msm.moped.modules.destinationChoice;

import cern.colt.matrix.tfloat.impl.DenseLargeFloatMatrix2D;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.data.Zone;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.jblas.FloatMatrix;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public abstract class TripDistributor {

    protected final DataSet dataSet;
    protected final Purpose purpose;
    private DenseLargeFloatMatrix2D tripDistribution;
    protected Map<Integer, Double> destinationUtility = new HashMap<>();
    private Map<Integer, Double> sumExpUtilityList = new HashMap<>();
    private final Map<Purpose, Double> coefByPurpose = new HashMap<Purpose, Double>(){{
        put(Purpose.HBW, -1.35068);
        put(Purpose.HBSHOP, -2.25591);
        put(Purpose.HBREC, -1.74957);
        put(Purpose.HBOTH, -1.94038);
    }};

    public TripDistributor(DataSet dataSet, Purpose purpose) {
        this.dataSet = dataSet;
        this.purpose = purpose;
    }

    public void run () {
        int origins = dataSet.getOriginPAZs().size();
        int destinations = dataSet.getDestinationSuperPAZs().size();
        System.out.println(origins);
        System.out.println(destinations);
        //distributionExpUtility = new DenseLargeFloatMatrix2D(origins,destinations);
        tripDistribution = new DenseLargeFloatMatrix2D(origins, destinations);
        calculateDestinationUtility();
        calculateUtilityCalculator(coefByPurpose.get(purpose));
        distributeTrips();
        dataSet.addDistribution(tripDistribution, purpose);
    }

    private void calculateUtilityCalculator(double coef) {
        for (Zone origin : dataSet.getOriginPAZs().values()) {
            double sumExpUtility = 0.0;
            float expUtility;
            Map<Integer, Float> impedance = dataSet.getSuperPAZ(origin.getSuperPAZId()).getImpedanceToSuperPAZs();
            for (int index : impedance.keySet()) {
                double utilitySum = coef * impedance.get(index) + destinationUtility.get(index);
                expUtility = (float) Math.exp(utilitySum);
                tripDistribution.setQuick(origin.getIndex(), index, expUtility);
                sumExpUtility += expUtility;
            }
            sumExpUtilityList.put(origin.getIndex(), sumExpUtility);
        }

    }

    private void distributeTrips() {
        float distributions;
        double probability;

        for (Zone origin: dataSet.getOriginPAZs().values()) {
            Map<Integer, Float> impedance = dataSet.getSuperPAZ(origin.getSuperPAZId()).getImpedanceToSuperPAZs();
            for (int index : impedance.keySet()) {
                if (sumExpUtilityList.get(origin.getIndex()) == 0.0){
                    distributions = 0.0f;
                }else{
                    probability = tripDistribution.get(origin.getIndex(),index)/sumExpUtilityList.get(origin.getIndex());
                    distributions =  (float)probability * origin.getTotalWalkTripsByPurpose().get(purpose);
                }
                tripDistribution.setQuick(origin.getIndex(),index,distributions);


            }
        }

    }


    protected abstract void calculateDestinationUtility();

}
