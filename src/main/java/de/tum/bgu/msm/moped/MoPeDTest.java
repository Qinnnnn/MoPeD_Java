package de.tum.bgu.msm.moped;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.io.input.InputManager;
import de.tum.bgu.msm.moped.modules.tripGeneration.TripGeneration;
import de.tum.bgu.msm.moped.util.MoPeDUtil;
import org.apache.log4j.Logger;

import java.util.ResourceBundle;

class MoPeDTest {

    private static final Logger logger = Logger.getLogger(MoPeDTest.class);
    private static String scenarioName;

    public static void main(String[] args) {
        // main run method
        MoPeDTest test = new MoPeDTest();
        ResourceBundle rb = MoPeDUtil.createResourceBundle(args[0]);
        MoPeDUtil.setBaseDirectory(rb.getString("base.directory"));
        test.run(rb);
    }

    private void run (ResourceBundle resources) {
        // main run method
        logger.info("Started the Model of Pedestrian Demand (MoPeD)");
        MoPeDModel model = new MoPeDModel(resources);
        model.initializeStandAlone();
        model.runModel();
    }
}
