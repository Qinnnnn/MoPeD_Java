package de.tum.bgu.msm.moped.modules.tripAssignment;

import de.tum.bgu.msm.moped.MoPeDModel;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.io.input.readers.ZonesReader;
import de.tum.bgu.msm.moped.util.MoPeDUtil;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;
import java.util.ResourceBundle;

public class TripAssignmentTest {
    private static Logger logger = Logger.getLogger(TripAssignmentTest.class);

    public static void main(String[] args) {
        ResourceBundle rb = MoPeDUtil.createResourceBundle(args[0]);
        MoPeDModel model = new MoPeDModel(rb);
        DataSet dataSet = model.getDataSet();
        logger.info("  Reading input data for MoPeD");
        new ZonesReader(dataSet).read();
        logger.info(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );

        PedestrianFlow calculator=  new PedestrianFlow(dataSet);
        Map<String,Double> linkVolume = calculator.getPedestrianFlow(args[1],args[2]);


        StringBuilder link = new StringBuilder();
        link.append("linkId,volume");
        link.append('\n');

        for(String linkId: linkVolume.keySet()){
            link.append(linkId);
            link.append(',');
            link.append(linkVolume.get(linkId));
            link.append('\n');
        }

        try {
            PrintWriter bd = new PrintWriter(new FileOutputStream(args[3], true));
            bd.write(link.toString());
            bd.close();
            System.out.println("Write out is done!");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
