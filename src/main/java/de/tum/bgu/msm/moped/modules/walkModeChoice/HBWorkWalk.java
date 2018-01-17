package de.tum.bgu.msm.moped.modules.walkModeChoice;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.HouseholdType;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class HBWorkWalk {

    private static final Logger logger = Logger.getLogger(HBWorkWalk.class);
    private final DataSet dataSet;
    private Table<Long, Integer, Double> WalkTrip;
    private Table<Long, Integer, Double> WalkExpUtility;
    private Table<Long, Integer, Double> VehicleExpUtility;


    public HBWorkWalk(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public void run () {
        Collection<Long> zones = dataSet.getZones().keySet();
        Collection<Integer> households = dataSet.getHhTypes().keySet();
        WalkTrip = ArrayTable.create(zones, households);
        WalkExpUtility = ArrayTable.create(zones, households);
        VehicleExpUtility = ArrayTable.create(zones, households);
        walkUtilityCalculator();
        vehicleUtilityCalculator();
        walkTripsCalculator();
        dataSet.setHbWorkWalk(WalkTrip);
    }

    private void walkUtilityCalculator() {
        for (long zoneId : dataSet.getZones().keySet()) {
            //zonal-related utility
            double pie = dataSet.getZone(zoneId).getPie();
            int pieFlag = dataSet.getZone(zoneId).getPieFlag();
            double utilityZone = 0.036 * pie + 1.240 * pieFlag;

            for (int hhTypeId : dataSet.getHhTypes().keySet()) {
                HouseholdType hhType = dataSet.getHouseholdType(hhTypeId);
                //household-related utility
                double utilityHousehold = -5.033 + 0.719*(hhType.getHouseholdSize()==3?1:0) + -0.794*(hhType.getIncome()==2?1:0) + 0.957*(hhType.getAge()==1?1:0) + 0.343*(hhType.getAge()==3?1:0) +
                        1.597*(hhType.getCars()==0?1:0) + -0.834*(hhType.getCars()==2?1:0) + -1.178*(hhType.getCars()==3?1:0) + 0.752*(hhType.getKids()==2?1:0) + 1.121*(hhType.getKids()==3?1:0);

                //total utility
                double utilitySum = utilityZone + utilityHousehold;

                //exponential utility
                double expUtility = Math.exp(utilitySum);
                WalkExpUtility.put(zoneId,hhTypeId,expUtility);
            }
        }
    }

    private void vehicleUtilityCalculator() {
        for (long zoneId : dataSet.getZones().keySet()) {
            //TODO: zonal-related utility
            double utilityZone = 0.0;

            for (int hhTypeId : dataSet.getHhTypes().keySet()) {
                //TODO: household-related utility
                double utilityHousehold = 0.0;
                double utilitySum = utilityZone + utilityHousehold;
                double expUtility = Math.exp(utilitySum);
                VehicleExpUtility.put(zoneId,hhTypeId,expUtility);
            }
        }
    }

    private void walkTripsCalculator() {
        for (long zoneId : dataSet.getZones().keySet()) {
            double totalWalkTrips = 0.0;
            for (int hhTypeId : dataSet.getHhTypes().keySet()) {
                double walkExpUtility = WalkExpUtility.get(zoneId,hhTypeId);
                double vehicleExpUtility = VehicleExpUtility.get(zoneId,hhTypeId);
                double sumExpUtility = walkExpUtility + vehicleExpUtility;
                double walkProbability = walkExpUtility/sumExpUtility;
                double walkTrips = walkProbability * dataSet.getHbWorkProduction().get(zoneId,hhTypeId);
                WalkTrip.put(zoneId,hhTypeId,walkTrips);
                totalWalkTrips += walkTrips;
            }
            dataSet.getZone(zoneId).setHbWorkWalkTrips(totalWalkTrips);
        }
    }
}
