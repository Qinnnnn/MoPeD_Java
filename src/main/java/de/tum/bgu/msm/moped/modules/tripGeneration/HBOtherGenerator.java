package de.tum.bgu.msm.moped.modules.tripGeneration;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.HouseholdType;
import de.tum.bgu.msm.moped.data.Purpose;

public final class HBOtherGenerator extends TripGenerator{

    public HBOtherGenerator(DataSet dataSet) {
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
                    tripGenRate = 1.187586f;//0.89368165f;
                    break;
                case 2:
                    tripGenRate = 2.076545f;//1.62810500f;
                    break;
                case 3:
                    tripGenRate = 2.613932f;//2.22561020f;
                    break;
                case 4:
                    tripGenRate = 4.027823f;//3.48763360f;
                    break;
            }
        }else if (workers == hhSize){
            switch (hhSize){
                case 1:
                    tripGenRate = 0.6723447f;//0.54391065f;
                    break;
                case 2:
                    tripGenRate = 1.421209f;//1.24163040f;
                    break;
                case 3:
                    tripGenRate = 1.916667f;//1.44898570f;
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
