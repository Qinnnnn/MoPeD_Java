package de.tum.bgu.msm.moped.io.input.readers;

import cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.io.input.OMXReader;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import org.apache.log4j.Logger;
import org.jblas.FloatMatrix;

public class DistanceOMXReader extends OMXReader {

    private static final Logger logger = Logger.getLogger(DistanceOMXReader.class);
    private SparseFloatMatrix2D impedance;
    public DistanceOMXReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        logger.info("  Reading network distance");
        readWalkingDistance();
    }

    private void readWalkingDistance() {
        System.out.println(dataSet.getSuperPAZs().size());
        impedance = new SparseFloatMatrix2D(dataSet.getSuperPAZs().size()+1, dataSet.getSuperPAZs().size()+1);
        //impedance = new SparseFloatMatrix2D(1814, 1814);
        dataSet.setImpedance(impedance);
        super.readAndConvertToMatrix(Resources.INSTANCE.getString(Properties.SUPERPAZIMPEDANCE), "mat1");

    }


}
