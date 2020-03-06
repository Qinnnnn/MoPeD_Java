package de.tum.bgu.msm.moped.io.input.readers;


import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.MopedZone;
import de.tum.bgu.msm.moped.data.SuperPAZ;
import de.tum.bgu.msm.moped.io.input.CSVReader;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import de.tum.bgu.msm.moped.util.MoPeDUtil;
import org.apache.log4j.Logger;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

public class ZonesReader extends CSVReader {
    private static final Logger logger = Logger.getLogger(ZonesReader.class);

    private int idIndex;
    //private int mitoZoneIndex;
    private int superPAZIndex;
    private int totalHouseholdIndex;
    private int index = 0;
    private int growthRateIndex;

    public ZonesReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        logger.info("  Reading zones from zone file");
        super.read(Resources.INSTANCE.getString(Properties.ZONES), ",");
        //mapFeaturesToZones(dataSet);
    }

    @Override
    protected void processHeader(String[] header) {
        idIndex = MoPeDUtil.findPositionInArray("zoneID", header);
        //mitoZoneIndex = MoPeDUtil.findPositionInArray("mitoZone", header);
        superPAZIndex = MoPeDUtil.findPositionInArray("superPAZID", header);
        totalHouseholdIndex = MoPeDUtil.findPositionInArray("totalHH", header);
        growthRateIndex = MoPeDUtil.findPositionInArray("Hhgrowth", header);

    }

    @Override
    protected void processRecord(String[] record) {
        int zoneId = Integer.parseInt(record[idIndex]);
        //int mitoZoneId = Integer.parseInt(record[mitoZoneIndex]);
        int superPAZID = Integer.parseInt(record[superPAZIndex]);//internalIndex
        float totalHH = Float.parseFloat(record[totalHouseholdIndex]);
        float growthRate = Float.parseFloat(record[growthRateIndex]);

        MopedZone zone = new MopedZone(zoneId, superPAZID, totalHH);
        //zone.setMitoZoneId(mitoZoneId);
        dataSet.addZone(zone);
        //dataSet.getInternal2External().put(superPAZID,zoneId);
        //dataSet.getExternal2Internal().put(zoneId,superPAZID);
        zone.setGrowthRate(growthRate);

        //if (totalHH != 0.0){
            SuperPAZ superPAZ = dataSet.getSuperPAZ(superPAZID);
            if (superPAZ == null){
                superPAZ = new SuperPAZ(superPAZID, "ORIGIN");
                dataSet.addSuperPAZ(superPAZ);
            }
            superPAZ.getPazs().put(zoneId,zone);
            zone.setIndex(index);
            dataSet.addOriginPAZ(index, zone);
            index++;
        //}
    }


    //TODO: like the one in MITO
    private void mapFeaturesToZones(DataSet dataSet) {
        for (SimpleFeature feature: ShapeFileReader.getAllFeatures(Resources.INSTANCE.getString(Properties.ZONE_SHAPEFILE))) {
            int zoneId = Integer.parseInt(feature.getAttribute("id").toString());
            MopedZone zone = dataSet.getZones().get(zoneId);
            if (zone != null){
                zone.setShapeFeature(feature);
                zone.setActivityDensity(Double.parseDouble(feature.getAttribute("popInBuffe").toString())+Double.parseDouble(feature.getAttribute("totalJobIn").toString()));
                zone.setTotalJobDensity(Double.parseDouble(feature.getAttribute("totalJobIn").toString()));
                zone.setIndustrialJobDensity(Double.parseDouble(feature.getAttribute("totalJobIn").toString())-Double.parseDouble(feature.getAttribute("uliJobInBu").toString()));
            }else{
                logger.warn("zoneId " + zoneId + " doesn't exist in moped zone system");
            }
        }
    }
}
