package de.tum.bgu.msm.moped.io.input.readers;


import com.sun.org.apache.xpath.internal.operations.Bool;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.MopedZone;
import de.tum.bgu.msm.moped.data.SuperPAZ;
import de.tum.bgu.msm.moped.io.input.CSVReader;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import de.tum.bgu.msm.moped.util.MoPeDUtil;
import org.apache.log4j.Logger;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import java.io.File;
import java.io.IOException;

public class ZonesReader extends CSVReader {
    private static final Logger logger = Logger.getLogger(ZonesReader.class);

    private int idIndex;
    private int mitoZoneIndex;
    private int superPAZIndex;
    private int totalHouseholdIndex;
    private int index = 0;
    private int growthRateIndex;
    private QuadTree<MopedZone> zoneSearchTree;
    private int blockIndex;
    private int scenarioZoneIndex;
    
    private int agricultureIndex;
    private int constructionIndex;
    private int financialIndex;
    private int governmentIndex;
    private int manufacturingIndex;
    private int retailIndex;
    private int serviceIndex;
    private int transportationIndex;
    private int wholesaleIndex;
    private int shoppingAreaIndex;
    private int collegeVehicleTripIndex;
    private int parkIndex;

    private int waIndex;
    private int stfwyIndex;

    private int pieEmplIndex;
    private int piePopIndex;

    public ZonesReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        logger.info("  Reading zones from zone file");
        super.read(Resources.INSTANCE.getString(Properties.ZONES), ",");
        mapFeaturesToZones(dataSet);
    }

    @Override
    protected void processHeader(String[] header) {
        idIndex = MoPeDUtil.findPositionInArray("zoneID", header);
        //mitoZoneIndex = MoPeDUtil.findPositionInArray("mitoZone", header);
        superPAZIndex = MoPeDUtil.findPositionInArray("superPAZID", header);
        totalHouseholdIndex = MoPeDUtil.findPositionInArray("totalHH", header);
        //growthRateIndex = MoPeDUtil.findPositionInArray("Hhgrowth", header);
        blockIndex = MoPeDUtil.findPositionInArray("PAZ_block_motorway", header);
        scenarioZoneIndex = MoPeDUtil.findPositionInArray("scenarioZone", header);

        idIndex = MoPeDUtil.findPositionInArray("zoneID", header);
        agricultureIndex = MoPeDUtil.findPositionInArray("afm", header);
        constructionIndex = MoPeDUtil.findPositionInArray("con", header);
        financialIndex = MoPeDUtil.findPositionInArray("fi", header);
        governmentIndex = MoPeDUtil.findPositionInArray("gov", header);
        manufacturingIndex = MoPeDUtil.findPositionInArray("mfg", header);
        retailIndex = MoPeDUtil.findPositionInArray("ret", header);
        serviceIndex = MoPeDUtil.findPositionInArray("ser", header);
        transportationIndex = MoPeDUtil.findPositionInArray("tpu", header);
        wholesaleIndex = MoPeDUtil.findPositionInArray("wt", header);
        shoppingAreaIndex = MoPeDUtil.findPositionInArray("shsqft", header);
        collegeVehicleTripIndex = MoPeDUtil.findPositionInArray("colveh", header);
        parkIndex = MoPeDUtil.findPositionInArray("parka", header);

        waIndex = MoPeDUtil.findPositionInArray("WA", header);
        stfwyIndex = MoPeDUtil.findPositionInArray("stfwy", header);

        pieEmplIndex = MoPeDUtil.findPositionInArray("nonIndustrial_in800m", header);
        piePopIndex = MoPeDUtil.findPositionInArray("hh_in800m", header);

    }

    @Override
    protected void processRecord(String[] record) {
        int zoneId = Integer.parseInt(record[idIndex]);
        int superPAZID = Integer.parseInt(record[superPAZIndex]);//internalIndex
        float totalHH = Float.parseFloat(record[totalHouseholdIndex]);

        MopedZone zone = new MopedZone(zoneId, superPAZID, totalHH);

        dataSet.addZone(zone);

        setZonalAttributes(zone,record);
        //if (totalHH != 0.0){
            SuperPAZ superPAZ = dataSet.getSuperPAZ(superPAZID);
            if (superPAZ == null){
                superPAZ = new SuperPAZ(superPAZID, "ORIGIN");
                dataSet.addSuperPAZ(superPAZ);
            }
            superPAZ.getPazs().add(zoneId);
            zone.setIndex(index);
            dataSet.addOriginPAZ(index, zone);
            index++;
        //}
    }

    private void setZonalAttributes(MopedZone zone, String[] record) {
        //int mitoZoneId = Integer.parseInt(record[mitoZoneIndex]);
        //float growthRate = Float.parseFloat(record[growthRateIndex]);
        int block = Integer.parseInt(record[blockIndex]);
        int scenarioZone = Integer.parseInt(record[scenarioZoneIndex]);
        float agriculture = Float.parseFloat(record[agricultureIndex]);
        float construction = Float.parseFloat(record[constructionIndex]);
        float financial = Float.parseFloat(record[financialIndex]);
        float government = Float.parseFloat(record[governmentIndex]);
        float manufacturing = Float.parseFloat(record[manufacturingIndex]);
        float retail = Float.parseFloat(record[retailIndex]);
        float service = Float.parseFloat(record[serviceIndex]);
        float transportation = Float.parseFloat(record[transportationIndex]);
        float wholesale = Float.parseFloat(record[wholesaleIndex]);
        float shoppingArea = Float.parseFloat(record[shoppingAreaIndex]);
        float collegeVehicleTrip = Float.parseFloat(record[collegeVehicleTripIndex]);
        float park = Float.parseFloat(record[parkIndex]);
        int wa = Integer.parseInt(record[waIndex]);
        float stfwy = Float.parseFloat(record[stfwyIndex]);
        float pieEmpl = Float.parseFloat(record[pieEmplIndex]);
        float piePop = Float.parseFloat(record[piePopIndex]);
        zone.setPieEmpl(pieEmpl);
        zone.setPiePop(piePop);
        zone.setWa(wa);
        zone.setStfwy(stfwy);
        //zone.setMitoZoneId(mitoZoneId);
        //zone.setGrowthRate(growthRate);
        zone.setBlock(block);
        zone.setScenarioZone(scenarioZone==1?Boolean.TRUE:Boolean.FALSE);
        zone.setAgriculture(agriculture);
        zone.setConstruction(construction);
        zone.setFinancial(financial);
        zone.setGovernment(government);
        zone.setManufacturing(manufacturing);
        zone.setRetail(retail);
        zone.setService(service);
        zone.setTransportation(transportation);
        zone.setWholesale(wholesale);
        zone.setShoppingArea(shoppingArea);
        zone.setCollegeVehicleTrip(collegeVehicleTrip);
        zone.setSlope(0);
        zone.setFreeway(0);
        zone.setTotalEmpl();
        zone.setIndustrial(agriculture+construction+manufacturing);
        zone.setParkArce(park);
        zone.setPark(0);
    }


    //TODO: like the one in MITO
    private void mapFeaturesToZones(DataSet dataSet) {
        setZoneSearchTree();
        int counter = 0;
        for (SimpleFeature feature: ShapeFileReader.getAllFeatures(Resources.INSTANCE.getString(Properties.ZONE_SHAPEFILE))) {
            int zoneId = (int) Double.parseDouble(feature.getAttribute("OBJECTID").toString());
            MopedZone zone = dataSet.getZones().get(zoneId);
            if (zone != null){
                zone.setShapeFeature(feature);
                //zone.setActivityDensity(Double.parseDouble(feature.getAttribute("popInBuffe").toString())+Double.parseDouble(feature.getAttribute("totalJobIn").toString()));
                //zone.setTotalJobDensity(Double.parseDouble(feature.getAttribute("totalJobIn").toString()));
                //zone.setIndustrialJobDensity(Double.parseDouble(feature.getAttribute("totalJobIn").toString())-Double.parseDouble(feature.getAttribute("uliJobInBu").toString()));
                zoneSearchTree.put(((Geometry)feature.getDefaultGeometry()).getCentroid().getX(),((Geometry)feature.getDefaultGeometry()).getCentroid().getY(),zone);
            }else{
                counter++;
                //logger.warn("zoneId " + zoneId + " doesn't exist in moped zone system");
            }
        }
        logger.warn(counter + " zones in shapefile doesn't exist in zone.csv");

        dataSet.setZoneSearchTree(zoneSearchTree);
    }

    private void setZoneSearchTree() {
        ReferencedEnvelope bounds = loadEnvelope();
        double minX = bounds.getMinX()-1;
        double minY = bounds.getMinY()-1;
        double maxX = bounds.getMaxX()+1;
        double maxY = bounds.getMaxY()+1;
        this.zoneSearchTree = new QuadTree<>(minX,minY,maxX,maxY);
    }

    private ReferencedEnvelope loadEnvelope() {
        File zonesShapeFile = new File(Resources.INSTANCE.getString(Properties.ZONE_SHAPEFILE));

        try {
            FileDataStore dataStore = FileDataStoreFinder.getDataStore(zonesShapeFile);
            return dataStore.getFeatureSource().getBounds();
        } catch (IOException e) {
            logger.error("Error reading file " + zonesShapeFile);
            throw new RuntimeException(e);
        }
    }
}
