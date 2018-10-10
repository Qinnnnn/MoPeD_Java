package de.tum.bgu.msm.moped.modules.tripGeneration;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.data.HouseholdType;
import de.tum.bgu.msm.moped.data.Purpose;
import org.jblas.FloatMatrix;

public abstract class TripGenerator {

    protected final DataSet dataSet;
    protected FloatMatrix production;
    protected final Purpose purpose;

    public TripGenerator(DataSet dataSet, Purpose purpose) {
        this.dataSet = dataSet;
        this.purpose = purpose;
    }

    public void run () {
        production = new FloatMatrix(dataSet.getOriginPAZs().size(), dataSet.getHOUSEHOLDTYPESIZE());
        calculateProductions();
        System.out.println("finish production");
        scaleProductions();
        System.out.println("finish scalar");
        dataSet.addProduction(production, purpose);
    }


    public void calculateProductions() {
        for (int index : dataSet.getOriginPAZs().keySet()){
            for (int hhTypeId : dataSet.getHhTypes().keySet()){
                float distribution = dataSet.getDistribution().get(index,hhTypeId);
                HouseholdType hhType = dataSet.getHouseholdType(hhTypeId);
                float tripGen = calculateProduction(distribution, hhType);
                production.put(index, hhTypeId, tripGen);
            }
        }
    }

    protected abstract float calculateProduction(float distribution, HouseholdType hhType);
    protected abstract void scaleProductions();


}
