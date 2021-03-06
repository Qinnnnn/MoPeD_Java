package de.tum.bgu.msm.moped.modules.walkModeChoice;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.modules.Module;
import org.apache.log4j.Logger;

public class WalkModeChoice extends Module {

    private static final Logger logger = Logger.getLogger(WalkModeChoice.class);

    public WalkModeChoice(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void run(Purpose purpose)  {
        logger.info("  Started walk mode choice model.");

        switch (purpose) {
            case HBW:
                hbWorkWalk();
                break;
            case HBSHOP:
                hbShopWalk();
                break;
            case HBREC:
                hbRecreationWalk();
                break;
            case HBOTH:
                hbOtherWalk();
                break;
            case HBSCH:
                hbSchoolWalk();
                break;
            case HBCOLL:
                hbCollegeWalk();
                break;
        }
        logger.info("  Completed walk mode choice model.");
    }

    private void hbWorkWalk() {
        HBWorkWalk hbWorkWalk = new HBWorkWalk(dataSet);
        hbWorkWalk.run();
    }

    private void hbShopWalk() {
        HBShopWalk hbShopWalk = new HBShopWalk(dataSet);
        hbShopWalk.run();
    }

    private void hbRecreationWalk() {
        HBRecreationWalk hbRecreationWalk = new HBRecreationWalk(dataSet);
        hbRecreationWalk.run();
    }

    private void hbOtherWalk() {
        HBOtherWalk hbOtherWalk = new HBOtherWalk(dataSet);
        hbOtherWalk.run();
    }

    private void hbSchoolWalk() {
        HBSchoolWalk hbSchoolWalk = new HBSchoolWalk(dataSet);
        hbSchoolWalk.run();
    }

    private void hbCollegeWalk() {
        HBCollegeWalk hbCollegeWalk = new HBCollegeWalk(dataSet);
        hbCollegeWalk.run();
    }



}
