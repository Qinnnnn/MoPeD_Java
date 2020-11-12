package pie;

import com.google.common.util.concurrent.AtomicDouble;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import de.tum.bgu.msm.moped.data.Purpose;
import org.opengis.feature.simple.SimpleFeature;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class MucPaz {

    private final int id;
    private Map<String,Integer> jobsByType = new HashMap<String,Integer>(){{
        put("Agri", 0);
        put("Mnft", 0);
        put("Util", 0);
        put("Cons", 0);
        put("Retl", 0);
        put("Trns", 0);
        put("Finc", 0);
        put("Rlst", 0);
        put("Admn", 0);
        put("Serv", 0);
    }};

    Map<String,AtomicInteger> jobsInBufferByType = new HashMap<String, AtomicInteger>(){{
        put("Agri", new AtomicInteger(0));
        put("Mnft", new AtomicInteger(0));
        put("Util", new AtomicInteger(0));
        put("Cons", new AtomicInteger(0));
        put("Retl", new AtomicInteger(0));
        put("Trns", new AtomicInteger(0));
        put("Finc", new AtomicInteger(0));
        put("Rlst", new AtomicInteger(0));
        put("Admn", new AtomicInteger(0));
        put("Serv", new AtomicInteger(0));
    }};

    private int households;
    AtomicInteger householdWithinBuffer = new AtomicInteger(0);
    private SimpleFeature feature;

    public MucPaz(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public Map<String, Integer> getJobsByType() {
        return jobsByType;
    }

    public int getHouseholds() {
        return households;
    }

    public SimpleFeature getFeature() {
        return feature;
    }

    public void setFeature(SimpleFeature feature) {
        this.feature = feature;
    }

    public void setHouseholds(int households) {
        this.households = households;
    }
}
