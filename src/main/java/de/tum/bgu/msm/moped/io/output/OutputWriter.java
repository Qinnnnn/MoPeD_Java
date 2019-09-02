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
    private double totalIntrazonal = 0.0;

    public OutputWriter(DataSet dataSet, Purpose purpose) {
        this.dataSet = dataSet;
        this.purpose = purpose;
    }

    public void run () throws FileNotFoundException {
            writeOut();
//            writeOutTG();
//            writeOutWM();
//            writeOutTripLength();
//            writeOutIntrazonalTrip();
            System.out.println(totalTrip + "," + totalWalk + "," + totalWalk/totalTrip);
            System.out.println(totalLenghth + "," + totalWalk + "," + totalLenghth/totalWalk);
            System.out.println(totalIntrazonal + "," + totalWalk + "," + totalIntrazonal/totalWalk);
    }


    public void writeOut () throws FileNotFoundException  {
        String outputTripGen = Resources.INSTANCE.getString(Properties.BASE) + Resources.INSTANCE.getString(Properties.OUTPUT_ALL);
        StringBuilder tripGen = new StringBuilder();

        //write header
        tripGen.append("zoneId,tripGen,walkTrips,walkShare,tripLength,averageTripLengthSuperPAZ,intrazonalTripShare,averageTripLengthPAZ");
        tripGen.append('\n');

        String testpath = Resources.INSTANCE.getString(Properties.BASE) + Resources.INSTANCE.getString(Properties.OUTPUT_OD);
        StringBuilder test = new StringBuilder();

        //write header
        test.append("origin,destination,trips,impedence");
        test.append('\n');

        //write data
        for (MopedZone zone : dataSet.getOriginPAZs().values()){

                float sumTripGen = 0.0f;
                float sumWalkTrip = 0.0f;
                double sumDistribution = 0.0;
                double sumDistributionPAZ = 0.0;

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

                for (SuperPAZ destination : dataSet.getDestinationSuperPAZs().values()){
                    double tripsNoCar = dataSet.getDistributionsNoCarByPurpose().get(purpose).get(zone.getIndex(),destination.getIndex());
                    double tripsHasCar = dataSet.getDistributionsHasCarByPurpose().get(purpose).get(zone.getIndex(),destination.getIndex());
                    float impedance = dataSet.getImpedance().get(zone.getSuperPAZId(),destination.getIndex());
                    if ( (impedance == 0.f)){
                        continue;
                    }

                    if(tripsNoCar == 0 && tripsHasCar == 0){
                        continue;
                    }

                    double distance = tripsNoCar * impedance + tripsHasCar * impedance;

                    test.append(zone.getZoneId());
                    test.append(',');
                    test.append(destination.getSuperPAZId());
                    test.append(',');
                    test.append(tripsNoCar+tripsHasCar);
                    test.append(',');
                    test.append(impedance);
                    test.append('\n');
                    sumDistribution += distance;
                }
                tripGen.append(',');
                tripGen.append(sumDistribution);
                tripGen.append(',');
                tripGen.append(sumDistribution/sumWalkTrip);

                totalLenghth += sumDistribution;


                for (SuperPAZ superPAZ : dataSet.getDestinationSuperPAZs().values()){
                    int originSuperPAZ = zone.getSuperPAZId();
                    if (originSuperPAZ == superPAZ.getSuperPAZId()){
                        float intrazonalNoCar = dataSet.getDistributionsNoCarByPurpose().get(purpose).get(zone.getIndex(),superPAZ.getIndex());
                        float intrazonalHasCar = dataSet.getDistributionsHasCarByPurpose().get(purpose).get(zone.getIndex(),superPAZ.getIndex());
                        float intrazonal = intrazonalNoCar+intrazonalHasCar;
                        tripGen.append(",");
                        tripGen.append(intrazonal/sumWalkTrip);
                        totalIntrazonal += intrazonal;
                        break;
                    }
                }

                tripGen.append('\n');

        }

        writeToFile(outputTripGen,tripGen.toString());
        writeToFile(testpath,test.toString());

    }

    public void writeOutTG () throws FileNotFoundException  {
        String outputTripGen = Resources.INSTANCE.getString(Properties.BASE) + Resources.INSTANCE.getString(Properties.OUTPUT_TG);
        StringBuilder tripGen = new StringBuilder();

        //write header
        tripGen.append("zoneId,tripGen");
        tripGen.append('\n');

        //write data
        for (MopedZone zone : dataSet.getOriginPAZs().values()){
            float sumTripGen = 0.0f;
            tripGen.append(zone.getZoneId());
            for (int hhTypeId : dataSet.getHhTypes().keySet()){
                float pr = dataSet.getProductionsByPurpose().get(purpose).get(zone.getIndex(),hhTypeId);
                sumTripGen += pr;
            }


            tripGen.append(',');
            tripGen.append(sumTripGen);
            tripGen.append('\n');
            totalTrip += sumTripGen;
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

        for (MopedZone zone : dataSet.getOriginPAZs().values()){
            float sumWalkTrip = 0.0f;
            walkMode.append(zone.getZoneId());
            for (int hhTypeId : dataSet.getHhTypes().keySet()){
                float walkTrip = dataSet.getWalkTripsByPurpose().get(purpose).get(zone.getIndex(),hhTypeId);
                sumWalkTrip += walkTrip;
            }
            walkMode.append(',');
            walkMode.append(sumWalkTrip);
            walkMode.append('\n');
            totalWalk += sumWalkTrip;
        }

        writeToFile(outputWalkTrip,walkMode.toString());

    }

//    public void writeOutTripLength () throws FileNotFoundException  {
//        String outputDistribution = Resources.INSTANCE.getString(Properties.BASE) + Resources.INSTANCE.getString(Properties.OUTPUT_TRIPLENGTH);
//        StringBuilder distribution = new StringBuilder();
//
//        //write header
//
//        distribution.append("zoneId,TripLength,WalkTrip,AverageTripLength");
//        distribution.append('\n');
//
//        //write data
//        for (MopedZone zone : dataSet.getOriginPAZs().values()){
//            int superPAZ = zone.getSuperPAZId();
//            double sumDistribution = 0.0;
//            double sumTrips = 0.0;
//            distribution.append(zone.getZoneId());
//            for (SuperPAZ destination : dataSet.getDestinationSuperPAZs().values()){
//                double trips = dataSet.getDistributionsByPurpose().get(purpose).get(zone.getIndex(),destination.getIndex());
//                float impedance = dataSet.getImpedance().get(zone.getSuperPAZId(),destination.getIndex());
//                if ( (impedance == 0.f) || (trips == 0.0)){
//                    continue;
//                }
//                double distance = trips * impedance;
//                sumDistribution += distance;
//                sumTrips += trips;
//            }
//
//            distribution.append(",");
//            distribution.append(sumDistribution);
//            distribution.append(",");
//            distribution.append(sumTrips);
//            distribution.append(",");
//            distribution.append(sumDistribution/sumTrips);
//            distribution.append('\n');
//            totalLenghth += sumDistribution;
//        }
//
//        writeToFile(outputDistribution,distribution.toString());
//
//    }



//    public void writeOutTripLength () throws FileNotFoundException  {
//        String outputDistribution = Resources.INSTANCE.getString(Properties.BASE) + Resources.INSTANCE.getString(Properties.OUTPUT_TRIPLENGTH);
//        StringBuilder distribution = new StringBuilder();
//
//        //write header
//
//        distribution.append("origin,destination,impedance,trips");
//        distribution.append('\n');
//
//        //write data
//        for (Zone zone : dataSet.getOriginPAZs().values()){
//            if(zone.getTest()==0){
//                continue;
//            }
//            for (SuperPAZ destination : dataSet.getDestinationSuperPAZs().values()){
//                double trips = dataSet.getDistributionsByPurpose().get(purpose).get(zone.getIndex(),destination.getIndex());
//                float impedance = dataSet.getImpedance().get(zone.getSuperPAZId(),destination.getSuperPAZId());
//                if ( (impedance == 0.f) || (trips == 0.0)){
//                    continue;
//                }
//                distribution.append(zone.getZoneId());
//                distribution.append(",");
//                distribution.append(destination.getSuperPAZId());
//                distribution.append(",");
//                distribution.append(impedance);
//                distribution.append(",");
//                distribution.append(trips);
//                distribution.append('\n');
//            }
//        }
//
//        writeToFile(outputDistribution,distribution.toString());
//
//    }

//    public void writeOutIntrazonalTrip () throws FileNotFoundException  {
//        String outputDistribution = Resources.INSTANCE.getString(Properties.BASE) + Resources.INSTANCE.getString(Properties.OUTPUT_INTRAZONAL);
//        StringBuilder distribution = new StringBuilder();
//
//        distribution.append("zoneId, InnerZoneTrip");
//        distribution.append('\n');
//
//        for (MopedZone zone : dataSet.getOriginPAZs().values()){
//            distribution.append(zone.getZoneId());
//            for (SuperPAZ superPAZ : dataSet.getDestinationSuperPAZs().values()){
//                int originSuperPAZ = zone.getSuperPAZId();
//                if (originSuperPAZ == superPAZ.getSuperPAZId()){
//                    float intrazonal = dataSet.getDistributionsByPurpose().get(purpose).get(zone.getIndex(),superPAZ.getIndex());
//                    distribution.append(",");
//                    distribution.append(intrazonal);
//                    totalIntrazonal += intrazonal;
//                    break;
//                }
//            }
//            distribution.append('\n');
//        }
//
//        writeToFile(outputDistribution,distribution.toString());
//
//    }


    public static void writeToFile(String path, String building) throws FileNotFoundException {
        PrintWriter bd = new PrintWriter(new FileOutputStream(path, true));
        bd.write(building);
        bd.close();
    }



}
