package pie.data;

import org.locationtech.jts.geom.Coordinate;

@Deprecated
public class Transit {

    private final int id;
    private final Coordinate coord;
    private final int weightedTransitStops;

    public Transit(int id, Coordinate coord, int weightedTransitStops) {
        this.id = id;
        this.coord = coord;
        this.weightedTransitStops = weightedTransitStops;
    }

    public int getId() {
        return id;
    }

    public Coordinate getCoord() {
        return coord;
    }

    public int getWeightedTransitStops() {
        return weightedTransitStops;
    }
}
