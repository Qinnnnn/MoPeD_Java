package de.tum.bgu.msm.moped.data;

import cern.colt.matrix.tfloat.impl.DenseLargeFloatMatrix2D;
import cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import org.apache.log4j.Logger;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureSource;
import org.jblas.FloatMatrix;
import org.opengis.feature.simple.SimpleFeature;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class DataSet {

    private static final Logger logger = Logger.getLogger(DataSet.class);
    private final int HOUSEHOLDTYPESIZE = 4097;
    private Map<Integer, MopedZone> zones = new HashMap<>();
    private Map<Integer, SuperPAZ> superPAZs = new HashMap<>();
    private Map<Integer, HouseholdType> hhTypes = new HashMap<>();
    private Map<Integer, MopedZone> originPAZs = new HashMap<>();
    private Map<Integer, SuperPAZ> destinationSuperPAZs = new HashMap<>();
    private FloatMatrix distribution;
    private FloatMatrix impedance;

    private Map<Purpose, FloatMatrix> productionsByPurpose = new HashMap<>();
    private  Map<Purpose, FloatMatrix> walkTripsByPurpose = new HashMap<>();
    private Map<Purpose, DenseLargeFloatMatrix2D> distributionsNoCarByPurpose = new HashMap<>();
    private Map<Purpose, DenseLargeFloatMatrix2D> distributionsHasCarByPurpose = new HashMap<>();
    private Map<Purpose, DenseLargeFloatMatrix2D> distributionsByPAZByPurpose = new HashMap<>();


    private Map<Integer, SimpleFeature> zoneFeatureMap = new HashMap<>();
    private SimpleFeatureSource ozMapSource;


    private int year;
    private final Map<Integer, MopedHousehold> households = new LinkedHashMap<>();
    private final Map<Integer, MopedPerson> persons = new LinkedHashMap<>();
    private final Map<Integer, MopedTrip> trips = new LinkedHashMap<>();
    private SparseFloatMatrix2D PAZImpedance;
    private final float totalPop = 592234.327f;


    public void addZone( MopedZone zone) {
        MopedZone test = this.zones.get(zone.getZoneId());
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

    public void addOriginPAZ(int index, MopedZone zone) {
        MopedZone test = this.originPAZs.get(index);
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

    public void addDistributionNoCar(DenseLargeFloatMatrix2D distributionNoCar, Purpose purpose) {
        this.distributionsNoCarByPurpose.put(purpose, distributionNoCar);
    }

    public void addDistributionHasCar(DenseLargeFloatMatrix2D distributionHasCar, Purpose purpose) {
        this.distributionsHasCarByPurpose.put(purpose, distributionHasCar);
    }

    public void addDistributionPAZ(DenseLargeFloatMatrix2D distribution, Purpose purpose) {
        this.distributionsByPAZByPurpose.put(purpose, distribution);
    }

    public Map<Purpose, DenseLargeFloatMatrix2D> getDistributionsByPAZByPurpose() {
        return distributionsByPAZByPurpose;
    }

    public Map<Integer, MopedZone> getZones() {
        return zones;
    }

    public Map<Integer, HouseholdType> getHhTypes() {
        return hhTypes;
    }

    public Map<Integer, MopedZone> getOriginPAZs() {
        return originPAZs;
    }

    public Map<Integer, SuperPAZ> getSuperPAZs() { return superPAZs; }

    public Map<Integer, SuperPAZ> getDestinationSuperPAZs() {
        return destinationSuperPAZs;
    }

    public MopedZone getZone(int id) {
        return zones.get(id);
    }

    public SuperPAZ getSuperPAZ(int id) {
        return superPAZs.get(id);
    }

    public HouseholdType getHouseholdType(int id) {
        return hhTypes.get(id);
    }

    public MopedZone getOriginPAZ(int id) {
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

    public Map<Purpose, DenseLargeFloatMatrix2D> getDistributionsNoCarByPurpose() {
        return distributionsNoCarByPurpose;
    }

    public Map<Purpose, DenseLargeFloatMatrix2D> getDistributionsHasCarByPurpose() {
        return distributionsHasCarByPurpose;
    }

    public int getHOUSEHOLDTYPESIZE() {
        return HOUSEHOLDTYPESIZE;
    }

    public Map<Integer, SimpleFeature> getZoneFeatureMap() {
        return zoneFeatureMap;
    }

    public void setZoneFeatureMap(Map<Integer, SimpleFeature> zoneFeatureMap) {
        this.zoneFeatureMap = zoneFeatureMap;
    }

    public SimpleFeatureSource getOzMapSource() {
        return ozMapSource;
    }

    public void setOzMapSource() throws IOException {
        File zoneShapeFile = new File(Resources.INSTANCE.getString(Properties.PIE), ",");
        FileDataStore dataStore = FileDataStoreFinder.getDataStore(zoneShapeFile);
        this.ozMapSource = dataStore.getFeatureSource();
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void addHousehold(MopedHousehold household) {
        MopedHousehold test = households.putIfAbsent(household.getId(), household);
        if(test != null) {
            throw new IllegalArgumentException("MopedHousehold id " + household.getId() + " already exists!");
        }
    }

    public void addPerson(MopedPerson person) {
        MopedPerson test = persons.putIfAbsent(person.getId(), person);
        if(test != null) {
            throw new IllegalArgumentException("MopedPerson id " + person.getId() + " already exists!");
        }
    }

    public void addTrip(MopedTrip trip) {
        MopedTrip test = trips.putIfAbsent(trip.getId(), trip);
        if(test != null) {
            throw new IllegalArgumentException("MopedTrip id " + trip.getId() + " already exists!");
        }
    }

    public Map<Integer, MopedHousehold> getHouseholds() {
        return households;
    }

    public Map<Integer, MopedPerson> getPersons() {
        return persons;
    }

    public Map<Integer, MopedTrip> getTrips() {
        return trips;
    }

    public void setPAZImpedance(SparseFloatMatrix2D PAZImpedance) {
        this.PAZImpedance = PAZImpedance;
    }

    public SparseFloatMatrix2D getPAZImpedance() {
        return PAZImpedance;
    }

    public float getTotalPop() {
        return totalPop;
    }
}
