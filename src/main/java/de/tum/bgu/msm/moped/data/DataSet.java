package de.tum.bgu.msm.moped.data;

import com.google.common.collect.Table;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class DataSet {

    private static final Logger logger = Logger.getLogger(DataSet.class);
    private final Map<Long, Zone> zones= new HashMap<Long, Zone>();
    private final Map<Integer, HouseholdType> hhTypes= new HashMap<Integer, HouseholdType>();
    private Table<Long, Integer, Double> distribution;
    private Table<Long, Integer, Double> hbShopTripGen;
    private Table<Long, Integer, Double> hbRecreationTripGen;
    private Table<Long, Integer, Double> hbOtherTripGen;
    private Table<Long, Integer, Double> hbCollegeProduction;
    private Map<Long, Double> hbCollegeAttraction;
    private Table<Long, Integer, Double> hbSchoolProduction;
    private Map<Long, Double> hbSchoolAttraction;
    private Table<Long, Integer, Double> hbWorkProduction;
    private Map<Long, Double> hbWorkAttraction;
    private Table<Long, Integer, Double> hbWorkWalk;
    private Table<Long, Integer, Double> hbShopWalk;
    private Table<Long, Integer, Double> hbRecreationWalk;
    private Table<Long, Integer, Double> hbOtherWalk;
    private Table<Long, Integer, Double> hbCollegeWalk;
    private Table<Long, Integer, Double> hbSchoolWalk;

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

    public Map<Long, Zone> getZones() {
        return zones;
    }

    public Map<Integer, HouseholdType> getHhTypes() {
        return hhTypes;
    }

    public Zone getZone(Long id) {
        return zones.get(id);
    }

    public HouseholdType getHouseholdType(int id) {
        return hhTypes.get(id);
    }

    public void setDistribution(Table<Long, Integer, Double> distribution) {
        this.distribution = distribution;
    }

    public void setHbShopTripGen(Table<Long, Integer, Double> hbShopTripGen) {
        this.hbShopTripGen = hbShopTripGen;
    }

    public Table<Long, Integer, Double> getDistribution() {
        return distribution;
    }

    public void setHbRecreationTripGen(Table<Long, Integer, Double> hbRecreationTripGen) {
        this.hbRecreationTripGen = hbRecreationTripGen;
    }

    public void setHbOtherTripGen(Table<Long, Integer, Double> hbOtherTripGen) {
        this.hbOtherTripGen = hbOtherTripGen;
    }

    public void setHbCollegeProduction(Table<Long, Integer, Double> hbCollegeProduction) {
        this.hbCollegeProduction = hbCollegeProduction;
    }

    public void setHbCollegeAttraction(Map<Long, Double> hbCollegeAttraction) {
        this.hbCollegeAttraction = hbCollegeAttraction;
    }

    public void setHbSchoolProduction(Table<Long, Integer, Double> hbSchoolProduction) {
        this.hbSchoolProduction = hbSchoolProduction;
    }

    public void setHbSchoolAttraction(Map<Long, Double> hbSchoolAttraction) {
        this.hbSchoolAttraction = hbSchoolAttraction;
    }

    public void setHbWorkProduction(Table<Long, Integer, Double> hbWorkProduction) {
        this.hbWorkProduction = hbWorkProduction;
    }

    public void setHbWorkAttraction(Map<Long, Double> hbWorkAttraction) {
        this.hbWorkAttraction = hbWorkAttraction;
    }

    public Table<Long, Integer, Double> getHbWorkProduction() {
        return hbWorkProduction;
    }

    public Map<Long, Double> getHbWorkAttraction() {
        return hbWorkAttraction;
    }

    public void setHbWorkWalk(Table<Long, Integer, Double> hbWorkWalk) { this.hbWorkWalk = hbWorkWalk; }

    public void setHbShopWalk(Table<Long, Integer, Double> hbShopWalk) { this.hbShopWalk = hbShopWalk; }

    public void setHbRecreationWalk(Table<Long, Integer, Double> hbRecreationWalk) { this.hbRecreationWalk = hbRecreationWalk; }

    public void setHbOtherWalk(Table<Long, Integer, Double> hbOtherWalk) {
        this.hbOtherWalk = hbOtherWalk;
    }

    public void setHbCollegeWalk(Table<Long, Integer, Double> hbCollegeWalk) {
        this.hbCollegeWalk = hbCollegeWalk;
    }

    public void setHbSchoolWalk(Table<Long, Integer, Double> hbSchoolWalk) {
        this.hbSchoolWalk = hbSchoolWalk;
    }

    public Table<Long, Integer, Double> getHbShopTripGen() { return hbShopTripGen; }

    public Table<Long, Integer, Double> getHbRecreationTripGen() {
        return hbRecreationTripGen;
    }

    public Table<Long, Integer, Double> getHbOtherTripGen() {
        return hbOtherTripGen;
    }

    public Table<Long, Integer, Double> getHbCollegeProduction() {
        return hbCollegeProduction;
    }

    public Table<Long, Integer, Double> getHbSchoolProduction() {
        return hbSchoolProduction;
    }

    public Table<Long, Integer, Double> getHbWorkWalk() {
        return hbWorkWalk;
    }

    public Table<Long, Integer, Double> getHbShopWalk() {
        return hbShopWalk;
    }

    public Table<Long, Integer, Double> getHbRecreationWalk() {
        return hbRecreationWalk;
    }

    public Table<Long, Integer, Double> getHbOtherWalk() {
        return hbOtherWalk;
    }

    public Table<Long, Integer, Double> getHbCollegeWalk() {
        return hbCollegeWalk;
    }

    public Table<Long, Integer, Double> getHbSchoolWalk() {
        return hbSchoolWalk;
    }
}
