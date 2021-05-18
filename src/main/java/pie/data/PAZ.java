package pie.data;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import java.util.HashMap;
import java.util.Map;

public class PAZ {

    private final int id;
    private Map<Integer,Double> jobsByType = new HashMap<>();
    private Geometry geometry;
    private double households;
    private double totalJob;
    private double uliJob;
    private Coordinate coord;

    private double householdWithinBuffer;
    private double totalJobWithinBuffer;
    private double uliJobWithinBuffer;
    private int intersectionWithinBuffer;


    public PAZ(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public Map<Integer, Double> getJobsByType() {
        return jobsByType;
    }

    public void setJobsByType(Map<Integer, Double> jobsByType) {
        this.jobsByType = jobsByType;
    }

    public double getHouseholdWithinBuffer() {
        return householdWithinBuffer;
    }

    public void setHouseholdWithinBuffer(double householdWithinBuffer) {
        this.householdWithinBuffer = householdWithinBuffer;
    }

    public double getTotalJobWithinBuffer() {
        return totalJobWithinBuffer;
    }

    public void setTotalJobWithinBuffer(double totalJobWithinBuffer) {
        this.totalJobWithinBuffer = totalJobWithinBuffer;
    }

    public double getUliJobWithinBuffer() {
        return uliJobWithinBuffer;
    }

    public void setUliJobWithinBuffer(double uliJobWithinBuffer) {
        this.uliJobWithinBuffer = uliJobWithinBuffer;
    }

    public int getIntersectionWithinBuffer() {
        return intersectionWithinBuffer;
    }

    public void setIntersectionWithinBuffer(int intersectionWithinBuffer) {
        this.intersectionWithinBuffer = intersectionWithinBuffer;
    }

    public double getHouseholds() {
        return households;
    }

    public void setHouseholds(double households) {
        this.households = households;
    }

    public double getTotalJob() {
        return totalJob;
    }

    public void setTotalJob(double totalJob) {
        this.totalJob = totalJob;
    }

    public double getUliJob() {
        return uliJob;
    }

    public void setUliJob(double uliJob) {
        this.uliJob = uliJob;
    }

    public Coordinate getCoord() {
        return coord;
    }

    public void setCoord(Coordinate coord) {
        this.coord = coord;
    }
}
