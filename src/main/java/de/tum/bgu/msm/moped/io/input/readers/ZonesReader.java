package de.tum.bgu.msm.moped.io.input.readers;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.Zone;
import de.tum.bgu.msm.moped.io.input.CSVReader;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import de.tum.bgu.msm.moped.util.MoPeDUtil;

public class ZonesReader extends CSVReader {
    private int idIndex;

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
    }

    @Override
    protected void processRecord(String[] record) {
        int zoneId = Integer.parseInt(record[idIndex]);
        Zone zone = new Zone(zoneId);
        dataSet.addZone(zone);
    }
}
