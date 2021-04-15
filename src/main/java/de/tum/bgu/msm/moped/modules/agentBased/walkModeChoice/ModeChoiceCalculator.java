package de.tum.bgu.msm.moped.modules.agentBased.walkModeChoice;

import de.tum.bgu.msm.moped.data.*;

public interface ModeChoiceCalculator {
    double calculateProbabilities(MopedHousehold household, MopedTrip trip);
}


