package de.tum.bgu.msm.moped.data;

public class MopedTrip {

    private final int tripId;
    private final Purpose tripPurpose;
    private Zone tripOrigin;
    private Zone tripDestination;
    private boolean walkMode;
    private int personAge;

    public MopedTrip(int tripId, Purpose tripPurpose) {
        this.tripId = tripId;
        this.tripPurpose = tripPurpose;
    }

    public int getId() {
        return tripId;
    }

    public Zone getTripOrigin() {
        return tripOrigin;
    }

    public void setTripOrigin(Zone origin) {
        this.tripOrigin = origin;
    }

    public Purpose getTripPurpose() {
        return tripPurpose;
    }

    public Zone getTripDestination() {
        return this.tripDestination;
    }

    public void setTripDestination(Zone destination) {
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


    @Override
    public String toString() {
        return "Trip [id: " + this.tripId + " purpose: " + this.tripPurpose + "]";
    }
}
