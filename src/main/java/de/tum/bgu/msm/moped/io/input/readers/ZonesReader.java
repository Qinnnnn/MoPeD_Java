package de.tum.bgu.msm.moped.io.input.readers;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.SuperPAZ;
import de.tum.bgu.msm.moped.data.Zone;
import de.tum.bgu.msm.moped.io.input.CSVReader;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import de.tum.bgu.msm.moped.util.MoPeDUtil;

public class ZonesReader extends CSVReader {
    private int idIndex;
    private int superPAZIndex;

    public ZonesReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        super.read(Resources.INSTANCE.getString(Properties.ZONES), ",");
    }

    @Override
    protected void processHeader(String[] header) {
        idIndex = MoPeDUtil.findPositionInArray("zoneID", header);
        superPAZIndex = MoPeDUtil.findPositionInArray("superPAZID", header);
    }

    @Override
    protected void processRecord(String[] record) {
        long zoneId = Long.parseLong(record[idIndex]);
        long superPAZID = Long.parseLong(record[superPAZIndex]);
        Zone zone = new Zone(zoneId, superPAZID);
        SuperPAZ superPAZ = dataSet.getOriginSuperPAZ(superPAZID);
        if (superPAZ == null){
            superPAZ = new SuperPAZ(superPAZID, "ORIGIN");
        }
        superPAZ.getPazs().put(zoneId,zone);
        dataSet.addOriginSuperPAZ(superPAZ);
        dataSet.addZone(zone);
    }
}
