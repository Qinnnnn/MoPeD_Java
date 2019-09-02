package pie.readers;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.io.input.CSVReader;
import de.tum.bgu.msm.moped.util.MoPeDUtil;
import org.osgeo.proj4j.BasicCoordinateTransform;
import org.osgeo.proj4j.ProjCoordinate;
import pie.data.OHASTrip;
import pie.data.employmentAllocation.BlockGroupToOSMObjects;

public class OHASTripReader extends CSVReader {
    private int idIndex;
    private int incomeIndex;
    private int genderIndex;
    private int ageIndex;
    private int modeIndex;
    private int pazIndex;
    private int weightIndex;
    private int purposeIndex;
    private int distanceIndex;
    private int disableIndex;
    private int drivingLicenseIndex;
    private int transitPassIndex;


    public OHASTripReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {

        super.read("/F:/Qin/01_Dissertation/01_MoPeD/NewPIE/data/LINKEDTRIP_6559_filter_noAP.csv", ",");

    }

    @Override
    protected void processHeader(String[] header) {
        idIndex = MoPeDUtil.findPositionInArray("tripId", header);
        incomeIndex = MoPeDUtil.findPositionInArray("INCOME", header);
        genderIndex = MoPeDUtil.findPositionInArray("GENDER", header);
        ageIndex = MoPeDUtil.findPositionInArray("AGEGroup", header);
        modeIndex = MoPeDUtil.findPositionInArray("MODE", header);
        pazIndex = MoPeDUtil.findPositionInArray("pazId", header);
        weightIndex = MoPeDUtil.findPositionInArray("weight", header);
        purposeIndex = MoPeDUtil.findPositionInArray("PURPOSE1", header);
        distanceIndex = MoPeDUtil.findPositionInArray("DistanceRoute", header);
        disableIndex = MoPeDUtil.findPositionInArray("disable", header);
        drivingLicenseIndex = MoPeDUtil.findPositionInArray("drivingLicense", header);
        transitPassIndex = MoPeDUtil.findPositionInArray("transitPass", header);


    }

    @Override
    protected void processRecord(String[] record) {
        int id = Integer.parseInt(record[idIndex]);
        String income = record[incomeIndex];
        String gender = record[genderIndex];
        String age = record[ageIndex];
        String mode = record[modeIndex];
        int pazId = Integer.parseInt(record[pazIndex]);
        double weight = Double.parseDouble(record[weightIndex]);
        String purpose = record[purposeIndex];
        double distanceInFeet = Double.parseDouble(record[distanceIndex]);
        String disable = record[disableIndex];
        String drivingLicense = record[drivingLicenseIndex];
        String transitPass = record[transitPassIndex];


        OHASTrip ohasTrip = new OHASTrip(id,income,gender,age,mode,pazId,weight,purpose,distanceInFeet);
        ohasTrip.setDisable(disable);
        ohasTrip.setDrivingLicense(drivingLicense);
        ohasTrip.setTransitPass(transitPass);

        BlockGroupToOSMObjects.ohasTrips.add(ohasTrip);



    }



}
