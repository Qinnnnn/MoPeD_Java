package de.tum.bgu.msm.moped.data;

import com.vividsolutions.jts.geom.Geometry;
import org.opengis.feature.simple.SimpleFeature;

import java.util.HashMap;
import java.util.Map;

public class MopedZone {

    private int index;
    private final int zoneId;
    private int superPAZId;
    private float totalHH;
    private float agriculture;
    private float construction;
    private float financial;
    private float government;
    private float manufacturing;
    private float retail;
    private float service;
    private float transportation;
    private float wholesale;
    private float shoppingArea;
    private float collegeVehicleTrip;
    private float pie;
    private int pieFlag;
    private int tazId;
    private int wa;
    private float stfwy;
    private Map<Purpose, Float> totalWalkTripsByPurpose = new HashMap<>();
    private Map<Purpose, Float> totalWalkTripsNoCarByPurpose = new HashMap<>();
    private Map<Purpose, Float> totalWalkTripsHasCarByPurpose = new HashMap<>();
    private SimpleFeature shapeFeature;

    private double activityDensity;
    private double intersectionDensity;
    private float slope;
    private int freeway;
    private float totalEmpl;
    private float piePop;
    private float pieEmpl;
    private float pieArea;
    private float pieActivity;

    private Map<Integer, Float> distribution = new HashMap<>();

    public float getTotalEmpl() {
        return totalEmpl;
    }

    public void setTotalEmpl() {
        this.totalEmpl = agriculture+construction+financial+government+manufacturing+retail+service+transportation+wholesale;
    }

    public float getSlope() {
        return slope;
    }

    public void setSlope(float slope) {
        this.slope = slope;
    }

    public int getFreeway() {
        return freeway;
    }

    public void setFreeway(int freeway) {
        this.freeway = freeway;
    }

    public MopedZone(int id, int superPAZ, float totalHH){
        this.zoneId = id;
        this.superPAZId = superPAZ;
        this.totalHH = totalHH;
    }

    public MopedZone(int id){
        this.zoneId = id;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getZoneId() { return zoneId; }

    public int getSuperPAZId() {
        return superPAZId;
    }

    public float getAgriculture() {
        return agriculture;
    }

    public void setAgriculture(float agriculture) {
        this.agriculture = agriculture;
    }

    public float getConstruction() {
        return construction;
    }

    public void setConstruction(float construction) {
        this.construction = construction;
    }

    public float getFinancial() {
        return financial;
    }

    public void setFinancial(float financial) {
        this.financial = financial;
    }

    public float getGovernment() {
        return government;
    }

    public void setGovernment(float government) {
        this.government = government;
    }

    public float getManufacturing() {
        return manufacturing;
    }

    public void setManufacturing(float manufacturing) {
        this.manufacturing = manufacturing;
    }

    public float getRetail() {
        return retail;
    }

    public void setRetail(float retail) {
        this.retail = retail;
    }

    public float getService() {
        return service;
    }

    public void setService(float service) {
        this.service = service;
    }

    public float getTransportation() {
        return transportation;
    }

    public void setTransportation(float transportation) {
        this.transportation = transportation;
    }

    public float getWholesale() {
        return wholesale;
    }

    public void setWholesale(float wholesale) {
        this.wholesale = wholesale;
    }

    public float getShoppingArea() {
        return shoppingArea;
    }

    public void setShoppingArea(float shoppingArea) {
        this.shoppingArea = shoppingArea;
    }

    public float getCollegeVehicleTrip() {
        return collegeVehicleTrip;
    }

    public void setCollegeVehicleTrip(float collegeVehicleTrip) {
        this.collegeVehicleTrip = collegeVehicleTrip;
    }

    public float getPie() { return pie; }

    public void setPie(float pie) {
        this.pie = pie;
    }

    public int getPieFlag() {
        return pieFlag;
    }

    public void setPieFlag(int pieFlag) {
        this.pieFlag = pieFlag;
    }

    public int getWa() {
        return wa;
    }

    public void setWa(int wa) {
        this.wa = wa;
    }

    public float getStfwy() {
        return stfwy;
    }

    public void setStfwy(float stfwy) {
        this.stfwy = stfwy;
    }

    public float getTotalHH() { return totalHH; }

    public Map<Purpose, Float> getTotalWalkTripsByPurpose() {
        return totalWalkTripsByPurpose;
    }

    public Map<Purpose, Float> getTotalWalkTripsNoCarByPurpose() {
        return totalWalkTripsNoCarByPurpose;
    }

    public Map<Purpose, Float> getTotalWalkTripsHasCarByPurpose() {
        return totalWalkTripsHasCarByPurpose;
    }

    public void addTotalWalkTrips(float totalWalkTrips, Purpose purpose) {
        this.totalWalkTripsByPurpose.put(purpose, totalWalkTrips);
    }

    public void addTotalWalkTripsNoCar(float totalWalkTripsNoCar, Purpose purpose) {
        this.totalWalkTripsNoCarByPurpose.put(purpose, totalWalkTripsNoCar);
    }

    public void addTotalWalkTripsHasCar(float totalWalkTripsHasCar, Purpose purpose) {
        this.totalWalkTripsHasCarByPurpose.put(purpose, totalWalkTripsHasCar);
    }

    public SimpleFeature getShapeFeature() {
        return shapeFeature;
    }

    public void setShapeFeature(SimpleFeature shapeFeature) {
        this.shapeFeature = shapeFeature;
    }

    public double getActivityDensity() {
        return activityDensity;
    }

    public void setActivityDensity(double activityDensity) {
        this.activityDensity = activityDensity;
    }


    public double getIntersectionDensity() {
        return intersectionDensity;
    }

    public void setIntersectionDensity(double intersectionDensity) {
        this.intersectionDensity = intersectionDensity;
    }

    public Map<Integer, Float> getDistribution() {
        return distribution;
    }

    public void setDistribution(Map<Integer, Float> distribution) {
        this.distribution = distribution;
    }

    public float getPieEmpl() {
        return pieEmpl;
    }

    public void setPieEmpl(float pieEmpl) {
        this.pieEmpl = pieEmpl;
    }

    public float getPieArea() {
        return pieArea;
    }

    public void setPieArea(float pieArea) {
        this.pieArea = pieArea;
    }

    public float getPieActivity() {
        return pieActivity;
    }

    public void setPieActivity(float pieActivity) {
        this.pieActivity = pieActivity;
    }

    public float getPiePop() {
        return piePop;
    }

    public void setPiePop(float piePop) {
        this.piePop = piePop;
    }
}
