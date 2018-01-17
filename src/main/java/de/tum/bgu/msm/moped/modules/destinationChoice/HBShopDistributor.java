package de.tum.bgu.msm.moped.modules.destinationChoice;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.SuperPAZ;
import de.tum.bgu.msm.moped.data.Zone;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class HBShopDistributor {

    private static final Logger logger = Logger.getLogger(HBShopDistributor.class);
    private final DataSet dataSet;
    private Table<Long, Long, Double> distributionExpUtility;
    private Table<Long, Long, Double> tripDistribution;
    private Map<Long, Double> destinationUtility = new HashMap<Long, Double>();
    private Map<Long, Double> sumExpUtilityList = new HashMap<Long, Double>();

    public HBShopDistributor(DataSet dataSet) {
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
        dataSet.setHbShopDistribution(tripDistribution);
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
                    distributions =  probability * origin.getHbShopWalkTrips();
                }
                tripDistribution.put(origin.getZoneId(),destination.getSuperPAZId(),distributions);
            }
        }
    }


    private void UtilityCalculator() {
        double impedanceCoef = -2.25591;
        for (Zone origin : dataSet.getZones().values()) {
            double sumExpUtility = 0.0;
            double expUtility;
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
        double size = 0.910195;
        double empRET = 5.45389;
        double empAllOth = 0.0;
        double pie = -0.013919; //to be checked
        double slope = -0.194741;
        double freeway = -1.02347;
        double empAllOthPropotion = -1.74298;

        for (SuperPAZ superPAZ: dataSet.getDestinationSuperPAZs().values()){
            double employment = superPAZ.getRetail();
            double sizeVariable = Math.exp(empRET)* employment + Math.exp(empAllOth)*((superPAZ.getTotalEmpl() - employment));
            double empOtherPropotion;
            if (superPAZ.getTotalEmpl() == 0.0){
                empOtherPropotion = 0.0;
            }else {
                empOtherPropotion = (superPAZ.getTotalEmpl() - employment) / superPAZ.getTotalEmpl();
            }
            double barrierVariable = pie * superPAZ.getPie() + slope*superPAZ.getSlope() + freeway*superPAZ.getFreeway() + empAllOthPropotion*empOtherPropotion;
            double utility = size * Math.log(sizeVariable) + barrierVariable;
            destinationUtility.put(superPAZ.getSuperPAZId(),utility);
        }
    }
}
