package de.tum.bgu.msm.moped.data;

import java.util.HashMap;
import java.util.Map;

public class SuperPAZ {

    private final int superPAZId;
    private final String type;
    private int index;
    private float household;
    private float totalEmpl;
    private float financial;
    private float government;
    private float retail;
    private float service;
    private float pie;
    private float slope;
    private int freeway;
    private int park;
    private Map<Integer, Zone> pazList= new HashMap<>();
    private Map<Integer, Float> impedanceToSuperPAZs = new HashMap<>();

    public SuperPAZ(int superPAZId, String type) {
        this.superPAZId = superPAZId;
        this.type = type;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getSuperPAZId() {
        return superPAZId;
    }

    public float getHousehold() {
        return household;
    }

    public void setHousehold(float household) {
        this.household = household;
    }

    public float getTotalEmpl() {
        return totalEmpl;
    }

    public void setTotalEmpl(float totalEmpl) {
        this.totalEmpl = totalEmpl;
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

    public float getPie() {
        return pie;
    }

    public void setPie(float pie) {
        this.pie = pie;
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

    public int getPark() {
        return park;
    }

    public void setPark(int park) {
        this.park = park;
    }

    public Map<Integer, Zone> getPazs() { return pazList; }

    public Map<Integer, Float> getImpedanceToSuperPAZs() {
        return impedanceToSuperPAZs;
    }

}
