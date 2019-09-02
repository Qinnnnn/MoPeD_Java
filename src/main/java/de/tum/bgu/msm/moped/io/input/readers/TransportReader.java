package de.tum.bgu.msm.moped.io.input.readers;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.io.input.CSVReader;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import de.tum.bgu.msm.moped.util.MoPeDUtil;
import org.apache.log4j.Logger;

public class TransportReader extends CSVReader {
    private int idIndex;
    private int waIndex;
    private int stfwyIndex;
    private static final Logger logger = Logger.getLogger(TransportReader.class);

    public TransportReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        logger.info("  Reading transport attributes from transport file");
        super.read(Resources.INSTANCE.getString(Properties.TRANSPORT), ",");
    }

    @Override
    protected void processHeader(String[] header) {
        idIndex = MoPeDUtil.findPositionInArray("zoneID", header);
        waIndex = MoPeDUtil.findPositionInArray("WA", header);
        stfwyIndex = MoPeDUtil.findPositionInArray("stfwy", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int zoneId = Integer.parseInt(record[idIndex]);
        if (dataSet.getZone(zoneId) != null) {
            int wa = Integer.parseInt(record[waIndex]);
            float stfwy = Float.parseFloat(record[stfwyIndex]);
            dataSet.getZone(zoneId).setWa(wa);
            dataSet.getZone(zoneId).setStfwy(stfwy);
        }
    }
}
