package de.tum.bgu.msm.moped.modules.walkModeChoice;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.Purpose;

import java.util.Map;

public final class HBWorkWalk extends WalkTripGenerator{

    public HBWorkWalk(DataSet dataSet) {
        super(dataSet, Purpose.HBW);
    }

    //Old mode choice --> max 38.5% avg 0.0385
    //New mode choice model - no pie, but pedestrian accessibility - log(empl+pop)
    @Override
    protected float calculateZoneRelatedUtility(float pie, int pieFlag, int wa, float stfwy, float pieEmpl, float piePop) {
        float utilityZone;
        float pieActivity = pieEmpl + piePop;
        if (pieActivity<=1.0f){
            utilityZone = (float) (0.754201 *Math.log(pieActivity+1));
        }else{
            utilityZone = (float) (0.754201 *Math.log(pieActivity));
        }
        return utilityZone;
    }

    @Override
    protected float calculateHouseholdRelatedUtility(int hhSize, int worker, int income, int age, int car, int kid) {
        float utilityHousehold = (float) (-8.391881 + 1.000765*(car==0?1:0) + -0.226096*(car==2?1:0) + -0.39406*(car==3?1:0)+
                -0.553804*(kid==0?1:0) + -0.573976*(kid==2?1:0) + -0.71849*(kid==3?1:0)+
                1.028633*(purpose.equals(Purpose.HBSHOP)?1:0) + 1.045939*(purpose.equals(Purpose.HBOTH)?1:0) + 1.565908*(purpose.equals(Purpose.HBREC)?1:0));
        return utilityHousehold;
    }
}
