package de.tum.bgu.msm.moped.io.input;

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
        System.out.println(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
        new ZoneAttributesReader(dataSet).read();
        System.out.println(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
        new HouseholdTypeReader(dataSet).read();
        System.out.println(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
        new HouseholdTypeDistributionReader(dataSet).read();
        System.out.println(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
        new PIEReader(dataSet).read();
        System.out.println(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
        new TransportReader(dataSet).read();
        System.out.println(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
        new SuperPAZAttributesReader(dataSet).read();
        System.out.println(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
        new DistanceOMXReader(dataSet).read();
        System.out.println(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
    }



    public void readFromMITO(InputFeed feed) {

    }


    public void readAdditionalData() {
        new ZoneAttributesReader(dataSet).read();
        System.out.println(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
        new PIEReader(dataSet).read();
        System.out.println(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
        new TransportReader(dataSet).read();
        System.out.println(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
        new SuperPAZAttributesReader(dataSet).read();
        System.out.println(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
        new DistanceOMXReader(dataSet).read();
        System.out.println(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
    }

    public final static class InputFeed {

        private final Map<Integer, MopedHousehold> households;
        private final Map<Integer, MopedPerson> persons;
        private final Map<Integer, MopedTrip> trips;
        private final int year;

        public InputFeed(Map<Integer, MopedHousehold> households, Map<Integer, MopedPerson> persons,Map<Integer, MopedTrip> trips,int year) {
            this.households = households;
            this.persons = persons;
            this.trips = trips;
            this.year = year;
        }
    }

    //TODO:read and set zone features
    public void readZoneData() {
        new ZonesReader(dataSet).read();
        System.out.println(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
    }



}
