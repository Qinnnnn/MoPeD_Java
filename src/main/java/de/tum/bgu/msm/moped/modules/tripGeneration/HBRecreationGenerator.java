package de.tum.bgu.msm.moped.modules.tripGeneration;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.HouseholdType;
import de.tum.bgu.msm.moped.data.Purpose;

public final class HBRecreationGenerator extends TripGenerator{


    public HBRecreationGenerator(DataSet dataSet) {
        super(dataSet, Purpose.HBO);
    }


    @Override
    protected float calculateProduction(float distribution, HouseholdType hhType) {
        int workers = hhType.getWorkers();
        int hhSize = hhType.getHouseholdSize();
        float tripGenRate = 0.0f;
        if (workers < hhSize){
            switch (hhSize){
                case 1:
                    tripGenRate = 0.2772414f;//0.47897259f;
                    break;
                case 2:
                    tripGenRate = 0.5582865f;//0.88111840f;
                    break;
                case 3:
                    tripGenRate = 0.7933884f;//1.21373370f;
                    break;
                case 4:
                    tripGenRate = 1.43126f;//2.24007530f;
                    break;
            }
        }else if (workers == hhSize){
            switch (hhSize){
                case 1:
                    tripGenRate = 0.1783567f;//0.50317472f;
                    break;
                case 2:
                    tripGenRate = 0.4122894f;//0.57970395f;
                    break;
                case 3:
                    tripGenRate = 0.5462963f;//1.16564740f;
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

        return tripGen;
    }

    @Override
    protected void scaleProductions() {
        return;
    }

}
