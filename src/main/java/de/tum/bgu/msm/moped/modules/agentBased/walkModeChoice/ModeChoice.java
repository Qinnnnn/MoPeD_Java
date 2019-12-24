package de.tum.bgu.msm.moped.modules.agentBased.walkModeChoice;


import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.MopedTrip;
import de.tum.bgu.msm.moped.data.Purpose;
import org.apache.log4j.Logger;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Random;


public class ModeChoice {

    private final static Logger logger = Logger.getLogger(ModeChoice.class);
    private final DataSet dataSet;
    private final ModeChoiceJSCalculator calculator;
    private final Random rand;

    public ModeChoice(DataSet dataSet) {
        this.dataSet = dataSet;
        Reader reader = new InputStreamReader(this.getClass().getResourceAsStream("ModeChoiceAgent"));
        this.calculator = new ModeChoiceJSCalculator(reader);
        //this.travelDistance = dataSet.getImpedance();
        this.rand = new Random();
    }

    public void run() {
        logger.info(" Calculating walk mode choice probabilities for each trip.");
        int counter = 0;
        for (MopedTrip trip : dataSet.getTrips().values()) {
            if(trip.getTripPurpose().equals(Purpose.HBW)||trip.getTripPurpose().equals(Purpose.HBE)){
                if(trip.getTripOrigin()==null||trip.getTripDestination()==null){
                    //logger.warn("trip " + trip.getTripId() + ", purpose " + trip.getTripPurpose()+", has no home or occupation zone" + trip.getTripOrigin() + "," + trip.getTripDestination());
                    counter++;
                    continue;
                }else{
                    trip.setTripDistance(dataSet.getPAZImpedance().get(trip.getTripOrigin().getSuperPAZId(),trip.getTripDestination().getSuperPAZId()));
                }
            }


            chooseMode(trip, calculateTripProbabilities(trip));
        }
        logger.warn(counter + " HBW and HBE trips have been skipped since no origin or destination");

        logger.info("total walk trips: " + dataSet.getTrips().values().stream().filter(tt -> tt.isWalkMode()).count());
        logger.info("total trips: " + dataSet.getTrips().values().size());
        logger.info("total HBW walk trips: " + dataSet.getTrips().values().stream().filter(tt -> tt.isWalkMode()).filter(tt-> tt.getTripPurpose().equals(Purpose.HBW)).count());
        logger.info("total HBW trips: " + dataSet.getTrips().values().stream().filter(tt-> tt.getTripPurpose().equals(Purpose.HBW)).count());

    }


    private double calculateTripProbabilities(MopedTrip trip) {
        return calculator.calculateProbabilities(trip.getPerson().getMopedHousehold(), trip.getPerson(),trip);
    }

    private void chooseMode(MopedTrip trip, double probabilities) {
        double random = rand.nextDouble();
        trip.setWalkMode(random <= probabilities);
    }
}
