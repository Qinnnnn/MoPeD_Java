package de.tum.bgu.msm.moped.io.input.readers;

import cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.io.input.OMXReader;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import org.apache.log4j.Logger;
import org.jblas.FloatMatrix;

@Deprecated
public class DistanceOMXReader extends OMXReader {

    private static final Logger logger = Logger.getLogger(DistanceOMXReader.class);
    private SparseFloatMatrix2D impedance;
    public DistanceOMXReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        logger.info("  Reading paz distance");
        readWalkingDistance();
    }

    private void readWalkingDistance() {
        impedance = new SparseFloatMatrix2D(dataSet.getZones().size()+1, dataSet.getZones().size()+1);
        dataSet.setPAZImpedance(impedance);
        super.readAndConvertToMatrix(Resources.INSTANCE.getString(Properties.PAZIMPEDANCE), "mat1");
        System.out.println(dataSet.getPAZImpedance().get(0,0));
        System.out.println(dataSet.getPAZImpedance().get(1,1));
        System.out.println(dataSet.getPAZImpedance().get(2,1));
        System.out.println(dataSet.getPAZImpedance().get(1,2));
        System.out.println(1);

    }


}
