package de.tum.bgu.msm.moped.io.input.readers;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.io.input.CSVReader;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import de.tum.bgu.msm.moped.util.MoPeDUtil;

public class PIEReader extends CSVReader {
    private int idIndex;
    private int tazIndex;
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
        tazIndex = MoPeDUtil.findPositionInArray("TAZ", header);
        pieIndex = MoPeDUtil.findPositionInArray("pie", header);
        pieFlagIndex = MoPeDUtil.findPositionInArray("pie_flag", header);
    }

    @Override
    protected void processRecord(String[] record) {
        long zoneId = Long.parseLong(record[idIndex]);
        int taz = Integer.parseInt(record[tazIndex]);
        double pie = Double.parseDouble(record[pieIndex]);
        int pieFlag = Integer.parseInt(record[pieFlagIndex]);

        dataSet.getZone(zoneId).setTazId(taz);
        dataSet.getZone(zoneId).setPie(pie);
        dataSet.getZone(zoneId).setPieFlag(pieFlag);
    }
}
