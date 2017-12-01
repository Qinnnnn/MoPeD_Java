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
    private Table<Integer, Integer, Double> hbSchoolTripGen;
    private Table<Integer, Integer, Double> hbCollegeProduction;
    private Map<Integer, Double> hbCollegeAttraction;
    private Table<Integer, Integer, Double> hbSchoolProduction;
    private Map<Integer, Double> hbSchoolAttraction;
    private Table<Integer, Integer, Double> hbWorkProduction;
    private Map<Integer, Double> hbWorkAttraction;

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

    public void setHbSchoolTripGen(Table<Integer, Integer, Double> hbSchoolTripGen) {
        this.hbSchoolTripGen = hbSchoolTripGen;
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
}
