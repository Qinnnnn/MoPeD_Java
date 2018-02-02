package de.tum.bgu.msm.moped.modules.walkModeChoice;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.HouseholdType;
import de.tum.bgu.msm.moped.data.Purpose;
import org.apache.log4j.Logger;

import java.util.Collection;

public final class HBShopWalk extends WalkTripGenerator{


    public HBShopWalk(DataSet dataSet) {
        super(dataSet, Purpose.HBSHOP);
    }


    @Override
    protected float calculateZoneRelatedUtility(float pie, int pieFlag, int wa, float stfwy) {
        float utilityZone = -1.093f * stfwy + 0.792f * wa+ 0.043f * pie + 0.530f * pieFlag;
        return utilityZone;
    }

    @Override
    protected float calculateHouseholdRelatedUtility(int hhSize, int worker, int income, int age, int car, int kid) {
        float utilityHousehold = -0.145f + -4.377f + 0.191f*(hhSize==2?1:0) + -0.242f*(age==3?1:0)+ 0.208f*(worker==1?1:0)+ 0.301f*(worker==2?1:0) +
                1.089f*(car==0?1:0) + -0.463f*(car==2?1:0) + -0.690f*(car==3?1:0)+ 0.295f*(kid==1?1:0) + 0.455f*(kid==2?1:0) + 0.479f*(kid==3?1:0);
        return utilityHousehold;
    }
}
