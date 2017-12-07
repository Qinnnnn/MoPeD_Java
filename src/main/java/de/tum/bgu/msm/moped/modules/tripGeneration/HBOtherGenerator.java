package de.tum.bgu.msm.moped.modules.tripGeneration;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.HouseholdType;
import de.tum.bgu.msm.moped.data.Zone;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Iterator;

public class HBOtherGenerator {

    private static final Logger logger = Logger.getLogger(HBOtherGenerator.class);
    private final DataSet dataSet;
    private Table<Long, Integer, Double> hbOtherProduction;
    public HBOtherGenerator(DataSet dataSet) {
        this.dataSet = dataSet;
    }



    public void run () {
        Collection<Long> zones = dataSet.getZones().keySet();
        Collection<Integer> households = dataSet.getHhTypes().keySet();
        hbOtherProduction = ArrayTable.create(zones, households);
        productionCalculator();
        dataSet.setHbOtherTripGen(hbOtherProduction);
    }

    public void productionCalculator() {
        for (long zoneId : dataSet.getZones().keySet()){
            for (int hhTypeId : dataSet.getHhTypes().keySet()){
                double distribution = dataSet.getDistribution().get(zoneId,hhTypeId);
                Zone zone = dataSet.getZone(zoneId);
                HouseholdType hhType = dataSet.getHouseholdType(hhTypeId);
                int workers = hhType.getWorkers();
                int hhSize = hhType.getHouseholdSize();
                double tripGenRate = 0.0;
                if (workers < hhSize){
                    switch (hhSize){
                        case 1:
                            tripGenRate = 0.89368165;
                            break;
                        case 2:
                            tripGenRate = 1.62810500;
                            break;
                        case 3:
                            tripGenRate = 2.22561020;
                            break;
                        case 4:
                            tripGenRate = 3.48763360;
                            break;
                    }
                }else if (workers == hhSize){
                    switch (hhSize){
                        case 1:
                            tripGenRate = 0.54391065;
                            break;
                        case 2:
                            tripGenRate = 1.24163040;
                            break;
                        case 3:
                            tripGenRate = 1.44898570;
                            break;
                    }
                } else{
                    logger.warn("Number of workers is bigger than householdsize");
                }

                if (tripGenRate != 0){
                    double tripGen = tripGenRate * distribution * 1.2;
                    hbOtherProduction.put(zoneId,hhTypeId,tripGen);
                }else{
                    logger.warn("no HBOther - tripGenRate matches to" + hhType.getHhTypeId() + "with" + workers + "workers and " + hhSize +"persons");
                }
            }

        }

    }
}
