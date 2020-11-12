package de.tum.bgu.msm.moped.modules.destinationChoice;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.MopedZone;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.data.SuperPAZ;

public final class HBWorkDistributor extends TripDistributor {

    public HBWorkDistributor(DataSet dataSet) {
        super(dataSet, Purpose.HBW);
    }

    //WSTLUR paper
    @Override
    protected void calculateDestinationUtility() {
        double sizeRETSERCoef = 0.445;
        double sizeOTHERCoef = 0.352;
        double slopeCoef = -0.167;
        double industrialPropCoef = -1.249;
        double parkCoef = 0.0;
        double networkDensityCoef = 0.141;

        for (SuperPAZ superPAZ: dataSet.getSuperPAZs().values()){
            double industrialProp = superPAZ.getIndustrial() / superPAZ.getTotalEmpl();
            double sizeRETSER =  superPAZ.getRetail()+superPAZ.getService();
            double sizeOTHER = superPAZ.getFinancial()+superPAZ.getGovernment()+superPAZ.getTpu()+superPAZ.getWho();
            if(sizeRETSER <= 1){
                sizeRETSER = sizeRETSER+1;
            }

            if(sizeOTHER <= 1){
                sizeOTHER = sizeOTHER+1;
            }

            double supportVariable = parkCoef*superPAZ.getPark()+networkDensityCoef*superPAZ.getNetworkDesnity();
            double barrierVariable = slopeCoef*superPAZ.getSlope() + industrialPropCoef*industrialProp;
            float utility = (float) (sizeRETSERCoef * Math.log(sizeRETSER) + sizeOTHERCoef * Math.log(sizeOTHER) + supportVariable + barrierVariable);
            if (Double.isInfinite(utility) || Double.isNaN(utility)) {
                throw new RuntimeException(utility + " utility calculated! Please check calculation!" +
                        " sizeRETSER: " + sizeRETSER + " | Park: " + superPAZ.getPark() + " | slope: "
                        + superPAZ.getSlope() + " | networkDensity: " + superPAZ.getNetworkDesnity() +
                        " | industrial: " + industrialProp);
            }
            destinationUtility.put(superPAZ.getIndex(), utility);
        }
    }

    @Override
    protected void calculateDestinationUtilityPAZ() {
        double sizeOtherCoef =  0.541;
        double householdCoef = -0.433;
        double industrialPropCoef = 1.629;
        double parkCoef = 0.;

        for (MopedZone mopedZone: dataSet.getZones().values()){
            double sizeOther = Math.max(1,mopedZone.getRetail()+mopedZone.getService()+mopedZone.getFinancial()+mopedZone.getGovernment());
            double sizeHH =  Math.max(1,mopedZone.getTotalHH());
            double industrialProp = mopedZone.getIndustrial() / mopedZone.getTotalEmpl();


            float utility = (float) (sizeOtherCoef * Math.log(sizeOther)+
                    householdCoef * Math.log(sizeHH) + industrialPropCoef * industrialProp +
                    parkCoef * mopedZone.getParkArce());
            if (Double.isInfinite(utility) || Double.isNaN(utility)) {
                throw new RuntimeException(utility + " utility calculated! Please check calculation!" +
                        " sizeOther: " + sizeOther + " sizeHH: " + sizeHH +
                        " industrial: " + industrialProp +
                        " parkAcre: " + mopedZone.getParkArce());
            }
            destinationUtilityPAZ.put(mopedZone.getZoneId(),utility);
        }
    }
}
