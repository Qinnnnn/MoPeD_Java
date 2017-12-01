package de.tum.bgu.msm.moped.modules.tripGeneration;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.io.output.TripGenerationWriter;
import de.tum.bgu.msm.moped.modules.Module;
import org.apache.log4j.Logger;

import java.util.Collection;

public class TripGeneration extends Module {

    private static final Logger logger = Logger.getLogger(TripGeneration.class);

    public TripGeneration(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void run() {
        logger.info("  Started trip generation model.");
        hbWorkGenerator();
        hbShopGenerator();
        hbRecreationGenerator();
        hbSchoolGenerator();
        hbCollegeGenerator();
        hbOtherGenerator();
        writeOut();
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

    private void writeOut() {
        TripGenerationWriter writer = new TripGenerationWriter();
        writer.writeOut(dataSet);
    }

}
