package de.tum.bgu.msm.moped.modules.walkModeChoice;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.Purpose;

import java.util.Map;

public final class HBWorkWalk extends WalkTripGenerator{

    public HBWorkWalk(DataSet dataSet) {
        super(dataSet, Purpose.HBW);
    }

    //Old mode choice --> max 38.5% avg 0.0385
//    @Override
//    protected float calculateZoneRelatedUtility(float pie, int pieFlag, int wa, float stfwy) {
//        float utilityZone = 0.036f * pie + 1.240f * pieFlag;
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
//        float utilityHousehold = -5.033f + 0.719f*(hhSize==3?1:0) + -0.794f*(income==2?1:0) + 0.957f*(age==1?1:0) + 0.343f*(age==3?1:0) +
//                1.597f*(car==0?1:0) + -0.834f*(car==2?1:0) + -1.178f*(car==3?1:0) + 0.752f*(kid==2?1:0) + 1.121f*(kid==3?1:0);
//        return utilityHousehold;
//    }


    //new mode choice_HHVEH+CHILD+BE --> max 70% avg 0.0365
//    @Override
//    protected float calculateZoneRelatedUtility(float pie, int pieFlag, int wa, float stfwy, float pieEmpl, float pieArea) {
//        float utilityZone = (float) (0.000055127 *pieEmpl + 0.023307 * pieArea);
//        return utilityZone;
//    }
//
//    @Override
//    protected float calculateHouseholdRelatedUtility(int hhSize, int worker, int income, int age, int car, int kid) {
//        float utilityHousehold = (float) (-3.3336 + -1.6888*(car==1?1:0) + -1.9227*(car==2?1:0) + -2.1461*(car==3?1:0)+
//                0.59477*(kid==1?1:0) + 0.59477*(kid==2?1:0) + 0.59477*(kid==3?1:0));
//        return utilityHousehold;
//    }

        //new mode choice_HHVEH+CHILD+BElog --> max 54% avg 0.042
//    @Override
//    protected float calculateZoneRelatedUtility(float pie, int pieFlag, int wa, float stfwy, float pieEmpl, float pieArea) {
//        float utilityZone;
//        if (pieEmpl<=1.0f){
//            utilityZone = (float) (0.5541033 *Math.log(pieEmpl+1) + 0.0060035 * pieArea);
//        }else{
//            utilityZone = (float) (0.5541033 *Math.log(pieEmpl) + 0.0060035 * pieArea);
//        }
//        return utilityZone;
//    }
//
//    @Override
//    protected float calculateHouseholdRelatedUtility(int hhSize, int worker, int income, int age, int car, int kid) {
//        float utilityHousehold = (float) (-5.8239359 + -1.4329401*(car==1?1:0) + -1.4109487*(car==2?1:0) + -1.5428592*(car==3?1:0)+
//                0.7431440*(kid==1?1:0) + 0.7431440*(kid==2?1:0) + 0.7431440*(kid==3?1:0));
//        return utilityHousehold;
//    }

    //new mode choice_HHVEH+CHILD+BEsqrt --> max % avg 0.
//    @Override
//    protected float calculateZoneRelatedUtility(float pie, int pieFlag, int wa, float stfwy, float pieEmpl, float pieArea) {
//        float utilityZone = (float) (0.0001234 *Math.sqrt(pieEmpl)*pieArea + 0.018637 * pieArea);
//        return utilityZone;
//    }
//
//    @Override
//    protected float calculateHouseholdRelatedUtility(int hhSize, int worker, int income, int age, int car, int kid) {
//        float utilityHousehold = (float) (-3.3093 + -1.5922*(car==1?1:0) + -1.8074*(car==2?1:0) + -2.0294*(car==3?1:0)+
//                0.63746*(kid==1?1:0) + 0.63746*(kid==2?1:0) + 0.63746*(kid==3?1:0));
//        return utilityHousehold;
//    }
//
//

    @Override
    protected float calculateZoneRelatedUtility(float pie, int pieFlag, int wa, float stfwy, float pieActivity, float pieArea) {
        float utilityZone;
        if (pieActivity<=1.0f){
            utilityZone = (float) (0.84192 *Math.log(pieActivity+1));
        }else{
            utilityZone = (float) (0.84192 *Math.log(pieActivity));
        }
        return utilityZone;
    }

    @Override
    protected float calculateHouseholdRelatedUtility(int hhSize, int worker, int income, int age, int car, int kid) {
        float utilityHousehold = (float) (-8.45531 + -1.51738*(car==1?1:0) + -1.59604*(car==2?1:0) + -1.78421*(car==3?1:0)+
                0.57217*(kid==1?1:0) + 0.57217*(kid==2?1:0) + 0.57217*(kid==3?1:0));
        return utilityHousehold;
    }

//    @Override
//    protected float calculateZoneRelatedUtility(float pie, int pieFlag, int wa, float stfwy, float pieEmpl, float piePop) {
//        float utilityZone = (float) (0.00020869 *piePop+0.000053566*pieEmpl);
//        return utilityZone;
//    }
//
//    @Override
//    protected float calculateHouseholdRelatedUtility(int hhSize, int worker, int income, int age, int car, int kid) {
//        float utilityHousehold = (float) (-2.3143 + -1.5609*(car==1?1:0) + -1.8136*(car==2?1:0) + -2.1448*(car==3?1:0)+
//                0.49229*(kid==1?1:0) + 0.49229*(kid==2?1:0) + 0.49229*(kid==3?1:0));
//        return utilityHousehold;
//    }

    @Override
    protected float calculateZoneRelatedUtility(float pie, int pieFlag, int wa, float stfwy) {
        return 0;
    }
}
