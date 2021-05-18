package de.tum.bgu.msm.moped.io.input.readers;


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
import org.locationtech.jts.geom.Geometry;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import java.io.File;
import java.io.IOException;

public class MitoZonesReader extends CSVReader{
    private static final Logger logger = Logger.getLogger(MitoZonesReader.class);

    private QuadTree<MopedZone> zoneSearchTree;

    private int idIndex;
    private int mitoZoneIndex;
    private int superPAZIndex;
    private int totalHouseholdIndex;
    private int index = 0;
    private int scenarioZoneIndex;
    private int agricultureIndex;
    private int constructionIndex;
    private int financialIndex;
    private int governmentIndex;
    private int manufacturingIndex;
    private int retailIndex;
    private int serviceIndex;
    private int transportationIndex;
    private int utilityIndex;
    private int wholesaleIndex;
    private int parkIndex;
    private int pieEmplIndex;
    private int piePopIndex;

    public MitoZonesReader(DataSet dataSet) {
        super(dataSet);
    }

    public void read() {
        logger.info("  Reading zones from zone file");
        super.read(Resources.INSTANCE.getString(Properties.ZONES), ",");
        mapFeaturesToZones(dataSet);
    }

    protected void processHeader(String[] header) {
        idIndex = MoPeDUtil.findPositionInArray("zoneId", header);
        mitoZoneIndex = MoPeDUtil.findPositionInArray("mitoZoneId", header);
        superPAZIndex = MoPeDUtil.findPositionInArray("superPAZId", header);
        totalHouseholdIndex = MoPeDUtil.findPositionInArray("pop", header);
        scenarioZoneIndex = MoPeDUtil.findPositionInArray("scenarioPAZ", header);

        agricultureIndex = MoPeDUtil.findPositionInArray("Agri", header);
        constructionIndex = MoPeDUtil.findPositionInArray("Cons", header);
        financialIndex = MoPeDUtil.findPositionInArray("Finc", header);
        governmentIndex = MoPeDUtil.findPositionInArray("Admn", header);
        manufacturingIndex = MoPeDUtil.findPositionInArray("Mnft", header);
        retailIndex = MoPeDUtil.findPositionInArray("Retl", header);
        serviceIndex = MoPeDUtil.findPositionInArray("Serv", header);
        transportationIndex = MoPeDUtil.findPositionInArray("Trns", header);
        utilityIndex = MoPeDUtil.findPositionInArray("Util", header);
        wholesaleIndex = MoPeDUtil.findPositionInArray("Rlst", header);
        parkIndex = MoPeDUtil.findPositionInArray("parka", header);
        pieEmplIndex = MoPeDUtil.findPositionInArray("nonIndustrial_in800m", header);
        piePopIndex = MoPeDUtil.findPositionInArray("hh_in800m", header);

    }


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
        int mitoZoneId = Integer.parseInt(record[mitoZoneIndex]);
        int scenarioZone = Integer.parseInt(record[scenarioZoneIndex]);
        float agriculture = Float.parseFloat(record[agricultureIndex]);
        float construction = Float.parseFloat(record[constructionIndex]);
        float financial = Float.parseFloat(record[financialIndex]);
        float government = Float.parseFloat(record[governmentIndex]);
        float manufacturing = Float.parseFloat(record[manufacturingIndex]);
        float retail = Float.parseFloat(record[retailIndex]);
        float service = Float.parseFloat(record[serviceIndex]);
        float transportation = Float.parseFloat(record[transportationIndex]);
        float utility = Float.parseFloat(record[utilityIndex]);
        float wholesale = Float.parseFloat(record[wholesaleIndex]);
        float park = Float.parseFloat(record[parkIndex]);
        float pieEmpl = Float.parseFloat(record[pieEmplIndex]);
        float piePop = Float.parseFloat(record[piePopIndex]);
        zone.setPieEmpl(pieEmpl);
        zone.setPiePop(piePop);
        zone.setAgriculture(agriculture);
        zone.setConstruction(construction);
        zone.setFinancial(financial);
        zone.setGovernment(government);
        zone.setManufacturing(manufacturing);
        zone.setRetail(retail);
        zone.setService(service);
        zone.setTransportation(transportation+utility);
        zone.setWholesale(wholesale);
        zone.setSlope(0);
        zone.setTotalEmpl();
        zone.setIndustrial(agriculture+construction+manufacturing);
        zone.setParkArce(park);
        zone.setScenarioZone(scenarioZone==1?Boolean.TRUE:Boolean.FALSE);
        zone.setMitoZoneId(mitoZoneId);
    }


    //TODO: like the one in MITO
    private void mapFeaturesToZones(DataSet dataSet) {
        setZoneSearchTree();
        int counter = 0;
        for (SimpleFeature feature: ShapeFileReader.getAllFeatures(Resources.INSTANCE.getString(Properties.ZONE_SHAPEFILE))) {
            int zoneId = (int) Double.parseDouble(feature.getAttribute("zoneID").toString());
            MopedZone zone = dataSet.getZones().get(zoneId);
            if (zone != null){
                zone.setShapeFeature(feature);
                zoneSearchTree.put(((Geometry)feature.getDefaultGeometry()).getCentroid().getX(),((Geometry)feature.getDefaultGeometry()).getCentroid().getY(),zone);
            }else{
                counter++;
                //logger.warn("zoneId " + zoneId + " doesn't exist in moped zone system");
            }
        }
        logger.warn(counter + " zones in shapefile doesn't exist in paz.csv");

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
