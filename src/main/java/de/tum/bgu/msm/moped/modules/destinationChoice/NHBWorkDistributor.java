package de.tum.bgu.msm.moped.modules.destinationChoice;

import cern.colt.map.tdouble.OpenIntDoubleHashMap;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.MopedZone;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.data.SuperPAZ;

import java.util.Map;

public final class NHBWorkDistributor extends TripDistributor {

    public NHBWorkDistributor(DataSet dataSet) {
        super(dataSet, Purpose.NHBW);
    }

    //WSTLUR paper
    @Override
    protected void calculateDestinationUtility() {
        double sizeOTHERCoef = 0.667;
        double sizeHHCoef = 0.0;
        double slopeCoef = -0.157;
        double industrialPropCoef = -0.749;
        double parkCoef = 0.0;
        double networkDensityCoef = 0.185;

        for (SuperPAZ superPAZ: dataSet.getSuperPAZs().values()){
            double industrialProp = superPAZ.getIndustrial() / superPAZ.getTotalEmpl();
            if(superPAZ.getTotalEmpl()==0){
                industrialProp = 0.;
            }
            double sizeOTHER =  superPAZ.getTotalEmpl()-superPAZ.getIndustrial();
            double sizeHH = superPAZ.getHousehold();
            if(sizeOTHER <= 1){
                sizeOTHER = sizeOTHER+1;
            }

            if(sizeHH <= 1){
                sizeHH = sizeHH+1;
            }

            double supportVariable = parkCoef*superPAZ.getPark()+networkDensityCoef*superPAZ.getNetworkDesnity();
            double barrierVariable = slopeCoef*superPAZ.getSlope() + industrialPropCoef*industrialProp;
            double utility =  sizeOTHERCoef * Math.log(sizeOTHER) + sizeHHCoef * Math.log(sizeHH) + supportVariable + barrierVariable;
            if (Double.isInfinite(utility) || Double.isNaN(utility)) {
                throw new RuntimeException(utility + " utility calculated! Please check calculation!" +
                        " sizeOther: " + sizeOTHER + " | Park: " + superPAZ.getPark() + " | slope: "
                        + superPAZ.getSlope() + " | networkDensity: " + superPAZ.getNetworkDesnity() +
                        " | industrial: " + industrialProp + " | HH: " + superPAZ.getHousehold());
            }
            destinationUtility.put(superPAZ.getIndex(), (float) utility);
        }
    }

    @Override
    protected void calculateDestinationUtilityPAZ() {
        double sizeRETSERCoef =  0.316;
        double sizeFINGOVCoef =  0.062;
        double householdCoef = -0.051;
        double industrialPropCoef = 0.;
        double parkCoef = 0.;

        for (MopedZone mopedZone: dataSet.getZones().values()){
            double sizeRETSER = Math.max(1,mopedZone.getRetail()+mopedZone.getService());
            double sizeFINGOV = Math.max(1,mopedZone.getFinancial()+mopedZone.getGovernment());
            double sizeHH =  Math.max(1,mopedZone.getTotalHH());
            double industrialProp = mopedZone.getIndustrial() / mopedZone.getTotalEmpl();

            if(mopedZone.getTotalEmpl()==0){
                industrialProp = 0.;
            }

            float utility = (float) (sizeRETSERCoef * Math.log(sizeRETSER) +
                    sizeFINGOVCoef * Math.log(sizeFINGOV) +
                    householdCoef * Math.log(sizeHH) + industrialPropCoef * industrialProp +
                    parkCoef * mopedZone.getParkArce());
            if (Double.isInfinite(utility) || Double.isNaN(utility)) {
                throw new RuntimeException(utility + " utility calculated! Please check calculation!" +
                        " sizeRETSER: " + sizeRETSER +
                        " sizeFINGOV: " + sizeFINGOV +
                        " sizeHH: " + sizeHH +
                        " industrial: " + industrialProp +
                        " parkAcre: " + mopedZone.getParkArce());
            }
            destinationUtilityPAZ.put(mopedZone.getZoneId(),utility);
        }
    }
}