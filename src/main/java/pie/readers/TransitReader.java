package pie.readers;

import com.vividsolutions.jts.geom.Coordinate;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.io.input.CSVReader;
import de.tum.bgu.msm.moped.util.MoPeDUtil;
import org.osgeo.proj4j.BasicCoordinateTransform;
import org.osgeo.proj4j.ProjCoordinate;
import pie.useCase.MucPIECaculator;
import pie.data.Transit;

@Deprecated
public class TransitReader extends CSVReader {
    private int idIndex;
    private int xIndex;
    private int yIndex;
    private int weightedTransitStopIndex;
    private BasicCoordinateTransform transform;


    public TransitReader(DataSet dataSet, BasicCoordinateTransform transform) {
        super(dataSet);
        this.transform = transform;
    }

    @Override
    public void read() {

        super.read("F:/Qin/MoPeD/MunichPIE/data/transit/transitStops_clean_qin.csv", ",");

    }

    @Override
    protected void processHeader(String[] header) {
        idIndex = MoPeDUtil.findPositionInArray("id", header);
        xIndex = MoPeDUtil.findPositionInArray("x", header);
        yIndex = MoPeDUtil.findPositionInArray("y", header);
        weightedTransitStopIndex = MoPeDUtil.findPositionInArray("weightedTransitStop", header);

    }

    @Override
    protected void processRecord(String[] record) {
        int id = Integer.parseInt(record[idIndex]);
        double x = Double.parseDouble(record[xIndex]);
        double y = Double.parseDouble(record[yIndex]);
        int weightedTransitStop = Integer.parseInt(record[weightedTransitStopIndex]);

        ProjCoordinate targetCoordinate = transform(x,y);
        Transit transit = new Transit(id, new Coordinate(targetCoordinate.x,targetCoordinate.y),weightedTransitStop);

        MucPIECaculator.transitTree.put(transit.getCoord().x,transit.getCoord().y,transit);


    }

    private ProjCoordinate transform(double x, double y) {

        ProjCoordinate srcCoord = new ProjCoordinate(x,y);
        ProjCoordinate dstCoord = new ProjCoordinate();
        return transform.transform(srcCoord, dstCoord);

    }

}
