package de.tum.bgu.msm.moped.data;

import cern.colt.matrix.tfloat.impl.DenseFloatMatrix2D;
import cern.colt.matrix.tfloat.impl.DenseLargeFloatMatrix2D;
import org.apache.log4j.Logger;
import org.jblas.FloatMatrix;

import java.util.HashMap;
import java.util.Map;

public class DataSet {

    private static final Logger logger = Logger.getLogger(DataSet.class);
    private final int HOUSEHOLDTYPESIZE = 4097;
    private Map<Integer, Zone> zones = new HashMap<>();
    private Map<Integer, SuperPAZ> superPAZs = new HashMap<>();
    private Map<Integer, HouseholdType> hhTypes = new HashMap<>();
    private Map<Integer, Zone> originPAZs = new HashMap<>();
    private Map<Integer, SuperPAZ> destinationSuperPAZs = new HashMap<>();
    private FloatMatrix distribution;
    private FloatMatrix impedance;

    private Map<Purpose, FloatMatrix> productionsByPurpose = new HashMap<>();
    private  Map<Purpose, FloatMatrix> walkTripsByPurpose = new HashMap<>();
    private Map<Purpose, DenseLargeFloatMatrix2D> distributionsByPurpose = new HashMap<>();

    public void addZone( Zone zone) {
        Zone test = this.zones.get(zone.getZoneId());
        if (test != null) {
            if (test.equals(zone)) {
                logger.warn("Zone " + zone.getZoneId() + " was already added to data set.");
                return;
            }
            throw new IllegalArgumentException("Zone id " + zone.getZoneId() + " already exists!");
        }
        zones.put(zone.getZoneId(), zone);
    }

    public void addSuperPAZ( SuperPAZ superPAZ) {
        SuperPAZ test = this.superPAZs.get(superPAZ.getSuperPAZId());
        if (test != null) {
            if (test.equals(superPAZ)) {
                return;
            }
            throw new IllegalArgumentException("SuperPAZ id " + superPAZ.getSuperPAZId() + " already exists!");
        }
        superPAZs.put(superPAZ.getSuperPAZId(), superPAZ);
    }

    public void addHouseholdType( HouseholdType householdType) {
        HouseholdType test = this.hhTypes.get(householdType.getHhTypeId());
        if (test != null) {
            if (test.equals(householdType)) {
                logger.warn("HouseholdType " + householdType.getHhTypeId() + " was already added to data set.");
                return;
            }
            throw new IllegalArgumentException("HouseholdType id " + householdType.getHhTypeId() + " already exists!");
        }
        hhTypes.put(householdType.getHhTypeId(), householdType);
    }

    public void addOriginPAZ(int index, Zone zone) {
        Zone test = this.originPAZs.get(index);
        if (test != null) {
            if (test.equals(zone)) {
                logger.warn("originPAZs " + zone.getZoneId() + " was already added to data set.");
                return;
            }
            throw new IllegalArgumentException("originPAZs id " + zone.getZoneId() + " already exists!");
        }
        originPAZs.put(index, zone);
    }


    public void addDestinationSuperPAZ(int index, SuperPAZ superPAZ) {
        SuperPAZ test = this.destinationSuperPAZs.get(index);
        if (test != null) {
            if (test.equals(superPAZ)) {
                return;
            }
            throw new IllegalArgumentException("destinationSuperPAZ id " + superPAZ.getSuperPAZId() + " already exists!");
        }
        destinationSuperPAZs.put(index, superPAZ);
    }


    public void addProduction(FloatMatrix production, Purpose purpose) {
        this.productionsByPurpose.put(purpose, production);
    }

    public void addWalkTrips(FloatMatrix production, Purpose purpose) {
        this.walkTripsByPurpose.put(purpose, production);
    }

    public void addDistribution(DenseLargeFloatMatrix2D production, Purpose purpose) {
        this.distributionsByPurpose.put(purpose, production);
    }


    public Map<Integer, Zone> getZones() {
        return zones;
    }

    public Map<Integer, HouseholdType> getHhTypes() {
        return hhTypes;
    }

    public Map<Integer, Zone> getOriginPAZs() {
        return originPAZs;
    }

    public Map<Integer, SuperPAZ> getSuperPAZs() { return superPAZs; }

    public Map<Integer, SuperPAZ> getDestinationSuperPAZs() {
        return destinationSuperPAZs;
    }

    public Zone getZone(int id) {
        return zones.get(id);
    }

    public SuperPAZ getSuperPAZ(int id) {
        return superPAZs.get(id);
    }

    public HouseholdType getHouseholdType(int id) {
        return hhTypes.get(id);
    }

    public Zone getOriginPAZ(int id) {
        return originPAZs.get(id);
    }

    public SuperPAZ getDestinationSuperPAZ(int id) {
        return destinationSuperPAZs.get(id);
    }

    public void setDistribution(FloatMatrix distribution) {
        this.distribution = distribution;
    }

    public FloatMatrix getDistribution() {
        return distribution;
    }

    public void setImpedance(FloatMatrix impedance) {
        this.impedance = impedance;
    }

    public FloatMatrix getImpedance() {
        return impedance;
    }

    public Map<Purpose, FloatMatrix> getProductionsByPurpose() {
        return productionsByPurpose;
    }

    public Map<Purpose, FloatMatrix> getWalkTripsByPurpose() {
        return walkTripsByPurpose;
    }

    public Map<Purpose, DenseLargeFloatMatrix2D> getDistributionsByPurpose() {
        return distributionsByPurpose;
    }

    public int getHOUSEHOLDTYPESIZE() {
        return HOUSEHOLDTYPESIZE;
    }
}
