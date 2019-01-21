package de.tum.bgu.msm.moped.modules.destinationChoice;

import cern.colt.matrix.tfloat.impl.DenseLargeFloatMatrix2D;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.data.SuperPAZ;
import de.tum.bgu.msm.moped.data.MopedZone;

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

        //distributionExpUtility = new DenseLargeFloatMatrix2D(origins,destinations);
        tripDistribution = new DenseLargeFloatMatrix2D(origins, destinations);
        calculateDestinationUtility();
        calculateUtilityCalculator(coefByPurpose.get(purpose));
        distributeTrips();
        dataSet.addDistribution(tripDistribution, purpose);
    }

//    private void calculateUtilityCalculator(double coef) {
//        for (Zone origin : dataSet.getOriginPAZs().values()) {
//            double sumExpUtility = 0.0;
//            float expUtility;
//            Map<Integer, Short> impedance = dataSet.getSuperPAZ(origin.getSuperPAZId()).getImpedanceToSuperPAZs();
//            for (int superPAZID : impedance.keySet()) {
//                int index = dataSet.getSuperPAZ(superPAZID).getIndex();
//                if ( destinationUtility.get(index) == null){
//                    continue;
//                }
//                double utilitySum = coef * impedance.get(superPAZID) + destinationUtility.get(index);
//                //System.out.println(utilitySum);
//                expUtility = (float) Math.exp(utilitySum);
//                tripDistribution.setQuick(origin.getIndex(), index, expUtility);
//                sumExpUtility += expUtility;
//            }
//            sumExpUtilityList.put(origin.getIndex(), sumExpUtility);
//        }
//
//    }

    private void calculateUtilityCalculator(double coef) {
        for (MopedZone origin : dataSet.getOriginPAZs().values()) {
            double sumExpUtility = 0.0;
            float expUtility;
            for (SuperPAZ destination : dataSet.getDestinationSuperPAZs().values()){
                float distance = dataSet.getImpedance().get(origin.getSuperPAZId(),destination.getSuperPAZId());
                if ( distance == 0.f){
                    continue;
                }
                double utilitySum = coef * distance + destinationUtility.get(destination.getIndex());
                expUtility = (float) Math.exp(utilitySum);
                tripDistribution.setQuick(origin.getIndex(), destination.getIndex(), expUtility);
                sumExpUtility += expUtility;
            }
            sumExpUtilityList.put(origin.getIndex(), sumExpUtility);
        }

    }

//    private void distributeTrips() {
//        float distributions;
//        double probability;
//
//        for (Zone origin: dataSet.getOriginPAZs().values()) {
//            Map<Integer, Short> impedance = dataSet.getSuperPAZ(origin.getSuperPAZId()).getImpedanceToSuperPAZs();
//            for (int superPAZID : impedance.keySet()) {
//                int index = dataSet.getSuperPAZ(superPAZID).getIndex();
//                if ( destinationUtility.get(index) == null){
//                    continue;
//                }
//                if (sumExpUtilityList.get(origin.getIndex()) == 0.0){
//                    distributions = 0.0f;
//                }else{
//                    probability = tripDistribution.get(origin.getIndex(),index)/sumExpUtilityList.get(origin.getIndex());
//                    distributions =  (float)probability * origin.getTotalWalkTripsByPurpose().get(purpose);
//                }
//                tripDistribution.setQuick(origin.getIndex(),index,distributions);
//            }
//        }
//
//    }

    private void distributeTrips() {
        float distributions;
        double probability;

        for (MopedZone origin: dataSet.getOriginPAZs().values()) {
            for (SuperPAZ destination : dataSet.getDestinationSuperPAZs().values()){
                float distance = dataSet.getImpedance().get(origin.getSuperPAZId(),destination.getSuperPAZId());
                if ( distance == 0.f){
                    continue;
                }
                if (sumExpUtilityList.get(origin.getIndex()) == 0.0){
                    distributions = 0.0f;
                }else{
                    probability = tripDistribution.get(origin.getIndex(),destination.getIndex())/sumExpUtilityList.get(origin.getIndex());
                    distributions =  (float)probability * origin.getTotalWalkTripsByPurpose().get(purpose);
                }
                tripDistribution.setQuick(origin.getIndex(),destination.getIndex(),distributions);
            }
        }

    }


    protected abstract void calculateDestinationUtility();

}
