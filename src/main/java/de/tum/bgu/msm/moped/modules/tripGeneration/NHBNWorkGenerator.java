package de.tum.bgu.msm.moped.modules.tripGeneration;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.HouseholdType;
import de.tum.bgu.msm.moped.data.MopedZone;
import de.tum.bgu.msm.moped.data.Purpose;
import org.jblas.FloatMatrix;

import java.util.HashMap;
import java.util.Map;

public final class NHBNWorkGenerator extends TripGenerator{

    private double productionSum = 0;

    public NHBNWorkGenerator(DataSet dataSet) {
        super(dataSet, Purpose.NHBNW);
    }


    @Override
    protected float calculateProduction(float distribution, HouseholdType hhType) {
        int workers = hhType.getWorkers();
        int hhSize = hhType.getHouseholdSize();
        float tripGenRate = 0.0f;
        if (workers < hhSize){
            switch (hhSize){
                case 1:
                    tripGenRate = 1.165517f;
                    break;
                case 2:
                    tripGenRate = 1.651685f;
                    break;
                case 3:
                    tripGenRate = 1.956316f;
                    break;
                case 4:
                    tripGenRate = 3.161211f;
                    break;
            }
        }else if (workers == hhSize){
            switch (hhSize){
                case 1:
                    tripGenRate = 0.511022f;
                    break;
                case 2:
                    tripGenRate = 0.9187314f;
                    break;
                case 3:
                    tripGenRate = 1.425926f;
                    break;
            }
        } else{
            //logger.warn("Number of workers is bigger than householdsize");
        }

        float tripGen;
        if (tripGenRate != 0){
            tripGen = tripGenRate * distribution * 1.025f;//1.2f;
        }else{
            tripGen = 0.0f;
        }
        productionSum += tripGen;
        return tripGen;
    }


    @Override
    protected void scaleProductions() {
        Map<Integer, Float> zoneWeightList = new HashMap<>();
        float sumWeight = 0.0f;
        for(MopedZone zone : dataSet.getZones().values()){
            float zoneWeight = 0.2060f * zone.getAgriculture() +
                    0.1249f * zone.getConstruction() + 0.0255f * zone.getFinancial() + 0.3263f * zone.getRetail() +
                    1.0f * zone.getService() + 0.0255f * zone.getGovernment() + 0.0005f * zone.getManufacturing() +
                    0.0085f * zone.getWholesale() + 0.0185f * zone.getTransportation();
            sumWeight += zoneWeight;
            zoneWeightList.put(zone.getZoneId(),zoneWeight);
        }

        FloatMatrix attraction = new FloatMatrix(dataSet.getZones().size(), dataSet.getHOUSEHOLDTYPESIZE());;
        for (int hhTypeId : dataSet.getHhTypes().keySet()){
            float sumTripsByhhType = production.getColumn(hhTypeId).sum();
            for(MopedZone zone : dataSet.getZones().values()){
                float tripGen = sumTripsByhhType * (zoneWeightList.get(zone.getZoneId())/sumWeight);
                attraction.put(zone.getIndex(),hhTypeId,tripGen);
            }
        }

        production = attraction;
    }
}
