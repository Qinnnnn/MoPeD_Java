package de.tum.bgu.msm.moped;

import de.tum.bgu.msm.moped.data.*;
import de.tum.bgu.msm.moped.io.input.InputManager;

import de.tum.bgu.msm.moped.io.input.readers.ZonesReader;
import de.tum.bgu.msm.moped.io.output.*;
import de.tum.bgu.msm.moped.modules.destinationChoice.TripDistribution;
import de.tum.bgu.msm.moped.modules.tripGeneration.TripGeneration;
import de.tum.bgu.msm.moped.modules.walkModeChoice.WalkModeChoice;
import de.tum.bgu.msm.moped.resources.Resources;
import de.tum.bgu.msm.moped.util.MoPeDUtil;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.util.Map;
import java.util.ResourceBundle;

public class MoPeDModel {

    private static final Logger logger = Logger.getLogger(MoPeDModel.class);

    private final InputManager manager;
    private final DataSet dataSet;

    public MoPeDModel(ResourceBundle resources) {
        this.dataSet = new DataSet();
        this.manager = new InputManager(dataSet);
        Resources.INSTANCE.setResources(resources);
    }

    public static MoPeDModel initializeModelFromMito(String propertiesFile) {
        logger.info("  Initializing MoPeD from MITO");
        ResourceBundle rb = MoPeDUtil.createResourceBundle(propertiesFile);
        MoPeDModel model = new MoPeDModel(rb);
        return model;
    }



    public void feedDataFromMITO(InputManager.InputFeed feed) {
        // Read data from MITO
        logger.info("  Reading input data for MoPeD");
        manager.readFromMITO(feed);
        manager.readAdditionalData();
    }

    public void runAgentBasedModel(){

        //TODO


    }


    public void initializeStandAlone() {
        // Read data if MoPeD is used as a stand-alone program and data are not fed from other program
        logger.info("  Reading input data for MoPeD");
        manager.readAsStandAlone();
    }

    public void runAggregatedModel(Purpose purpose) {
        long startTime = System.currentTimeMillis();
        logger.info("Started the Model of Pedestrian Demand (MoPeD)");

        TripGeneration tripGen = new TripGeneration(dataSet);
        tripGen.run(purpose);
        long generationTime = System.currentTimeMillis()- startTime;
        System.out.println(generationTime);

        long t1 = System.currentTimeMillis();
        WalkModeChoice walkMode = new WalkModeChoice(dataSet);
        walkMode.run(purpose);
        long modeChoiceTime = System.currentTimeMillis()- t1;
        System.out.println(modeChoiceTime);

        //manager.readAsStandAlone2();
        long t2 = System.currentTimeMillis();
        TripDistribution distribution = new TripDistribution(dataSet);
        distribution.run(purpose);
        long distributionTime = System.currentTimeMillis()- t2;
        System.out.println(distributionTime);

        long total = generationTime+modeChoiceTime+distributionTime;
        System.out.println("total:" + total);

        try {
            writeOut(purpose);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        printOutline(startTime);
    }

    private void printOutline(long startTime) {
        logger.info("Completed the Model of Pedestrian Demand (MoPeD)");
        float endTime = MoPeDUtil.rounder(((System.currentTimeMillis() - startTime) / 60000), 1);
        int hours = (int) (endTime / 60);
        int min = (int) (endTime - 60 * hours);
        logger.info("Runtime: " + hours + " hours and " + min + " minutes.");
    }

    private void writeOut(Purpose purpose)throws FileNotFoundException {
        OutputWriter writer = new OutputWriter(dataSet,purpose);
        writer.run();
    }

    public DataSet getDataSet() {
        return dataSet;
    }

    public InputManager getManager() {
        return manager;
    }
}
