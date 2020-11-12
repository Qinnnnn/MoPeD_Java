package de.tum.bgu.msm.moped.modules.destinationChoice;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.data.SuperPAZ;

public final class NHBOtherDistributor extends TripDistributor {

    public NHBOtherDistributor(DataSet dataSet) {
        super(dataSet, Purpose.NHBNW);
    }

    //WSTLUR paper
    @Override
    protected void calculateDestinationUtility() {
        double sizeOTHERCoef = 0.516;
        double sizeHHCoef = 0.0;
        double slopeCoef = -0.060;
        double freewayCoef = 0.0;
        double industrialPropCoef = 0.0;
        double parkCoef = 0.0;
        double networkDensityCoef = 0.184;

        for (SuperPAZ superPAZ: dataSet.getSuperPAZs().values()){
            double industrialProp = superPAZ.getIndustrial() / superPAZ.getTotalEmpl();
            double sizeOTHER =  superPAZ.getTotalEmpl()-superPAZ.getIndustrial();
            double sizeHH = superPAZ.getHousehold();
            if(sizeOTHER <= 1){
                sizeOTHER = sizeOTHER+1;
            }

            if(sizeHH <= 1){
                sizeHH = sizeHH+1;
            }

            double supportVariable = parkCoef*superPAZ.getPark()+networkDensityCoef*superPAZ.getNetworkDesnity();
            double barrierVariable = slopeCoef*superPAZ.getSlope() + freewayCoef*superPAZ.getFreeway() + industrialPropCoef*industrialProp;
            double utility =  sizeOTHERCoef * Math.log(sizeOTHER) + sizeHHCoef * Math.log(sizeHH) + supportVariable + barrierVariable;
            destinationUtility.put(superPAZ.getIndex(), (float) utility);
        }
    }

    @Override
    protected void calculateDestinationUtilityPAZ() {
    }

//    @Override
//    protected void calculateDestinationUtility() {
//        double size = 0.399613f;
//        double park = 0.115274f;
//        double empRETGOV = 3.82922f;
//        double household = -1.96896f;
//        double pie = 0.0247138f;
//        double slope = -0.426383f;
//        double freeway = 0.10023f;
//        double empAllOthPropotion = -0.398784f;
//
//        for (SuperPAZ superPAZ: dataSet.getDestinationSuperPAZs().values()){
//            double employmentRETGOV = superPAZ.getRetail() + superPAZ.getGovernment();
//            double empOtherPropotion;
//            if (superPAZ.getTotalEmpl() == 0.0){
//                empOtherPropotion = 0.0f;
//            }else {
//                empOtherPropotion = (superPAZ.getTotalEmpl() - employmentRETGOV)/superPAZ.getTotalEmpl();
//            }
//            double sizeVariable =  (Math.exp(empRETGOV)* employmentRETGOV + Math.exp(household)* superPAZ.getHousehold());
//            double supportVariable = pie * superPAZ.getPie() + park * superPAZ.getPark();
//            double barrierVariable = slope*superPAZ.getSlope() + freeway*superPAZ.getFreeway() + empAllOthPropotion*empOtherPropotion;
//            double utility =  (size * Math.log(sizeVariable) + supportVariable + barrierVariable);
//            destinationUtility.put(superPAZ.getIndex(),utility);
//        }
//    }

}
