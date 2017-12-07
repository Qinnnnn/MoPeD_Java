package de.tum.bgu.msm.moped.io.output;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class OutputWriter {

    private static final Logger logger = Logger.getLogger(OutputWriter.class);

    public void writeOut (DataSet dataSet) throws FileNotFoundException  {
        String outputTripGen = Resources.INSTANCE.getString(Properties.BASE) + Resources.INSTANCE.getString(Properties.OUTPUT_TG);
        String outputWalkTrip = Resources.INSTANCE.getString(Properties.BASE) + Resources.INSTANCE.getString(Properties.OUTPUT_WM);
        StringBuilder tripGen = new StringBuilder();
        StringBuilder walkMode = new StringBuilder();

        //write header
        tripGen.append("zoneId,tripGen");
        tripGen.append('\n');
        walkMode.append("zoneId,walkMode");
        walkMode.append('\n');

        //write data

        for (int zoneId : dataSet.getZones().keySet()){
            double sumTripGen = 0.0;
            double sumWalkTrip = 0.0;
            tripGen.append(zoneId);
            walkMode.append(zoneId);
            for (int hhTypeId : dataSet.getHhTypes().keySet()){
                double pr = dataSet.getHbWorkProduction().get(zoneId,hhTypeId);
                sumTripGen += pr;
                double walkTrip = dataSet.getHbWorkWalk().get(zoneId,hhTypeId);
                sumWalkTrip += walkTrip;
            }
            tripGen.append(',');
            tripGen.append(sumTripGen);
            tripGen.append('\n');
            walkMode.append(',');
            walkMode.append(sumWalkTrip);
            walkMode.append('\n');
        }

        writeToFile(outputTripGen,tripGen.toString());
        writeToFile(outputWalkTrip,walkMode.toString());

    }


    public static void writeToFile(String path, String building) throws FileNotFoundException {
        //String newLine = System.getProperty("line.separator");
        PrintWriter bd = new PrintWriter(new FileOutputStream(path, true));
        bd.write(building);
        bd.close();

    }



}
