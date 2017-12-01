package de.tum.bgu.msm.moped.modules.tripGeneration;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.HouseholdType;
import de.tum.bgu.msm.moped.data.Zone;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class HBSchoolGenerator {

    private static final Logger logger = Logger.getLogger(HBSchoolGenerator.class);
    private final DataSet dataSet;
    private Table<Integer, Integer, Double> hbSchoolProduction;
    private Map<Integer, Double> hbSchoolAttraction;
    public HBSchoolGenerator(DataSet dataSet) {
        this.dataSet = dataSet;
    }



    public void run () {
        Collection<Integer> zones = dataSet.getZones().keySet();
        Collection<Integer> households = dataSet.getHhTypes().keySet();
        hbSchoolProduction = ArrayTable.create(zones, households);
        productionCalculator();
        attractionCalculator();
        dataSet.setHbSchoolProduction(hbSchoolProduction);
        dataSet.setHbSchoolAttraction(hbSchoolAttraction);
    }

    public void productionCalculator() {
        Iterator<Table.Cell<Integer,Integer,Double>> cells = hbSchoolProduction.cellSet().iterator();
        if (cells.hasNext()){
            Table.Cell<Integer,Integer,Double> element = cells.next();
            int zoneId = element.getRowKey();
            int hhTypeId = element.getColumnKey();
            double distribution = dataSet.getDistribution().get(zoneId,hhTypeId);
            Zone zone = dataSet.getZone(zoneId);
            HouseholdType hhType = dataSet.getHouseholdType(hhTypeId);
            int kids = hhType.getKids();
            int hhSize = hhType.getHouseholdSize();
            double tripGenRate = 0.0;
            if (kids == 1){
                switch (hhSize){
                    case 2:
                        tripGenRate = 1.0000000;
                        break;
                    case 3:
                        tripGenRate = 1.3933333;
                        break;
                    case 4:
                        tripGenRate = 1.3132530;
                        break;
                }
            }else if (kids == 2){
                switch (hhSize){
                    case 2:
                        tripGenRate = 0.8000000;
                        break;
                    case 3:
                        tripGenRate = 2.5000000;
                        break;
                    case 4:
                        tripGenRate = 2.9808102;
                        break;
                }
            } else if (kids == 3){
                if (hhSize == 4){
                    tripGenRate = 4.8750000;
                }
            }

            if (tripGenRate != 0){
                double tripGen = tripGenRate * distribution;
                hbSchoolProduction.put(zoneId,hhTypeId,tripGen);
            }else{
                logger.warn("no HBOther - tripGenRate matches to" + hhType.getHhTypeId() + "with" + kids + "kids and " + hhSize +"persons");
            }

        }

        //Calibration the trip production based on school target
        double householdSum = 0.0;
        for (double hh : dataSet.getDistribution().values()){
            householdSum += hh;
        }

        double householdGrowth = householdSum/501701.0;
        double schoolTarget = householdGrowth * 392005;

        double hbSchoolTripSum = 0.0;
        for (double trips : hbSchoolProduction.values()){
            hbSchoolTripSum += trips;
        }

        double calibrationParameter = schoolTarget/hbSchoolTripSum;

        Iterator<Table.Cell<Integer,Integer,Double>> productions = hbSchoolProduction.cellSet().iterator();
        if (productions.hasNext()){
            Table.Cell<Integer,Integer,Double> element = productions.next();
            int zoneId = element.getRowKey();
            int hhTypeId = element.getColumnKey();
            double nonCaliTripGen = element.getValue();
            double caliTripGen = nonCaliTripGen * calibrationParameter;
            hbSchoolProduction.put(zoneId,hhTypeId,caliTripGen);
        }

    }

    public void attractionCalculator() {

        Set<Integer> zoneIdList = hbSchoolProduction.rowKeySet();
        for (int zoneId : zoneIdList){
            double attraction = 0.0;
            for (double pr : hbSchoolProduction.row(zoneId).values()){
                attraction += pr;
            }
            hbSchoolAttraction.put(zoneId,attraction);
        }
    }

}
