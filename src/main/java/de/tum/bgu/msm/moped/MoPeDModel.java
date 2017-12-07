package de.tum.bgu.msm.moped;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.io.input.InputManager;

import de.tum.bgu.msm.moped.io.output.OutputWriter;
import de.tum.bgu.msm.moped.modules.tripGeneration.TripGeneration;
import de.tum.bgu.msm.moped.modules.walkModeChoice.WalkModeChoice;
import de.tum.bgu.msm.moped.resources.Resources;
import de.tum.bgu.msm.moped.util.MoPeDUtil;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.util.ResourceBundle;

public class MoPeDModel {

    private static final Logger logger = Logger.getLogger(MoPeDModel.class);
    private static String scenarioName;

    private final InputManager manager;
    private final DataSet dataSet;
    private boolean initialised = false;

    public MoPeDModel(ResourceBundle resources) {
        this.dataSet = new DataSet();
        this.manager = new InputManager(dataSet);
        Resources.INSTANCE.setResources(resources);

    }

    public void initializeStandAlone() {
        // Read data if MoPeD is used as a stand-alone program and data are not fed from other program
        logger.info("  Reading input data for MoPeD");
        manager.readAsStandAlone();
    }

    public void runModel() {
        long startTime = System.currentTimeMillis();
        logger.info("Started the Model of Pedestrian Demand (MoPeD)");
        TripGeneration tripGen = new TripGeneration(dataSet);
        tripGen.run();
        long generationTime = System.currentTimeMillis()-startTime;
        System.out.println(generationTime);
        WalkModeChoice walkMode = new WalkModeChoice(dataSet);
        walkMode.run();
        long modeChoiceTime = System.currentTimeMillis()-generationTime;
        System.out.println(modeChoiceTime);
        try {
            writeOut();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        printOutline(startTime);
    }

    private void printOutline(long startTime) {
//        String trips = MitoUtil.customFormat("  " + "###,###", dataSet.getTrips().size());
//        logger.info("A total of " + trips.trim() + " microscopic trips were generated");
        logger.info("Completed the Model of Pedestrian Demand (MoPeD)");
        float endTime = MoPeDUtil.rounder(((System.currentTimeMillis() - startTime) / 60000), 1);
        int hours = (int) (endTime / 60);
        int min = (int) (endTime - 60 * hours);
        logger.info("Runtime: " + hours + " hours and " + min + " minutes.");
    }

    private void writeOut()throws FileNotFoundException {
        OutputWriter writer = new OutputWriter();
        writer.writeOut(dataSet);
    }
}
