package de.tum.bgu.msm.moped.modules.agentBased.walkModeChoice;

import de.tum.bgu.msm.moped.data.*;

public class ModeChoiceCalculatorImpl implements ModeChoiceCalculator {
    //intercept,distanceKm,car,kid1,kid2,kid2+,LogpedestrianAccessibility,HBE
    private double[] coefficientsHBW = {-5.798, -0.299, -0.296, 1.099, 1.123, 0.836, 0.559, 0.0};
    private double[] coefficientsHBE = {-5.798, -0.299, -0.296, 1.099, 1.123, 0.836, 0.559, 0.938};

    //intercept,car0,car2,car2+,kidYes,LogpedestrianAccessibility,HBS
    private double[] coefficientsHBS = {-7.276, 0.945, -0.239, -0.408, 0.184, 0.727, -0.513};
    private double[] coefficientsHBO = {-7.276, 0.945, -0.239, -0.408, 0.184, 0.727, 0};

    //intercept,income2,income3,income4,car0,car2,car2+,kidYes,LogpedestrianAccessibility,NHBW
    private double[] coefficientsNHBW = {-7.411, -0.205, 0.222, 0.448, 1.375, -0.898, -0.963, -0.162, 0.686, -0.362};
    private double[] coefficientsNHBO = {-7.411, -0.205, 0.222, 0.448, 1.375, -0.898, -0.963, -0.162, 0.686, 0.0};

    private int car;
    private int kid;
    private double travelDistance;
    private double activityDensity;
    private double utilityWalk;
    private double probabilityWalk;
    private double monthlyIncome;
    private int incomeGroup;

    @Override
    public double calculateProbabilities(MopedHousehold hh, MopedTrip trip) {
        switch (trip.getTripPurpose()) {
            case HBW:
                car = hh.getAutos();
                kid = hh.getKids();
                travelDistance = trip.getTripDistance();
                activityDensity = hh.getHomeZone().getPieEmpl()+hh.getHomeZone().getPiePop();
                activityDensity = Math.max(activityDensity,1);

                utilityWalk = coefficientsHBW[0]+coefficientsHBW[1]*travelDistance+coefficientsHBW[2]*(car>0?1:0) +
                        coefficientsHBW[3]*(kid==1?1:0)+coefficientsHBW[4]*(kid==2?1:0)+coefficientsHBW[5]*(kid>2?1:0)+
                        coefficientsHBW[6]*Math.log(activityDensity)+coefficientsHBW[7];

                probabilityWalk = Math.exp(utilityWalk) / (Math.exp(utilityWalk) + 1);

                return probabilityWalk;
            case HBE:
                car = hh.getAutos();
                kid = hh.getKids();
                travelDistance = trip.getTripDistance();
                activityDensity = hh.getHomeZone().getPieEmpl()+hh.getHomeZone().getPiePop();
                activityDensity = Math.max(activityDensity,1);

                utilityWalk = coefficientsHBE[0]+coefficientsHBE[1]*travelDistance+coefficientsHBE[2]*(car>0?1:0) +
                        coefficientsHBE[3]*(kid==1?1:0)+coefficientsHBE[4]*(kid==2?1:0)+coefficientsHBE[5]*(kid>2?1:0)+
                        coefficientsHBE[6]*Math.log(activityDensity)+coefficientsHBE[7];

                probabilityWalk = Math.exp(utilityWalk) / (Math.exp(utilityWalk) + 1);

                return probabilityWalk;

            case HBS:
                car = hh.getAutos();
                kid = hh.getKids();
                activityDensity = hh.getHomeZone().getPieEmpl()+hh.getHomeZone().getPiePop();
                activityDensity = Math.max(activityDensity,1);

                utilityWalk = coefficientsHBS[0]+coefficientsHBS[1]*(car==0?1:0)+coefficientsHBS[2]*(car==2?1:0) +
                        coefficientsHBS[3]*(car>2?1:0)+coefficientsHBS[4]*(kid>0?1:0)+
                        coefficientsHBS[5]*Math.log(activityDensity)+coefficientsHBS[6];

                probabilityWalk = Math.exp(utilityWalk) / (Math.exp(utilityWalk) + 1);

                return probabilityWalk;
            case HBO:
                car = hh.getAutos();
                kid = hh.getKids();

                activityDensity = hh.getHomeZone().getPieEmpl()+hh.getHomeZone().getPiePop();
                activityDensity = Math.max(activityDensity,1);

                utilityWalk = coefficientsHBO[0]+coefficientsHBO[1]*(car==0?1:0)+coefficientsHBO[2]*(car==2?1:0) +
                        coefficientsHBO[3]*(car>2?1:0)+coefficientsHBO[4]*(kid>0?1:0)+
                        coefficientsHBO[5]*Math.log(activityDensity)+coefficientsHBO[6];

                probabilityWalk = Math.exp(utilityWalk) / (Math.exp(utilityWalk) + 1);

                return probabilityWalk;
            case NHBW:
                car = hh.getAutos();
                kid = hh.getKids();
                monthlyIncome = hh.getIncome();
                incomeGroup = 0;
                if (monthlyIncome*12<=18000){
                    incomeGroup = 1;
                }else if(monthlyIncome*12<=24000){
                    incomeGroup = 2;
                }else if(monthlyIncome*12<=36000){
                    incomeGroup = 3;
                }else{
                    incomeGroup = 4;
                }
                activityDensity = hh.getHomeZone().getPieEmpl()+hh.getHomeZone().getPiePop();
                activityDensity = Math.max(activityDensity,1);

                utilityWalk = coefficientsNHBW[0]+coefficientsNHBW[1]*(incomeGroup==2?1:0)+
                        coefficientsNHBW[2]*(incomeGroup==3?1:0) +coefficientsNHBW[3]*(incomeGroup==4?1:0)+
                        coefficientsNHBW[4]*(car==0?1:0)+coefficientsNHBW[5]*(car==2?1:0) +
                        coefficientsNHBW[6]*(car>2?1:0)+coefficientsNHBW[7]*(kid>0?1:0)+
                        coefficientsNHBW[8]*Math.log(activityDensity)+coefficientsNHBW[9];

                probabilityWalk = Math.exp(utilityWalk) / (Math.exp(utilityWalk) + 1);

                return probabilityWalk;
            case NHBO:
                car = hh.getAutos();
                kid = hh.getKids();
                monthlyIncome = hh.getIncome();
                incomeGroup = 0;
                if (monthlyIncome*12<=18000){
                    incomeGroup = 1;
                }else if(monthlyIncome*12<=24000){
                    incomeGroup = 2;
                }else if(monthlyIncome*12<=36000){
                    incomeGroup = 3;
                }else{
                    incomeGroup = 4;
                }
                activityDensity = hh.getHomeZone().getPieEmpl()+hh.getHomeZone().getPiePop();
                activityDensity = Math.max(activityDensity,1);

                utilityWalk = coefficientsNHBO[0]+coefficientsNHBO[1]*(incomeGroup==2?1:0)+
                        coefficientsNHBO[2]*(incomeGroup==3?1:0)+coefficientsNHBO[3]*(incomeGroup==4?1:0)+
                        coefficientsNHBO[4]*(car==0?1:0)+coefficientsNHBO[5]*(car==2?1:0)+
                        coefficientsNHBO[6]*(car>2?1:0)+coefficientsNHBO[7]*(kid>0?1:0)+
                        coefficientsNHBO[8]*Math.log(activityDensity)+coefficientsNHBO[9];

                probabilityWalk = Math.exp(utilityWalk) / (Math.exp(utilityWalk) + 1);

                return probabilityWalk;
            default:
                throw new RuntimeException("not implemented!");
        }
    }
}
