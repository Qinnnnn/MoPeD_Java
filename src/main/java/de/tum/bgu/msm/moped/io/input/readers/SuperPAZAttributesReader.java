package de.tum.bgu.msm.moped.io.input.readers;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.io.input.CSVReader;
import de.tum.bgu.msm.moped.resources.Properties;
import de.tum.bgu.msm.moped.resources.Resources;
import de.tum.bgu.msm.moped.util.MoPeDUtil;

public class SuperPAZAttributesReader extends CSVReader {
    private int idIndex;
    private int householdIndex;
    private int totalEmplIndex;
    private int financialIndex;
    private int governmentIndex;
    private int retailIndex;
    private int serviceIndex;
    private int pieIndex;
    private int slopeIndex;
    private int freewayIndex;
    private int parkIndex;


    public SuperPAZAttributesReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {
        super.read(Resources.INSTANCE.getString(Properties.SUPERPAZATTRIBUTE), ",");
    }

    @Override
    protected void processHeader(String[] header) {
        idIndex = MoPeDUtil.findPositionInArray("superPAZ", header);
        householdIndex = MoPeDUtil.findPositionInArray("HH", header);
        totalEmplIndex = MoPeDUtil.findPositionInArray("EMP_TOT", header);
        financialIndex = MoPeDUtil.findPositionInArray("EMP_FIN", header);
        governmentIndex = MoPeDUtil.findPositionInArray("EMP_GOV", header);
        retailIndex = MoPeDUtil.findPositionInArray("EMP_RET", header);
        serviceIndex = MoPeDUtil.findPositionInArray("EMP_SER", header);
        pieIndex = MoPeDUtil.findPositionInArray("PIE_AVG", header);
        slopeIndex = MoPeDUtil.findPositionInArray("SLP_MEAN", header);
        freewayIndex = MoPeDUtil.findPositionInArray("FWY_IN_ZONE", header);
        parkIndex = MoPeDUtil.findPositionInArray("PRK", header);
    }

    @Override
    protected void processRecord(String[] record) {
        long superPAZId = Long.parseLong(record[idIndex]);
        double household = Double.parseDouble(record[householdIndex]);
        double totalEmpl = Double.parseDouble(record[totalEmplIndex]);
        double financial = Double.parseDouble(record[financialIndex]);
        double government = Double.parseDouble(record[governmentIndex]);
        double retail = Double.parseDouble(record[retailIndex]);
        double service = Double.parseDouble(record[serviceIndex]);
        double pie = Double.parseDouble(record[pieIndex]);
        double slope = Double.parseDouble(record[slopeIndex]);
        int freeway = Integer.parseInt(record[freewayIndex]);
        int park = Integer.parseInt(record[parkIndex]);

        dataSet.getDestinationSuperPAZ(superPAZId).setHousehold(household);
        dataSet.getDestinationSuperPAZ(superPAZId).setTotalEmpl(totalEmpl);
        dataSet.getDestinationSuperPAZ(superPAZId).setFinancial(financial);
        dataSet.getDestinationSuperPAZ(superPAZId).setGovernment(government);
        dataSet.getDestinationSuperPAZ(superPAZId).setRetail(retail);
        dataSet.getDestinationSuperPAZ(superPAZId).setService(service);
        dataSet.getDestinationSuperPAZ(superPAZId).setPie(pie);
        dataSet.getDestinationSuperPAZ(superPAZId).setSlope(slope);
        dataSet.getDestinationSuperPAZ(superPAZId).setFreeway(freeway);
        dataSet.getDestinationSuperPAZ(superPAZId).setPark(park);
    }
}
