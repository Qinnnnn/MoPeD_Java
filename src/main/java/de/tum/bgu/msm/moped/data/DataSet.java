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
}
