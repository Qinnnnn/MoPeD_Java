package pie;

import com.google.common.collect.Iterables;
import com.google.common.math.LongMath;
import de.tum.bgu.msm.common.datafile.TableDataFileReader;
import de.tum.bgu.msm.common.datafile.TableDataSet;
import de.tum.bgu.msm.moped.util.concurrent.ConcurrentExecutor;
import org.apache.log4j.Logger;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Point;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.contrib.dvrp.router.DistanceAsTravelDisutility;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.router.*;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.core.utils.geometry.CoordUtils;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class isochroneGenerator {

    private static final double BUFFER_AREA = 800;
    private static Set<Integer> ohasZones = new HashSet<>();
    private static QuadTree<Integer> zoneSearchTree;
    private static Map<Integer, Point> zoneSimpleFeatures = new HashMap<>();
    private final static Logger logger = Logger.getLogger(isochroneGenerator.class);

    public static void main(String[] args) {

        long startTime = System.currentTimeMillis();
        readZoneCsv(args[3]);
        zonesReader(args[0]);
        logger.info("   done zone read!");
        generateIsochrones(args[1],args[2]);
        logger.info("   done in seconds: " + (System.currentTimeMillis()-startTime));
    }

    private static void generateIsochrones (String networkFile, String output) {
//        final int partitionSize = (int) ((double) zoneSimpleFeatures.keySet().size() / Runtime.getRuntime().availableProcessors()) + 1;
//        Iterable<List<Integer>> partitions = Iterables.partition(zoneSimpleFeatures.keySet(), partitionSize);
//        logger.info( zoneSimpleFeatures.keySet().size() + " zones, " + Runtime.getRuntime().availableProcessors() + " processors, with " + partitionSize);

        final int partitionSize = (int) ((double) ohasZones.size() / Runtime.getRuntime().availableProcessors()) + 1;
        Iterable<List<Integer>> partitions = Iterables.partition(ohasZones, partitionSize);
        logger.info( ohasZones.size() + " zones, " + Runtime.getRuntime().availableProcessors() + " processors, with " + partitionSize);


        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(networkFile);
        logger.info("   done network read!");
        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Runtime.getRuntime().availableProcessors());

        AtomicInteger counter = new AtomicInteger();
        FastMultiNodeDijkstraFactory fastMultiNodeDijkstraFactory = new FastMultiNodeDijkstraFactory(true);

        //write header
        StringBuilder isochronesSB = new StringBuilder();
        isochronesSB.append("origin,destination");
        isochronesSB.append('\n');
        try {
            writeToFile(output,isochronesSB.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for (final List<Integer> partition : partitions) {
            MultiNodePathCalculator pathCalculator = (MultiNodePathCalculator) fastMultiNodeDijkstraFactory.createPathCalculator(network, new DistanceAsTravelDisutility(), (link, v, person, vehicle) -> link.getLength());
            executor.addTaskToQueue(() -> {
                try {
                    int id = counter.incrementAndGet();
                    int counterr = 0;
                    for (int origin : partition) {
                        StringBuilder isochronesList = new StringBuilder();

                        if(LongMath.isPowerOfTwo(counterr)) {
                            logger.info(counterr + " in " + id);
                        };

                        Map<Integer,InitialNode> toNodes = new HashMap<>();
                        Coord originCoord = CoordUtils.createCoord(zoneSimpleFeatures.get(origin).getCoordinate());
                        Node originNode = NetworkUtils.getNearestNode(network, originCoord);
                        Collection<Integer> destinationZones = zoneSearchTree.getDisk(originCoord.getX(),originCoord.getY(),BUFFER_AREA);

                        for(int destination : destinationZones){
                            Coord destinationCoord = CoordUtils.createCoord(zoneSimpleFeatures.get(destination).getCoordinate());
                            Node destinationNode = NetworkUtils.getNearestNode(network, destinationCoord);
                            toNodes.put(destination,new InitialNode(destinationNode, 0., 0.));
                        }


                        ImaginaryNode aggregatedToNodes = MultiNodeDijkstra.createImaginaryNode(toNodes.values());
                        pathCalculator.calcLeastCostPath(originNode, aggregatedToNodes, 8*3600, null, null);

                        for (int destination : destinationZones) {

                            Coord destinationCoord = CoordUtils.createCoord(zoneSimpleFeatures.get(destination).getCoordinate());
                            Node destinationNode = NetworkUtils.getNearestNode(network, destinationCoord);
                            int travelDistance = (int) pathCalculator.constructPath(originNode, destinationNode, 8*3600).travelTime;
                            if(travelDistance<=BUFFER_AREA){
                                isochronesList.append(origin);
                                isochronesList.append(',');
                                isochronesList.append(destination);
                                isochronesList.append('\n');
                            }
                        }

                        try {
                            writeToFile(output,isochronesList.toString());
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }

                        counterr++;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            });
        }
        executor.execute();
        /*try {
            writeToFile(output,isochronesSB.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }*/
    }

    private static void zonesReader(String input) {
        setZoneSearchTree(input);
        for (SimpleFeature feature: ShapeFileReader.getAllFeatures(input)) {

            int zoneId = Integer.parseInt(feature.getAttribute("OBJECTID").toString());
            Point centroid = ((Geometry)feature.getDefaultGeometry()).getCentroid();
            zoneSimpleFeatures.put(zoneId,centroid);
            zoneSearchTree.put(centroid.getX(),centroid.getY(),zoneId);
        }
    }

    public static void readZoneCsv(String path) {
        TableDataSet zonalData = readCSVfile(path);
        int[] zoneIds = zonalData.getColumnAsInt("zoneID");
        for (int id : zoneIds){
            ohasZones.add(id);
        }
    }

    public static TableDataSet readCSVfile (String fileName) {
        // read csv file and return as TableDataSet
        File dataFile = new File(fileName);
        // line 210 debugging:
        // System.out.println("File path and name: " + new File(fileName).getAbsolutePath());
        TableDataSet dataTable;
        boolean exists = dataFile.exists();
        if (!exists) {
            final String msg = "File not found: " + fileName;
            logger.error(msg);
            throw new RuntimeException(msg) ;
        }
        try {
            TableDataFileReader reader = TableDataFileReader.createReader(dataFile);
            dataTable = reader.readFile(dataFile);
            reader.close();
        } catch (Exception e) {
            logger.error("Error reading file " + dataFile);
            throw new RuntimeException(e);
        }
        return dataTable;
    }

    private static void setZoneSearchTree(String input) {
        ReferencedEnvelope bounds = loadEnvelope(input);
        double minX = bounds.getMinX()-1;
        double minY = bounds.getMinY()-1;
        double maxX = bounds.getMaxX()+1;
        double maxY = bounds.getMaxY()+1;
        zoneSearchTree = new QuadTree<>(minX,minY,maxX,maxY);
    }

    private static ReferencedEnvelope loadEnvelope(String input) {
        File zonesShapeFile = new File(input);
        try {
            FileDataStore dataStore = FileDataStoreFinder.getDataStore(zonesShapeFile);
            return dataStore.getFeatureSource().getBounds();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static synchronized void writeToFile(String path, String building) throws FileNotFoundException {
        PrintWriter bd = new PrintWriter(new FileOutputStream(path, true));
        bd.write(building);
        bd.close();
    }
}

