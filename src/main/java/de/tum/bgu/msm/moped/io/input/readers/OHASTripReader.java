package de.tum.bgu.msm.moped.io.input.readers;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.io.input.CSVReader;
import de.tum.bgu.msm.moped.util.MoPeDUtil;
import pie.data.OHASTrip;
import pie.data.employmentAllocation.BlockGroupToOSMObjects;

public class OHASTripReader extends CSVReader {
    private int idIndex;
    private int zoneIndex;


    public OHASTripReader(DataSet dataSet) {
        super(dataSet);
    }

    @Override
    public void read() {

        super.read("F:/models\\mopedPortland\\testOutput\\OHASWalkTrips.csv", ",");

    }

    @Override
    protected void processHeader(String[] header) {
        idIndex = MoPeDUtil.findPositionInArray("tripId", header);
        zoneIndex = MoPeDUtil.findPositionInArray("PAZP", header);
    }

    @Override
    protected void processRecord(String[] record) {
        int id = Integer.parseInt(record[idIndex]);
        int zone = Integer.parseInt(record[zoneIndex]);
        if(dataSet.getZone(zone)!=null){
            dataSet.getOhasTripMap().put(id,dataSet.getZone(zone));
        }
    }



}
