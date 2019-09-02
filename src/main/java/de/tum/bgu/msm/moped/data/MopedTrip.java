package de.tum.bgu.msm.moped.data;

public class MopedTrip {

    private final int tripId;
    private final Purpose tripPurpose;
    private MopedZone tripOrigin;
    private MopedZone tripDestination;
    private double tripDistance;
    private boolean walkMode;
    private MopedPerson person;

    public MopedTrip(int tripId, Purpose tripPurpose) {
        this.tripId = tripId;
        this.tripPurpose = tripPurpose;
    }

    public int getId() {
        return tripId;
    }

    public MopedZone getTripOrigin() {
        return tripOrigin;
    }

    public void setTripOrigin(MopedZone origin) {
        this.tripOrigin = origin;
    }

    public Purpose getTripPurpose() {
        return tripPurpose;
    }

    public MopedZone getTripDestination() {
        return this.tripDestination;
    }

    public void setTripDestination(MopedZone destination) {
        this.tripDestination = destination;
    }


    public boolean isWalkMode() {
        return walkMode;
    }

    public void setWalkMode(boolean walkMode) {
        this.walkMode = walkMode;
    }

    public int getTripId() {
        return tripId;
    }

    public MopedPerson getPerson() {
        return person;
    }

    public void setPerson(MopedPerson person) {
        this.person = person;
    }

    public double getTripDistance() {
        return tripDistance;
    }

    public void setTripDistance(double tripDistance) {
        this.tripDistance = tripDistance;
    }

    @Override
    public String toString() {
        return "Trip [id: " + this.tripId + " purpose: " + this.tripPurpose + "]";
    }
}
