package de.tum.bgu.msm.moped.io.input.readers;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.MopedZone;
import de.tum.bgu.msm.moped.io.input.CSVReader;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import org.jblas.FloatMatrix;

public class HouseholdTypeDistributionReader extends CSVReader{

    private FloatMatrix distribution;

    public HouseholdTypeDistributionReader(DataSet dataSet) { super(dataSet);}

    private int zoneIndex;

    @Override
    public void read() {
        distribution = new FloatMatrix(dataSet.getOriginPAZs().size(), dataSet.getHOUSEHOLDTYPESIZE());
        super.read(Resources.INSTANCE.getString(Properties.HOUSEHOLDTYPEDISTRIBUTION), ",");
//        super.read(Resources.INSTANCE.getString(Properties.HOUSEHOLDTYPEDISTRIBUTION1), ",");
//        super.read(Resources.INSTANCE.getString(Properties.HOUSEHOLDTYPEDISTRIBUTION2), ",");
////        super.read(Resources.INSTANCE.getString(Properties.HOUSEHOLDTYPEDISTRIBUTION3), ",");
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
                        distribution.put(zone.getIndex(), id, Float.parseFloat(record[id]));
                    }
                }
            }

        }
    }
}
