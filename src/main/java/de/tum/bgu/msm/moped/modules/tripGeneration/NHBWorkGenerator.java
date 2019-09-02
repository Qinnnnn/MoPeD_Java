package de.tum.bgu.msm.moped.modules.tripGeneration;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.HouseholdType;
import de.tum.bgu.msm.moped.data.MopedZone;
import de.tum.bgu.msm.moped.data.Purpose;
import org.jblas.FloatMatrix;

import java.util.HashMap;
import java.util.Map;

public final class NHBWorkGenerator extends TripGenerator{

    private double productionSum = 0;

    public NHBWorkGenerator(DataSet dataSet) {
        super(dataSet, Purpose.NHBW);
    }


    @Override
    protected float calculateProduction(float distribution, HouseholdType hhType) {
        int workers = hhType.getWorkers();
        float tripGenRate = 0;
        if (workers == 0){
            tripGenRate = 0.107864f;
        }else if (workers == 1){
            tripGenRate = 0.835659f;
        } else if (workers == 2){
            tripGenRate = 1.723404f;
        }else if (workers == 3){
            tripGenRate = 2.33209f;
        }
        float tripGen = tripGenRate * distribution * 1.025f;
        productionSum += tripGen;
        return tripGen;
    }


    @Override
    protected void scaleProductions() {
//        double attractionSum = 0;
//        double test = 0;
//        for (MopedZone zone : dataSet.getZones().values()){
//            double shopEmpl = zone.getShoppingArea()/1000*3;
//            double retailEmpl = Math.max(shopEmpl,zone.getRetail());
//            double totalEmpl = zone.getAgriculture()+zone.getConstruction()+zone.getFinancial()+zone.getGovernment()+zone.getManufacturing()+retailEmpl+zone.getService()+zone.getTransportation()+zone.getWholesale();
//            attractionSum += totalEmpl * 1.025;
//            test += retailEmpl;
//        }
//        System.out.println(test);
//        float factor = (float) attractionSum/(float)productionSum;
//        System.out.println(attractionSum + "," + productionSum);
//        production = production.muli(factor);
        Map<Integer, Float> zoneWeightList = new HashMap<>();
        float sumWeight = 0.0f;
        for(MopedZone zone : dataSet.getZones().values()){
            float zoneWeight = 0.3362f * zone.getTotalHH() + 1.0f * zone.getAgriculture() +
                    4.2631f * zone.getConstruction() + 1.0f * zone.getFinancial() + 1.0f * zone.getRetail() +
                    4.2631f * zone.getService() + 1.9232f * zone.getGovernment() + 2.5396f * zone.getManufacturing() +
                    2.5396f * zone.getWholesale() + 3.2544f * zone.getTransportation();
            sumWeight += zoneWeight;
            zoneWeightList.put(zone.getZoneId(),zoneWeight);
        }

        FloatMatrix attraction = new FloatMatrix(dataSet.getOriginPAZs().size(), dataSet.getHOUSEHOLDTYPESIZE());;
        for (int hhTypeId : dataSet.getHhTypes().keySet()){
            float sumTripsByhhType = production.getColumn(hhTypeId).sum();
            for(MopedZone zone : dataSet.getOriginPAZs().values()){
                float tripGen = sumTripsByhhType * (zoneWeightList.get(zone.getZoneId())/sumWeight);
                attraction.put(zone.getIndex(),hhTypeId,tripGen);
            }
        }

        production = attraction;

    }
}
