package de.tum.bgu.msm.moped.io.input.readers;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.SuperPAZ;
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
    private int index = 0;


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
        int superPAZId = Integer.parseInt(record[idIndex]);

        SuperPAZ superPAZ = dataSet.getSuperPAZ(superPAZId);
        if (superPAZ == null){
            superPAZ = new SuperPAZ(superPAZId, "ORIGIN");
            dataSet.addSuperPAZ(superPAZ);
        }

        float household = Float.parseFloat(record[householdIndex]);
        float totalEmpl = Float.parseFloat(record[totalEmplIndex]);
        float financial = Float.parseFloat(record[financialIndex]);
        float government = Float.parseFloat(record[governmentIndex]);
        float retail = Float.parseFloat(record[retailIndex]);
        float service = Float.parseFloat(record[serviceIndex]);
        float pie = Float.parseFloat(record[pieIndex]);
        float slope = Float.parseFloat(record[slopeIndex]);
        int freeway = Integer.parseInt(record[freewayIndex]);
        int park = Integer.parseInt(record[parkIndex]);

        dataSet.getSuperPAZ(superPAZId).setHousehold(household);
        dataSet.getSuperPAZ(superPAZId).setTotalEmpl(totalEmpl);
        dataSet.getSuperPAZ(superPAZId).setFinancial(financial);
        dataSet.getSuperPAZ(superPAZId).setGovernment(government);
        dataSet.getSuperPAZ(superPAZId).setRetail(retail);
        dataSet.getSuperPAZ(superPAZId).setService(service);
        dataSet.getSuperPAZ(superPAZId).setPie(pie);
        dataSet.getSuperPAZ(superPAZId).setSlope(slope);
        dataSet.getSuperPAZ(superPAZId).setFreeway(freeway);
        dataSet.getSuperPAZ(superPAZId).setPark(park);

        if (totalEmpl != 0){
            dataSet.addDestinationSuperPAZ(index, superPAZ);
            superPAZ.setIndex(index);
            index++;
        }
    }
}
