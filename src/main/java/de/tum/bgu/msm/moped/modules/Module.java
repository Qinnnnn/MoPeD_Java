package de.tum.bgu.msm.moped.modules;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.Purpose;

import java.io.FileNotFoundException;

/**
 * Created by Nico on 14.07.2017.
 */
public abstract class Module {

    protected final DataSet dataSet;

    protected Module(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public abstract void run(Purpose purpose) throws FileNotFoundException;

}
