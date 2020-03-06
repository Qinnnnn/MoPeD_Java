package de.tum.bgu.msm.moped.modules.destinationChoice;

import cern.colt.matrix.tfloat.impl.DenseLargeFloatMatrix2D;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.data.SuperPAZ;
import de.tum.bgu.msm.moped.data.MopedZone;
import de.tum.bgu.msm.moped.io.output.OutputWriter;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

public abstract class TripDistributor {

    protected final DataSet dataSet;
    protected final Purpose purpose;
    private DenseLargeFloatMatrix2D tripDistribution;
    protected Map<Integer, Double> destinationUtility = new HashMap<>();
    private Map<Integer, Double> sumExpUtilityList;
    private final Map<Purpose, Double> coefNoCarByPurpose = new HashMap<Purpose, Double>(){{
        put(Purpose.HBW, -1.588032);
        put(Purpose.HBSHOP, -1.586985);
        put(Purpose.HBREC, -2.232597);
        put(Purpose.HBOTH, -2.616255);
        put(Purpose.NHBW, -2.540999);
        put(Purpose.NHBNW, -2.639274);
    }};

    private final Map<Purpose, Double> coefHasCarByPurpose = new HashMap<Purpose, Double>(){{
        put(Purpose.HBW, -1.857719);
        put(Purpose.HBSHOP, -1.586985);
        put(Purpose.HBREC, -2.232597);
        put(Purpose.HBOTH, -2.616255);
        put(Purpose.NHBW, -2.540999);
        put(Purpose.NHBNW, -2.639274);
    }};

    private final Map<Purpose, Double> coefInteractionByPurpose = new HashMap<Purpose, Double>(){{
        put(Purpose.HBW, 0.0);
        put(Purpose.HBSHOP, -0.152136);
        put(Purpose.HBREC, 0.0);
        put(Purpose.HBOTH, 0.0);
        put(Purpose.NHBW, 0.0);
        put(Purpose.NHBNW, 0.0);
    }};

    //private final double intercept = -0.761;//-0.261;

    public TripDistributor(DataSet dataSet, Purpose purpose) {
        this.dataSet = dataSet;
        this.purpose = purpose;
    }

    public void run () {
        int origins = dataSet.getOriginPAZs().size();
        int destinations = dataSet.getDestinationSuperPAZs().size();
        calculateDestinationUtility();
        tripDistribution = new DenseLargeFloatMatrix2D(origins, destinations);
        sumExpUtilityList = new HashMap<>();
        calculateUtilityCalculator(coefNoCarByPurpose.get(purpose),coefInteractionByPurpose.get(purpose));
        distributeNoCarTrips();
        dataSet.addDistributionNoCar(tripDistribution, purpose);

        tripDistribution = new DenseLargeFloatMatrix2D(origins, destinations);
        sumExpUtilityList = new HashMap<>();
        calculateUtilityCalculator(coefHasCarByPurpose.get(purpose),coefInteractionByPurpose.get(purpose));
        distributeHasCarTrips();
        dataSet.addDistributionHasCar(tripDistribution, purpose);
        //distributeTripsToPAZ(coefByPurpose.get(purpose));
    }


    //Read impedance from csv file
    private void calculateUtilityCalculator(double coef, double interaction) {
        for (MopedZone origin : dataSet.getOriginPAZs().values()) {
            double sumExpUtility = 0.0;
            float expUtility;
            for (SuperPAZ destination : dataSet.getDestinationSuperPAZs().values()){
                float distance = dataSet.getImpedance().get(origin.getSuperPAZId(),destination.getIndex());
                if ( distance == 0.f){
                    continue;
                }

                double utilitySum = coef * distance + destinationUtility.get(destination.getIndex())+interaction*distance*destination.getRetail();

                expUtility = (float) Math.exp(utilitySum);
                tripDistribution.setQuick(origin.getIndex(), destination.getIndex(), expUtility);
                sumExpUtility += expUtility;
            }
            sumExpUtilityList.put(origin.getIndex(), sumExpUtility);
        }
    }


    private void distributeNoCarTrips() {
        float distributions;
        double probability;

        for (MopedZone origin: dataSet.getOriginPAZs().values()) {
            for (SuperPAZ destination : dataSet.getDestinationSuperPAZs().values()){
                float distance = dataSet.getImpedance().get(origin.getSuperPAZId(),destination.getIndex());
                if ( distance == 0.f){
                    continue;
                }
                if (sumExpUtilityList.get(origin.getIndex()) == 0.0){
                    distributions = 0.0f;
                }else{
                    probability = tripDistribution.get(origin.getIndex(),destination.getIndex())/sumExpUtilityList.get(origin.getIndex());
                    distributions =  (float)probability * origin.getTotalWalkTripsNoCarByPurpose().get(purpose);
                }
                tripDistribution.setQuick(origin.getIndex(),destination.getIndex(),distributions);
            }
        }

    }

    private void distributeHasCarTrips() {
        float distributions;
        double probability;

        for (MopedZone origin: dataSet.getOriginPAZs().values()) {
            for (SuperPAZ destination : dataSet.getDestinationSuperPAZs().values()){
                float distance = dataSet.getImpedance().get(origin.getSuperPAZId(),destination.getIndex());
                if ( distance == 0.f){
                    continue;
                }
                if (sumExpUtilityList.get(origin.getIndex()) == 0.0){
                    distributions = 0.0f;
                }else{
                    probability = tripDistribution.get(origin.getIndex(),destination.getIndex())/sumExpUtilityList.get(origin.getIndex());
                    distributions =  (float)probability * origin.getTotalWalkTripsHasCarByPurpose().get(purpose);
                }
                tripDistribution.setQuick(origin.getIndex(),destination.getIndex(),distributions);
            }
        }

    }

    //    //Read impedance from omx matrices
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
//                System.out.println(utilitySum);
//                expUtility = (float) Math.exp(utilitySum);
//                tripDistribution.setQuick(origin.getIndex(), index, expUtility);
//                sumExpUtility += expUtility;
//            }
//            sumExpUtilityList.put(origin.getIndex(), sumExpUtility);
//        }
//
//    }

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

//    private void distributeTripsToPAZ(double coef) {
//        for(MopedZone origin : dataSet.getOriginPAZs().values()){
//            for(SuperPAZ superPAZ: dataSet.getDestinationSuperPAZs().values()) {
//                float sumExpUtility = 0;
//                Map<Integer, Double> destinationUtilityPAZ = calculateDestinationUtilityPAZ(superPAZ);
//
//                for (MopedZone paz : superPAZ.getPazs().values()) {
//                    double impedance = dataSet.getPAZImpedance().getQuick(origin.getZoneId(),paz.getZoneId());
//                    float expUtility = (float) Math.exp(coef*impedance + destinationUtilityPAZ.get(paz.getZoneId()));
//                    origin.getDistribution().put(paz.getZoneId(),expUtility);
//                    sumExpUtility += expUtility;
//                }
//
//                for (MopedZone paz : superPAZ.getPazs().values()) {
//                    double probability = origin.getDistribution().get(paz.getZoneId()) / sumExpUtility;
//                    float distributions = (float) probability * dataSet.getDistributionsByPurpose().get(purpose).get(origin.getIndex(),superPAZ.getIndex());
//                    origin.getDistribution().put(paz.getZoneId(),distributions);
//                }
//            }
//        }
//    }


    protected abstract void calculateDestinationUtility();
    protected abstract Map<Integer, Double> calculateDestinationUtilityPAZ(SuperPAZ superPAZ);
}
