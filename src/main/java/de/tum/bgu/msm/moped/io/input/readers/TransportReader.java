package de.tum.bgu.msm.moped.io.input.readers;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.io.input.CSVReader;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import de.tum.bgu.msm.moped.util.MoPeDUtil;

public class TransportReader extends CSVReader {
    private int idIndex;
    private int waIndex;
    private int stfwyIndex;
    private int trailIndex;

    public TransportReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        super.read(Resources.INSTANCE.getString(Properties.TRANSPORT), ",");
    }

    @Override
    protected void processHeader(String[] header) {
        idIndex = MoPeDUtil.findPositionInArray("zoneID", header);
        waIndex = MoPeDUtil.findPositionInArray("WA", header);
        stfwyIndex = MoPeDUtil.findPositionInArray("stfwy", header);
        trailIndex = MoPeDUtil.findPositionInArray("trail", header);
    }

    @Override
    protected void processRecord(String[] record) {
        long zoneId = Long.parseLong(record[idIndex]);
        int wa = Integer.parseInt(record[waIndex]);
        double stfwy = Double.parseDouble(record[stfwyIndex]);
        double trail = Double.parseDouble(record[trailIndex]);

        dataSet.getZone(zoneId).setWa(wa);
        dataSet.getZone(zoneId).setStfwy(stfwy);
        dataSet.getZone(zoneId).setTrail(trail);
    }
}
