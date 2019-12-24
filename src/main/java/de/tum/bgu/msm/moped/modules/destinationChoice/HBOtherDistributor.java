package de.tum.bgu.msm.moped.modules.destinationChoice;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.data.SuperPAZ;

import java.util.Map;

public final class HBOtherDistributor extends TripDistributor {

    public HBOtherDistributor(DataSet dataSet) {
        super(dataSet, Purpose.HBO);
    }

    @Override
    protected void calculateDestinationUtility() {
        double size = 0.399613f;
        double park = 0.115274f;
        double empRETGOV = 3.82922f;
        double household = -1.96896f;
        double pie = 0.0247138f;
        double slope = -0.426383f;
        double freeway = 0.10023f;
        double empAllOthPropotion = -0.398784f;

        for (SuperPAZ superPAZ: dataSet.getDestinationSuperPAZs().values()){
            double employmentRETGOV = superPAZ.getRetail() + superPAZ.getGovernment();
            double empOtherPropotion;
            if (superPAZ.getTotalEmpl() == 0.0){
                empOtherPropotion = 0.0f;
            }else {
                empOtherPropotion = (superPAZ.getTotalEmpl() - employmentRETGOV)/superPAZ.getTotalEmpl();
            }
            double sizeVariable =  (Math.exp(empRETGOV)* employmentRETGOV + Math.exp(household)* superPAZ.getHousehold());
            double supportVariable = pie * superPAZ.getPie() + park * superPAZ.getPark();
            double barrierVariable = slope*superPAZ.getSlope() + freeway*superPAZ.getFreeway() + empAllOthPropotion*empOtherPropotion;
            double utility =  (size * Math.log(sizeVariable) + supportVariable + barrierVariable);
            destinationUtility.put(superPAZ.getIndex(),utility);
        }
    }

    @Override
    protected Map<Integer, Double> calculateDestinationUtilityPAZ(SuperPAZ superPAZ) {

        return null;
    }
}
