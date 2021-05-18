package pie.readers;

import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.io.input.CSVReader;
import de.tum.bgu.msm.moped.util.MoPeDUtil;
import org.osgeo.proj4j.BasicCoordinateTransform;
import org.osgeo.proj4j.ProjCoordinate;


public class IntersectionReader extends CSVReader {
    private int idIndex;
    private int xIndex;
    private int yIndex;
    private int totalJobIndex;
    private int uliJobIndex;
    private BasicCoordinateTransform transform;


    public IntersectionReader(DataSet dataSet, BasicCoordinateTransform transform) {
        super(dataSet);
        this.transform = transform;
    }

    @Override
    public void read() {

        super.read("F:/Qin/MoPeD/MunichPIE/data/employment/jobLocation.csv", ",");

    }

    @Override
    protected void processHeader(String[] header) {
        idIndex = MoPeDUtil.findPositionInArray("OBJECTID", header);
        xIndex = MoPeDUtil.findPositionInArray("x", header);
        yIndex = MoPeDUtil.findPositionInArray("y", header);
        totalJobIndex = MoPeDUtil.findPositionInArray("totalCount", header);
        uliJobIndex = MoPeDUtil.findPositionInArray("uliCount", header);

    }

    @Override
    protected void processRecord(String[] record) {
        int id = Integer.parseInt(record[idIndex]);
        double x = Double.parseDouble(record[xIndex]);
        double y = Double.parseDouble(record[yIndex]);
        double totalJob = Double.parseDouble(record[totalJobIndex]);
        double uliJob = Double.parseDouble(record[uliJobIndex]);


        ProjCoordinate targetCoordinate = transform(x,y);
        //Job job = new Job(id, new Coordinate(targetCoordinate.x,targetCoordinate.y),totalJob,uliJob);


        //MucPIECaculator.jobTree.put(job.getCoord().x,job.getCoord().y,job);


    }

    private ProjCoordinate transform(double x, double y) {

        ProjCoordinate srcCoord = new ProjCoordinate(x,y);
        ProjCoordinate dstCoord = new ProjCoordinate();
        return transform.transform(srcCoord, dstCoord);

    }

}
