package de.tum.bgu.msm.moped.io.input;

import cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.MopedHousehold;
import de.tum.bgu.msm.moped.data.MopedPerson;
import de.tum.bgu.msm.moped.data.MopedTrip;
import de.tum.bgu.msm.moped.io.input.readers.*;
import org.apache.log4j.Logger;
import org.opengis.feature.simple.SimpleFeature;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Map;

public class InputManager {
    private static final Logger logger = Logger.getLogger(InputManager.class);

    private final DataSet dataSet;

    public InputManager(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public void readAsStandAlone() {
        new ZonesReader(dataSet).read();
        logger.info(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
        new ZoneAttributesReader(dataSet).read();
        logger.info(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
        new HouseholdTypeReader(dataSet).read();
        logger.info(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
        new HouseholdTypeDistributionReader(dataSet).read();
        logger.info(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
        new PIEReader(dataSet).read();
        logger.info(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
        new TransportReader(dataSet).read();
        logger.info(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
        new SuperPAZAttributesReader(dataSet).read();
        logger.info(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
    }



    public void readFromMITO(InputFeed feed) {
        setHouseholdsFromFeed(feed.households);
        dataSet.setYear(feed.year);
    }

    private void setHouseholdsFromFeed(Map<Integer, MopedHousehold> households) {
        for (MopedHousehold household : households.values()) {
            if(household.getHomeZone()==null){
                logger.info("household " +household.getId()+ " is outside study area");
                continue;
            }
            if (!dataSet.getZones().containsKey(household.getHomeZone().getZoneId())) {
                throw new RuntimeException("Feed household " + household.getId() + " refers to non-existing home zone "
                        + household.getHomeZone());
            }
            dataSet.addHousehold(household);
            for (MopedPerson person : household.getPersons().values()) {
                dataSet.addPerson(person);
                for (MopedTrip trip : person.getTrips()){
                    dataSet.addTrip(trip);
                }
            }
        }
    }

    public final static class InputFeed {

        private final Map<Integer, MopedHousehold> households;
        private final int year;

        public InputFeed(Map<Integer, MopedHousehold> households, int year) {
            this.households = households;
            this.year = year;
        }
    }

    //TODO:read and set zone features
    public void readZoneData() {
        new ZonesReader(dataSet).read();
        //System.out.println(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
    }

    public void readDistanceData() {
        new DistanceOMXReader(dataSet).read();
        //System.out.println(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
    }



}
