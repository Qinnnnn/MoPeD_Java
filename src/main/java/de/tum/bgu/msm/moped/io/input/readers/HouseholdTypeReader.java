package de.tum.bgu.msm.moped.io.input.readers;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.HouseholdType;
import de.tum.bgu.msm.moped.io.input.CSVReader;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import de.tum.bgu.msm.moped.util.MoPeDUtil;
import org.apache.log4j.Logger;

public class HouseholdTypeReader extends CSVReader {

    private static final Logger logger = Logger.getLogger(HouseholdTypeReader.class);

    private int idIndex;
    private int nameIndex;
    private int kIndex;
    private int cIndex;
    private int wIndex;
    private int hIndex;
    private int iIndex;
    private int aIndex;


    public HouseholdTypeReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        logger.info("  Reading householdType from kcwhia file");
        super.read(Resources.INSTANCE.getString(Properties.HOUSEHOLDTYPE), ",");
    }

    @Override
    protected void processHeader(String[] header) {

        idIndex = MoPeDUtil.findPositionInArray("id", header);
        nameIndex = MoPeDUtil.findPositionInArray("name", header);
        kIndex = MoPeDUtil.findPositionInArray("k", header);
        cIndex = MoPeDUtil.findPositionInArray("c", header);
        wIndex = MoPeDUtil.findPositionInArray("w", header);
        hIndex = MoPeDUtil.findPositionInArray("h", header);
        iIndex = MoPeDUtil.findPositionInArray("i", header);
        aIndex = MoPeDUtil.findPositionInArray("a", header);

    }

    @Override
    protected void processRecord(String[] record) {
        int householdId = Integer.parseInt(record[idIndex]);
        String name = record[nameIndex];
        int kids = Integer.parseInt(record[kIndex]);
        int cars = Integer.parseInt(record[cIndex]);
        int workers = Integer.parseInt(record[wIndex]);
        int householdSize = Integer.parseInt(record[hIndex]);
        int income = Integer.parseInt(record[iIndex]);
        int age = Integer.parseInt(record[aIndex]);

        if((workers > householdSize) || (kids > householdSize)){
            //logger.warn("householdTypeID" + householdId + " is an invalid household type. " + workers + "Workers, " + kids + "Kids, " + householdSize + "Household Size.");
            return;
        } else {
            HouseholdType hhType = new HouseholdType(name, householdId, kids, cars, workers, householdSize, income, age);
            dataSet.addHouseholdType(hhType);
        }

    }

}
