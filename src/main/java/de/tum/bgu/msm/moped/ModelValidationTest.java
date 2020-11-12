package de.tum.bgu.msm.moped;

import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.modules.destinationChoice.HBShopDistributor;
import de.tum.bgu.msm.moped.modules.destinationChoice.HBShopDistributor_Validation;
import de.tum.bgu.msm.moped.modules.destinationChoice.TripDistribution;
import de.tum.bgu.msm.moped.modules.destinationChoice.TripDistributor_Validation;
import de.tum.bgu.msm.moped.util.MoPeDUtil;
import org.apache.log4j.Logger;

import java.util.ResourceBundle;

class ModelValidationTest {

    private static final Logger logger = Logger.getLogger(ModelValidationTest.class);
    private static String scenarioName;

    public static void main(String[] args) {
        // main run method
        Purpose purpose = Purpose.HBSHOP;
        ModelValidationTest test = new ModelValidationTest();
        ResourceBundle rb = MoPeDUtil.createResourceBundle(args[0]);
        MoPeDUtil.setBaseDirectory(rb.getString("base.directory"));
        test.run(rb,purpose);
    }

    private void run (ResourceBundle resources, Purpose purpose) {
        // main run method
        logger.info("Started the Model of Pedestrian Demand (MoPeD)");
        MoPeDModel model = new MoPeDModel(resources);
        long t1 = System.currentTimeMillis();
        HBShopDistributor_Validation validation = new HBShopDistributor_Validation(model.getDataSet());
        validation.run();
        long validationTime = System.currentTimeMillis()- t1;
        logger.info("Destination choice validation run time:" + validationTime);

    }

}
