package pie.data.employmentAllocation;



import org.locationtech.jts.geom.Geometry;
import pie.TimelineLocationPOICounter;

import java.util.HashMap;
import java.util.Map;


public class OSMObject {
    private final long id;
    private Geometry geometry;
    private double area;
    private int jobType;
    private int blockGroupId;
    private int zoneId;
    private Map<Integer,Float> areasByType = new HashMap<>();
    private Map<Integer,Float> jobsByType = new HashMap<>();
    private int numberOfIntersectPAZ;
    private TimelineLocationPOICounter.OSMType amenityType;

    public OSMObject(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public int getZoneId() {
        return zoneId;
    }

    public void setZoneId(int zoneId) {
        this.zoneId = zoneId;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public int getJobType() {
        return jobType;
    }

    public void setJobType(int jobType) {
        this.jobType = jobType;
    }

    public int getBlockGroupId() {
        return blockGroupId;
    }

    public void setBlockGroupId(int blockGroupId) {
        this.blockGroupId = blockGroupId;
    }

    public Map<Integer, Float> getAreasByType() {
        return areasByType;
    }


    public Map<Integer, Float> getJobsByType() {
        return jobsByType;
    }


    public int getNumberOfIntersectPAZ() {
        return numberOfIntersectPAZ;
    }

    public void setNumberOfIntersectPAZ(int numberOfIntersectPAZ) {
        this.numberOfIntersectPAZ = numberOfIntersectPAZ;
    }

    public TimelineLocationPOICounter.OSMType getAmenityType() {
        return amenityType;
    }

    public void setAmenityType(TimelineLocationPOICounter.OSMType amenityType) {
        this.amenityType = amenityType;
    }
}
