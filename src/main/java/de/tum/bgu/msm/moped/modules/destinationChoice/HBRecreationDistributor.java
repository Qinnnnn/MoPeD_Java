package de.tum.bgu.msm.moped.modules.destinationChoice;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.data.SuperPAZ;

import java.util.Map;

public final class HBRecreationDistributor extends TripDistributor{

    public HBRecreationDistributor(DataSet dataSet) {
        super(dataSet, Purpose.HBO);
    }

    @Override
    protected void calculateDestinationUtility() {
        double size = 0.0517537f;
        double park = 0.460169f;
        double empRET = 6.50648f;
        double empGOV = 17.1087f;
        double household = -3.16331f;
        double pie = 0.0110469f;
        double slope = -0.0529455f;
        double freeway = -0.16851f;
        double empAllOthPropotion = -0.0898361f;

        for (SuperPAZ superPAZ: dataSet.getDestinationSuperPAZs().values()){
            double employmentRET = superPAZ.getRetail();
            double employmentGOV = superPAZ.getGovernment();
            double empOtherPropotion;
            if (superPAZ.getTotalEmpl() == 0.0){
                empOtherPropotion = 0.0f;
            }else {
                empOtherPropotion = (superPAZ.getTotalEmpl() - employmentRET-employmentGOV)/superPAZ.getTotalEmpl();
            }
            double sizeVariable = (Math.exp(empRET)* employmentRET + Math.exp(empGOV)* employmentGOV + Math.exp(household)* superPAZ.getHousehold());
            double supportVariable = pie * superPAZ.getPie() + park * superPAZ.getPark();
            double barrierVariable = slope*superPAZ.getSlope() + freeway*superPAZ.getFreeway() + empAllOthPropotion*empOtherPropotion;
            double utility = (size * Math.log(sizeVariable) + supportVariable + barrierVariable);
            destinationUtility.put(superPAZ.getIndex(),utility);
        }
    }

    @Override
    protected Map<Integer, Double> calculateDestinationUtilityPAZ(SuperPAZ superPAZ) {

        return null;
    }
}
