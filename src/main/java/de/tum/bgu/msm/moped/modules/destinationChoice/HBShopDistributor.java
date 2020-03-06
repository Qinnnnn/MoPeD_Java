package de.tum.bgu.msm.moped.modules.destinationChoice;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.MopedZone;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.data.SuperPAZ;

import java.util.HashMap;
import java.util.Map;

public final class HBShopDistributor extends TripDistributor{

    private Map<Integer, Double> destinationUtilityPAZ = new HashMap<>();
    public HBShopDistributor(DataSet dataSet) {
        super(dataSet, Purpose.HBSHOP);
    }

    //WSTLUR paper - advance
    @Override
    protected void calculateDestinationUtility() {
        double sizeRETCoef =  1.095920;
        double slopeCoef = -0.375781;
        double freewayCoef = -0.347886;
        double industrialPropCoef = -1.398291;
        double parkCoef = 0.0;

        for (SuperPAZ superPAZ: dataSet.getDestinationSuperPAZs().values()){
            double industrialProp = superPAZ.getIndustrial() / superPAZ.getTotalEmpl();
            double sizeRET =  superPAZ.getRetail();
            if(sizeRET <= 1){
                sizeRET = sizeRET+1;
            }

            double supportVariable = parkCoef*superPAZ.getPark();
            double barrierVariable = slopeCoef*superPAZ.getSlope() + freewayCoef*superPAZ.getFreeway() + industrialPropCoef*industrialProp;
            double utility =  sizeRETCoef * Math.log(sizeRET) + supportVariable + barrierVariable;
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

    @Override
    protected Map<Integer, Double> calculateDestinationUtilityPAZ(SuperPAZ superPAZ) {
        double size = 0.910195f;
        double empRET = 5.45389f;
        double empAllOth = 0.0f;
        double pie = -0.013919f; //to be checked
        double slope = -0.194741f;
        double freeway = -1.02347f;
        double empAllOthPropotion = -1.74298f;

        for (MopedZone mopedZone: superPAZ.getPazs().values()){
            double employment = mopedZone.getRetail();
            double sizeVariable = (Math.exp(empRET)* employment + Math.exp(empAllOth)*((mopedZone.getTotalEmpl() - employment)));
            double empOtherPropotion;
            if (mopedZone.getTotalEmpl() == 0.0){
                empOtherPropotion = 0.0f;
            }else {
                empOtherPropotion = (mopedZone.getTotalEmpl() - employment) / mopedZone.getTotalEmpl();
            }
            double barrierVariable = pie * mopedZone.getPie() + slope*mopedZone.getSlope() + freeway*mopedZone.getFreeway() + empAllOthPropotion*empOtherPropotion;
            double utility = (size * Math.log(sizeVariable) + barrierVariable);
            destinationUtilityPAZ.put(mopedZone.getZoneId(),utility);
        }
        return destinationUtilityPAZ;
    }
}
