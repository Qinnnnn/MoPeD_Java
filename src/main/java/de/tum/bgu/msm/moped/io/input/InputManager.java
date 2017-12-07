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
        System.out.println("1");
        new ZoneAttributesReader(dataSet).read();
        System.out.println("2");
        new HouseholdTypeReader(dataSet).read();
        System.out.println("3");
        new HouseholdTypeDistributionReader(dataSet).read();
        System.out.println("4");
        new PIEReader(dataSet).read();
        System.out.println("5");
        new TransportReader(dataSet).read();
        System.out.println("6");
    }


}
