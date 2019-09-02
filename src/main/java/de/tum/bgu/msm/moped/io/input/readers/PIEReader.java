package de.tum.bgu.msm.moped.io.input.readers;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.io.input.CSVReader;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import de.tum.bgu.msm.moped.util.MoPeDUtil;
import org.apache.log4j.Logger;

public class PIEReader extends CSVReader {
    private int idIndex;
    private int pieIndex;
    private int pieFlagIndex;
    private int pieEmplIndex;
    private int pieActivityIndex;
    private int pieAreaIndex;
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
        pieIndex = MoPeDUtil.findPositionInArray("pie", header);
        pieFlagIndex = MoPeDUtil.findPositionInArray("pie_flag", header);
        pieEmplIndex = MoPeDUtil.findPositionInArray("empl", header);
        piePopIndex = MoPeDUtil.findPositionInArray("pop", header);
        pieActivityIndex = MoPeDUtil.findPositionInArray("activity", header);
        pieAreaIndex = MoPeDUtil.findPositionInArray("area", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int zoneId = Integer.parseInt(record[idIndex]);
        if (dataSet.getZone(zoneId) != null) {
            float pie = Float.parseFloat(record[pieIndex]);
            int pieFlag = Integer.parseInt(record[pieFlagIndex]);
            float pieEmpl = Float.parseFloat(record[pieEmplIndex]);
            float pieArea = Float.parseFloat(record[pieAreaIndex]);
            float pieActivity = Float.parseFloat(record[pieActivityIndex]);
            float piePop = Float.parseFloat(record[piePopIndex]);

            dataSet.getZone(zoneId).setPie(pie);
            dataSet.getZone(zoneId).setPieFlag(pieFlag);
            dataSet.getZone(zoneId).setPieEmpl(pieEmpl);
            dataSet.getZone(zoneId).setPieArea(pieArea);
            dataSet.getZone(zoneId).setPieActivity(pieActivity);
            dataSet.getZone(zoneId).setPiePop(piePop);
        }
    }
}
