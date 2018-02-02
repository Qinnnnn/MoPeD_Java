package de.tum.bgu.msm.moped.modules.walkModeChoice;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.Purpose;

public final class HBWorkWalk extends WalkTripGenerator{

    public HBWorkWalk(DataSet dataSet) {
        super(dataSet, Purpose.HBW);
    }

    @Override
    protected float calculateZoneRelatedUtility(float pie, int pieFlag, int wa, float stfwy) {
        float utilityZone = 0.036f * pie + 1.240f * pieFlag;
        return utilityZone;
    }

    @Override
    protected float calculateHouseholdRelatedUtility(int hhSize, int worker, int income, int age, int car, int kid) {
        float utilityHousehold = -5.033f + 0.719f*(hhSize==3?1:0) + -0.794f*(income==2?1:0) + 0.957f*(age==1?1:0) + 0.343f*(age==3?1:0) +
                1.597f*(car==0?1:0) + -0.834f*(car==2?1:0) + -1.178f*(car==3?1:0) + 0.752f*(kid==2?1:0) + 1.121f*(kid==3?1:0);
        return utilityHousehold;
    }
}
