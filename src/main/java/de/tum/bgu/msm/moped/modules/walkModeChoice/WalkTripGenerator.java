package de.tum.bgu.msm.moped.modules.walkModeChoice;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.HouseholdType;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.data.MopedZone;
import org.jblas.FloatMatrix;

public abstract class WalkTripGenerator {

    protected final DataSet dataSet;
    protected FloatMatrix WalkTrip;
    protected FloatMatrix VehicleExpUtility;
    protected final Purpose purpose;

    public WalkTripGenerator(DataSet dataSet, Purpose purpose) {
        this.dataSet = dataSet;
        this.purpose = purpose;
    }

    public void run () {
        int zoneSize;
        //TODO NHB getzones, other getOriginPAZ
        if(purpose.equals(Purpose.NHBW)||purpose.equals(Purpose.NHBNW)){
            zoneSize = dataSet.getZones().size();
        }else {
            zoneSize = dataSet.getOriginPAZs().size();
        }
        System.out.println(zoneSize);

        int hhTypeSize = dataSet.getHOUSEHOLDTYPESIZE();
        System.out.println(hhTypeSize);
        System.out.println(hhTypeSize*zoneSize);
        WalkTrip = new FloatMatrix(zoneSize, hhTypeSize);
        calculateWalkUtilities();
        calculateVehicleUtilities();
        calculateWalkTrips();
        dataSet.addWalkTrips(WalkTrip, purpose);
    }

    public void runForMito () {
        int zoneSize = dataSet.getOriginPAZs().size();
        int hhTypeSize = dataSet.getHOUSEHOLDTYPESIZE();
        WalkTrip = new FloatMatrix(zoneSize, hhTypeSize);
        calculateWalkUtilities();
        calculateVehicleUtilities();
        calculateWalkTrips();
        dataSet.addWalkTrips(WalkTrip, purpose);
    }

    public void calculateWalkUtilities(){
        //TODO NHB getzones, other getOriginPAZ
        for (MopedZone originZone : dataSet.getOriginPAZs().values()) {
            for (HouseholdType hhType : dataSet.getHhTypes().values()) {
                float pie = originZone.getPie();
                int pieFlag = originZone.getPieFlag();
                int wa = originZone.getWa();
                float stfwy = originZone.getStfwy();
                int hhSize = hhType.getHouseholdSize();
                int worker = hhType.getWorkers();
                int income = hhType.getIncome();
                int age = hhType.getAge();
                int car = hhType.getCars();
                int kid = hhType.getKids();
                float pieEmpl = originZone.getPieEmpl();
                float piePop = originZone.getPiePop();
                float utilityZone = calculateZoneRelatedUtility(pie, pieFlag, wa, stfwy, pieEmpl, piePop);
                float utilityHousehold = calculateHouseholdRelatedUtility(hhSize, worker, income, age, car, kid);
                float utilitySum = utilityZone + utilityHousehold;
                float expUtility = (float) Math.exp(utilitySum);
                WalkTrip.put(originZone.getIndex(),hhType.getHhTypeId(),expUtility);
            }
        }
    }

    public void calculateVehicleUtilities() {
        if(purpose.equals(Purpose.NHBW)||purpose.equals(Purpose.NHBNW)){
            VehicleExpUtility = FloatMatrix.ones(dataSet.getOriginPAZs().size(),dataSet.getHOUSEHOLDTYPESIZE());
        }else {
            VehicleExpUtility = FloatMatrix.ones(dataSet.getOriginPAZs().size(),dataSet.getHOUSEHOLDTYPESIZE());

        }
    }

    public void calculateWalkTrips() {
        FloatMatrix sumExpUtility = WalkTrip.add(VehicleExpUtility);
        WalkTrip = WalkTrip.div(sumExpUtility);
        WalkTrip = WalkTrip.mul(dataSet.getProductionsByPurpose().get(purpose));
        for (int index : dataSet.getOriginPAZs().keySet()) {
            float totalWalkTrips = WalkTrip.getRow(index).sum();
            float totalWalkTripsNoCars = 0.0f;
            float totalWalkTripsHasCars = 0.0f;
            for (HouseholdType hhType : dataSet.getHhTypes().values()) {
                if (hhType.getCars() == 0) {
                    totalWalkTripsNoCars += WalkTrip.get(index, hhType.getHhTypeId());
                }else{
                    totalWalkTripsHasCars += WalkTrip.get(index, hhType.getHhTypeId());
                }
            }

            float totalWalkTripsNoChilds = 0.0f;
            float totalWalkTripsHasChilds = 0.0f;
            for (HouseholdType hhType : dataSet.getHhTypes().values()) {
                if (hhType.getKids() == 0) {
                    totalWalkTripsNoChilds += WalkTrip.get(index, hhType.getHhTypeId());
                }else{
                    totalWalkTripsHasChilds += WalkTrip.get(index, hhType.getHhTypeId());
                }
            }

            dataSet.getOriginPAZ(index).addTotalWalkTrips(totalWalkTrips,purpose);
            dataSet.getOriginPAZ(index).addTotalWalkTripsNoCar(totalWalkTripsNoCars,purpose);
            dataSet.getOriginPAZ(index).addTotalWalkTripsHasCar(totalWalkTripsHasCars,purpose);
            dataSet.getOriginPAZ(index).addTotalWalkTripsNoChild(totalWalkTripsNoChilds,purpose);
            dataSet.getOriginPAZ(index).addTotalWalkTripsHasChild(totalWalkTripsHasChilds,purpose);

        }
    }

    protected abstract float calculateZoneRelatedUtility(float pie, int pieFlag, int wa, float stfwy, float pieEmpl, float piePop);
    protected abstract float calculateHouseholdRelatedUtility(int hhSize, int worker, int income, int age, int car, int kid);
}
