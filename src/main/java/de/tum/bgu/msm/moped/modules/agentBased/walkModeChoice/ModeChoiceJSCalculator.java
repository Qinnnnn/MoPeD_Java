package de.tum.bgu.msm.moped.modules.agentBased.walkModeChoice;


import cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D;
import de.tum.bgu.msm.moped.data.MopedHousehold;
import de.tum.bgu.msm.moped.data.MopedPerson;
import de.tum.bgu.msm.moped.data.MopedTrip;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.util.js.JavaScriptCalculator;

import java.io.Reader;

public class ModeChoiceJSCalculator extends JavaScriptCalculator<Double> {

    public ModeChoiceJSCalculator(Reader reader) {
        super(reader);
    }

    public double calculateProbabilities(MopedHousehold household, MopedPerson person,MopedTrip trip){
        if (trip.getTripPurpose().equals(Purpose.HBW)){
            return super.calculate("calculateHBWProbabilities", household,person, trip);
        }else if(trip.getTripPurpose().equals(Purpose.HBE)) {
            return super.calculate("calculateHBEProbabilities", household, trip);
        }else{
            return super.calculate("calculateOTHERProbabilities", household, trip);
        }
    }
}
