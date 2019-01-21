package de.tum.bgu.msm.moped.io.input;

import cern.colt.function.tfloat.FloatFunction;
import cern.colt.matrix.tdouble.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.tfloat.impl.DenseFloatMatrix2D;
import cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.Purpose;
import omx.OmxFile;
import omx.OmxMatrix;
import omx.hdf5.OmxHdf5Datatype;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nico on 19.07.2017.
 */
public abstract class OMXReader extends AbstractInputReader {

    protected OMXReader(DataSet dataSet) {
        super(dataSet);
    }

    private float distanceInMile;

    private final Map<Integer, Float> coefByScale = new HashMap<Integer, Float>(){{
        put(80, 0.02485f);
        put(160, 0.04970969536f);
        put(240, 0.07456454304f);
        put(320, 0.09941939072f);
        put(400, 0.1242742384f);
        put(800, 0.2485484768f);
        put(1200, 0.3728227152f);
        put(2400, 0.7456454304f);
    }};

    private final int PAZ = 320;

    protected void readAndConvertToMatrix(String fileName, String matrixName) {
        OmxFile distanceOmx = new OmxFile(fileName);
        distanceOmx.openReadOnly();
        OmxMatrix omxMatrix = distanceOmx.getMatrix(matrixName);
        OmxHdf5Datatype.OmxJavaType type = omxMatrix.getOmxJavaType();
        String name = omxMatrix.getName();
        int[] dimensions = omxMatrix.getShape();
        if (type.equals(OmxHdf5Datatype.OmxJavaType.FLOAT)) {
            float[][] fArray = (float[][]) omxMatrix.getData();
            //new SparseFloatMatrix2D(fArray).assign(argument -> argument / 5280.0f * 1609.344f);

            for (int i = 0; i < dimensions[0]; i++) {

                if (dataSet.getSuperPAZ(i + 1) == null) {
                    continue;
                }

                for (int j = 0; j < dimensions[1]; j++) {

                    if (i < j) {
                        break;
                    }

                    if (dataSet.getSuperPAZ(j + 1) == null) {
                        continue;
                    }
                    // mat.setQuick(i + 1, j + 1, fArray[i][j]);

                    if (fArray[i][j] == 0.0f) {
                        continue;
                    }

                    if (fArray[i][j] > 15840.0f) {
                        continue;
                    }

                    if (i == j) {

                        distanceInMile = coefByScale.get(PAZ);
                        //distanceInMile = fArray[i][j]*3/5280.0f;
                        //dataSet.getSuperPAZ(i + 1).getImpedanceToSuperPAZs().put(j + 1,(short) distanceInMile);
                        dataSet.getImpedance().setQuick(i + 1, j + 1, distanceInMile);
                    } else {
                        //distanceInMile = fArray[i][j] *0.0006213f;
                        //                  if (((i+1)==6775) && ((j+1) == 6547)){
                        //                    distanceInMile = fArray[i][j] / 5280.0f;
                        //                  System.out.println("1");
                        //                System.out.println("2");
                        //          }
                        distanceInMile = fArray[i][j] / 5280.0f;
                        dataSet.getImpedance().setQuick(i + 1, j + 1, Math.max(coefByScale.get(PAZ), distanceInMile));
                        dataSet.getImpedance().setQuick(j + 1, i + 1, Math.max(coefByScale.get(PAZ), distanceInMile));
                        //dataSet.getSuperPAZ(i + 1).getImpedanceToSuperPAZs().put(j + 1, (short) distanceInMile);
                        //dataSet.getSuperPAZ(j + 1).getImpedanceToSuperPAZs().put(i + 1, (short) distanceInMile);


                    }

                }
            }

        } else if (type.equals(OmxHdf5Datatype.OmxJavaType.DOUBLE)) {
            double[][] dArray = (double[][]) omxMatrix.getData();
            //SparseFloatMatrix2D mat = new SparseFloatMatrix2D(dimensions[0], dimensions[1],157756*120, 0.8f,0.9f);
            for (int i = 0; i < dimensions[0]; i++) {
                for (int j = 0; j < dimensions[1]; j++) {
                    // mat.setQuick(i + 1, j + 1, (float) dArray[i][j]);
                }
            }

        } else {
            //logger.info("OMX Matrix type " + type.toString() + " not yet implemented. Program exits.");
            System.exit(1);
            //return null;
        }
        }
    }