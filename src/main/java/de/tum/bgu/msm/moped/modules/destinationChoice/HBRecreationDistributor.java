package de.tum.bgu.msm.moped.modules.destinationChoice;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.data.SuperPAZ;

public final class HBRecreationDistributor extends TripDistributor{

    public HBRecreationDistributor(DataSet dataSet) {
        super(dataSet, Purpose.HBREC);
    }

    //WSTLUR paper
    @Override
    protected void calculateDestinationUtility() {
        double sizeRETSERCoef = 0.133;
        double sizeHHCoef = 0.054;
        double slopeCoef = -0.139;
        double freewayCoef = 0.0;
        double industrialPropCoef = 0.0;
        double parkCoef = 0.662;
        double networkDensityCoef = 0.0;

        for (SuperPAZ superPAZ: dataSet.getSuperPAZs().values()){
            double industrialProp = superPAZ.getIndustrial() / superPAZ.getTotalEmpl();
            double sizeRETSER =  superPAZ.getRetail()+superPAZ.getService();
            double sizeHH = superPAZ.getHousehold();
            if(sizeRETSER <= 1){
                sizeRETSER = sizeRETSER+1;
            }

            if(sizeHH <= 1){
                sizeHH = sizeHH+1;
            }

            double supportVariable = parkCoef*superPAZ.getPark()+networkDensityCoef*superPAZ.getNetworkDesnity();
            double barrierVariable = slopeCoef*superPAZ.getSlope() + freewayCoef*superPAZ.getFreeway() + industrialPropCoef*industrialProp;
            double utility =  sizeRETSERCoef * Math.log(sizeRETSER) + sizeHHCoef * Math.log(sizeHH) + supportVariable + barrierVariable;
            destinationUtility.put(superPAZ.getIndex(), (float) utility);
        }
    }

    @Override
    protected void calculateDestinationUtilityPAZ() {
    }

//    @Override
//    protected void calculateDestinationUtility() {
//        double size = 0.0517537f;
//        double park = 0.460169f;
//        double empRET = 6.50648f;
//        double empGOV = 17.1087f;
//        double household = -3.16331f;
//        double pie = 0.0110469f;
//        double slope = -0.0529455f;
//        double freeway = -0.16851f;
//        double empAllOthPropotion = -0.0898361f;
//
//        for (SuperPAZ superPAZ: dataSet.getDestinationSuperPAZs().values()){
//            double employmentRET = superPAZ.getRetail();
//            double employmentGOV = superPAZ.getGovernment();
//            double empOtherPropotion;
//            if (superPAZ.getTotalEmpl() == 0.0){
//                empOtherPropotion = 0.0f;
//            }else {
//                empOtherPropotion = (superPAZ.getTotalEmpl() - employmentRET-employmentGOV)/superPAZ.getTotalEmpl();
//            }
//            double sizeVariable = (Math.exp(empRET)* employmentRET + Math.exp(empGOV)* employmentGOV + Math.exp(household)* superPAZ.getHousehold());
//            double supportVariable = pie * superPAZ.getPie() + park * superPAZ.getPark();
//            double barrierVariable = slope*superPAZ.getSlope() + freeway*superPAZ.getFreeway() + empAllOthPropotion*empOtherPropotion;
//            double utility = (size * Math.log(sizeVariable) + supportVariable + barrierVariable);
//            destinationUtility.put(superPAZ.getIndex(),utility);
//        }
//    }
}
