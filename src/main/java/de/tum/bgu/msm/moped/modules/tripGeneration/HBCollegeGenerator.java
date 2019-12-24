package de.tum.bgu.msm.moped.modules.tripGeneration;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.HouseholdType;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.data.MopedZone;

public class HBCollegeGenerator extends TripGenerator {

    private float productionSum = 0.0f;

    public HBCollegeGenerator(DataSet dataSet) {
        super(dataSet, Purpose.HBE);
    }

    @Override
    protected float calculateProduction(float distribution, HouseholdType hhType) {
        int age = hhType.getAge();
        int hhSize = hhType.getHouseholdSize();
        float tripGenRate = 0.0f;
        if (hhSize == 1){
            switch (age){
                case 1:
                    tripGenRate = 0.5384615f;//0.52380952f;
                    break;
                case 2:
                    tripGenRate = 0.0473684f;//0.05958549f;
                    break;
                case 3:
                    tripGenRate = 0.0059761f;//0.02985075f;
                    break;
                case 4:
                    tripGenRate = 0.007837f;//0.01823708f;
                    break;
            }
        }else if (hhSize == 2){
            switch (age){
                case 1:
                    tripGenRate = 0.375f;//0.48387097f;
                    break;
                case 2:
                    tripGenRate = 0.1138107f;//0.17915691f;
                    break;
                case 3:
                    tripGenRate = 0.0289079f;//0.03581267f;
                    break;
                case 4:
                    tripGenRate = 0.0183357f;//0.03353057f;
                    break;
            }
        } else if (hhSize == 3){
            switch (age){
                case 1:
                    tripGenRate = 0.6666667f;//1.00000000f;
                    break;
                case 2:
                    tripGenRate = 0.1226576f;//0.23421927f;
                    break;
                case 3:
                    tripGenRate = 0.1610487f;//0.33980583f;
                    break;
                case 4:
                    tripGenRate = 0.1413043f;//0.06557377f;
                    break;
            }
        } else if (hhSize == 4) {
            switch (age){
                case 1:
                    tripGenRate = 0.8333333f;//0.45833333f;
                    break;
                case 2:
                    tripGenRate = 0.1359852f;//0.38158996f;
                    break;
                case 3:
                    tripGenRate = 0.468254f;//0.58510638f;
                    break;
                case 4:
                    tripGenRate = 0.2758621f;//0.00000000f;
                    break;
            }
        }

        float tripGen;

        if (tripGenRate != 0){
            tripGen = tripGenRate * distribution * 1.5f;//1.074f;

        }else{
            tripGen = 0.0f;
        }
        productionSum += tripGen;
        return tripGen;
    }

    @Override
    protected void scaleProductions() {
        float collegeTripSum = 0.0f;
        for (MopedZone zone : dataSet.getZones().values()){
            collegeTripSum += zone.getCollegeVehicleTrip();
        }

        System.out.println(collegeTripSum);
        System.out.println(productionSum);
        float balanceParameter = productionSum/collegeTripSum;
        production = production.muli(balanceParameter);
    }
}
