package de.tum.bgu.msm.moped.io.input.readers;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.SuperPAZ;
import de.tum.bgu.msm.moped.io.input.CSVReader;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import de.tum.bgu.msm.moped.util.MoPeDUtil;

import java.util.Collection;

public class SuperPAZImpedanceReader extends CSVReader{

    private Table<Long, Long, Double> impedance;

    public SuperPAZImpedanceReader(DataSet dataSet) { super(dataSet);}

    private int originIndex;
    private int destinationIndex;
    private int distanceIndex;

    @Override
    public void read() {
        impedance = HashBasedTable.create();
        super.read(Resources.INSTANCE.getString(Properties.SUPERPAZIMPEDANCE), ",");
        dataSet.setImpedance(impedance);
    }

    @Override
    protected void processHeader(String[] header) {
        originIndex = MoPeDUtil.findPositionInArray("superPAZ_o", header);
        destinationIndex = MoPeDUtil.findPositionInArray("superPAZ_d", header);
        distanceIndex = MoPeDUtil.findPositionInArray("dist", header);
    }

    @Override
    protected void processRecord(String[] record) {
        long origin = Long.parseLong(record[originIndex]);
        long destination = Long.parseLong(record[destinationIndex]);
        double distance = Double.parseDouble(record[distanceIndex]);
        SuperPAZ superPAZ = dataSet.getDestinationSuperPAZ(destination);
        if (superPAZ == null){
            superPAZ = new SuperPAZ(destination, "DESTINATION");
        }
        dataSet.addDestinationSuperPAZ(superPAZ);
        impedance.put(origin, destination, distance);
    }
}
