package de.tum.bgu.msm.moped.io.input;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.Zone;
import de.tum.bgu.msm.moped.io.input.readers.*;
import org.apache.log4j.Logger;

public class InputManager {
    private static final Logger logger = Logger.getLogger(InputManager.class);

    private final DataSet dataSet;

    public InputManager(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public void readAsStandAlone() {
//        Zone dummyZone1 = new Zone(1, 2);
//        dataSet.addZone(dummyZone1);
//        dummyZone1.setAgriculture(agriculture);
//        dataSet.getZone(zoneId).setConstruction(construction);
//        dataSet.getZone(zoneId).setFinancial(financial);
//        dataSet.getZone(zoneId).setGovernment(government);
//        dataSet.getZone(zoneId).setManufacturing(manufacturing);
//        dataSet.getZone(zoneId).setRetail(retail);
//        dataSet.getZone(zoneId).setService(service);
//        dataSet.getZone(zoneId).setTransportation(transportation);
//        dataSet.getZone(zoneId).setWholesale(wholesale);
//        dataSet.getZone(zoneId).setParkCostShort(parkCostShort);
//        dataSet.getZone(zoneId).setParkCostLong(parkCostLong);
//        dataSet.getZone(zoneId).setPeakHousehold(peakHousehold);
//        dataSet.getZone(zoneId).setPeakEmployment(peakEmployment);
//        dataSet.getZone(zoneId).setOffpeakHousehold(offpeakHousehold);
//        dataSet.getZone(zoneId).setOffpeakEmployment(offpeakEmployment);
//        dataSet.getZone(zoneId).setIntersection(intersection);
//        dataSet.getZone(zoneId).setShoppingArea(shoppingArea);
//        dataSet.getZone(zoneId).setSingleFamily(singleFamily);
//        dataSet.getZone(zoneId).setCollegeVehicleTrip(collegeVehicleTrip);
//        dataSet.getZone(zoneId).setAuov(auov);
//        dataSet.getZone(zoneId).setParkCharge(parkCharge);
//        dataSet.getZone(zoneId).setCanpnr(canpnr);
//        dataSet.getZone(zoneId).setCaninf(caninf);

        new ZonesReader(dataSet).read();
        System.out.println("1");
        new ZoneAttributesReader(dataSet).read();
        System.out.println("2");
        new HouseholdTypeReader(dataSet).read();
        System.out.println("3");
        new HouseholdTypeDistributionReader(dataSet).read();
        System.out.println("4");
        new PIEReader(dataSet).read();
        System.out.println("5");
        new TransportReader(dataSet).read();
        System.out.println("6");
        new SuperPAZImpedanceReader(dataSet).read();
        System.out.println("7");
        new SuperPAZAttributesReader(dataSet).read();
        System.out.println("8");
    }


}
