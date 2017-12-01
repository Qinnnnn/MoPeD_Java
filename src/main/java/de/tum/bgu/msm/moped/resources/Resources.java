package de.tum.bgu.msm.moped.resources;

import com.pb.common.util.ResourceUtil;

import java.util.ResourceBundle;

/**
 * Created by Nico on 19.07.2017.
 */
public enum Resources {

    INSTANCE;

    private ResourceBundle resources;

    Resources() {

    }

    public void setResources(ResourceBundle resources) {
        this.resources = resources;
    }

    public synchronized int getInt(String key) {
        return ResourceUtil.getIntegerProperty(resources, key);
    }

    public synchronized String getString(String key) {
        return ResourceUtil.getProperty(resources, key);
    }

    public synchronized String[] getArray(String key) {
        return ResourceUtil.getArray(resources, key);
    }

    public synchronized  boolean getBoolean(String key) {
        return ResourceUtil.getBooleanProperty(resources, key);
    }

    public synchronized double getDouble(String key) {
        return ResourceUtil.getDoubleProperty(resources, key);
    }

}
