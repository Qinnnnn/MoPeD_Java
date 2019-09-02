package de.tum.bgu.msm.moped.modules.destinationChoice;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.data.SuperPAZ;

import java.util.Map;

public final class HBWorkDistributor extends TripDistributor {

    public HBWorkDistributor(DataSet dataSet) {
        super(dataSet, Purpose.HBW);
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

    //linear non-transformed
    @Override
    protected void calculateDestinationUtility() {
        double size = 0.197;
        double pie = 0.0;
        double slope = 0.0;
        double freeway = 0.0;
        double empAllOthPropotion = -1.14;

        for (SuperPAZ superPAZ: dataSet.getDestinationSuperPAZs().values()){
            //double empIndustrial = superPAZ.getTotalEmpl() - superPAZ.getRetail() - superPAZ.getFinancial() - superPAZ.getGovernment() - superPAZ.getService();
            double empOtherPropotion = superPAZ.getIndustrial() / superPAZ.getTotalEmpl();
            double sizeVariable =  superPAZ.getTotalEmpl();
            if(sizeVariable <= 1){
                sizeVariable = sizeVariable+1;
            }
            double supportVariable = pie * superPAZ.getPie();
            double barrierVariable = slope*superPAZ.getSlope() + freeway*superPAZ.getFreeway() + empAllOthPropotion*empOtherPropotion;
            double utility =  (size * Math.log(sizeVariable) + supportVariable + barrierVariable);
            destinationUtility.put(superPAZ.getIndex(),utility);
        }
    }

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
