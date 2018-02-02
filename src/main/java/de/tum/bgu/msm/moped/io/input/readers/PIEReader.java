package de.tum.bgu.msm.moped.io.input.readers;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.io.input.CSVReader;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import de.tum.bgu.msm.moped.util.MoPeDUtil;

public class PIEReader extends CSVReader {
    private int idIndex;
    private int pieIndex;
    private int pieFlagIndex;

    public PIEReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        super.read(Resources.INSTANCE.getString(Properties.PIE), ",");
    }

    @Override
    protected void processHeader(String[] header) {
        idIndex = MoPeDUtil.findPositionInArray("zoneID", header);
        pieIndex = MoPeDUtil.findPositionInArray("pie", header);
        pieFlagIndex = MoPeDUtil.findPositionInArray("pie_flag", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int zoneId = Integer.parseInt(record[idIndex]);
        if (dataSet.getZone(zoneId) != null) {
            float pie = Float.parseFloat(record[pieIndex]);
            int pieFlag = Integer.parseInt(record[pieFlagIndex]);
            dataSet.getZone(zoneId).setPie(pie);
            dataSet.getZone(zoneId).setPieFlag(pieFlag);
        }
    }
}
