package de.tum.bgu.msm.moped.io.input.readers;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.SuperPAZ;
import de.tum.bgu.msm.moped.io.input.CSVReader;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import de.tum.bgu.msm.moped.util.MoPeDUtil;
import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.jblas.FloatMatrix;

public class SuperPAZImpedanceReader extends CSVReader{

    private FloatMatrix impedance;

    public SuperPAZImpedanceReader(DataSet dataSet) { super(dataSet);}

    private int originIndex;
    private int destinationIndex;
    private int distanceIndex;

    @Override
    public void read() {
        //impedance = new OpenMapRealMatrix(dataSet.getSuperPAZs().size(), dataSet.getDestinationSuperPAZs().size());
        impedance = new FloatMatrix(60110, dataSet.getDestinationSuperPAZs().size());
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
        int origin = Integer.parseInt(record[originIndex]);
        int destination = Integer.parseInt(record[destinationIndex]);
        double distance = Double.parseDouble(record[distanceIndex]);
        float distanceInMile = (float)distance / 5280.0f;
        SuperPAZ originSuperPAZ = dataSet.getSuperPAZ(origin);
        SuperPAZ destinationSuperPAZ = dataSet.getSuperPAZ(destination);
        if ((originSuperPAZ.getHousehold() != 0.0) & (destinationSuperPAZ.getTotalEmpl() != 0.0)&(distanceInMile <= 3.0)) {
            distanceInMile = Math.max(440.0f / 5280.0f,distanceInMile);
            impedance.put(origin, destinationSuperPAZ.getIndex(), distanceInMile);
            originSuperPAZ.getImpedanceToSuperPAZs().put(destinationSuperPAZ.getIndex(), distanceInMile);
        }
    }
}
