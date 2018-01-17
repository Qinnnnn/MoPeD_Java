package de.tum.bgu.msm.moped.io.output;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class OutputWriterHBWork {

    private static final Logger logger = Logger.getLogger(OutputWriterHBWork.class);
    private final DataSet dataSet;

    public OutputWriterHBWork(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public void run () throws FileNotFoundException {
        writeOutTG();
        writeOutWM();
        writeOutTripLength();
        writeOutIntrazonalTrip();
    }


    public void writeOutTG () throws FileNotFoundException  {
        String outputTripGen = Resources.INSTANCE.getString(Properties.BASE) + Resources.INSTANCE.getString(Properties.OUTPUT_TG);
        StringBuilder tripGen = new StringBuilder();

        //write header
        tripGen.append("zoneId,tripGen");
        tripGen.append('\n');

        //write data

        for (long zoneId : dataSet.getZones().keySet()){
            double sumTripGen = 0.0;
            tripGen.append(zoneId);
            for (int hhTypeId : dataSet.getHhTypes().keySet()){
                double pr = dataSet.getHbWorkProduction().get(zoneId,hhTypeId);
                sumTripGen += pr;
            }
            tripGen.append(',');
            tripGen.append(sumTripGen);
            tripGen.append('\n');
        }

        writeToFile(outputTripGen,tripGen.toString());

    }

    public void writeOutWM () throws FileNotFoundException  {
        String outputWalkTrip = Resources.INSTANCE.getString(Properties.BASE) + Resources.INSTANCE.getString(Properties.OUTPUT_WM);
        StringBuilder walkMode = new StringBuilder();

        //write header
        walkMode.append("zoneId,walkMode");
        walkMode.append('\n');

        //write data

        for (long zoneId : dataSet.getZones().keySet()){
            double sumWalkTrip = 0.0;
            walkMode.append(zoneId);
            for (int hhTypeId : dataSet.getHhTypes().keySet()){
                double walkTrip = dataSet.getHbWorkWalk().get(zoneId,hhTypeId);
                sumWalkTrip += walkTrip;
            }
            walkMode.append(',');
            walkMode.append(sumWalkTrip);
            walkMode.append('\n');
        }

        writeToFile(outputWalkTrip,walkMode.toString());

    }

    public void writeOutTripLength () throws FileNotFoundException  {
        String outputDistribution = Resources.INSTANCE.getString(Properties.BASE) + Resources.INSTANCE.getString(Properties.OUTPUT_TRIPLENGTH);
        StringBuilder distribution = new StringBuilder();

        //write header

        distribution.append("zoneId,TripLength");
        distribution.append('\n');

        //write data
        for (long origin : dataSet.getHbWorkDistribution().rowKeySet()){
            long superPAZ = dataSet.getZone(origin).getSuperPAZId();
            double sumDistribution = 0.0;
            distribution.append(origin);
            for (long destination : dataSet.getHbWorkDistribution().columnKeySet()){
                double trips = dataSet.getHbWorkDistribution().get(origin,destination);
                if (trips != 0.0){
                    if (dataSet.getImpedance().get(superPAZ,destination) == null){
                        System.out.println("error");
                        System.out.println(superPAZ);
                        System.out.println(destination);
                    }
                    double impedance = dataSet.getImpedance().get(superPAZ,destination);
                    double distance = trips * impedance;
                    sumDistribution += distance;
                }
            }
            distribution.append(",");
            distribution.append(sumDistribution);
            distribution.append('\n');
        }

        writeToFile(outputDistribution,distribution.toString());

    }

    public void writeOutIntrazonalTrip () throws FileNotFoundException  {
        String outputDistribution = Resources.INSTANCE.getString(Properties.BASE) + Resources.INSTANCE.getString(Properties.OUTPUT_INTRAZONAL);
        StringBuilder distribution = new StringBuilder();

        distribution.append("zoneId, InnerZoneTrip");
        distribution.append('\n');

        for (long origin : dataSet.getHbWorkDistribution().rowKeySet()){
            distribution.append(origin);
            for (long destination : dataSet.getHbWorkDistribution().columnKeySet()){
                long superPAZ = dataSet.getZone(origin).getSuperPAZId();
                if (superPAZ == destination){
                    distribution.append(",");
                    distribution.append(dataSet.getHbWorkDistribution().get(origin,destination));
                }
            }
            distribution.append('\n');
        }


        writeToFile(outputDistribution,distribution.toString());

    }


    public static void writeToFile(String path, String building) throws FileNotFoundException {
        PrintWriter bd = new PrintWriter(new FileOutputStream(path, true));
        bd.write(building);
        bd.close();
    }



}
