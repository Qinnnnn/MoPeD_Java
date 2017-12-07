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
        long zoneId = Long.parseLong(record[idIndex]);
        double agriculture = Double.parseDouble(record[agricultureIndex]);
        double construction = Double.parseDouble(record[constructionIndex]);
        double financial = Double.parseDouble(record[financialIndex]);
        double government = Double.parseDouble(record[governmentIndex]);
        double manufacturing = Double.parseDouble(record[manufacturingIndex]);
        double retail = Double.parseDouble(record[retailIndex]);
        double service = Double.parseDouble(record[serviceIndex]);
        double transportation = Double.parseDouble(record[transportationIndex]);
        double wholesale = Double.parseDouble(record[wholesaleIndex]);
        double parkCostShort = Double.parseDouble(record[parkCostShortIndex]);
        double parkCostLong = Double.parseDouble(record[parkCostLongIndex]);
        double peakHousehold = Double.parseDouble(record[peakHouseholdIndex]);
        double peakEmployment = Double.parseDouble(record[peakEmploymentIndex]);
        double offpeakHousehold = Double.parseDouble(record[offpeakHouseholdIndex]);
        double offpeakEmployment = Double.parseDouble(record[offpeakEmploymentIndex]);
        double intersection = Double.parseDouble(record[intersectionIndex]);
        double shoppingArea = Double.parseDouble(record[shoppingAreaIndex]);
        double singleFamily = Double.parseDouble(record[singleFamilyIndex]);
        double collegeVehicleTrip = Double.parseDouble(record[collegeVehicleTripIndex]);
        double auov = Double.parseDouble(record[auovIndex]);
        double parkCharge = Double.parseDouble(record[parkChargeIndex]);
        double canpnr = Double.parseDouble(record[canpnrIndex]);
        double caninf = Double.parseDouble(record[caninfIndex]);

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
