package de.tum.bgu.msm.moped.modules.tripGeneration;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.HouseholdType;
import de.tum.bgu.msm.moped.data.Purpose;

public final class HBRecreationGenerator extends TripGenerator{


    public HBRecreationGenerator(DataSet dataSet) {
        super(dataSet, Purpose.HBREC);
    }


    @Override
    protected float calculateProduction(float distribution, HouseholdType hhType) {
        int workers = hhType.getWorkers();
        int hhSize = hhType.getHouseholdSize();
        float tripGenRate = 0.0f;
        if (workers < hhSize){
            switch (hhSize){
                case 1:
                    tripGenRate = 0.47897259f;
                    break;
                case 2:
                    tripGenRate = 0.88111840f;
                    break;
                case 3:
                    tripGenRate = 1.21373370f;
                    break;
                case 4:
                    tripGenRate = 2.24007530f;
                    break;
            }
        }else if (workers == hhSize){
            switch (hhSize){
                case 1:
                    tripGenRate = 0.50317472f;
                    break;
                case 2:
                    tripGenRate = 0.57970395f;
                    break;
                case 3:
                    tripGenRate = 1.16564740f;
                    break;
            }
        } else{
            //logger.warn("Number of workers is bigger than householdsize");
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
