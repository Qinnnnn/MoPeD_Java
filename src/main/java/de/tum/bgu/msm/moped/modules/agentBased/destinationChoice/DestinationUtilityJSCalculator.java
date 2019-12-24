package de.tum.bgu.msm.moped.modules.agentBased.destinationChoice;


import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.util.js.JavaScriptCalculator;


import java.io.Reader;

public class DestinationUtilityJSCalculator extends JavaScriptCalculator<Double> {

    private final String function;

    DestinationUtilityJSCalculator(Reader reader, Purpose purpose) {
        super(reader);
        if (purpose.equals(Purpose.HBW)||purpose.equals(Purpose.HBE)) {
            this.function = "calculateHBWHBE";
        }else {
            this.function = "calculateOther";
        }
    }

    Double calculateUtility(double sizeVariable, double barrierVariable, double travelDistance) {
        return super.calculate(function, travelDistance, sizeVariable,barrierVariable);
    }
}
