package de.tum.bgu.msm.moped.modules.destinationChoice;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.data.SuperPAZ;

import java.util.Map;

public final class HBWorkDistributor extends TripDistributor {

    public HBWorkDistributor(DataSet dataSet) {
        super(dataSet, Purpose.HBW);
    }

    //WSTLUR paper
    @Override
    protected void calculateDestinationUtility() {
        double sizeRETSERCoef = 0.409906;
        double sizeOTHERCoef = 0.335296;
        double slopeCoef = -0.232709;
        double freewayCoef = 0.0;
        double industrialPropCoef = -0.823084;
        double parkCoef = 0.0;

        for (SuperPAZ superPAZ: dataSet.getDestinationSuperPAZs().values()){
            //double empIndustrial = superPAZ.getTotalEmpl() - superPAZ.getRetail() - superPAZ.getFinancial() - superPAZ.getGovernment() - superPAZ.getService();
            double industrialProp = superPAZ.getIndustrial() / superPAZ.getTotalEmpl();
            double sizeRETSER =  superPAZ.getRetail()+superPAZ.getService();
            double sizeOTHER = superPAZ.getFinancial()+superPAZ.getGovernment()+superPAZ.getTpu()+superPAZ.getWho();
            if(sizeRETSER <= 1){
                sizeRETSER = sizeRETSER+1;
            }

            if(sizeOTHER <= 1){
                sizeOTHER = sizeOTHER+1;
            }

            double supportVariable = parkCoef*superPAZ.getPark();
            double barrierVariable = slopeCoef*superPAZ.getSlope() + freewayCoef*superPAZ.getFreeway() + industrialPropCoef*industrialProp;
            double utility =  sizeRETSERCoef * Math.log(sizeRETSER) + sizeOTHERCoef * Math.log(sizeOTHER) + supportVariable + barrierVariable;
            destinationUtility.put(superPAZ.getIndex(),utility);
        }
    }

    //nonlinear non-transformed
//    @Override
//    protected void calculateDestinationUtility() {
//        double size = 0.195;
//        double empRETFINGOV = 0.22;
//        double pie = 0.0;
//        double slope = 0.0;
//        double freeway = 0.0;
//        double empAllOthPropotion = -1.11;
//
//        for (SuperPAZ superPAZ: dataSet.getDestinationSuperPAZs().values()){
//            double employment = superPAZ.getRetail() + superPAZ.getFinancial() + superPAZ.getGovernment();
//            double empOtherPropotion = (superPAZ.getTotalEmpl() - employment - superPAZ.getService()) / superPAZ.getTotalEmpl();
//            double sizeVariable =  Math.exp(empRETFINGOV)* employment + superPAZ.getTotalEmpl() - employment;
//            double supportVariable = pie * superPAZ.getPie();
//            double barrierVariable = slope*superPAZ.getSlope() + freeway*superPAZ.getFreeway() + empAllOthPropotion*empOtherPropotion;
//            double utility =  (size * Math.log(sizeVariable) + supportVariable + barrierVariable);
//            destinationUtility.put(superPAZ.getIndex(),utility);
//        }
//
//
//    }

//    //linear non-transformed
//    @Override
//    protected void calculateDestinationUtility() {
//        double size = 0.197;
//        double pie = 0.0;
//        double slope = 0.0;
//        double freeway = 0.0;
//        double empAllOthPropotion = -1.14;
//
//        for (SuperPAZ superPAZ: dataSet.getDestinationSuperPAZs().values()){
//            //double empIndustrial = superPAZ.getTotalEmpl() - superPAZ.getRetail() - superPAZ.getFinancial() - superPAZ.getGovernment() - superPAZ.getService();
//            double empOtherPropotion = superPAZ.getIndustrial() / superPAZ.getTotalEmpl();
//            double sizeVariable =  superPAZ.getTotalEmpl();
//            if(sizeVariable <= 1){
//                sizeVariable = sizeVariable+1;
//            }
//            double supportVariable = pie * superPAZ.getPie();
//            double barrierVariable = slope*superPAZ.getSlope() + freeway*superPAZ.getFreeway() + empAllOthPropotion*empOtherPropotion;
//            double utility =  (size * Math.log(sizeVariable) + supportVariable + barrierVariable);
//            destinationUtility.put(superPAZ.getIndex(),utility);
//        }
//    }

//    //    //linear exponential-transformed
//    @Override
//    protected void calculateDestinationUtility() {
//        double size = 0.234;
//        double pie = 0.0;
//        double slope = 0.0;
//        double freeway = 0.0;
//        double empAllOthPropotion = -1.37;
//
//        for (SuperPAZ superPAZ: dataSet.getDestinationSuperPAZs().values()){
//            double empIndustrial = superPAZ.getTotalEmpl() - superPAZ.getRetail() - superPAZ.getFinancial() - superPAZ.getGovernment() - superPAZ.getService();
//            double empOtherPropotion = empIndustrial / superPAZ.getTotalEmpl();
//            double sizeVariable =  superPAZ.getTotalEmpl();
//            double supportVariable = pie * superPAZ.getPie();
//            double barrierVariable = slope*superPAZ.getSlope() + freeway*superPAZ.getFreeway() + empAllOthPropotion*empOtherPropotion;
//            double utility =  (size * Math.log(sizeVariable) + supportVariable + barrierVariable);
//            destinationUtility.put(superPAZ.getIndex(),utility);
//        }
//    }

    //    //linear power-transformed
//    @Override
//    protected void calculateDestinationUtility() {
//        double size = 0.213;
//        double pie = 0.0;
//        double slope = 0.0;
//        double freeway = 0.0;
//        double empAllOthPropotion = -1.24;
//
//        for (SuperPAZ superPAZ: dataSet.getDestinationSuperPAZs().values()){
//            double empIndustrial = superPAZ.getTotalEmpl() - superPAZ.getRetail() - superPAZ.getFinancial() - superPAZ.getGovernment() - superPAZ.getService();
//            double empOtherPropotion = empIndustrial / superPAZ.getTotalEmpl();
//            double sizeVariable =  superPAZ.getTotalEmpl();
//            double supportVariable = pie * superPAZ.getPie();
//            double barrierVariable = slope*superPAZ.getSlope() + freeway*superPAZ.getFreeway() + empAllOthPropotion*empOtherPropotion;
//            double utility =  (size * Math.log(sizeVariable) + supportVariable + barrierVariable);
//            destinationUtility.put(superPAZ.getIndex(),utility);
//        }
//    }

//    //original
//    @Override
//    protected void calculateDestinationUtility() {
//        double size = 0.505691;
//        double empRETFINGOV = 2.01532;
//        double pie = 0.0296117;
//        double slope = -0.114512;
//        double freeway = -0.297436;
//        double empAllOthPropotion = -0.987008;
//
//        for (SuperPAZ superPAZ: dataSet.getDestinationSuperPAZs().values()){
//            double employment = superPAZ.getRetail() + superPAZ.getFinancial() + superPAZ.getGovernment();
//            double empOtherPropotion = (superPAZ.getTotalEmpl() - employment-superPAZ.getService()) / superPAZ.getTotalEmpl();
//            double sizeVariable =  (Math.exp(empRETFINGOV)* employment)+superPAZ.getTotalEmpl() - employment;
//            double supportVariable = pie * superPAZ.getPie();
//            double barrierVariable = slope*superPAZ.getSlope() + freeway*superPAZ.getFreeway() + empAllOthPropotion*empOtherPropotion;
//            double utility =  (size * Math.log(sizeVariable) + supportVariable + barrierVariable);
//            destinationUtility.put(superPAZ.getIndex(),utility);
//        }
//    }

    @Override
    protected Map<Integer, Double> calculateDestinationUtilityPAZ(SuperPAZ superPAZ) {
        return null;
    }
}
