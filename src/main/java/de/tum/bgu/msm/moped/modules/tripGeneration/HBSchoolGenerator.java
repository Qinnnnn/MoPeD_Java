package de.tum.bgu.msm.moped.modules.tripGeneration;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.HouseholdType;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.data.MopedZone;

public final class HBSchoolGenerator extends TripGenerator{

    private float productionSum = 0.0f;
    private final float HOUSEHOLD_PARAM = 501701.0f;

    public HBSchoolGenerator(DataSet dataSet) {
        super(dataSet, Purpose.HBSCH);
    }

    @Override
    protected float calculateProduction(float distribution, HouseholdType hhType) {
        int kids = hhType.getKids();
        int hhSize = hhType.getHouseholdSize();
        float tripGenRate = 0.0f;
        if (kids == 1){
            switch (hhSize){
                case 2:
                    tripGenRate = 1.978448f;//1.0000000f;
                    break;
                case 3:
                    tripGenRate = 1.84793f;//1.3933333f;
                    break;
                case 4:
                    tripGenRate = 2.248879f;//1.3132530f;
                    break;
            }
        }else if (kids == 2){
            switch (hhSize){
                case 2:
                    tripGenRate = 0.0f;//0.8000000f;
                    break;
                case 3:
                    tripGenRate = 3.326389f;//2.5000000f;
                    break;
                case 4:
                    tripGenRate = 3.441193f;//2.9808102f;
                    break;
            }
        } else if (kids == 3){
            if (hhSize == 4){
                tripGenRate = 5.103783f;//4.8750000f;
            }
        }

        float tripGen;

        if (tripGenRate != 0){
            tripGen = tripGenRate * distribution;

        }else{
            tripGen = 0.0f;
        }
        productionSum += tripGen;
        return tripGen;
    }

    @Override
    public void scaleProductions() {
        float householdSum = 0.0f;
        for (MopedZone zone : dataSet.getZones().values()){
            householdSum += zone.getTotalHH();
        }
        System.out.println(householdSum);
        System.out.println(productionSum);
        float householdGrowth = householdSum / HOUSEHOLD_PARAM;
        float schoolTarget = householdGrowth * 392005;
        float calibrationParameter = schoolTarget / productionSum;
        production = production.muli(calibrationParameter);
    }
}
