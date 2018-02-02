package de.tum.bgu.msm.moped.modules.tripGeneration;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.HouseholdType;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.data.Zone;

public class HBCollegeGenerator extends TripGenerator {

    private float productionSum = 0.0f;

    public HBCollegeGenerator(DataSet dataSet) {
        super(dataSet, Purpose.HBCOLL);
    }

    @Override
    protected float calculateProduction(float distribution, HouseholdType hhType) {
        int age = hhType.getAge();
        int hhSize = hhType.getHouseholdSize();
        float tripGenRate = 0.0f;
        if (hhSize == 1){
            switch (age){
                case 1:
                    tripGenRate = 0.52380952f;
                    break;
                case 2:
                    tripGenRate = 0.05958549f;
                    break;
                case 3:
                    tripGenRate = 0.02985075f;
                    break;
                case 4:
                    tripGenRate = 0.01823708f;
                    break;
            }
        }else if (hhSize == 2){
            switch (age){
                case 1:
                    tripGenRate = 0.48387097f;
                    break;
                case 2:
                    tripGenRate = 0.17915691f;
                    break;
                case 3:
                    tripGenRate = 0.03581267f;
                    break;
                case 4:
                    tripGenRate = 0.03353057f;
                    break;
            }
        } else if (hhSize == 3){
            switch (age){
                case 1:
                    tripGenRate = 1.00000000f;
                    break;
                case 2:
                    tripGenRate = 0.23421927f;
                    break;
                case 3:
                    tripGenRate = 0.33980583f;
                    break;
                case 4:
                    tripGenRate = 0.06557377f;
                    break;
            }
        } else if (hhSize == 4) {
            switch (age){
                case 1:
                    tripGenRate = 0.45833333f;
                    break;
                case 2:
                    tripGenRate = 0.38158996f;
                    break;
                case 3:
                    tripGenRate = 0.58510638f;
                    break;
                case 4:
                    tripGenRate = 0.00000000f;
                    break;
            }
        }

        float tripGen;

        if (tripGenRate != 0){
            tripGen = tripGenRate * distribution * 1.074f;

        }else{
            tripGen = 0.0f;
        }
        productionSum += tripGen;
        return tripGen;
    }

    @Override
    protected void scaleProductions() {
        float collegeTripSum = 0.0f;
        for (Zone zone : dataSet.getZones().values()){
            collegeTripSum += zone.getCollegeVehicleTrip();
        }
        float balanceParameter = productionSum/collegeTripSum;
        production = production.muli(balanceParameter);
    }
}
