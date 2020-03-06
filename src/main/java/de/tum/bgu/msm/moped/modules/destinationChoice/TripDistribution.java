package de.tum.bgu.msm.moped.modules.destinationChoice;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.modules.Module;
import de.tum.bgu.msm.moped.modules.tripGeneration.NHBWorkGenerator;
import org.apache.log4j.Logger;

public class TripDistribution extends Module {

    private static final Logger logger = Logger.getLogger(TripDistribution.class);

    public TripDistribution(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void run(Purpose purpose)  {
        logger.info("  Started trip distribution model.");
        long startTime = System.currentTimeMillis();

        switch (purpose) {
            case HBW:
                hbWorkDistributor();
                break;
            case HBSHOP:
                hbShopDistributor();
                break;
            case HBREC:
                hbRecreationDistributor();
                break;
            case HBOTH:
                hbOtherDistributor();
                break;
            case NHBW:
                nhbWorkDistributor();
                break;
            case NHBNW:
                nhbOtherDistributor();
                break;
        }
        logger.info("  Completed trip distribution model.");
    }

    private void nhbOtherDistributor() {
        NHBOtherDistributor nhbOtherDistributor = new NHBOtherDistributor(dataSet);
        nhbOtherDistributor.run();
    }

    private void nhbWorkDistributor() {
        NHBWorkDistributor nhbWorkDistributor = new NHBWorkDistributor(dataSet);
        nhbWorkDistributor.run();
    }

    private void hbOtherDistributor() {
        HBOtherDistributor hbOtherDistributor = new HBOtherDistributor(dataSet);
        hbOtherDistributor.run();
    }

    private void hbRecreationDistributor() {
        HBRecreationDistributor hbRecreationDistributor = new HBRecreationDistributor(dataSet);
        hbRecreationDistributor.run();
    }

    private void hbWorkDistributor() {
        HBWorkDistributor hbWorkDistributor = new HBWorkDistributor(dataSet);
        hbWorkDistributor.run();
    }

    private void hbShopDistributor() {
        HBShopDistributor hbShopDistributor = new HBShopDistributor(dataSet);
        hbShopDistributor.run();
    }


}
