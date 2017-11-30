package de.tum.bgu.msm.moped.util;

import org.apache.log4j.Logger;


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
}
