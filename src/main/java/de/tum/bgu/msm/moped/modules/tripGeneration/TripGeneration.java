package de.tum.bgu.msm.moped.modules.tripGeneration;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.modules.Module;
import org.apache.log4j.Logger;

public class TripGeneration extends Module {

    private static final Logger logger = Logger.getLogger(TripGeneration.class);

    public TripGeneration(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void run(Purpose purpose)  {
        logger.info("  Started trip generation model.");
        switch (purpose) {
            case HBW:
                hbWorkGenerator();
                break;
//            case HBSHOP:
//                hbShopGenerator();
//                break;
//            case HBREC:
//                hbRecreationGenerator();
//                break;
//            case HBOTH:
//                hbOtherGenerator();
//                break;
//            case HBSCH:
//                hbSchoolGenerator();
//                break;
//            case HBCOLL:
//                hbCollegeGenerator();
//                break;
//            case NHBW:
//                nhbWorkGenerator();
//                break;
//            case NHBNW:
//                nhbnWorkGenerator();
//                break;
        }
        logger.info("  Completed trip generation model.");
    }

    private void nhbnWorkGenerator() {
        NHBNWorkGenerator nhbnWorkGenerator = new NHBNWorkGenerator(dataSet);
        nhbnWorkGenerator.run();
    }

    private void nhbWorkGenerator() {
        NHBWorkGenerator nhbWorkGenerator = new NHBWorkGenerator(dataSet);
        nhbWorkGenerator.run();
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
