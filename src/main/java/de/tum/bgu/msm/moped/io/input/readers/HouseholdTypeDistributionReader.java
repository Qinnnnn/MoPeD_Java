package de.tum.bgu.msm.moped.io.input.readers;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.MopedZone;
import de.tum.bgu.msm.moped.io.input.CSVReader;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import org.apache.log4j.Logger;
import org.jblas.FloatMatrix;

public class HouseholdTypeDistributionReader extends CSVReader{

    private FloatMatrix distribution;

    public HouseholdTypeDistributionReader(DataSet dataSet) { super(dataSet);}

    private int zoneIndex;

    private static final Logger logger = Logger.getLogger(HouseholdTypeDistributionReader.class);


    @Override
    public void read() {
        logger.info(" Reading household distribution.");
        distribution = new FloatMatrix(dataSet.getOriginPAZs().size(), dataSet.getHOUSEHOLDTYPESIZE());
        super.read(Resources.INSTANCE.getString(Properties.HOUSEHOLDTYPEDISTRIBUTION), ",");
//        super.read(Resources.INSTANCE.getString(Properties.HOUSEHOLDTYPEDISTRIBUTION1), ",");
//        super.read(Resources.INSTANCE.getString(Properties.HOUSEHOLDTYPEDISTRIBUTION2), ",");
//        super.read(Resources.INSTANCE.getString(Properties.HOUSEHOLDTYPEDISTRIBUTION3), ",");
//        super.read(Resources.INSTANCE.getString(Properties.HOUSEHOLDTYPEDISTRIBUTION4), ",");
        dataSet.setDistribution(distribution);
    }

    @Override
    protected void processHeader(String[] header) {
        zoneIndex = 0;
    }

    @Override
    protected void processRecord(String[] record) {
        int zoneId = Integer.parseInt(record[zoneIndex]);
        MopedZone zone = dataSet.getZone(zoneId);

        if (zone != null) {
            if (zone.getTotalHH() != 0.0){
                for (int id = 1; id < record.length; id++) {
                    if (dataSet.getHouseholdType(id) != null) {
                        float hh = Float.parseFloat(record[id]);
                        //float scenariohh = hh*(1+hh/dataSet.getTotalPop()*20000);
                        //float scenariohh = (float) (Float.parseFloat(record[id])*(1+0.02));
                        //float scenariohh = Float.parseFloat(record[id])*zone.getGrowthRate();
                        distribution.put(zone.getIndex(), id, hh);
                        //distribution.put(zone.getIndex(), id, scenariohh);
                    }
                }
            }

        }
    }
}
