package de.tum.bgu.msm.moped.modules.walkModeChoice;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.io.output.TripGenerationWriter;
import de.tum.bgu.msm.moped.modules.Module;
import de.tum.bgu.msm.moped.modules.tripGeneration.*;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;

public class WalkModeChoice extends Module {

    private static final Logger logger = Logger.getLogger(WalkModeChoice.class);

    public WalkModeChoice(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void run()  {
        logger.info("  Started walk mode choice model.");
        hbWorkWalk();
        hbShopWalk();

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

    private void writeOut()throws FileNotFoundException {
        TripGenerationWriter writer = new TripGenerationWriter();
        writer.writeOut(dataSet);
    }

}
