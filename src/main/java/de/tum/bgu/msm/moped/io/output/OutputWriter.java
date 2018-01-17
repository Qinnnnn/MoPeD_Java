package de.tum.bgu.msm.moped.io.output;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.modules.Module;
import de.tum.bgu.msm.moped.modules.tripGeneration.*;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;

public class OutputWriter extends Module {

    private static final Logger logger = Logger.getLogger(OutputWriter.class);

    public OutputWriter(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void run(Purpose purpose) throws FileNotFoundException {
        logger.info("  Started write out.");
        long startTime = System.currentTimeMillis();

        switch (purpose) {
            case HBW:
                outputWriterHBWork();
                break;
            case HBSHOP:
                outputWriterHBShop();
                break;
            case HBREC:
                outputWriterHBRecreation();
                break;
            case HBOTH:
                outputWriterHBOther();
                break;
            case HBSCH:
                outputWriterHBSchool();
                break;
            case HBCOLL:
                outputWriterHBCollege();
                break;
        }

        logger.info("  Completed write out.");
    }

    private void outputWriterHBWork() throws FileNotFoundException {
        OutputWriterHBWork outputWriterHBWork = new OutputWriterHBWork(dataSet);
        outputWriterHBWork.run();
    }

    private void outputWriterHBShop() throws FileNotFoundException {
        OutputWriterHBShop outputWriterHBShop = new OutputWriterHBShop(dataSet);
        outputWriterHBShop.run();
    }

    private void outputWriterHBRecreation() throws FileNotFoundException {
        OutputWriterHBRecreation outputWriterHBRecreation = new OutputWriterHBRecreation(dataSet);
        outputWriterHBRecreation.run();
    }

    private void outputWriterHBOther() throws FileNotFoundException {
        OutputWriterHBOther outputWriterHBOther = new OutputWriterHBOther(dataSet);
        outputWriterHBOther.run();
    }

    private void outputWriterHBSchool() throws FileNotFoundException {
        OutputWriterHBSchool outputWriterHBSchool = new OutputWriterHBSchool(dataSet);
        outputWriterHBSchool.run();
    }

    private void outputWriterHBCollege() throws FileNotFoundException {
        OutputWriterHBcollege outputWriterHBCollege = new OutputWriterHBcollege(dataSet);
        outputWriterHBCollege.run();
    }



}
