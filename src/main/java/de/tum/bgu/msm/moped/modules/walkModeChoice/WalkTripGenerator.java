package de.tum.bgu.msm.moped.modules.walkModeChoice;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.HouseholdType;
import de.tum.bgu.msm.moped.data.Purpose;
import de.tum.bgu.msm.moped.data.Zone;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.OpenMapRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.jblas.FloatMatrix;

public abstract class WalkTripGenerator {

    protected final DataSet dataSet;
    protected FloatMatrix WalkTrip;
    //protected FloatMatrix WalkExpUtility;
    protected FloatMatrix VehicleExpUtility;
    protected final Purpose purpose;

    public WalkTripGenerator(DataSet dataSet, Purpose purpose) {
        this.dataSet = dataSet;
        this.purpose = purpose;
    }

    public void run () {
        int zoneSize = dataSet.getOriginPAZs().size();
        int hhTypeSize = dataSet.getHOUSEHOLDTYPESIZE();
        WalkTrip = new FloatMatrix(zoneSize, hhTypeSize);
        //WalkExpUtility = new FloatMatrix(zoneSize, hhTypeSize);
        calculateWalkUtilities();
        calculateVehicleUtilities();
        calculateWalkTrips();
        dataSet.addWalkTrips(WalkTrip, purpose);
    }

    public void calculateWalkUtilities(){
        for (Zone originZone : dataSet.getOriginPAZs().values()) {
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
                float utilityZone = calculateZoneRelatedUtility(pie, pieFlag, wa, stfwy);
                float utilityHousehold = calculateHouseholdRelatedUtility(hhSize, worker, income, age, car, kid);
                float utilitySum = utilityZone + utilityHousehold;
                float expUtility = (float) Math.exp(utilitySum);
                WalkTrip.put(originZone.getIndex(),hhType.getHhTypeId(),expUtility);
            }
        }
    }

    public void calculateVehicleUtilities() {
        VehicleExpUtility = FloatMatrix.ones(dataSet.getOriginPAZs().size(),dataSet.getHOUSEHOLDTYPESIZE());
    }

    public void calculateWalkTrips() {
        FloatMatrix sumExpUtility = WalkTrip.add(VehicleExpUtility);
        WalkTrip = WalkTrip.div(sumExpUtility);
        WalkTrip = WalkTrip.mul(dataSet.getProductionsByPurpose().get(purpose));
        for (int index : dataSet.getOriginPAZs().keySet()) {
            float totalWalkTrips = WalkTrip.getRow(index).sum();
            dataSet.getOriginPAZ(index).addTotalWalkTrips(totalWalkTrips,purpose);
        }
    }

    protected abstract float calculateZoneRelatedUtility(float pie, int pieFlag, int wa, float stfwy);
    protected abstract float calculateHouseholdRelatedUtility(int hhSize, int worker, int income, int age, int car, int kid);

}
