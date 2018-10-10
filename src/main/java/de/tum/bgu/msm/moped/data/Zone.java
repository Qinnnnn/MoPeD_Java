package de.tum.bgu.msm.moped.data;

import java.util.HashMap;
import java.util.Map;

public class Zone {

    private int index;
    private final int zoneId;
    private final int superPAZId;
    private final float totalHH;
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

    private int test;


    public Zone(int id, int superPAZ, float totalHH, int test){
        this.zoneId = id;
        this.superPAZId = superPAZ;
        this.totalHH = totalHH;
        this.test = test;
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

    public void addTotalWalkTrips(float totalWalkTrips, Purpose purpose) {
        this.totalWalkTripsByPurpose.put(purpose, totalWalkTrips);
    }

    public int getTest() {
        return test;
    }
}
