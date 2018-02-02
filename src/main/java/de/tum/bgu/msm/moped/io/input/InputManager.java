package de.tum.bgu.msm.moped.io.input;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.SuperPAZ;
import de.tum.bgu.msm.moped.io.input.readers.*;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import org.apache.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class InputManager {
    private static final Logger logger = Logger.getLogger(InputManager.class);

    private final DataSet dataSet;

    public InputManager(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public void readAsStandAlone() {
        new ZonesReader(dataSet).read();
        System.out.println(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
        new ZoneAttributesReader(dataSet).read();
        System.out.println(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
        new HouseholdTypeReader(dataSet).read();
        System.out.println(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
        new HouseholdTypeDistributionReader(dataSet).read();
        System.out.println(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
        new PIEReader(dataSet).read();
        System.out.println(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
        new TransportReader(dataSet).read();
        System.out.println(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
        new SuperPAZAttributesReader(dataSet).read();
        System.out.println(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
        new SuperPAZImpedanceReader(dataSet).read();
        System.out.println(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
    }




    public void readAsStandAlone2() {

        new SuperPAZAttributesReader(dataSet).read();
        System.out.println(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );
        new SuperPAZImpedanceReader(dataSet).read();
        System.out.println(new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime()) );

    }


}
