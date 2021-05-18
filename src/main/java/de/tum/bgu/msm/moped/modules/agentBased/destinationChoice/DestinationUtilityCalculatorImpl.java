package de.tum.bgu.msm.moped.modules.agentBased.destinationChoice;

import de.tum.bgu.msm.moped.data.MopedZone;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.data.SuperPAZ;

public class DestinationUtilityCalculatorImpl implements DestinationUtilityCalculator {

    //distHasCar,distNoCar,networkDensity,sizeRetSev,sizeFinGov,industrial,barrier,park
    private double[] coefficientsHBW = {-1.536, -1.372, 0.141, 0.445, 0.352, -1.249, -0.321, 0};
    private double[] coefficientsHBE = {-1.536, -1.372, 0.141, 0.445, 0.352, -1.249, -0.321, 0};

    //distHasKid,distNoKid,networkDensity,sizeRet,industrial,barrier,park
    private double[] coefficientsHBS = {-2.182, -1.776, 0.049, 0.977, -1.306, -0.279, 0};
    //distHasKid,distNoKid,networkDensity,sizeRetSev,sizeHH,barrier,park
    private double[] coefficientsHBR = {-2.321, -1.955, 0, 0.133, 0.054, -0.568, 0.662};

    //dist,networkDensity,sizeAll(non-industrial),industrial,barrier,park
    private double[] coefficientsHBO = {-2.217, 0.214, 0.389, 0, -0.828, 0.510};
    private double[] coefficientsNHBW = {-1.883, 0.185, 0.667, -0.749, -0.718, 0.0};
    private double[] coefficientsNHBO = {-2.141, 0.184, 0.516, 0, -1.361, 0.0};

    //PAZ level destination choice model
    //origin,dist,sizeRetSer,sizeFinGov,sizeHH,industrial,park
    private double[] coefficientsHBWPAZ = {2.068, -1.335, 0.541, 0.541,-0.433, 1.629, 0};

    //origin,dist,sizeRet,sizeOther,sizeHH,industrial,park
    private double[] coefficientsHBSPAZ = {0.623, -2.120, 0.820, 0.188, -0.169, 0, -0.651};
    //origin,dist,sizeRet,sizeOther,sizeHH,industrial,park
    private double[] coefficientsHBRPAZ = {2.704, -1.974, 0.123, 0.123, -0.560, -1.602, 1.473};

    //origin,dist,sizeRetSer,sizeFinGov,sizeHH,industrial,park
    private double[] coefficientsHBOPAZ = {3.162, -2.348, 0.145, 0.559, -0.507, -0.477, 0};
    private double[] coefficientsNHBWPAZ = {0.654, -2.894, 0.316, 0.062, -0.051, 0, 0};
    private double[] coefficientsNHBOPAZ = {1.628, -2.163, 0.355, 0.137, -0.082, -0.694, 0};

    private double networkDensity;
    private double sizeRetSev;
    private double sizeFinGov;
    private double sizeRet;
    private double sizeOther;
    private double industrialProp;
    private double park;
    private float utility;
    private float probability;
    private double sizeAll;
    private double sizeHH;

    @Override
    public float calculateUtility(Purpose purpose, SuperPAZ destination, double travelDistance, int crossMotorway) {

        switch (purpose) {
            case HBW:
            case HBE:
                networkDensity = destination.getNetworkDesnity();
                sizeRetSev = destination.getRetail()+destination.getService();
                sizeFinGov = destination.getFinancial()+destination.getGovernment();
                industrialProp = 0.;
                if(destination.getTotalEmpl()!=0){
                    industrialProp = destination.getIndustrial()/destination.getTotalEmpl();
                }
                park = destination.getPark();

                if(sizeRetSev < 1){
                    sizeRetSev = sizeRetSev+1;
                }

                if(sizeFinGov < 1){
                    sizeFinGov = sizeFinGov+1;
                }

                utility = (float) (coefficientsHBW[0]*travelDistance+coefficientsHBW[2]*networkDensity+
                                        coefficientsHBW[3]*Math.log(sizeRetSev)+coefficientsHBW[4]*Math.log(sizeFinGov)+
                                        coefficientsHBW[5]*industrialProp+coefficientsHBW[6]*crossMotorway+
                                        coefficientsHBW[7]*park);

                probability = (float) Math.exp(utility);
                return probability;
            case HBS:
                networkDensity = destination.getNetworkDesnity();
                sizeRet = destination.getRetail();
                industrialProp = 0.;
                if(destination.getTotalEmpl()!=0){
                    industrialProp = destination.getIndustrial()/destination.getTotalEmpl();
                }
                park = destination.getPark();

                if(sizeRet < 1){
                    sizeRet = sizeRet+1;
                }

                utility = (float) (coefficientsHBS[0]*travelDistance+coefficientsHBS[2]*networkDensity+
                        coefficientsHBS[3]*Math.log(sizeRet)+coefficientsHBS[4]*industrialProp+
                        coefficientsHBS[5]*crossMotorway+coefficientsHBS[6]*park);

                probability = (float) Math.exp(utility);

                return probability;
            case HBR:
                networkDensity = destination.getNetworkDesnity();
                sizeRetSev = destination.getRetail()+destination.getService();
                sizeHH = Math.max(1,destination.getHousehold());
                industrialProp = 0.;
                if(destination.getTotalEmpl()!=0){
                    industrialProp = destination.getIndustrial()/destination.getTotalEmpl();
                }
                park = destination.getPark();

                if(sizeRetSev < 1){
                    sizeRetSev = sizeRetSev+1;
                }

                utility = (float) (coefficientsHBR[0]*travelDistance+coefficientsHBR[2]*networkDensity+
                        coefficientsHBR[3]*Math.log(sizeRetSev)+coefficientsHBR[4]*Math.log(sizeHH)+
                        coefficientsHBR[5]*crossMotorway+coefficientsHBR[6]*park);

                probability = (float) Math.exp(utility);

                return probability;
            case HBO:
                networkDensity = destination.getNetworkDesnity();
                sizeAll = destination.getTotalEmpl()-destination.getIndustrial();
                industrialProp = 0.;
                if(destination.getTotalEmpl()!=0){
                    industrialProp = destination.getIndustrial()/destination.getTotalEmpl();
                }
                park = destination.getPark();

                if(sizeAll < 1){
                    sizeAll = sizeAll+1;
                }

                utility = (float) (coefficientsHBO[0]*travelDistance+coefficientsHBO[1]*networkDensity+
                                        coefficientsHBO[2]*Math.log(sizeAll)+coefficientsHBO[3]*industrialProp+
                                        coefficientsHBO[4]*crossMotorway+coefficientsHBO[5]*park);

                probability = (float) Math.exp(utility);

                return probability;
            case NHBW:
                networkDensity = destination.getNetworkDesnity();
                sizeAll = destination.getTotalEmpl()-destination.getIndustrial();
                industrialProp = 0.;
                if(destination.getTotalEmpl()!=0){
                    industrialProp = destination.getIndustrial()/destination.getTotalEmpl();
                }
                park = destination.getPark();

                if(sizeAll < 1){
                    sizeAll = sizeAll+1;
                }

                utility = (float) (coefficientsNHBW[0]*travelDistance+coefficientsNHBW[1]*networkDensity+
                                        coefficientsNHBW[2]*Math.log(sizeAll)+coefficientsNHBW[3]*industrialProp+
                                        coefficientsNHBW[4]*crossMotorway+coefficientsNHBW[5]*park);

                probability = (float) Math.exp(utility);

                return probability;
            case NHBO:
                networkDensity = destination.getNetworkDesnity();
                sizeAll = destination.getTotalEmpl()-destination.getIndustrial();
                industrialProp = 0.;
                if(destination.getTotalEmpl()!=0){
                    industrialProp = destination.getIndustrial()/destination.getTotalEmpl();
                }
                park = destination.getPark();

                if(sizeAll < 1){
                    sizeAll = sizeAll+1;
                }

                utility = (float) (coefficientsNHBO[0]*travelDistance+coefficientsNHBO[1]*networkDensity+
                                        coefficientsNHBO[2]*Math.log(sizeAll)+coefficientsNHBO[3]*industrialProp+
                                        coefficientsNHBO[4]*crossMotorway+coefficientsNHBO[5]*park);

                probability = (float) Math.exp(utility);

                return probability;
            default:
                throw new RuntimeException("not implemented!");
        }

    }

    @Override
    public float calculateUtility(Purpose purpose, MopedZone destination, double travelDistance, int originPAZ) {
        switch (purpose) {
            case HBW:
            case HBE:
                sizeRetSev = destination.getRetail()+destination.getService();
                sizeFinGov = destination.getFinancial()+destination.getGovernment();
                sizeHH =  Math.max(1,destination.getTotalHH());
                industrialProp = 0.;
                if(destination.getTotalEmpl()!=0){
                    industrialProp = destination.getIndustrial()/destination.getTotalEmpl();
                }
                park = destination.getParkArce();

                if(sizeRetSev < 1){
                    sizeRetSev = sizeRetSev+1;
                }

                if(sizeFinGov < 1){
                    sizeFinGov = sizeFinGov+1;
                }

                utility = (float) (coefficientsHBWPAZ[0]*originPAZ+coefficientsHBWPAZ[1]*travelDistance+
                                        coefficientsHBWPAZ[2]*Math.log(sizeRetSev+sizeFinGov)+coefficientsHBWPAZ[4]*Math.log(sizeHH)+
                                        coefficientsHBWPAZ[5]*industrialProp+coefficientsHBWPAZ[6]*park);

                probability = (float) Math.exp(utility);

                return probability;
            case HBS:
                sizeRet = destination.getRetail();
                sizeOther = destination.getFinancial()+destination.getGovernment()+destination.getService();
                sizeHH =  Math.max(1,destination.getTotalHH());
                industrialProp = 0.;
                if(destination.getTotalEmpl()!=0){
                    industrialProp = destination.getIndustrial()/destination.getTotalEmpl();
                }
                park = destination.getParkArce();

                if(sizeRet < 1){
                    sizeRet = sizeRet+1;
                }

                if(sizeOther < 1){
                    sizeOther = sizeOther+1;
                }

                utility = (float) (coefficientsHBSPAZ[0]*originPAZ+coefficientsHBSPAZ[1]*travelDistance+
                                        coefficientsHBSPAZ[2]*Math.log(sizeRet)+coefficientsHBSPAZ[3]*Math.log(sizeOther)+
                                        coefficientsHBSPAZ[4]*Math.log(sizeHH)+
                                        coefficientsHBSPAZ[5]*industrialProp+coefficientsHBSPAZ[6]*park);

                probability = (float) Math.exp(utility);

                return probability;
            case HBR:
                sizeAll = destination.getRetail()+destination.getFinancial()+destination.getGovernment()+destination.getService();
                sizeAll = Math.max(1, sizeAll);
                sizeHH =  Math.max(1,destination.getTotalHH());
                industrialProp = 0.;
                if(destination.getTotalEmpl()!=0){
                    industrialProp = destination.getIndustrial()/destination.getTotalEmpl();
                }
                park = destination.getParkArce();


                utility = (float) (coefficientsHBRPAZ[0]*originPAZ+coefficientsHBRPAZ[1]*travelDistance+
                        coefficientsHBRPAZ[2]*Math.log(sizeAll)+
                        coefficientsHBRPAZ[4]*Math.log(sizeHH)+
                        coefficientsHBRPAZ[5]*industrialProp+coefficientsHBRPAZ[6]*park);

                probability = (float) Math.exp(utility);

                return probability;
            case HBO:
                sizeRetSev = destination.getRetail()+destination.getService();
                sizeFinGov = destination.getFinancial()+destination.getGovernment();
                sizeHH =  Math.max(1,destination.getTotalHH());
                industrialProp = 0.;
                if(destination.getTotalEmpl()!=0){
                    industrialProp = destination.getIndustrial()/destination.getTotalEmpl();
                }
                park = destination.getParkArce();

                if(sizeRetSev < 1){
                    sizeRetSev = sizeRetSev+1;
                }

                if(sizeFinGov < 1){
                    sizeFinGov = sizeFinGov+1;
                }

                utility = (float) (coefficientsHBOPAZ[0]*originPAZ+coefficientsHBOPAZ[1]*travelDistance+
                                        coefficientsHBOPAZ[2]*Math.log(sizeRetSev)+coefficientsHBOPAZ[3]*Math.log(sizeFinGov)+
                                        coefficientsHBOPAZ[4]*Math.log(sizeHH)+
                                        coefficientsHBOPAZ[5]*industrialProp+coefficientsHBOPAZ[6]*park);

                probability = (float) Math.exp(utility);

                return probability;
            case NHBW:
                sizeRetSev = destination.getRetail()+destination.getService();
                sizeFinGov = destination.getFinancial()+destination.getGovernment();
                sizeHH =  Math.max(1,destination.getTotalHH());
                industrialProp = 0.;
                if(destination.getTotalEmpl()!=0){
                    industrialProp = destination.getIndustrial()/destination.getTotalEmpl();
                }
                park = destination.getParkArce();

                if(sizeRetSev < 1){
                    sizeRetSev = sizeRetSev+1;
                }

                if(sizeFinGov < 1){
                    sizeFinGov = sizeFinGov+1;
                }

                utility = (float) (coefficientsNHBWPAZ[0]*originPAZ+coefficientsNHBWPAZ[1]*travelDistance+
                                        coefficientsNHBWPAZ[2]*Math.log(sizeRetSev)+coefficientsNHBWPAZ[3]*Math.log(sizeFinGov)+
                                        coefficientsNHBWPAZ[4]*Math.log(sizeHH)+
                                        coefficientsNHBWPAZ[5]*industrialProp+coefficientsNHBWPAZ[6]*park);

                probability = (float) Math.exp(utility);

                return probability;
            case NHBO:
                sizeRetSev = destination.getRetail()+destination.getService();
                sizeFinGov = destination.getFinancial()+destination.getGovernment();
                sizeHH =  Math.max(1,destination.getTotalHH());
                industrialProp = 0.;
                if(destination.getTotalEmpl()!=0){
                    industrialProp = destination.getIndustrial()/destination.getTotalEmpl();
                }
                park = destination.getParkArce();

                if(sizeRetSev < 1){
                    sizeRetSev = sizeRetSev+1;
                }

                if(sizeFinGov < 1){
                    sizeFinGov = sizeFinGov+1;
                }

                utility = (float) (coefficientsNHBOPAZ[0]*originPAZ+coefficientsNHBOPAZ[1]*travelDistance+
                                        coefficientsNHBOPAZ[2]*Math.log(sizeRetSev)+coefficientsNHBOPAZ[3]*Math.log(sizeFinGov)+
                        coefficientsNHBOPAZ[4]*Math.log(sizeHH)+
                        coefficientsNHBOPAZ[5]*industrialProp+coefficientsNHBOPAZ[6]*park);

                probability = (float) Math.exp(utility);

                return probability;
            default:
                throw new RuntimeException("not implemented!");
        }
    }


}
