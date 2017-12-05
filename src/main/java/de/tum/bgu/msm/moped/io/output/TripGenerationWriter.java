package de.tum.bgu.msm.moped.io.output;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class TripGenerationWriter {

    private static final Logger logger = Logger.getLogger(TripGenerationWriter.class);

    public void writeOut (DataSet dataSet) throws FileNotFoundException  {
        String outputPath = Resources.INSTANCE.getString(Properties.BASE) + Resources.INSTANCE.getString(Properties.OUTPUTPATH);
        StringBuilder record = new StringBuilder();

        //write header
        record.append("zoneId");
        for (int hhTypeId : dataSet.getHhTypes().keySet()){
            record.append(",");
            record.append(dataSet.getHouseholdType(hhTypeId).getHhTypeName());
        }
        record.append('\n');

        //write data
        for (int zoneId : dataSet.getZones().keySet()){
            record.append(zoneId);
            for (int hhTypeId : dataSet.getHhTypes().keySet()){
                double pr = dataSet.getHbWorkProduction().get(zoneId,hhTypeId);
                record.append(",");
                record.append(pr);
            }
            record.append('\n');
        }

        writeToFile(outputPath,record.toString());

    }


    public static void writeToFile(String path, String building) throws FileNotFoundException {
        //String newLine = System.getProperty("line.separator");
        PrintWriter bd = new PrintWriter(new FileOutputStream(path, true));
        bd.write(building);
        bd.close();

    }



}
