package de.tum.bgu.msm.moped.modules.tripGeneration;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.HouseholdType;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.data.MopedZone;

public final class HBWorkGenerator extends TripGenerator{

    private static final double SCENARIO_JOB = 0;
    private double productionSum = 0;
    private double totalWorkers = 0;

    public HBWorkGenerator(DataSet dataSet) {
        super(dataSet, Purpose.HBW);
    }


    @Override
    protected float calculateProduction(float distribution, HouseholdType hhType) {
        int workers = hhType.getWorkers();
        float tripGenRate = 0;
        if (workers == 1){
            tripGenRate = 1.386047f;//1.38325222f;
        }else if (workers == 2){
            tripGenRate = 2.462282f;//2.39110122f;
        } else if (workers == 3){
            tripGenRate = 3.578358f;//3.88667372f;
        }
        float tripGen = tripGenRate * distribution;
        totalWorkers += (workers* distribution);
        productionSum += tripGen;
        return tripGen;
    }


    @Override
    protected void scaleProductions() {
        double attractionSum = 0;
        for (MopedZone zone : dataSet.getZones().values()){
            double shopEmpl = zone.getShoppingArea()/1000*3;
            double retailEmpl = Math.max(shopEmpl,zone.getRetail());
            double totalEmpl = zone.getAgriculture()+zone.getConstruction()+zone.getFinancial()+zone.getGovernment()+zone.getManufacturing()+retailEmpl+zone.getService()+zone.getTransportation()+zone.getWholesale();
            attractionSum += totalEmpl;
        }

        float factor = (float) ((attractionSum+SCENARIO_JOB)*1.36)/(float)productionSum;
        System.out.println(attractionSum + "," + productionSum + "," + totalWorkers);
        production = production.muli(factor);
    }
}
