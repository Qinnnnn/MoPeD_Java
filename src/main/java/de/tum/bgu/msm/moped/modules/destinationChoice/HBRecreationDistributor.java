package de.tum.bgu.msm.moped.modules.destinationChoice;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.MopedZone;
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
            double barrierVariable = slopeCoef*superPAZ.getSlope() + industrialPropCoef*industrialProp;
            float utility = (float) (sizeRETSERCoef * Math.log(sizeRETSER) + sizeHHCoef * Math.log(sizeHH) + supportVariable + barrierVariable);
            if (Double.isInfinite(utility) || Double.isNaN(utility)) {
                throw new RuntimeException(utility + " utility calculated! Please check calculation!" +
                        " sizeRETSER: " + sizeRETSER + " | Park: " + superPAZ.getPark() + " | slope: "
                        + superPAZ.getSlope() + " | networkDensity: " + superPAZ.getNetworkDesnity() +
                        " | industrial: " + industrialProp + " | HH: " + superPAZ.getHousehold());
            }
            destinationUtility.put(superPAZ.getIndex(), utility);
        }
    }

    @Override
    protected void calculateDestinationUtilityPAZ() {
        double sizeOtherCoef =  0.123;
        double householdCoef = -0.560;
        double industrialPropCoef = -1.602;
        double parkCoef = 1.473;

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
