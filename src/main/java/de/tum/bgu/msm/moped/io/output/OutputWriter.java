package de.tum.bgu.msm.moped.io.output;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.data.SuperPAZ;
import de.tum.bgu.msm.moped.data.MopedZone;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class OutputWriter {

    private final DataSet dataSet;
    private final Purpose purpose;
    private double totalTrip =0.0;
    private double totalWalk =0.0;
    private double totalLenghth = 0.0;

    public OutputWriter(DataSet dataSet, Purpose purpose) {
        this.dataSet = dataSet;
        this.purpose = purpose;
    }

    public void run () throws FileNotFoundException {
            writeOut();
            System.out.println(totalTrip + "," + totalWalk + "," + totalWalk/totalTrip);
            System.out.println(totalLenghth + "," + totalWalk + "," + totalLenghth/totalWalk);
    }


    public void writeOut () throws FileNotFoundException  {
        String outputTripGen = Resources.INSTANCE.getString(Properties.BASE) + Resources.INSTANCE.getString(Properties.OUTPUT_ALL)+"_"+purpose.toString()+".csv";
        StringBuilder tripGen = new StringBuilder();

        //write header
        tripGen.append("zoneId,tripGen,walkTrips,walkShare");
        tripGen.append('\n');

        //write data
        for (MopedZone zone : dataSet.getOriginPAZs().values()){

                float sumTripGen = 0.0f;
                float sumWalkTrip = 0.0f;

                tripGen.append(zone.getZoneId());

                for (int hhTypeId : dataSet.getHhTypes().keySet()) {
                    float pr = dataSet.getProductionsByPurpose().get(purpose).get(zone.getIndex(), hhTypeId);
                    sumTripGen += pr;
                    float walkTrip = dataSet.getWalkTripsByPurpose().get(purpose).get(zone.getIndex(),hhTypeId);
                    sumWalkTrip += walkTrip;
                }

                tripGen.append(',');
                tripGen.append(sumTripGen);
                tripGen.append(',');
                tripGen.append(sumWalkTrip);
                tripGen.append(',');
                tripGen.append(sumWalkTrip/sumTripGen);

                totalTrip += sumTripGen;
                totalWalk += sumWalkTrip;

                tripGen.append('\n');

        }
        writeToFile(outputTripGen,tripGen.toString());
    }

    public static void writeToFile(String path, String building) throws FileNotFoundException {
        PrintWriter bd = new PrintWriter(new FileOutputStream(path, true));
        bd.write(building);
        bd.close();
    }



}
