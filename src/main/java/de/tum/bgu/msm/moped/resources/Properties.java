package de.tum.bgu.msm.moped.resources;

import java.util.ResourceBundle;
import com.pb.common.util.ResourceUtil;

public class Properties {

    public final String ZONES;
    public final String ZONESATTRIBUTE;
    public final String HOUSEHOLDTYPE;
    public final String HOUSEHOLDTYPEDISTRIBUTION;

    private static Properties instance;

    public Properties(ResourceBundle bundle) {
        ZONES = ResourceUtil.getProperty(bundle, "zone.data.file");
        ZONESATTRIBUTE = ResourceUtil.getProperty(bundle, "zoneAttributes.data.file");
        HOUSEHOLDTYPE = ResourceUtil.getProperty(bundle, "householdType.data.file");
        HOUSEHOLDTYPEDISTRIBUTION = ResourceUtil.getProperty(bundle, "householdTypeDistribution.data.file");
    }


    public static Properties get() {
        if(instance == null) {
            throw new RuntimeException("Properties not initialized yet! Make sure to call initializeProperties Method first!");
        }
        return instance;
    }

    public static void initializeProperties(ResourceBundle bundle) {
        instance = new Properties(bundle);
    }
}
