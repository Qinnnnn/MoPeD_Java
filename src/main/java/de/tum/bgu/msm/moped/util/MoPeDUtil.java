package de.tum.bgu.msm.moped.util;

import cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D;
import com.pb.common.util.ResourceUtil;
import omx.OmxMatrix;
import omx.hdf5.OmxHdf5Datatype;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.ResourceBundle;


public class MoPeDUtil {

    private static final Logger logger = Logger.getLogger(MoPeDUtil.class);
    private static String baseDirectory = "";


    public static int findPositionInArray(String element, String[] arr) {
        // return index position of element in array arr
        int ind = -1;
        for (int a = 0; a < arr.length; a++) if (arr[a].equalsIgnoreCase(element)) ind = a;
        if (ind == -1) logger.error("Could not find element " + element +
                " in array (see method <findPositionInArray> in class <SiloUtil>");
        return ind;
    }

    public static float rounder(float value, int digits) {
        // rounds value to digits behind the decimal point
        return Math.round(value * Math.pow(10, digits) + 0.5) / (float) Math.pow(10, digits);
    }

    public static ResourceBundle createResourceBundle(String fileName) {
        // read properties file and return as ResourceBundle
        File propFile = new File(fileName);
        return ResourceUtil.getPropertyBundle(propFile);
    }

    public static void setBaseDirectory(String baseDirectoryInput) {
        baseDirectory = baseDirectoryInput;
    }

    public static SparseFloatMatrix2D convertOmxToMatrix(OmxMatrix omxMatrix) {
        // convert OMX matrix into java matrix

        OmxHdf5Datatype.OmxJavaType type = omxMatrix.getOmxJavaType();
        String name = omxMatrix.getName();
        int[] dimensions = omxMatrix.getShape();
        if (type.equals(OmxHdf5Datatype.OmxJavaType.FLOAT)) {
            float[][] fArray = (float[][]) omxMatrix.getData();
            SparseFloatMatrix2D mat = new SparseFloatMatrix2D(dimensions[0], dimensions[1],157756*120, 0.8f,0.9f);
            for (int i = 0; i < dimensions[0]; i++) {
                for (int j = 0; j < dimensions[1]; j++) {
                   // mat.setQuick(i + 1, j + 1, fArray[i][j]);
                    if ((i < j) || (fArray[i][j] == 0.f)) {
                        break;
                    }
                    if (i == j) {

                    } else {

                    }

                }
            }
            return mat;
        } else if (type.equals(OmxHdf5Datatype.OmxJavaType.DOUBLE)) {
            double[][] dArray = (double[][]) omxMatrix.getData();
            SparseFloatMatrix2D mat = new SparseFloatMatrix2D(dimensions[0], dimensions[1],157756*120, 0.8f,0.9f);
            for (int i = 0; i < dimensions[0]; i++) {
                for (int j = 0; j < dimensions[1]; j++) {
                   // mat.setQuick(i + 1, j + 1, (float) dArray[i][j]);
                }
            }
            return mat;
        } else {
            logger.info("OMX Matrix type " + type.toString() + " not yet implemented. Program exits.");
            System.exit(1);
            return null;
        }
    }
}
