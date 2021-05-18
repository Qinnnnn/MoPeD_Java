package pie.data.employmentAllocation;

import org.locationtech.jts.geom.Geometry;

import java.util.HashMap;
import java.util.Map;

public class BlockGroup {

    private final int id;
    private Map<Integer,Integer> jobsByType = new HashMap<>();
    private Geometry geometry;

    public BlockGroup(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }

    public Map<Integer, Integer> getJobsByType() {
        return jobsByType;
    }

    public void setJobsByType(Map<Integer, Integer> jobsByType) {
        this.jobsByType = jobsByType;
    }
}
