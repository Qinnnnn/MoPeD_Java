package de.tum.bgu.msm.moped.util;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.io.input.InputManager;

import org.apache.log4j.Logger;

public class MoPeDModel {

    private static final Logger logger = Logger.getLogger(MoPeDModel.class);
    private static String scenarioName;

    private final InputManager manager;
    private final DataSet dataSet;
    private boolean initialised = false;

    public MoPeDModel() {
        this.dataSet = new DataSet();
        this.manager = new InputManager(dataSet);
//        Resources.INSTANCE.setResources(resources);
//        MitoUtil.initializeRandomNumber();
    }

    public void initializeStandAlone() {
        // Read data if MoPeD is used as a stand-alone program and data are not fed from other program
        logger.info("  Reading input data for MoPeD");
//        manager.readAsStandAlone();
//        manager.readAdditionalData();
    }

}
