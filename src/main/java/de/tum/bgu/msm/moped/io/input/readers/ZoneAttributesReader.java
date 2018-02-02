package de.tum.bgu.msm.moped.io.input.readers;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.Zone;
import de.tum.bgu.msm.moped.io.input.CSVReader;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import de.tum.bgu.msm.moped.util.MoPeDUtil;

public class ZoneAttributesReader extends CSVReader {
    private int idIndex;
    private int agricultureIndex;
    private int constructionIndex;
    private int financialIndex;
    private int governmentIndex;
    private int manufacturingIndex;
    private int retailIndex;
    private int serviceIndex;
    private int transportationIndex;
    private int wholesaleIndex;
    private int shoppingAreaIndex;
    private int collegeVehicleTripIndex;


    public ZoneAttributesReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        super.read(Resources.INSTANCE.getString(Properties.ZONESATTRIBUTE), ",");
    }

    @Override
    protected void processHeader(String[] header) {
        idIndex = MoPeDUtil.findPositionInArray("zoneID", header);
        agricultureIndex = MoPeDUtil.findPositionInArray("afm", header);
        constructionIndex = MoPeDUtil.findPositionInArray("con", header);
        financialIndex = MoPeDUtil.findPositionInArray("fi", header);
        governmentIndex = MoPeDUtil.findPositionInArray("gov", header);
        manufacturingIndex = MoPeDUtil.findPositionInArray("mfg", header);
        retailIndex = MoPeDUtil.findPositionInArray("ret", header);
        serviceIndex = MoPeDUtil.findPositionInArray("ser", header);
        transportationIndex = MoPeDUtil.findPositionInArray("tpu", header);
        wholesaleIndex = MoPeDUtil.findPositionInArray("wt", header);
        shoppingAreaIndex = MoPeDUtil.findPositionInArray("shsqft", header);
        collegeVehicleTripIndex = MoPeDUtil.findPositionInArray("colveh", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int zoneId = Integer.parseInt(record[idIndex]);
        if(dataSet.getZone(zoneId) != null) {
            float agriculture = Float.parseFloat(record[agricultureIndex]);
            float construction = Float.parseFloat(record[constructionIndex]);
            float financial = Float.parseFloat(record[financialIndex]);
            float government = Float.parseFloat(record[governmentIndex]);
            float manufacturing = Float.parseFloat(record[manufacturingIndex]);
            float retail = Float.parseFloat(record[retailIndex]);
            float service = Float.parseFloat(record[serviceIndex]);
            float transportation = Float.parseFloat(record[transportationIndex]);
            float wholesale = Float.parseFloat(record[wholesaleIndex]);
            float shoppingArea = Float.parseFloat(record[shoppingAreaIndex]);
            float collegeVehicleTrip = Float.parseFloat(record[collegeVehicleTripIndex]);

            dataSet.getZone(zoneId).setAgriculture(agriculture);
            dataSet.getZone(zoneId).setConstruction(construction);
            dataSet.getZone(zoneId).setFinancial(financial);
            dataSet.getZone(zoneId).setGovernment(government);
            dataSet.getZone(zoneId).setManufacturing(manufacturing);
            dataSet.getZone(zoneId).setRetail(retail);
            dataSet.getZone(zoneId).setService(service);
            dataSet.getZone(zoneId).setTransportation(transportation);
            dataSet.getZone(zoneId).setWholesale(wholesale);
            dataSet.getZone(zoneId).setShoppingArea(shoppingArea);
            dataSet.getZone(zoneId).setCollegeVehicleTrip(collegeVehicleTrip);
        }
    }
}
