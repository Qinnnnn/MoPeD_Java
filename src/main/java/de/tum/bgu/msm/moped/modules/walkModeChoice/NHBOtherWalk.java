package de.tum.bgu.msm.moped.modules.walkModeChoice;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.Purpose;

public final class NHBOtherWalk extends WalkTripGenerator{

    public NHBOtherWalk(DataSet dataSet) {
        super(dataSet, Purpose.NHBNW);
    }

//    @Override
//    protected float calculateZoneRelatedUtility(float pie, int pieFlag, int wa, float stfwy) {
//        float utilityZone = -1.093f * stfwy + 0.792f * wa+ 0.043f * pie + 0.530f * pieFlag;
//        return utilityZone;
//    }
//
//    @Override
//    protected float calculateZoneRelatedUtility(float pie, int pieFlag, int wa, float stfwy, float pieEmpl, float pieArea) {
//        return 0;
//    }
//
//    @Override
//    protected float calculateHouseholdRelatedUtility(int hhSize, int worker, int income, int age, int car, int kid) {
//        float utilityHousehold = -4.377f + 0.191f*(hhSize==2?1:0) + -0.242f*(age==3?1:0)+ 0.208f*(worker==1?1:0)+ 0.301f*(worker==2?1:0) +
//                1.089f*(car==0?1:0) + -0.463f*(car==2?1:0) + -0.690f*(car==3?1:0)+ 0.295f*(kid==1?1:0) + 0.455f*(kid==2?1:0) + 0.479f*(kid==3?1:0);
//        return utilityHousehold;
//    }

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

    @Override
    protected float calculateZoneRelatedUtility(float pie, int pieFlag, int wa, float stfwy) {
        return 0;
    }

}
