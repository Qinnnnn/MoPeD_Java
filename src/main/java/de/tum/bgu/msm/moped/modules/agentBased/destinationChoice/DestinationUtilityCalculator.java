package de.tum.bgu.msm.moped.modules.agentBased.destinationChoice;

import de.tum.bgu.msm.moped.data.MopedZone;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.data.SuperPAZ;

public interface DestinationUtilityCalculator {
    float calculateUtility(Purpose purpose, SuperPAZ destination, double travelDistance, int crossMotorway);

    float calculateUtility(Purpose purpose, MopedZone destination, double travelDistance, int originPAZ);

}


