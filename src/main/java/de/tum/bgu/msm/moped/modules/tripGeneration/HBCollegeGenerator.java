package de.tum.bgu.msm.moped.modules.tripGeneration;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.HouseholdType;
import de.tum.bgu.msm.moped.data.Zone;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HBCollegeGenerator {

    private static final Logger logger = Logger.getLogger(HBCollegeGenerator.class);
    private final DataSet dataSet;
    private Table<Integer, Integer, Double> hbCollegeProduction;
    private Map<Integer, Double> hbCollegeAttraction;
    public HBCollegeGenerator(DataSet dataSet) {
        this.dataSet = dataSet;
    }



    public void run () {
        Collection<Integer> zones = dataSet.getZones().keySet();
        Collection<Integer> households = dataSet.getHhTypes().keySet();
        hbCollegeProduction = ArrayTable.create(zones, households);
        hbCollegeAttraction = new HashMap<Integer, Double>();
        productionCalculator();
        attractionCalculator();
        dataSet.setHbCollegeProduction(hbCollegeProduction);
        dataSet.setHbCollegeAttraction(hbCollegeAttraction);
    }


    public void productionCalculator() {
        for (int zoneId : dataSet.getZones().keySet()){
            for (int hhTypeId : dataSet.getHhTypes().keySet()){
                double distribution = dataSet.getDistribution().get(zoneId,hhTypeId);
                Zone zone = dataSet.getZone(zoneId);
                HouseholdType hhType = dataSet.getHouseholdType(hhTypeId);
                int age = hhType.getAge();
                int hhSize = hhType.getHouseholdSize();
                double tripGenRate = 0.0;
                if (hhSize == 1){
                    switch (age){
                        case 1:
                            tripGenRate = 0.52380952;
                            break;
                        case 2:
                            tripGenRate = 0.05958549;
                            break;
                        case 3:
                            tripGenRate = 0.02985075;
                            break;
                        case 4:
                            tripGenRate = 0.01823708;
                            break;
                    }
                }else if (hhSize == 2){
                    switch (age){
                        case 1:
                            tripGenRate = 0.48387097;
                            break;
                        case 2:
                            tripGenRate = 0.17915691;
                            break;
                        case 3:
                            tripGenRate = 0.03581267;
                            break;
                        case 4:
                            tripGenRate = 0.03353057;
                            break;
                    }
                } else if (hhSize == 3){
                    switch (age){
                        case 1:
                            tripGenRate = 1.00000000;
                            break;
                        case 2:
                            tripGenRate = 0.23421927;
                            break;
                        case 3:
                            tripGenRate = 0.33980583;
                            break;
                        case 4:
                            tripGenRate = 0.06557377;
                            break;
                    }
                } else if (hhSize == 4) {
                    switch (age){
                        case 1:
                            tripGenRate = 0.45833333;
                            break;
                        case 2:
                            tripGenRate = 0.38158996;
                            break;
                        case 3:
                            tripGenRate = 0.58510638;
                            break;
                        case 4:
                            tripGenRate = 0.00000000;
                            break;
                    }
                }

                if (tripGenRate != 0){
                    double tripGen = tripGenRate * distribution * 1.074;
                    hbCollegeProduction.put(zoneId,hhTypeId,tripGen);
                }else{
                    logger.warn("no HBOther - tripGenRate matches to" + hhType.getHhTypeId() + "with" + age + "age and " + hhSize +"persons");
                }
            }

        }

    }


    private void attractionCalculator() {
        double productionSum = 0.0;
        for (double pr : hbCollegeProduction.values()){
            productionSum += pr;
        }

        double collegeTripSum = 0.0;
        for (Zone zone : dataSet.getZones().values()){
            collegeTripSum += zone.getCollegeVehicleTrip();
        }

        double balanceParameter = productionSum/collegeTripSum;

        for (Zone zone : dataSet.getZones().values()){
            double newAttraction = zone.getCollegeVehicleTrip()*balanceParameter;
            hbCollegeAttraction.put(zone.getZoneId(),newAttraction);
        }

    }
}
