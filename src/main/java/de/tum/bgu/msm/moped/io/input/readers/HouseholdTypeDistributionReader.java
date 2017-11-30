package de.tum.bgu.msm.moped.io.input.readers;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.io.input.CSVReader;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.util.MoPeDUtil;

import java.util.Collection;

public class HouseholdTypeDistributionReader extends CSVReader{

    private Table<Integer, Integer, Double> distribution;

    public HouseholdTypeDistributionReader(DataSet dataSet) { super(dataSet);}

    private int zoneIndex;

    @Override
    public void read() {
        Collection<Integer> zones = dataSet.getZones().keySet();
        Collection<Integer> households = dataSet.getHhTypes().keySet();
        distribution = ArrayTable.create(zones, households);
        super.read(Properties.get().HOUSEHOLDTYPEDISTRIBUTION, ",");
        dataSet.setDistribution(distribution);
    }

    @Override
    protected void processHeader(String[] header) {
        zoneIndex = MoPeDUtil.findPositionInArray("ZoneId", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int zoneId = Integer.parseInt(record[zoneIndex]);
        for (int id=1; id< record.length; id++){
            distribution.put(zoneId, id, Double.parseDouble(record[id]));
        }
    }
}
