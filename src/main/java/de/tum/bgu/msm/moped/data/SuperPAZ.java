package de.tum.bgu.msm.moped.data;

import java.util.HashMap;
import java.util.Map;

public class SuperPAZ {

    private final int superPAZId;
    private final String type;
    private int index = 99999999;
    private float household;
    private float totalEmpl;
    private float financial;
    private float government;
    private float retail;
    private float service;

    public float getAfm() {
        return afm;
    }

    public void setAfm(float afm) {
        this.afm = afm;
    }

    public float getConstruction() {
        return construction;
    }

    public void setConstruction(float construction) {
        this.construction = construction;
    }

    public float getManufacturing() {
        return manufacturing;
    }

    public void setManufacturing(float manufacturing) {
        this.manufacturing = manufacturing;
    }

    public float getTpu() {
        return tpu;
    }

    public void setTpu(float tpu) {
        this.tpu = tpu;
    }

    public float getWho() {
        return who;
    }

    public void setWho(float who) {
        this.who = who;
    }

    private float afm;
    private float construction;
    private float manufacturing;
    private float tpu;
    private float who;
    private float pie;
    private float slope;
    private int freeway;
    private int park;
    private float industrial;
    private Map<Integer, MopedZone> pazList= new HashMap<>();
    private Map<Integer, Short> impedanceToSuperPAZs = new HashMap<>();

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

    public Map<Integer, MopedZone> getPazs() { return pazList; }

    public Map<Integer, Short> getImpedanceToSuperPAZs() {
        return impedanceToSuperPAZs;
    }

    public float getIndustrial() {
        return industrial;
    }

    public void setIndustrial(float industrial) {
        this.industrial = industrial;
    }
}
