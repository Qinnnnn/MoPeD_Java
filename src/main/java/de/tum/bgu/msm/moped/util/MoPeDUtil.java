package de.tum.bgu.msm.moped.util;

import cern.colt.map.tdouble.OpenIntDoubleHashMap;
import cern.colt.map.tfloat.OpenIntFloatHashMap;
import cern.colt.matrix.tfloat.impl.SparseFloatMatrix2D;
import cern.jet.stat.tdouble.DoubleDescriptive;
import cern.jet.stat.tfloat.FloatDescriptive;
import de.tum.bgu.msm.common.util.ResourceUtil;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import omx.OmxMatrix;
import omx.hdf5.OmxHdf5Datatype;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;


public class MoPeDUtil {

    private static final Logger logger = Logger.getLogger(MoPeDUtil.class);
    private static String baseDirectory = "";
    private static Random rand;


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

    public static Random getRandomObject() {
        return rand;
    }

    public static int select(List<Double> probabilities, Random random) {
        // select item based on probabilities (for zero-based double array)
        double selPos = getSum(probabilities) * random.nextDouble();
        double sum = 0;
        for (ListIterator<Double> it = probabilities.listIterator(); it.hasNext();) {
            sum += it.next();
            if (sum > selPos) {
                return it.previousIndex();
            }
        }
        return -1;
    }

    public static int select(double[] probabilities, Random random) {
        // select item based on probabilities (for zero-based double array)
        return select(probabilities, random, getSum(probabilities));
    }

    public static int select(double[] probabilities, Random random, double probabilitySum) {
        // select item based on probabilities (for zero-based double array)
        double selPos =probabilitySum * random.nextDouble();
        double sum = 0;
        for (int i = 0; i < probabilities.length; i++) {
            sum += probabilities[i];
            if (sum > selPos) {
                return i;
            }
        }
        return probabilities.length - 1;
    }

    public static int select(float[] probabilities, Random random, float probabilitySum) {
        // select item based on probabilities (for zero-based double array)
        double selPos =probabilitySum * random.nextDouble();
        double sum = 0;
        for (int i = 0; i < probabilities.length; i++) {
            sum += probabilities[i];
            if (sum > selPos) {
                return i;
            }
        }
        return probabilities.length - 1;
    }

    public static int select(double[] probabilities, double sum) {
        double selPos = sum * rand.nextDouble();
        double tempSum = 0;
        for (int i = 0; i < probabilities.length; i++) {
            tempSum += probabilities[i];
            if (tempSum > selPos) {
                return i;
            }
        }
        return probabilities.length - 1;
    }

    public static int select(double[] probabilities) {
        // select item based on probabilities (for zero-based double array)
        return select(probabilities, getSum(probabilities));
    }

    public static int select(float[] probabilities, Random random) {
        double selPos = getSum(probabilities) * random.nextDouble();
        double sum = 0;
        for (int i = 0; i < probabilities.length; i++) {
            sum += probabilities[i];
            if (sum > selPos) {
                return i;
            }
        }
        return probabilities.length - 1;
    }

    public static <T> T select(Map<T, Double> mappedProbabilities) {
        // select item based on probabilities (for mapped double probabilities)
        return select(mappedProbabilities, getSum(mappedProbabilities.values()));
    }

    public static <T> T select(Map<T, Double> mappedProbabilities, double sum) {
        return select(mappedProbabilities, rand, sum);
    }

    public static <T> T select(Map<T, Double> mappedProbabilities, Random random) {
        return select(mappedProbabilities, random, getSum(mappedProbabilities.values()));
    }

    public static <T> T select(Map<T, Double> probabilities, Random random, double sum) {
        // select item based on probabilities (for mapped double probabilities)
        double selectedWeight = random.nextDouble() * sum;
        double select = 0;
        for (Map.Entry<T, Double> entry : probabilities.entrySet()) {
            select += entry.getValue();
            if (select > selectedWeight) {
                return entry.getKey();
            }
        }
        throw new RuntimeException("Error selecting item from weighted probabilities");
    }



    public static <T> T select(Random rand, T... objects) {
        return objects[rand.nextInt(objects.length)];
    }

    public static <T> T select(List<T> objects) {
        return objects.get(rand.nextInt(objects.size()));
    }

    public static <T> T select(Random rand, List<T> objects) {
        return objects.get(rand.nextInt(objects.size()));
    }

    public static Integer getSum(Integer[] array) {
        Integer sm = 0;
        for (Integer value : array) {
            sm += value;
        }
        return sm;
    }

    public static float getSum(float[] array) {
        float sm = 0;
        for (float value : array) {
            sm += value;
        }
        return sm;
    }

    public static double getSum(double[] array) {
        double sum = 0;
        for (double val : array) {
            sum += val;
        }
        return sum;
    }


    private static double getSum(Collection<Double> values) {
        double sm = 0;
        for (Double value : values) {
            sm += value;
        }
        return sm;
    }

    public static void initializeRandomNumber() {
        int seed = Resources.INSTANCE.getInt(Properties.RANDOM_SEED);
        rand = new Random(seed);
    }

    public static int select(OpenIntDoubleHashMap openIntDoubleHashMap, Random rand) {
        // select item based on probabilities (for mapped double probabilities)
        double sum = DoubleDescriptive.sum(openIntDoubleHashMap.values());

        double selectedWeight = rand.nextDouble() * sum;
        double select = 0;
        for (int i: openIntDoubleHashMap.keys().elements()) {
            select += openIntDoubleHashMap.get(i);
            if (select > selectedWeight) {
                return i;
            }
        }
        throw new RuntimeException("Error selecting item from weighted probabilities");
    }

    public static synchronized int select(OpenIntFloatHashMap openIntFloatHashMap, Random rand) {
        // select item based on probabilities (for mapped double probabilities)
        float sum = FloatDescriptive.sum(openIntFloatHashMap.values());

        float selectedWeight = rand.nextFloat() * sum;
        float select = 0;
        for (int i: openIntFloatHashMap.keys().elements()) {
            select += openIntFloatHashMap.get(i);
            if (select > selectedWeight) {
                return i;
            }
        }
        throw new RuntimeException("Error selecting item from weighted probabilities");
    }

    public static synchronized int select(int zone, OpenIntFloatHashMap openIntFloatHashMap, Random rand) {
        // select item based on probabilities (for mapped double probabilities)
        if(openIntFloatHashMap.size()==1){
            int i = openIntFloatHashMap.keys().get(0);
            logger.warn("For zone: " + zone + " there is only one alternative destination zone in the map: " + i);
            return i;
        }

        float sum = FloatDescriptive.sum(openIntFloatHashMap.values());
        if (sum > 1){
            //logger.warn("Error selecting item from weighted probabilities for zone:  " + zone + "sum utility: " + sum);
            sum = 1.f;
        }

        float selectedWeight = rand.nextFloat() * sum;
        float select = 0;
        for (int i: openIntFloatHashMap.keys().elements()) {
            select += openIntFloatHashMap.get(i);
            if (select > selectedWeight) {
                return i;
            }
        }
        throw new RuntimeException("Error selecting item from weighted probabilities for zone:  " + zone + "sum utility: " + sum);
    }
}
