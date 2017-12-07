package de.tum.bgu.msm.moped.modules.walkModeChoice;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.HouseholdType;
import org.apache.log4j.Logger;

import java.util.Collection;

public class HBRecreationWalk {

    private static final Logger logger = Logger.getLogger(HBRecreationWalk.class);
    private final DataSet dataSet;
    private Table<Long, Integer, Double> WalkTrip;
    private Table<Long, Integer, Double> WalkExpUtility;
    private Table<Long, Integer, Double> VehicleExpUtility;



    public HBRecreationWalk(DataSet dataSet) {
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
        dataSet.setHbRecreationWalk(WalkTrip);
    }

    private void walkUtilityCalculator() {
        for (long zoneId : dataSet.getZones().keySet()) {
            //zonal-related utility
            double stfwy = dataSet.getZone(zoneId).getStfwy();
            double pie = dataSet.getZone(zoneId).getPie();
            int pieFlag = dataSet.getZone(zoneId).getPieFlag();
            int wa = dataSet.getZone(zoneId).getWa();
            double utilityZone = -1.093 * stfwy + 0.792 * wa+ 0.043 * pie + 0.530 * pieFlag;

            for (int hhTypeId : dataSet.getHhTypes().keySet()) {
                HouseholdType hhType = dataSet.getHouseholdType(hhTypeId);
                //household-related utility
                double utilityHousehold = 0.288 + -4.377 + 0.191*(hhType.getHouseholdSize()==2?1:0) + -0.242*(hhType.getAge()==3?1:0)+ 0.208*(hhType.getWorkers()==1?1:0)+ 0.301*(hhType.getWorkers()==2?1:0) +
                        1.089*(hhType.getCars()==0?1:0) + -0.463*(hhType.getCars()==2?1:0) + -0.690*(hhType.getCars()==3?1:0)+ 0.295*(hhType.getKids()==1?1:0) + 0.455*(hhType.getKids()==2?1:0) + 0.479*(hhType.getKids()==3?1:0);
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
            for (int hhTypeId : dataSet.getHhTypes().keySet()) {
                double walkExpUtility = WalkExpUtility.get(zoneId,hhTypeId);
                double vehicleExpUtility = VehicleExpUtility.get(zoneId,hhTypeId);
                double sumExpUtility = walkExpUtility + vehicleExpUtility;
                double walkProbability = walkExpUtility/sumExpUtility;
                double walkTrips = walkProbability * dataSet.getHbRecreationTripGen().get(zoneId,hhTypeId);
                WalkTrip.put(zoneId,hhTypeId,walkTrips);
            }
        }
    }
}
