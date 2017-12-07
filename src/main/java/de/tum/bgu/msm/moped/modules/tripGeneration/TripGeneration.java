package de.tum.bgu.msm.moped.modules.tripGeneration;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.modules.Module;
import org.apache.log4j.Logger;

public class TripGeneration extends Module {

    private static final Logger logger = Logger.getLogger(TripGeneration.class);

    public TripGeneration(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void run()  {
        logger.info("  Started trip generation model.");
        long startTime = System.currentTimeMillis();
        hbWorkGenerator();
//        System.out.println(System.currentTimeMillis() - startTime);
//        hbShopGenerator();
//        hbRecreationGenerator();
//        hbSchoolGenerator();
//        hbCollegeGenerator();
//        hbOtherGenerator();
        logger.info("  Completed trip generation model.");
    }

    private void hbOtherGenerator() {
        HBOtherGenerator hbOtherGenerator = new HBOtherGenerator(dataSet);
        hbOtherGenerator.run();
    }

    private void hbCollegeGenerator() {
        HBCollegeGenerator hbCollegeGenerator = new HBCollegeGenerator(dataSet);
        hbCollegeGenerator.run();
    }

    private void hbSchoolGenerator() {
        HBSchoolGenerator hbSchoolGenerator = new HBSchoolGenerator(dataSet);
        hbSchoolGenerator.run();
    }

    private void hbRecreationGenerator() {
        HBRecreationGenerator hbRecreationGenerator = new HBRecreationGenerator(dataSet);
        hbRecreationGenerator.run();
    }

    private void hbWorkGenerator() {
        HBWorkGenerator hbWorkGenerator = new HBWorkGenerator(dataSet);
        hbWorkGenerator.run();
    }

    private void hbShopGenerator() {
        HBShopGenerator hbShopGenerator = new HBShopGenerator(dataSet);
        hbShopGenerator.run();
    }


}
