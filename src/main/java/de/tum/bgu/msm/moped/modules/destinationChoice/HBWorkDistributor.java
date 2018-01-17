package de.tum.bgu.msm.moped.modules.destinationChoice;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.SuperPAZ;
import de.tum.bgu.msm.moped.data.Zone;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class HBWorkDistributor {

    private static final Logger logger = Logger.getLogger(HBWorkDistributor.class);
    private final DataSet dataSet;
    private Table<Long, Long, Double> distributionExpUtility;
    private Table<Long, Long, Double> tripDistribution;
    private Map<Long, Double> destinationUtility = new HashMap<Long, Double>();
    private Map<Long, Double> sumExpUtilityList = new HashMap<Long, Double>();

    public HBWorkDistributor(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public void run () {
        Collection<Long> origins = dataSet.getZones().keySet();
        Collection<Long> destinations = dataSet.getDestinationSuperPAZs().keySet();
        distributionExpUtility = ArrayTable.create(origins, destinations);
        tripDistribution = ArrayTable.create(origins, destinations);
        destinationUtilityCalculator();
        UtilityCalculator();
        TripDistributor();
        dataSet.setHbWorkDistribution(tripDistribution);
    }

    private void TripDistributor() {
        double distributions;
        double probability;
        for (Zone origin: dataSet.getZones().values()) {
            for (SuperPAZ destination : dataSet.getDestinationSuperPAZs().values()) {
                if (sumExpUtilityList.get(origin.getZoneId()) == 0.0){
                    distributions = 0.0;
                }else{
                    probability = distributionExpUtility.get(origin.getZoneId(),destination.getSuperPAZId())/sumExpUtilityList.get(origin.getZoneId());
                    distributions =  probability * origin.getHbWorkWalkTrips();
                }
                tripDistribution.put(origin.getZoneId(),destination.getSuperPAZId(),distributions);
            }
        }
    }


    private void UtilityCalculator() {
        double impedanceCoef = -1.35068;
        for (Zone origin : dataSet.getZones().values()) {
            double sumExpUtility = 0.0;
            double expUtility = 0.0;
            for (SuperPAZ destination : dataSet.getDestinationSuperPAZs().values()) {
                //total utility = impedance utility + utility of trip attraction end
                if ((destinationUtility.get(destination.getSuperPAZId()) == Double.NEGATIVE_INFINITY) || (!dataSet.getImpedance().contains(origin.getSuperPAZId(), destination.getSuperPAZId()))) {
                    expUtility = 0.0;
                } else {
                    double utilitySum = impedanceCoef * dataSet.getImpedance().get(origin.getSuperPAZId(), destination.getSuperPAZId()) + destinationUtility.get(destination.getSuperPAZId());
                    expUtility = Math.exp(utilitySum);
                }
                distributionExpUtility.put(origin.getZoneId(), destination.getSuperPAZId(), expUtility);
                sumExpUtility += expUtility;
            }
            sumExpUtilityList.put(origin.getZoneId(), sumExpUtility);
        }
    }


    private void destinationUtilityCalculator() {
        double size = 0.505691;
        double empRETFINGOV = 2.01532;
        double pie = 0.0296117;
        double slope = -0.114512;
        double freeway = -0.297436;
        double empAllOthPropotion = -0.987008;

        for (SuperPAZ superPAZ: dataSet.getDestinationSuperPAZs().values()){
            double employment = superPAZ.getRetail() + superPAZ.getFinancial() + superPAZ.getGovernment();
            double empOtherPropotion;
            if (superPAZ.getTotalEmpl() == 0.0){
                empOtherPropotion = 0.0;
            }else {
                empOtherPropotion = (superPAZ.getTotalEmpl() - employment) / superPAZ.getTotalEmpl();
            }
            double sizeVariable = Math.exp(empRETFINGOV)* employment;
            double supportVariable = pie * superPAZ.getPie();
            double barrierVariable = slope*superPAZ.getSlope() + freeway*superPAZ.getFreeway() + empAllOthPropotion*empOtherPropotion;
            double utility = size * Math.log(sizeVariable) + supportVariable + barrierVariable;
            destinationUtility.put(superPAZ.getSuperPAZId(),utility);
        }
    }
}
