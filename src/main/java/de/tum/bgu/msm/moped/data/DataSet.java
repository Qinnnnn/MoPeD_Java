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
    private Map<Long, SuperPAZ> originSuperPAZs= new HashMap<Long, SuperPAZ>();
    private Map<Long, SuperPAZ> destinationSuperPAZs= new HashMap<Long, SuperPAZ>();
    private Table<Long, Long, Double> impedance;
    private Table<Long, Long, Double> hbWorkDistribution;
    private Table<Long, Long, Double> hbShopDistribution;
    private Table<Long, Long, Double> hbRecreationDistribution;
    private Table<Long, Long, Double> hbOtherDistribution;
    private Table<Long, Long, Double> hbCollegeDistribution;
    private Table<Long, Long, Double> hbSchoolDistribution;


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

    public void addOriginSuperPAZ(final SuperPAZ superPAZ) {
        SuperPAZ test = this.originSuperPAZs.get(superPAZ.getSuperPAZId());
        if(test != null) {
            if(test.equals(superPAZ)) {
                logger.warn("originSuperPAZs " + superPAZ.getSuperPAZId() + " was already added to data set.");
                return;
            }
            throw new IllegalArgumentException("originSuperPAZs id " + superPAZ.getSuperPAZId() + " already exists!");
        }
        originSuperPAZs.put(superPAZ.getSuperPAZId(), superPAZ);
    }

    public void addDestinationSuperPAZ(final SuperPAZ superPAZ) {
        SuperPAZ test = this.destinationSuperPAZs.get(superPAZ.getSuperPAZId());
        if(test != null) {
            if(test.equals(superPAZ)) {
                //logger.warn("destinationSuperPAZ " + superPAZ.getSuperPAZId() + " was already added to data set.");
                return;
            }
            throw new IllegalArgumentException("destinationSuperPAZ id " + superPAZ.getSuperPAZId() + " already exists!");
        }
        destinationSuperPAZs.put(superPAZ.getSuperPAZId(), superPAZ);
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

    public HouseholdType getHouseholdType(int id) { return hhTypes.get(id); }

    public Map<Long, SuperPAZ> getOriginSuperPAZs() { return originSuperPAZs; }

    public Map<Long, SuperPAZ> getDestinationSuperPAZs() { return destinationSuperPAZs; }

    public SuperPAZ getOriginSuperPAZ(Long id) { return originSuperPAZs.get(id); }

    public SuperPAZ getDestinationSuperPAZ(Long id) { return destinationSuperPAZs.get(id); }

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

    public void setImpedance(Table<Long, Long, Double> impedance) {
        this.impedance = impedance;
    }

    public Table<Long, Long, Double> getImpedance() { return impedance; }

    public void setHbWorkWalk(Table<Long, Integer, Double> hbWorkWalk) { this.hbWorkWalk = hbWorkWalk; }

    public void setHbShopWalk(Table<Long, Integer, Double> hbShopWalk) {
        this.hbShopWalk = hbShopWalk;
    }

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

    public void setHbWorkDistribution(Table<Long, Long, Double> hbWorkDistribution) {
        this.hbWorkDistribution = hbWorkDistribution;
    }

    public void setHbShopDistribution(Table<Long, Long, Double> hbShopDistribution) {
        this.hbShopDistribution = hbShopDistribution;
    }

    public void setHbRecreationDistribution(Table<Long, Long, Double> hbRecreationDistribution) {
        this.hbRecreationDistribution = hbRecreationDistribution;
    }

    public void setHbOtherDistribution(Table<Long, Long, Double> hbOtherDistribution) {
        this.hbOtherDistribution = hbOtherDistribution;
    }

    public void setHbCollegeDistribution(Table<Long, Long, Double> hbCollegeDistribution) {
        this.hbCollegeDistribution = hbCollegeDistribution;
    }

    public void setHbSchoolDistribution(Table<Long, Long, Double> hbSchoolDistribution) {
        this.hbSchoolDistribution = hbSchoolDistribution;
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

    public Table<Long, Long, Double> getHbWorkDistribution() {
        return hbWorkDistribution;
    }

    public Table<Long, Long, Double> getHbShopDistribution() {
        return hbShopDistribution;
    }

    public Table<Long, Long, Double> getHbRecreationDistribution() {
        return hbRecreationDistribution;
    }

    public Table<Long, Long, Double> getHbOtherDistribution() {
        return hbOtherDistribution;
    }

    public Table<Long, Long, Double> getHbCollegeDistribution() {
        return hbCollegeDistribution;
    }

    public Table<Long, Long, Double> getHbSchoolDistribution() {
        return hbSchoolDistribution;
    }
}
