package de.tum.bgu.msm.moped.io.input.readers;

import cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.SuperPAZ;
import de.tum.bgu.msm.moped.io.input.CSVReader;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import de.tum.bgu.msm.moped.util.MoPeDUtil;
import org.apache.log4j.Logger;
import org.jblas.FloatMatrix;

public class SuperPAZImpedanceReader extends CSVReader{

    private FloatMatrix impedance;

    public SuperPAZImpedanceReader(DataSet dataSet) { super(dataSet);}

    private int originIndex;
    private int destinationIndex;
    private int distanceIndex;
    private static final Logger logger = Logger.getLogger(SuperPAZImpedanceReader.class);


    @Override
    public void read() {
        logger.info(" Reading superPAZ impedance");
        impedance = new FloatMatrix(dataSet.getSuperPAZs().size(), dataSet.getDestinationSuperPAZs().size());
        //impedance = new FloatMatrix(60110, dataSet.getDestinationSuperPAZs().size());
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
        //float distanceInMile = (float)distance / 5280.0f;
        float distanceInKM = (float)distance /3280.84f;
        SuperPAZ originSuperPAZ = dataSet.getSuperPAZ(origin);
        SuperPAZ destinationSuperPAZ = dataSet.getSuperPAZ(destination);
        if ((originSuperPAZ.getHousehold() != 0.0) & (destinationSuperPAZ.getTotalEmpl() != 0.0)&(distanceInKM <= 4.8)) {
            //distanceInMile = Math.max(440.0f / 5280.0f,distanceInMile);
            distanceInKM = Math.max(440.0f / 3280.84f,distanceInKM);
            impedance.put(origin, destinationSuperPAZ.getIndex(), distanceInKM);
            //originSuperPAZ.getImpedanceToSuperPAZs().put(destinationSuperPAZ.getIndex(), distanceInMile);
        }
    }
}
