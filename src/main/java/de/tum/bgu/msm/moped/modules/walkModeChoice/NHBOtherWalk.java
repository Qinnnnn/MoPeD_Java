package de.tum.bgu.msm.moped.modules.walkModeChoice;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.Purpose;

public final class NHBOtherWalk extends WalkTripGenerator{

    public NHBOtherWalk(DataSet dataSet) {
        super(dataSet, Purpose.NHBNW);
    }

    @Override
    protected float calculateZoneRelatedUtility(float pie, int pieFlag, int wa, float stfwy, float pieEmpl, float piePop) {
        float utilityZone;
        float pieActivity = pieEmpl + piePop;
        if (pieActivity<=1.0f){
            utilityZone = (float) (0.686378 *Math.log(pieActivity+1));
        }else{
            utilityZone = (float) (0.686378 *Math.log(pieActivity));
        }
        return utilityZone;
    }

    @Override
    protected float calculateHouseholdRelatedUtility(int hhSize, int worker, int income, int age, int car, int kid) {
        float utilityHousehold = (float) (-7.411403 + 1.375416*(car==0?1:0) + -0.897573*(car==2?1:0) + -0.962847*(car==3?1:0)+
                -0.162483*(kid==1?1:0) + -0.162483*(kid==2?1:0) + -0.162483*(kid==3?1:0)+ -0.361945*(purpose.equals(Purpose.NHBNW)?1:0)+
                -0.205052*(income==2?1:0) + 0.221567*(income==3?1:0) + 0.448212*(income==4?1:0));
        return utilityHousehold;
    }
}
