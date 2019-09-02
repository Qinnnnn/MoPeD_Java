package de.tum.bgu.msm.moped.data;

import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;


public class MopedPerson {
    private static final Logger logger = Logger.getLogger(MopedPerson.class);

    private final int id;
    private final int age;
    private final Gender gender;
    private final Occupation occupation;
    private final boolean driversLicense;
    private final boolean transitPass;
    private final boolean disable;
    private MopedZone occupationZone;
    private Set<MopedTrip> trips = new LinkedHashSet<>();
    private MopedHousehold mopedHousehold;



    public MopedPerson(int id, int age, Gender gender, Occupation occupation, boolean driversLicense, boolean transitPass, boolean disable) {
        this.id = id;
        this.age = age;
        this.gender = gender;
        this.occupation = occupation;
        this.driversLicense = driversLicense;
        this.transitPass = transitPass;
        this.disable = disable;
    }

    public void setOccupationZone(MopedZone occupationZone) {
        this.occupationZone = occupationZone;
    }

    public MopedZone getOccupationZone() {
        return occupationZone;
    }

    public int getAge() {
        return age;
    }

    public int getId() {
        return id;
    }

    public Gender getGender() {
        return gender;
    }

    public Occupation getOccupation() {
        return occupation;
    }

    public boolean hasDriversLicense() {
        return driversLicense;
    }

    public boolean hasTransitPass() {
        return transitPass;
    }

    public boolean isDisable() {
        return disable;
    }

    public Set<MopedTrip> getTrips() {
        return Collections.unmodifiableSet(this.trips);
    }

    public void addTrip(MopedTrip trip) {
        this.trips.add(trip);
        if(trip.getPerson() != this) {
            trip.setPerson(this);
        }
    }

    public MopedHousehold getMopedHousehold() {
        return mopedHousehold;
    }

    public void setMopedHousehold(MopedHousehold mopedHousehold) {
        this.mopedHousehold = mopedHousehold;
    }
}
