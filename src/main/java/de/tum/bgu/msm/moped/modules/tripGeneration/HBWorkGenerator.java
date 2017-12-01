package de.tum.bgu.msm.moped.modules.tripGeneration;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.HouseholdType;
import de.tum.bgu.msm.moped.data.Zone;
import org.apache.log4j.Logger;

import java.rmi.activation.ActivationGroup_Stub;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class HBWorkGenerator {

    private static final Logger logger = Logger.getLogger(HBWorkGenerator.class);
    private final DataSet dataSet;
    private Table<Integer, Integer, Double> hbWorkProduction;
    private Map<Integer, Double> hbWorkAttraction;
    public HBWorkGenerator(DataSet dataSet) {
        this.dataSet = dataSet;
    }



    public void run () {
        Collection<Integer> zones = dataSet.getZones().keySet();
        Collection<Integer> households = dataSet.getHhTypes().keySet();
        hbWorkProduction = ArrayTable.create(zones, households);
        hbWorkAttraction = new HashMap<Integer, Double>();
        attractionCalculator();
        productionCalculator();
        dataSet.setHbWorkProduction(hbWorkProduction);
        dataSet.setHbWorkAttraction(hbWorkAttraction);
    }


    public void productionCalculator() {
        Iterator<Table.Cell<Integer,Integer,Double>> cells = hbWorkProduction.cellSet().iterator();
        if (cells.hasNext()){
            Table.Cell<Integer,Integer,Double> cell = cells.next();
            int zoneId = cell.getRowKey();
            int hhTypeId = cell.getColumnKey();
            double distribution = dataSet.getDistribution().get(zoneId,hhTypeId);
            Zone zone = dataSet.getZone(zoneId);
            HouseholdType hhType = dataSet.getHouseholdType(hhTypeId);
            int workers = hhType.getWorkers();
            double tripGenRate = 0.0;
            if (workers == 1){
                tripGenRate = 1.38325222;
            }else if (workers == 2){
                tripGenRate = 2.39110122;
            } else if (workers == 3){
                tripGenRate = 3.88667372;
            }
            double tripGen = tripGenRate * distribution;
            hbWorkProduction.put(zoneId,hhTypeId,tripGen);
        }

        //Calculate the total trip production
        double productionSum = 0.0;
        for (double pr: hbWorkProduction.values()){
            productionSum += pr;
        }

        //Calculate the total trip attraction
        double attractionSum = 0.0;
        for (double at: hbWorkAttraction.values()){
            attractionSum += at;
        }

        //balance production and attraction
        double factor = attractionSum/productionSum;
        Iterator<Table.Cell<Integer,Integer,Double>> productions = hbWorkProduction.cellSet().iterator();
        if (cells.hasNext()) {
            Table.Cell<Integer, Integer, Double> production = cells.next();
            int zoneId = production.getRowKey();
            int hhTypeId = production.getColumnKey();
            double oldPR = production.getValue();
            double newPR = oldPR * factor;
            hbWorkProduction.put(zoneId,hhTypeId,newPR);
        }

    }


    private void attractionCalculator() {

        for (Zone zone : dataSet.getZones().values()){
            double retailEmpl = 0.0;
            double shopEmpl = zone.getShoppingArea()/1000*3;
            if (zone.getRetail() >= shopEmpl){
                retailEmpl = zone.getRetail();
            }else{
                retailEmpl = shopEmpl;
            }
            double totalEmpl = zone.getAgriculture()+zone.getConstruction()+zone.getFinancial()+zone.getGovernment()+zone.getManufacturing()+retailEmpl+zone.getService()+zone.getTransportation()+zone.getWholesale();
            double attraction = totalEmpl * 1.48;
            hbWorkAttraction.put(zone.getZoneId(),attraction);
        }

    }
}
