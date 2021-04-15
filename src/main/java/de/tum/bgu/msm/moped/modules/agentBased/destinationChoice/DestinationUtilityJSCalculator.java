package de.tum.bgu.msm.moped.modules.agentBased.destinationChoice;


import de.tum.bgu.msm.moped.data.MopedZone;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.data.SuperPAZ;
import de.tum.bgu.msm.moped.util.js.JavaScriptCalculator;


import java.io.Reader;

public class DestinationUtilityJSCalculator extends JavaScriptCalculator<Double> {

    private String function;

    DestinationUtilityJSCalculator(Reader reader, Purpose purpose) {
        super(reader);
        if (purpose.equals(Purpose.HBW)||purpose.equals(Purpose.HBE)) {
            this.function = "calculateHBWHBE";
        }else if (purpose.equals(Purpose.HBS) ){
            this.function = "calculateHBS";
        }else if (purpose.equals(Purpose.HBO)){
            this.function = "calculateHBO";
        }else if (purpose.equals(Purpose.NHBW)){
            this.function = "calculateNHBW";
        }else {
            this.function = "calculateNHBO";
        }

    }

    public DestinationUtilityJSCalculator(Reader reader) {
        super(reader);
    }

    Double calculateUtility(double sizeVariable, double barrierVariable, double travelDistance) {
        return super.calculate(function, travelDistance, sizeVariable,barrierVariable);
    }

    public Double calculateUtility(Purpose purpose, SuperPAZ destination, double travelDistance, int crossMotorway) {
        if (purpose.equals(Purpose.HBW)||purpose.equals(Purpose.HBE)) {
            return super.calculate("calculateHBWHBE", travelDistance, destination,crossMotorway);
        }else if (purpose.equals(Purpose.HBS) ){
            return super.calculate("calculateHBS", travelDistance, destination,crossMotorway);
        }else if (purpose.equals(Purpose.HBO)){
            return super.calculate("calculateHBO", travelDistance, destination,crossMotorway);
        }else if (purpose.equals(Purpose.NHBW)){
            return super.calculate("calculateNHBW", travelDistance, destination,crossMotorway);
        }else {
            return super.calculate("calculateNHBO", travelDistance, destination,crossMotorway);
        }
    }

    public Double calculateUtility(Purpose purpose, MopedZone destination, double travelDistance, int originPAZ) {
        if (purpose.equals(Purpose.HBW)||purpose.equals(Purpose.HBE)) {
            return super.calculate("calculateHBWHBEPAZ", travelDistance, destination,originPAZ);
        }else if (purpose.equals(Purpose.HBS) ){
            return super.calculate("calculateHBSPAZ", travelDistance, destination,originPAZ);
        }else if (purpose.equals(Purpose.HBO)){
            return super.calculate("calculateHBOPAZ", travelDistance, destination,originPAZ);
        }else if (purpose.equals(Purpose.NHBW)){
            return super.calculate("calculateNHBWPAZ", travelDistance, destination,originPAZ);
        }else {
            return super.calculate("calculateNHBOPAZ", travelDistance, destination,originPAZ);
        }
    }
}
