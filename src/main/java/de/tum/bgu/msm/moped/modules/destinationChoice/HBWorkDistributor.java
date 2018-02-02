package de.tum.bgu.msm.moped.modules.destinationChoice;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.data.SuperPAZ;

public final class HBWorkDistributor extends TripDistributor {

    public HBWorkDistributor(DataSet dataSet) {
        super(dataSet, Purpose.HBW);
    }

    @Override
    protected void calculateDestinationUtility() {
        double size = 0.505691;
        double empRETFINGOV = 2.01532;
        double pie = 0.0296117;
        double slope = -0.114512;
        double freeway = -0.297436;
        double empAllOthPropotion = -0.987008;

        for (SuperPAZ superPAZ: dataSet.getDestinationSuperPAZs().values()){
            double employment = superPAZ.getRetail() + superPAZ.getFinancial() + superPAZ.getGovernment();
            double empOtherPropotion = (superPAZ.getTotalEmpl() - employment) / superPAZ.getTotalEmpl();
            double sizeVariable =  (Math.exp(empRETFINGOV)* employment);
            double supportVariable = pie * superPAZ.getPie();
            double barrierVariable = slope*superPAZ.getSlope() + freeway*superPAZ.getFreeway() + empAllOthPropotion*empOtherPropotion;
            double utility =  (size * Math.log(sizeVariable) + supportVariable + barrierVariable);
            destinationUtility.put(superPAZ.getIndex(),utility);
        }


    }
}
