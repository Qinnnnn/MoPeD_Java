package de.tum.bgu.msm.moped.modules.tripGeneration;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.HouseholdType;
import de.tum.bgu.msm.moped.data.Purpose;

public class HBShopGenerator extends TripGenerator{

    public HBShopGenerator(DataSet dataSet) {
        super(dataSet, Purpose.HBSHOP);
    }

    @Override
    protected float calculateProduction(float distribution, HouseholdType hhType) {
        int workers = hhType.getWorkers();
        int hhSize = hhType.getHouseholdSize();
        float tripGenRate = 0.0f;
        if (workers == 0){
            switch (hhSize){
                case 1:
                    tripGenRate = 0.65370595f;
                    break;
                case 2:
                    tripGenRate = 1.47478580f;
                    break;
                case 3:
                    tripGenRate = 1.43978190f;
                    break;
                case 4:
                    tripGenRate = 1.79258760f;
                    break;
            }
        }else if (workers == 1){
            switch (hhSize){
                case 1:
                    tripGenRate = 0.36543758f;
                    break;
                case 2:
                    tripGenRate = 0.96459839f;
                    break;
                case 3:
                    tripGenRate = 1.16952820f;
                    break;
                case 4:
                    tripGenRate = 1.80668250f;
                    break;
            }
        }else if (workers == 2){
            switch (hhSize){
                case 2:
                    tripGenRate = 0.66841305f;
                    break;
                case 3:
                    tripGenRate = 0.93650663f;
                    break;
                case 4:
                    tripGenRate = 1.51069650f;
                    break;
            }
        }else if (workers == 3){
            switch (hhSize){
                case 3:
                    tripGenRate = 1.00633950f;
                    break;
                case 4:
                    tripGenRate = 1.23472770f;
                    break;
            }
        }

        float tripGen;

        if (tripGenRate != 0){
            tripGen = tripGenRate * distribution * 1.2f;
        }else{
            tripGen = 0.0f;
        }
        return tripGen;
    }

    @Override
    protected void scaleProductions() {
        return;
    }
}
