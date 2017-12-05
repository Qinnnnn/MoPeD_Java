package de.tum.bgu.msm.moped.io.input;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.io.input.readers.*;
import org.apache.log4j.Logger;

public class InputManager {
    private static final Logger logger = Logger.getLogger(InputManager.class);

    private final DataSet dataSet;

    public InputManager(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public void readAsStandAlone() {
        new ZonesReader(dataSet).read();
        new ZoneAttributesReader(dataSet).read();
        new HouseholdTypeReader(dataSet).read();
        new HouseholdTypeDistributionReader(dataSet).read();
        new PIEReader(dataSet).read();
        new TransportReader(dataSet).read();
    }


}
