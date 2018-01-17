package de.tum.bgu.msm.moped.data;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class SuperPAZ {

    private static final Logger logger = Logger.getLogger(SuperPAZ.class);
    private final long superPAZId;
    private final String type;
    private double household;
    private double totalEmpl;
    private double financial;
    private double government;
    private double retail;
    private double service;
    private double pie;
    private double slope;
    private int freeway;
    private int park;
    private Map<Long, Zone> pazList= new HashMap<Long, Zone>();
    private double hbWorkWalkTrips;
    private double hbShopWalkTrips;
    private double hbRecreationWalkTrips;
    private double hbOtherWalkTrips;
    private double hbSchoolWalkTrips;
    private double hbCollegeWalkTrips;


    public SuperPAZ(long superPAZId, String type) {
        this.superPAZId = superPAZId;
        this.type = type;
    }

    public long getSuperPAZId() {
        return superPAZId;
    }

    public double getHousehold() {
        return household;
    }

    public void setHousehold(double household) {
        this.household = household;
    }

    public double getTotalEmpl() {
        return totalEmpl;
    }

    public void setTotalEmpl(double totalEmpl) {
        this.totalEmpl = totalEmpl;
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

    public double getPie() {
        return pie;
    }

    public void setPie(double pie) {
        this.pie = pie;
    }

    public double getSlope() {
        return slope;
    }

    public void setSlope(double slope) {
        this.slope = slope;
    }

    public int getFreeway() {
        return freeway;
    }

    public void setFreeway(int freeway) {
        this.freeway = freeway;
    }

    public int getPark() {
        return park;
    }

    public void setPark(int park) {
        this.park = park;
    }

    public Map<Long, Zone> getPazs() { return pazList; }

    public String getType() {
        return type;
    }

    public double getHbWorkWalkTrips() {
        return hbWorkWalkTrips;
    }

    public void setHbWorkWalkTrips(double hbWorkWalkTrips) {
        this.hbWorkWalkTrips = hbWorkWalkTrips;
    }

    public double getHbShopWalkTrips() {
        return hbShopWalkTrips;
    }

    public void setHbShopWalkTrips(double hbShopWalkTrips) {
        this.hbShopWalkTrips = hbShopWalkTrips;
    }

    public double getHbRecreationWalkTrips() {
        return hbRecreationWalkTrips;
    }

    public void setHbRecreationWalkTrips(double hbRecreationWalkTrips) {
        this.hbRecreationWalkTrips = hbRecreationWalkTrips;
    }

    public double getHbOtherWalkTrips() {
        return hbOtherWalkTrips;
    }

    public void setHbOtherWalkTrips(double hbOtherWalkTrips) {
        this.hbOtherWalkTrips = hbOtherWalkTrips;
    }

    public double getHbSchoolWalkTrips() {
        return hbSchoolWalkTrips;
    }

    public void setHbSchoolWalkTrips(double hbSchoolWalkTrips) {
        this.hbSchoolWalkTrips = hbSchoolWalkTrips;
    }

    public double getHbCollegeWalkTrips() {
        return hbCollegeWalkTrips;
    }

    public void setHbCollegeWalkTrips(double hbCollegeWalkTrips) {
        this.hbCollegeWalkTrips = hbCollegeWalkTrips;
    }
}
