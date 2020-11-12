package de.tum.bgu.msm.moped.io.input.readers;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.io.input.CSVReader;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import de.tum.bgu.msm.moped.util.MoPeDUtil;
import org.apache.log4j.Logger;

public class PIEReader extends CSVReader {
    private int idIndex;
    private int pieEmplIndex;
    private int piePopIndex;
    private static final Logger logger = Logger.getLogger(PIEReader.class);

    public PIEReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        logger.info("reading PIE data");
        super.read(Resources.INSTANCE.getString(Properties.PIE), ",");
    }

    @Override
    protected void processHeader(String[] header) {
        idIndex = MoPeDUtil.findPositionInArray("zoneID", header);
        pieEmplIndex = MoPeDUtil.findPositionInArray("non_industrial", header);
        piePopIndex = MoPeDUtil.findPositionInArray("pop", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int zoneId = Integer.parseInt(record[idIndex]);
        if (dataSet.getZone(zoneId) != null) {
            float pieEmpl = Float.parseFloat(record[pieEmplIndex]);
            float piePop = Float.parseFloat(record[piePopIndex]);
            dataSet.getZone(zoneId).setPieEmpl(pieEmpl);
            dataSet.getZone(zoneId).setPiePop(piePop);
        }
    }
}
