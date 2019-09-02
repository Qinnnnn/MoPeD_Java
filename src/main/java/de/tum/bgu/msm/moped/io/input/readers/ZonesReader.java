package de.tum.bgu.msm.moped.io.input.readers;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.MopedZone;
import de.tum.bgu.msm.moped.data.SuperPAZ;
import de.tum.bgu.msm.moped.io.input.CSVReader;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import de.tum.bgu.msm.moped.util.MoPeDUtil;
import org.apache.log4j.Logger;
import org.opengis.feature.simple.SimpleFeature;

public class ZonesReader extends CSVReader {
    private static final Logger logger = Logger.getLogger(ZonesReader.class);

    private int idIndex;
    private int superPAZIndex;
    private int totalHouseholdIndex;
    private int index = 0;

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
        superPAZIndex = MoPeDUtil.findPositionInArray("superPAZID", header);
        totalHouseholdIndex = MoPeDUtil.findPositionInArray("totalHH", header);

    }

    @Override
    protected void processRecord(String[] record) {
        int zoneId = Integer.parseInt(record[idIndex]);
        int superPAZID = Integer.parseInt(record[superPAZIndex]);
        float totalHH = Float.parseFloat(record[totalHouseholdIndex]);

        MopedZone zone = new MopedZone(zoneId, superPAZID, totalHH);
        dataSet.addZone(zone);

        if (totalHH != 0.0){
            SuperPAZ superPAZ = dataSet.getSuperPAZ(superPAZID);
            if (superPAZ == null){
                superPAZ = new SuperPAZ(superPAZID, "ORIGIN");
                dataSet.addSuperPAZ(superPAZ);
            }
            superPAZ.getPazs().put(zoneId,zone);
            zone.setIndex(index);
            dataSet.addOriginPAZ(index, zone);
            index++;
        }
    }


    //TODO: like the one in MITO
    public static void mapFeaturesToZones(DataSet dataSet) {

    }
}
