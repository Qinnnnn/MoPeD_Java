package de.tum.bgu.msm.moped.io.input.readers;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.Zone;
import de.tum.bgu.msm.moped.io.input.CSVReader;
import de.tum.bgu.msm.moped.resources.Properties;
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
    private int parkCostShortIndex;
    private int parkCostLongIndex;
    private int peakHouseholdIndex;
    private int peakEmploymentIndex;
    private int offpeakHouseholdIndex;
    private int offpeakEmploymentIndex;
    private int intersectionIndex;
    private int shoppingAreaIndex;
    private int singleFamilyIndex;
    private int collegeVehicleTripIndex;
    private int auovIndex;
    private int parkChargeIndex;
    private int canpnrIndex;
    private int caninfIndex;

    public ZoneAttributesReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        super.read(Properties.get().ZONESATTRIBUTE, ",");
    }

    @Override
    protected void processHeader(String[] header) {
        idIndex = MoPeDUtil.findPositionInArray("ZoneId", header);
        agricultureIndex = MoPeDUtil.findPositionInArray("afm", header);
        constructionIndex = MoPeDUtil.findPositionInArray("con", header);
        financialIndex = MoPeDUtil.findPositionInArray("fi", header);
        governmentIndex = MoPeDUtil.findPositionInArray("gov", header);
        manufacturingIndex = MoPeDUtil.findPositionInArray("mfg", header);
        retailIndex = MoPeDUtil.findPositionInArray("ret", header);
        serviceIndex = MoPeDUtil.findPositionInArray("ser", header);
        transportationIndex = MoPeDUtil.findPositionInArray("tpu", header);
        wholesaleIndex = MoPeDUtil.findPositionInArray("wt", header);
        parkCostShortIndex = MoPeDUtil.findPositionInArray("stpkg", header);
        parkCostLongIndex = MoPeDUtil.findPositionInArray("ltpkg", header);
        peakHouseholdIndex = MoPeDUtil.findPositionInArray("phhcov", header);
        peakEmploymentIndex = MoPeDUtil.findPositionInArray("pempco", header);
        offpeakHouseholdIndex  = MoPeDUtil.findPositionInArray("ohhcov", header);
        offpeakEmploymentIndex = MoPeDUtil.findPositionInArray("oempco", header);
        intersectionIndex = MoPeDUtil.findPositionInArray("inthm", header);
        shoppingAreaIndex = MoPeDUtil.findPositionInArray("shsqft", header);
        singleFamilyIndex = MoPeDUtil.findPositionInArray("sfp", header);
        collegeVehicleTripIndex = MoPeDUtil.findPositionInArray("colveh", header);
        auovIndex = MoPeDUtil.findPositionInArray("auov", header);
        parkChargeIndex = MoPeDUtil.findPositionInArray("parka", header);
        canpnrIndex = MoPeDUtil.findPositionInArray("canpnr", header);
        caninfIndex = MoPeDUtil.findPositionInArray("caninf", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int zoneId = Integer.parseInt(record[idIndex]);
        double agriculture = Integer.parseInt(record[agricultureIndex]);
        double construction = Integer.parseInt(record[constructionIndex]);
        double financial = Integer.parseInt(record[financialIndex]);
        double government = Integer.parseInt(record[governmentIndex]);
        double manufacturing = Integer.parseInt(record[manufacturingIndex]);
        double retail = Integer.parseInt(record[retailIndex]);
        double service = Integer.parseInt(record[serviceIndex]);
        double transportation = Integer.parseInt(record[transportationIndex]);
        double wholesale = Integer.parseInt(record[wholesaleIndex]);
        double parkCostShort = Integer.parseInt(record[parkCostShortIndex]);
        double parkCostLong = Integer.parseInt(record[parkCostLongIndex]);
        double peakHousehold = Integer.parseInt(record[peakHouseholdIndex]);
        double peakEmployment = Integer.parseInt(record[peakEmploymentIndex]);
        double offpeakHousehold = Integer.parseInt(record[offpeakHouseholdIndex]);
        double offpeakEmployment = Integer.parseInt(record[offpeakEmploymentIndex]);
        double intersection = Integer.parseInt(record[intersectionIndex]);
        double shoppingArea = Integer.parseInt(record[shoppingAreaIndex]);
        double singleFamily = Integer.parseInt(record[singleFamilyIndex]);
        double collegeVehicleTrip = Integer.parseInt(record[collegeVehicleTripIndex]);
        double auov = Integer.parseInt(record[auovIndex]);
        double parkCharge = Integer.parseInt(record[parkChargeIndex]);
        double canpnr = Integer.parseInt(record[canpnrIndex]);
        double caninf = Integer.parseInt(record[caninfIndex]);

        dataSet.getZone(zoneId).setAgriculture(agriculture);
        dataSet.getZone(zoneId).setAgriculture(construction);
        dataSet.getZone(zoneId).setFinancial(financial);
        dataSet.getZone(zoneId).setGovernment(government);
        dataSet.getZone(zoneId).setManufacturing(manufacturing);
        dataSet.getZone(zoneId).setRetail(retail);
        dataSet.getZone(zoneId).setService(service);
        dataSet.getZone(zoneId).setTransportation(transportation);
        dataSet.getZone(zoneId).setWholesale(wholesale);
        dataSet.getZone(zoneId).setParkCostShort(parkCostShort);
        dataSet.getZone(zoneId).setParkCostLong(parkCostLong);
        dataSet.getZone(zoneId).setPeakHousehold(peakHousehold);
        dataSet.getZone(zoneId).setPeakEmployment(peakEmployment);
        dataSet.getZone(zoneId).setOffpeakHousehold(offpeakHousehold);
        dataSet.getZone(zoneId).setOffpeakEmployment(offpeakEmployment);
        dataSet.getZone(zoneId).setIntersection(intersection);
        dataSet.getZone(zoneId).setShoppingArea(shoppingArea);
        dataSet.getZone(zoneId).setSingleFamily(singleFamily);
        dataSet.getZone(zoneId).setCollegeVehicleTrip(collegeVehicleTrip);
        dataSet.getZone(zoneId).setAuov(auov);
        dataSet.getZone(zoneId).setParkCharge(parkCharge);
        dataSet.getZone(zoneId).setCanpnr(canpnr);
        dataSet.getZone(zoneId).setCaninf(caninf);
    }
}
