package de.tum.bgu.msm.moped.modules.destinationChoice;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.data.SuperPAZ;

public final class HBShopDistributor extends TripDistributor{

    public HBShopDistributor(DataSet dataSet) {
        super(dataSet, Purpose.HBSHOP);
    }

    @Override
    protected void calculateDestinationUtility() {
        double size = 0.910195f;
        double empRET = 5.45389f;
        double empAllOth = 0.0f;
        double pie = -0.013919f; //to be checked
        double slope = -0.194741f;
        double freeway = -1.02347f;
        double empAllOthPropotion = -1.74298f;

        for (SuperPAZ superPAZ: dataSet.getDestinationSuperPAZs().values()){
            double employment = superPAZ.getRetail();
            double sizeVariable = (Math.exp(empRET)* employment + Math.exp(empAllOth)*((superPAZ.getTotalEmpl() - employment)));
            double empOtherPropotion;
            if (superPAZ.getTotalEmpl() == 0.0){
                empOtherPropotion = 0.0f;
            }else {
                empOtherPropotion = (superPAZ.getTotalEmpl() - employment) / superPAZ.getTotalEmpl();
            }
            double barrierVariable = pie * superPAZ.getPie() + slope*superPAZ.getSlope() + freeway*superPAZ.getFreeway() + empAllOthPropotion*empOtherPropotion;
            double utility = (size * Math.log(sizeVariable) + barrierVariable);
            destinationUtility.put(superPAZ.getIndex(),utility);
        }
    }
}
