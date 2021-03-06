package de.tum.bgu.msm.moped.io.output;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.data.SuperPAZ;
import de.tum.bgu.msm.moped.data.Zone;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import org.apache.commons.math3.linear.RealMatrix;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class OutputWriter {

    private final DataSet dataSet;
    private final Purpose purpose;

    public OutputWriter(DataSet dataSet, Purpose purpose) {
        this.dataSet = dataSet;
        this.purpose = purpose;
    }

    public void run () throws FileNotFoundException {
        if (purpose.equals(Purpose.HBSCH)||purpose.equals(Purpose.HBCOLL)){
            writeOutTG();
            writeOutWM();
        }else{
            writeOutTG();
            writeOutWM();
            writeOutTripLength();
            writeOutIntrazonalTrip();
        }
    }


    public void writeOutTG () throws FileNotFoundException  {
        String outputTripGen = Resources.INSTANCE.getString(Properties.BASE) + Resources.INSTANCE.getString(Properties.OUTPUT_TG);
        StringBuilder tripGen = new StringBuilder();

        //write header
        tripGen.append("zoneId,tripGen");
        tripGen.append('\n');

        //write data
        for (Zone zone : dataSet.getOriginPAZs().values()){
            float sumTripGen = 0.0f;
            tripGen.append(zone.getZoneId());
            for (int hhTypeId : dataSet.getHhTypes().keySet()){
                float pr = dataSet.getProductionsByPurpose().get(purpose).get(zone.getIndex(),hhTypeId);
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

        for (Zone zone : dataSet.getOriginPAZs().values()){
            float sumWalkTrip = 0.0f;
            walkMode.append(zone.getZoneId());
            for (int hhTypeId : dataSet.getHhTypes().keySet()){
                float walkTrip = dataSet.getWalkTripsByPurpose().get(purpose).get(zone.getIndex(),hhTypeId);
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
        for (Zone zone : dataSet.getOriginPAZs().values()){
            int superPAZ = zone.getSuperPAZId();
            double sumDistribution = 0.0;
            double sumTrips = 0.0;
            distribution.append(zone.getZoneId());
            for (int destination : dataSet.getDestinationSuperPAZs().keySet()){
                double trips = dataSet.getDistributionsByPurpose().get(purpose).get(zone.getIndex(),destination);
                if (trips != 0.0){
                    double impedance = dataSet.getImpedance().get(superPAZ, destination);
                    double distance = trips * impedance;
                    sumDistribution += distance;
                    sumTrips += trips;
                }
            }

            distribution.append(",");
            distribution.append(sumDistribution);
            distribution.append(",");
            distribution.append(sumTrips);
            distribution.append('\n');
        }

        writeToFile(outputDistribution,distribution.toString());

    }

    public void writeOutIntrazonalTrip () throws FileNotFoundException  {
        String outputDistribution = Resources.INSTANCE.getString(Properties.BASE) + Resources.INSTANCE.getString(Properties.OUTPUT_INTRAZONAL);
        StringBuilder distribution = new StringBuilder();

        distribution.append("zoneId, InnerZoneTrip");
        distribution.append('\n');

        for (Zone zone : dataSet.getOriginPAZs().values()){
            distribution.append(zone.getZoneId());
            for (SuperPAZ superPAZ : dataSet.getDestinationSuperPAZs().values()){
                int originSuperPAZ = zone.getSuperPAZId();
                if (originSuperPAZ == superPAZ.getSuperPAZId()){
                    distribution.append(",");
                    distribution.append(dataSet.getDistributionsByPurpose().get(purpose).get(zone.getIndex(),superPAZ.getIndex()));
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
