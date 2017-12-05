package de.tum.bgu.msm.moped.data;

import org.apache.log4j.Logger;

public class Zone {

    private static final Logger logger = Logger.getLogger(Zone.class);
    private final int zoneId;
    private double agriculture;
    private double construction;
    private double financial;
    private double government;
    private double manufacturing;
    private double retail;
    private double service;
    private double transportation;
    private double wholesale;
    private double parkCostShort;
    private double parkCostLong;
    private double peakHousehold;
    private double peakEmployment;
    private double offpeakHousehold;
    private double offpeakEmployment;
    private double intersection;
    private double shoppingArea;
    private double singleFamily;
    private double collegeVehicleTrip;
    private double auov;
    private double parkCharge;
    private double canpnr;
    private double caninf;
    private double pie;
    private int pieFlag;
    private int tazId;
    private int wa;
    private double stfwy;
    private double trail;

    public Zone(int id){ this.zoneId = id; }

    public int getZoneId() { return zoneId; }

    public double getAgriculture() {
        return agriculture;
    }

    public void setAgriculture(double agriculture) {
        this.agriculture = agriculture;
    }

    public double getConstruction() {
        return construction;
    }

    public void setConstruction(double construction) {
        this.construction = construction;
    }

    public double getFinancial() {
        return financial;
    }

    public void setFinancial(double financial) {
        this.financial = financial;
    }

    public double getGovernment() {
        return government;
    }

    public void setGovernment(double government) {
        this.government = government;
    }

    public double getManufacturing() {
        return manufacturing;
    }

    public void setManufacturing(double manufacturing) {
        this.manufacturing = manufacturing;
    }

    public double getRetail() {
        return retail;
    }

    public void setRetail(double retail) {
        this.retail = retail;
    }

    public double getService() {
        return service;
    }

    public void setService(double service) {
        this.service = service;
    }

    public double getTransportation() {
        return transportation;
    }

    public void setTransportation(double transportation) {
        this.transportation = transportation;
    }

    public double getWholesale() {
        return wholesale;
    }

    public void setWholesale(double wholesale) {
        this.wholesale = wholesale;
    }

    public double getParkCostShort() {
        return parkCostShort;
    }

    public void setParkCostShort(double parkCostShort) {
        this.parkCostShort = parkCostShort;
    }

    public double getParkCostLong() {
        return parkCostLong;
    }

    public void setParkCostLong(double parkCostLong) {
        this.parkCostLong = parkCostLong;
    }

    public double getPeakHousehold() {
        return peakHousehold;
    }

    public void setPeakHousehold(double peakHousehold) {
        this.peakHousehold = peakHousehold;
    }

    public double getPeakEmployment() {
        return peakEmployment;
    }

    public void setPeakEmployment(double peakEmployment) {
        this.peakEmployment = peakEmployment;
    }

    public double getOffpeakHousehold() {
        return offpeakHousehold;
    }

    public void setOffpeakHousehold(double offpeakHousehold) {
        this.offpeakHousehold = offpeakHousehold;
    }

    public double getOffpeakEmployment() {
        return offpeakEmployment;
    }

    public void setOffpeakEmployment(double offpeakEmployment) {
        this.offpeakEmployment = offpeakEmployment;
    }

    public double getIntersection() {
        return intersection;
    }

    public void setIntersection(double intersection) {
        this.intersection = intersection;
    }

    public double getShoppingArea() {
        return shoppingArea;
    }

    public void setShoppingArea(double shoppingArea) {
        this.shoppingArea = shoppingArea;
    }

    public double getSingleFamily() {
        return singleFamily;
    }

    public void setSingleFamily(double singleFamily) {
        this.singleFamily = singleFamily;
    }

    public double getCollegeVehicleTrip() {
        return collegeVehicleTrip;
    }

    public void setCollegeVehicleTrip(double collegeVehicleTrip) {
        this.collegeVehicleTrip = collegeVehicleTrip;
    }

    public double getAuov() {
        return auov;
    }

    public void setAuov(double auov) {
        this.auov = auov;
    }

    public double getParkCharge() {
        return parkCharge;
    }

    public void setParkCharge(double parkCharge) {
        this.parkCharge = parkCharge;
    }

    public double getCanpnr() {
        return canpnr;
    }

    public void setCanpnr(double canpnr) {
        this.canpnr = canpnr;
    }

    public double getCaninf() {
        return caninf;
    }

    public void setCaninf(double caninf) {
        this.caninf = caninf;
    }

    public double getPie() { return pie; }

    public void setPie(double pie) {
        this.pie = pie;
    }

    public int getPieFlag() {
        return pieFlag;
    }

    public void setPieFlag(int pieFlag) {
        this.pieFlag = pieFlag;
    }

    public int getTazId() {
        return tazId;
    }

    public void setTazId(int tazId) {
        this.tazId = tazId;
    }

    public int getWa() {
        return wa;
    }

    public void setWa(int wa) {
        this.wa = wa;
    }

    public double getStfwy() {
        return stfwy;
    }

    public void setStfwy(double stfwy) {
        this.stfwy = stfwy;
    }

    public double getTrail() {
        return trail;
    }

    public void setTrail(double trail) {
        this.trail = trail;
    }
}
