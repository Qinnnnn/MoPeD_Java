package de.tum.bgu.msm.moped.modules.tripGeneration;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.HouseholdType;
import de.tum.bgu.msm.moped.data.MopedZone;
import de.tum.bgu.msm.moped.data.Purpose;

public final class NHBNWorkGenerator extends TripGenerator{

    private double productionSum = 0;

    public NHBNWorkGenerator(DataSet dataSet) {
        super(dataSet, Purpose.NHBNW);
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
        productionSum += tripGen;
        return tripGen;
    }


    @Override
    protected void scaleProductions() {
        double attractionSum = 0;
        double test = 0;
        for (MopedZone zone : dataSet.getZones().values()){
            double shopEmpl = zone.getShoppingArea()/1000*3;
            double retailEmpl = Math.max(shopEmpl,zone.getRetail());
            double totalEmpl = zone.getAgriculture()+zone.getConstruction()+zone.getFinancial()+zone.getGovernment()+zone.getManufacturing()+retailEmpl+zone.getService()+zone.getTransportation()+zone.getWholesale();
            attractionSum += totalEmpl * 1.36;//1.48;
            test += retailEmpl;
        }
        System.out.println(test);
        float factor = (float) attractionSum/(float)productionSum;
        System.out.println(attractionSum + "," + productionSum);
        production = production.muli(factor);
    }
}
