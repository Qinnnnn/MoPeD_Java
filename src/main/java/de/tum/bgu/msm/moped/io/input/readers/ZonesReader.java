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
    private int originIndex;
    private int clarkIndex;
    private int blockIndex;

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
        growthRateIndex = MoPeDUtil.findPositionInArray("Hhgrowth", header);
        blockIndex = MoPeDUtil.findPositionInArray("PAZ_block_motorway", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int zoneId = Integer.parseInt(record[idIndex]);
       //int mitoZoneId = Integer.parseInt(record[mitoZoneIndex]);
        int superPAZID = Integer.parseInt(record[superPAZIndex]);//internalIndex
        float totalHH = Float.parseFloat(record[totalHouseholdIndex]);
        float growthRate = Float.parseFloat(record[growthRateIndex]);
        int block = Integer.parseInt(record[blockIndex]);

        MopedZone zone = new MopedZone(zoneId, superPAZID, totalHH);
        //zone.setMitoZoneId(mitoZoneId);
        dataSet.addZone(zone);
        zone.setGrowthRate(growthRate);
        zone.setBlock(block);
        if (totalHH != 0.0){
            SuperPAZ superPAZ = dataSet.getSuperPAZ(superPAZID);
            if (superPAZ == null){
                superPAZ = new SuperPAZ(superPAZID, "ORIGIN");
                dataSet.addSuperPAZ(superPAZ);
            }
            superPAZ.getPazs().add(zoneId);
            zone.setIndex(index);
            dataSet.addOriginPAZ(index, zone);
            index++;
        }
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
