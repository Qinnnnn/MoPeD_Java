package de.tum.bgu.msm.moped.modules.tripGeneration;

import com.google.common.collect.ArrayTable;
import com.google.common.collect.Table;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.HouseholdType;
import de.tum.bgu.msm.moped.data.Zone;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Iterator;

public class HBRecreationGenerator {

    private static final Logger logger = Logger.getLogger(HBRecreationGenerator.class);
    private final DataSet dataSet;
    private Table<Long, Integer, Double> hbRecreationProduction;
    public HBRecreationGenerator(DataSet dataSet) {
        this.dataSet = dataSet;
    }



    public void run () {
        Collection<Long> zones = dataSet.getZones().keySet();
        Collection<Integer> households = dataSet.getHhTypes().keySet();
        hbRecreationProduction = ArrayTable.create(zones, households);
        productionCalculator();
        dataSet.setHbRecreationTripGen(hbRecreationProduction);
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
                            tripGenRate = 0.47897259;
                            break;
                        case 2:
                            tripGenRate = 0.88111840;
                            break;
                        case 3:
                            tripGenRate = 1.21373370;
                            break;
                        case 4:
                            tripGenRate = 2.24007530;
                            break;
                    }
                }else if (workers == hhSize){
                    switch (hhSize){
                        case 1:
                            tripGenRate = 0.50317472;
                            break;
                        case 2:
                            tripGenRate = 0.57970395;
                            break;
                        case 3:
                            tripGenRate = 1.16564740;
                            break;
                    }
                } else{
                    logger.warn("Number of workers is bigger than householdsize");
                }

                double tripGen;
                if (tripGenRate != 0){
                    tripGen = tripGenRate * distribution * 1.2;

                }else{
                    tripGen = 0.0;
                }

                hbRecreationProduction.put(zoneId,hhTypeId,tripGen);
            }

        }

    }
}
