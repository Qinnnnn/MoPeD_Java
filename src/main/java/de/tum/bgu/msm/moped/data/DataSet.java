package de.tum.bgu.msm.moped.data;

import com.google.common.collect.Table;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class DataSet {

    private static final Logger logger = Logger.getLogger(DataSet.class);
    private final Map<Integer, Zone> zones= new HashMap<Integer, Zone>();
    private final Map<Integer, HouseholdType> hhTypes= new HashMap<Integer, HouseholdType>();
    private Table<Integer, Integer, Double> distribution;
    private Table<Integer, Integer, Double> hbShopTripGen;
    private Table<Integer, Integer, Double> hbRecreationTripGen;
    private Table<Integer, Integer, Double> hbOtherTripGen;
    private Table<Integer, Integer, Double> hbCollegeProduction;
    private Map<Integer, Double> hbCollegeAttraction;
    private Table<Integer, Integer, Double> hbSchoolProduction;
    private Map<Integer, Double> hbSchoolAttraction;
    private Table<Integer, Integer, Double> hbWorkProduction;
    private Map<Integer, Double> hbWorkAttraction;
    private Table<Integer, Integer, Double> hbWorkWalk;
    private Table<Integer, Integer, Double> hbShopWalk;
    private Table<Integer, Integer, Double> hbRecreationWalk;
    private Table<Integer, Integer, Double> hbOtherWalk;
    private Table<Integer, Integer, Double> hbCollegeWalk;
    private Table<Integer, Integer, Double> hbSchoolWalk;

    public void addZone(final Zone zone) {
        Zone test = this.zones.get(zone.getZoneId());
        if(test != null) {
            if(test.equals(zone)) {
                logger.warn("Zone " + zone.getZoneId() + " was already added to data set.");
                return;
            }
            throw new IllegalArgumentException("Zone id " + zone.getZoneId() + " already exists!");
        }
        zones.put(zone.getZoneId(), zone);
    }

    public void addHouseholdType(final HouseholdType householdType) {
        HouseholdType test = this.hhTypes.get(householdType.getHhTypeId());
        if(test != null) {
            if(test.equals(householdType)) {
                logger.warn("HouseholdType " + householdType.getHhTypeId() + " was already added to data set.");
                return;
            }
            throw new IllegalArgumentException("HouseholdType id " + householdType.getHhTypeId() + " already exists!");
        }
        hhTypes.put(householdType.getHhTypeId(), householdType);
    }

    public Map<Integer, Zone> getZones() {
        return zones;
    }

    public Map<Integer, HouseholdType> getHhTypes() {
        return hhTypes;
    }

    public Zone getZone(int id) {
        return zones.get(id);
    }

    public HouseholdType getHouseholdType(int id) {
        return hhTypes.get(id);
    }

    public void setDistribution(Table<Integer, Integer, Double> distribution) {
        this.distribution = distribution;
    }

    public void setHbShopTripGen(Table<Integer, Integer, Double> hbShopTripGen) {
        this.hbShopTripGen = hbShopTripGen;
    }

    public Table<Integer, Integer, Double> getDistribution() {
        return distribution;
    }

    public void setHbRecreationTripGen(Table<Integer, Integer, Double> hbRecreationTripGen) {
        this.hbRecreationTripGen = hbRecreationTripGen;
    }

    public void setHbOtherTripGen(Table<Integer, Integer, Double> hbOtherTripGen) {
        this.hbOtherTripGen = hbOtherTripGen;
    }

    public void setHbCollegeProduction(Table<Integer, Integer, Double> hbCollegeProduction) {
        this.hbCollegeProduction = hbCollegeProduction;
    }

    public void setHbCollegeAttraction(Map<Integer, Double> hbCollegeAttraction) {
        this.hbCollegeAttraction = hbCollegeAttraction;
    }

    public void setHbSchoolProduction(Table<Integer, Integer, Double> hbSchoolProduction) {
        this.hbSchoolProduction = hbSchoolProduction;
    }

    public void setHbSchoolAttraction(Map<Integer, Double> hbSchoolAttraction) {
        this.hbSchoolAttraction = hbSchoolAttraction;
    }

    public void setHbWorkProduction(Table<Integer, Integer, Double> hbWorkProduction) {
        this.hbWorkProduction = hbWorkProduction;
    }

    public void setHbWorkAttraction(Map<Integer, Double> hbWorkAttraction) {
        this.hbWorkAttraction = hbWorkAttraction;
    }

    public Table<Integer, Integer, Double> getHbWorkProduction() {
        return hbWorkProduction;
    }

    public Map<Integer, Double> getHbWorkAttraction() {
        return hbWorkAttraction;
    }

    public void setHbWorkWalk(Table<Integer, Integer, Double> hbWorkWalk) { this.hbWorkWalk = hbWorkWalk; }

    public void setHbShopWalk(Table<Integer, Integer, Double> hbShopWalk) { this.hbShopWalk = hbShopWalk; }

    public void setHbRecreationWalk(Table<Integer, Integer, Double> hbRecreationWalk) { this.hbRecreationWalk = hbRecreationWalk; }

    public void setHbOtherWalk(Table<Integer, Integer, Double> hbOtherWalk) {
        this.hbOtherWalk = hbOtherWalk;
    }

    public void setHbCollegeWalk(Table<Integer, Integer, Double> hbCollegeWalk) {
        this.hbCollegeWalk = hbCollegeWalk;
    }

    public void setHbSchoolWalk(Table<Integer, Integer, Double> hbSchoolWalk) {
        this.hbSchoolWalk = hbSchoolWalk;
    }

    public Table<Integer, Integer, Double> getHbShopTripGen() {
        return hbShopTripGen;
    }

    public Table<Integer, Integer, Double> getHbRecreationTripGen() {
        return hbRecreationTripGen;
    }

    public Table<Integer, Integer, Double> getHbOtherTripGen() {
        return hbOtherTripGen;
    }

    public Table<Integer, Integer, Double> getHbCollegeProduction() {
        return hbCollegeProduction;
    }

    public Table<Integer, Integer, Double> getHbSchoolProduction() {
        return hbSchoolProduction;
    }
}
