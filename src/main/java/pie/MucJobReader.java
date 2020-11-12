package pie;

import com.google.common.math.LongMath;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.io.input.CSVReader;
import de.tum.bgu.msm.moped.util.MoPeDUtil;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Coordinate;
import org.osgeo.proj4j.BasicCoordinateTransform;
import org.osgeo.proj4j.ProjCoordinate;

import java.util.HashMap;
import java.util.Map;


public class MucJobReader extends CSVReader {
    private int xIndex;
    private int yIndex;
    private int typeIndex;
    private String jobFile;
    private final static Logger logger = Logger.getLogger(MucPIECaculator.class);
    private int counter;


    public MucJobReader(DataSet dataSet, String jobFile) {
        super(dataSet);
        this.jobFile = jobFile;
    }

    @Override
    public void read() {
        super.read(jobFile, ",");
    }

    @Override
    protected void processHeader(String[] header) {
        typeIndex = MoPeDUtil.findPositionInArray("type", header);
        xIndex = MoPeDUtil.findPositionInArray("coordX", header);
        yIndex = MoPeDUtil.findPositionInArray("coordY", header);
    }

    @Override
    protected void processRecord(String[] record) {
        if(LongMath.isPowerOfTwo(counter)) {
            logger.info(counter + " jobs have read.");
        };
        double x = Double.parseDouble(record[xIndex]);
        double y = Double.parseDouble(record[yIndex]);
        String type = record[typeIndex].replace("\"", "");
        MucPaz paz = MucPIECaculator.locateJobToPAZ(new Coordinate(x,y));
        if(paz.getJobsByType().get(type)==null){
            System.out.println(type + "!" + counter);
        }
        int count = paz.getJobsByType().get(type);
        paz.getJobsByType().put(type, count + 1);
        counter++;
    }
}
