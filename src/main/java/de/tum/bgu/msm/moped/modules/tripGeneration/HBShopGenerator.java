package de.tum.bgu.msm.moped.modules.tripGeneration;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.HouseholdType;
import de.tum.bgu.msm.moped.data.Zone;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class HBShopGenerator {

    private static final Logger logger = Logger.getLogger(HBShopGenerator.class);
    private final DataSet dataSet;
    private Table<Long, Integer, Double> hbShopProduction;
    public HBShopGenerator(DataSet dataSet) {
        this.dataSet = dataSet;
    }



    public void run () {
        Collection<Long> zones = dataSet.getZones().keySet();
        Collection<Integer> households = dataSet.getHhTypes().keySet();
        hbShopProduction = ArrayTable.create(zones, households);
        productionCalculator();
        dataSet.setHbShopTripGen(hbShopProduction);
    }

    public void productionCalculator() {
        for (long zoneId : dataSet.getZones().keySet()){
            for (int hhTypeId : dataSet.getHhTypes().keySet()){
                double distribution = dataSet.getDistribution().get(zoneId,hhTypeId);
                HouseholdType hhType = dataSet.getHouseholdType(hhTypeId);
                int workers = hhType.getWorkers();
                int hhSize = hhType.getHouseholdSize();
                double tripGenRate = 0.0;
                if (workers == 0){
                    switch (hhSize){
                        case 1:
                            tripGenRate = 0.65370595;
                            break;
                        case 2:
                            tripGenRate = 1.47478580;
                            break;
                        case 3:
                            tripGenRate = 1.43978190;
                            break;
                        case 4:
                            tripGenRate = 1.79258760;
                            break;
                    }
                }else if (workers == 1){
                    switch (hhSize){
                        case 1:
                            tripGenRate = 0.36543758;
                            break;
                        case 2:
                            tripGenRate = 0.96459839;
                            break;
                        case 3:
                            tripGenRate = 1.16952820;
                            break;
                        case 4:
                            tripGenRate = 1.80668250;
                            break;
                    }
                }else if (workers == 2){
                    switch (hhSize){
                        case 2:
                            tripGenRate = 0.66841305;
                            break;
                        case 3:
                            tripGenRate = 0.93650663;
                            break;
                        case 4:
                            tripGenRate = 1.51069650;
                            break;
                    }
                }else if (workers == 3){
                    switch (hhSize){
                        case 3:
                            tripGenRate = 1.00633950;
                            break;
                        case 4:
                            tripGenRate = 1.23472770;
                            break;
                    }
                }

                double tripGen;

                if (tripGenRate != 0){
                     tripGen = tripGenRate * distribution * 1.2;
                }else{
                     tripGen = 0.0;
                }

                hbShopProduction.put(zoneId,hhTypeId,tripGen);
            }

        }

    }
}
