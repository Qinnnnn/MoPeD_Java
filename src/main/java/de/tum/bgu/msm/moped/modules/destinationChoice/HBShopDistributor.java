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
        double freewayCoef = 0.0;
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
            double barrierVariable = slopeCoef*superPAZ.getSlope() + freewayCoef*superPAZ.getFreeway() + industrialPropCoef*industrialProp;
            float utility = (float) (sizeRETCoef * Math.log(sizeRET) + supportVariable + barrierVariable);
//            if (Double.isInfinite(utility) || Double.isNaN(utility)) {
//                throw new RuntimeException(utility + " utility calculated! Please check calculation!" +
//                        " sizeRET: " + sizeRET + " | Park: " + superPAZ.getPark() + " | slope: "
//                        + superPAZ.getSlope() + " | freeway: " + superPAZ.getFreeway() +
//                        " | industrial: " + industrialProp);
//            }
            destinationUtility.put(superPAZ.getIndex(),utility);
        }
    }



//    //WSTLUR paper
//    @Override
//    protected void calculateDestinationUtility() {
//        double sizeRETCoef = 0.926527;
//        double slopeCoef = -0.360128;
//        double freewayCoef = -0.364433;
//        double industrialPropCoef = -1.382613;
//        double parkCoef = 0.0;
//
//        for (SuperPAZ superPAZ: dataSet.getDestinationSuperPAZs().values()){
//            double industrialProp = superPAZ.getIndustrial() / superPAZ.getTotalEmpl();
//            double sizeRET =  superPAZ.getRetail();
//            if(sizeRET <= 1){
//                sizeRET = sizeRET+1;
//            }
//
//            double supportVariable = parkCoef*superPAZ.getPark();
//            double barrierVariable = slopeCoef*superPAZ.getSlope() + freewayCoef*superPAZ.getFreeway() + industrialPropCoef*industrialProp;
//            double utility =  sizeRETCoef * Math.log(sizeRET) + supportVariable + barrierVariable;
//            destinationUtility.put(superPAZ.getIndex(),utility);
//        }
//    }

//    @Override
//    protected void calculateDestinationUtility() {
//        double size = 0.910195f;
//        double empRET = 5.45389f;
//        double empAllOth = 0.0f;
//        double pie = -0.013919f; //to be checked
//        double slope = -0.194741f;
//        double freeway = -1.02347f;
//        double empAllOthPropotion = -1.74298f;
//
//        for (SuperPAZ superPAZ: dataSet.getDestinationSuperPAZs().values()){
//            double employment = superPAZ.getRetail();
//            double sizeVariable = (Math.exp(empRET)* employment + Math.exp(empAllOth)*((superPAZ.getTotalEmpl() - employment)));
//            double empOtherPropotion;
//            if (superPAZ.getTotalEmpl() == 0.0){
//                empOtherPropotion = 0.0f;
//            }else {
//                empOtherPropotion = (superPAZ.getTotalEmpl() - employment) / superPAZ.getTotalEmpl();
//            }
//            double barrierVariable = pie * superPAZ.getPie() + slope*superPAZ.getSlope() + freeway*superPAZ.getFreeway() + empAllOthPropotion*empOtherPropotion;
//            double utility = (size * Math.log(sizeVariable) + barrierVariable);
//            destinationUtility.put(superPAZ.getIndex(),utility);
//        }
//    }

    //TODO:Method 1 Gravity model
//    protected void calculateDestinationUtilityPAZ() {
//        for (MopedZone mopedZone: dataSet.getZones().values()){
//            double sizeRET =  mopedZone.getRetail();
//            float utility = (float) Math.log(sizeRET);
//            if (Double.isInfinite(utility) || Double.isNaN(utility)) {
//                throw new RuntimeException(utility + " utility calculated! Please check calculation!" +
//                        " sizeRET: " + sizeRET);
//            }
//            destinationUtilityPAZ.put(mopedZone.getZoneId(),utility);
//        }
//    }

    //TODO:Method 2 reapply superPAZ model to PAZ
//    @Override
//    protected void calculateDestinationUtilityPAZ() {
//        double sizeRETCoef =  0.927;
//        double slopeCoef = -0.36;
//        double freewayCoef = -0.364;
//        double industrialPropCoef = -1.383;
//        double parkCoef = 0.0;
//
//        for (MopedZone mopedZone: dataSet.getZones().values()){
//            double industrialProp = mopedZone.getIndustrial() / mopedZone.getTotalEmpl();
//
//            if(mopedZone.getTotalEmpl()==0){
//                industrialProp = 0.;
//            }
//
//            double sizeRET =  mopedZone.getRetail();
//            if(sizeRET <= 1){
//                sizeRET = sizeRET+1;
//            }
//
//            double supportVariable = parkCoef*mopedZone.getPark();
//            double barrierVariable = slopeCoef*mopedZone.getSlope() + freewayCoef*mopedZone.getFreeway() + industrialPropCoef*industrialProp;
//            float utility = (float) (sizeRETCoef * Math.log(sizeRET) + supportVariable + barrierVariable);
//            if (Double.isInfinite(utility) || Double.isNaN(utility)) {
//                throw new RuntimeException(utility + " utility calculated! Please check calculation!" +
//                        " sizeRET: " + sizeRET + " | Park: " + mopedZone.getPark() + " | slope: "
//                        + mopedZone.getSlope() + " | freeway: " + mopedZone.getFreeway() +
//                        " | industrial: " + industrialProp);
//            }
//            destinationUtilityPAZ.put(mopedZone.getZoneId(),utility);
//        }
//    }

    //TODO:Method 3 apply PAZ allocation model
    @Override
    protected void calculateDestinationUtilityPAZ() {
        double sizeRETCoef =  0.767;
        double sizeSERCoef =  0.064;
        double sizeFINCoef =  0.148;
        double sizeGOVCoef =  0.098;
        double householdCoef = -0.185;
        double parkCoef = -0.703;

        for (MopedZone mopedZone: dataSet.getZones().values()){
            double sizeRET =  Math.max(1,mopedZone.getRetail());
            double sizeSER =  Math.max(1,mopedZone.getService());
            double sizeFIN =  Math.max(1,mopedZone.getFinancial());
            double sizeGOV =  Math.max(1,mopedZone.getGovernment());
            double sizeHH =  Math.max(1,mopedZone.getTotalHH());


            float utility = (float) (sizeRETCoef * Math.log(sizeRET)+sizeSERCoef * Math.log(sizeSER)+
                    sizeFINCoef * Math.log(sizeFIN)+sizeGOVCoef * Math.log(sizeGOV) +
                    householdCoef * Math.log(sizeHH)+parkCoef * mopedZone.getParkArce());
            if (Double.isInfinite(utility) || Double.isNaN(utility)) {
                throw new RuntimeException(utility + " utility calculated! Please check calculation!" +
                        " sizeRET: " + sizeRET + " sizeSER: " + sizeSER + " sizeFIN: " + sizeFIN +
                        " sizeGOV: " + sizeGOV + " sizeHH: " + sizeHH + " parkAcre: " + mopedZone.getParkArce());
            }
            destinationUtilityPAZ.put(mopedZone.getZoneId(),utility);
        }
    }
}
