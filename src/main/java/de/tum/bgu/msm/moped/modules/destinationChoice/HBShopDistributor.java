package de.tum.bgu.msm.moped.modules.destinationChoice;

import cern.colt.map.tdouble.OpenIntDoubleHashMap;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.MopedZone;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.data.SuperPAZ;

import java.util.HashMap;
import java.util.Map;

public final class HBShopDistributor extends TripDistributor{

    public HBShopDistributor(DataSet dataSet) {
        super(dataSet, Purpose.HBSHOP);
    }

    //WSTLUR paper - advance
    @Override
    protected void calculateDestinationUtility() {
        double sizeRETCoef =  0.977;
        double slopeCoef = -0.386;
        double industrialPropCoef = -1.306;
        double parkCoef = 0.0;
        double networkDensityCoef = 0.049;

        for (SuperPAZ superPAZ: dataSet.getSuperPAZs().values()){
            double industrialProp = superPAZ.getIndustrial() / superPAZ.getTotalEmpl();

            if(superPAZ.getTotalEmpl()==0){
                industrialProp = 0.;
            }

            double sizeRET =  superPAZ.getRetail();
            if(sizeRET <= 1){
                sizeRET = sizeRET+1;
            }

            double supportVariable = parkCoef*superPAZ.getPark()+networkDensityCoef*superPAZ.getNetworkDesnity();
            double barrierVariable = slopeCoef*superPAZ.getSlope() + industrialPropCoef*industrialProp;
            float utility = (float) (sizeRETCoef * Math.log(sizeRET) + supportVariable + barrierVariable);
            if (Double.isInfinite(utility) || Double.isNaN(utility)) {
                throw new RuntimeException(utility + " utility calculated! Please check calculation!" +
                        " sizeRET: " + sizeRET + " | Park: " + superPAZ.getPark() + " | slope: "
                        + superPAZ.getSlope() + " | networkDensity: " + superPAZ.getNetworkDesnity() +
                        " | industrial: " + industrialProp);
            }
            destinationUtility.put(superPAZ.getIndex(),utility);
        }
    }

    @Override
    protected void calculateDestinationUtilityPAZ() {
        double sizeRETCoef =  0.820;
        double sizeOtherCoef =  0.188;
        double householdCoef = -0.169;
        double industrialPropCoef = 0.;
        double parkCoef = -0.651;

        for (MopedZone mopedZone: dataSet.getZones().values()){
            double sizeRET = Math.max(1,mopedZone.getRetail());
            double sizeOther = Math.max(1,mopedZone.getService()+mopedZone.getFinancial()+mopedZone.getGovernment());
            double sizeHH =  Math.max(1,mopedZone.getTotalHH());
            double industrialProp = mopedZone.getIndustrial() / mopedZone.getTotalEmpl();

            if(mopedZone.getTotalEmpl()==0){
                industrialProp = 0.;
            }

            float utility = (float) (sizeRETCoef * Math.log(sizeRET) +
                    sizeOtherCoef * Math.log(sizeOther) +
                    householdCoef * Math.log(sizeHH) + industrialPropCoef * industrialProp +
                    parkCoef * mopedZone.getParkArce());
            if (Double.isInfinite(utility) || Double.isNaN(utility)) {
                throw new RuntimeException(utility + " utility calculated! Please check calculation!" +
                        " sizeOther: " + sizeOther + " sizeHH: " + sizeHH +
                        " industrial: " + industrialProp +
                        " parkAcre: " + mopedZone.getParkArce());
            }
            destinationUtilityPAZ.put(mopedZone.getZoneId(),utility);
        }
    }
}
