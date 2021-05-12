package de.tum.bgu.msm.moped.util;

import de.tum.bgu.msm.common.util.ResourceUtil;
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
}
