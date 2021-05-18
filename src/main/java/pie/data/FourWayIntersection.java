package pie.data;


import org.locationtech.jts.geom.Coordinate;

public class FourWayIntersection {
    private  int id;
    private Coordinate coord;


    public FourWayIntersection(int id) {
        this.id = id;
    }


    public int getId() {
        return id;
    }

    public Coordinate getCoord() {
        return coord;
    }

    public void setCoord(Coordinate coord) {
        this.coord = coord;
    }
}
