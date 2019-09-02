package de.tum.bgu.msm.moped.modules.agentBased.walkModeChoice;


import cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.MopedTrip;
import org.apache.log4j.Logger;
import java.io.InputStreamReader;
import java.util.Random;


public class ModeChoice {

    private final static Logger logger = Logger.getLogger(ModeChoice.class);
    private final DataSet dataSet;
    private final ModeChoiceJSCalculator calculator;
   // private final SparseFloatMatrix2D travelDistance;
    private final Random rand;

    public ModeChoice(DataSet dataSet) {
        this.dataSet = dataSet;
        this.calculator = new ModeChoiceJSCalculator(new InputStreamReader(this.getClass().getResourceAsStream("ModeChoice")));
        //this.travelDistance = dataSet.getImpedance();
        this.rand = new Random();
    }

    public void run() {
        logger.info(" Calculating walk mode choice probabilities for each trip.");
        for (MopedTrip trip : dataSet.getTrips().values()) {
            chooseMode(trip, calculateTripProbabilities(trip));
        }
    }


    private double calculateTripProbabilities(MopedTrip trip) {
        return calculator.calculateProbabilities(trip.getPerson().getMopedHousehold(), trip.getPerson(), trip);
    }

    private void chooseMode(MopedTrip trip, double probabilities) {
        double random = rand.nextDouble();
        trip.setWalkMode(random <= probabilities);
    }
}
