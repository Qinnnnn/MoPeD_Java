package pie;


import com.google.common.collect.Iterables;
import com.google.common.math.LongMath;
import de.tum.bgu.msm.moped.util.concurrent.ConcurrentExecutor;
import org.apache.log4j.Logger;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.index.quadtree.Quadtree;
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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;


public class MucPIECaculator {
    private final static Logger logger = Logger.getLogger(MucPIECaculator.class);
    private final static AtomicInteger failedJobMatchingCounter = new AtomicInteger(0);
    private static final double BUFFER_AREA = 800;
    private static QuadTree<MucPaz> zoneSearchTree;
    private static Quadtree pazQuadTree = new Quadtree();
    private static ConcurrentMap<Integer,MucPaz> pazs = new ConcurrentHashMap<>();

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        zonesReader(args[0]);
        logger.info("   done zone read!");
        MucJobReader mucJobReader = new MucJobReader(null,args[1]);
        mucJobReader.read();
        logger.warn(failedJobMatchingCounter + " jobs cannot find a paz!");
        logger.info("   done job read!");
        writeOutZones(args[4]);
        //generateIsochrones();
        generatePIE(args[2],args[3]);
        logger.info("   done in seconds: " + (System.currentTimeMillis()-startTime));
    }

    private static void writeOutZones(String output) {
        StringBuilder zoneSB = new StringBuilder();
        zoneSB.append("zoneId,pop,Agri,Mnft,Util,Cons,Retl,Trns,Finc,Rlst,Admn,Serv");
        zoneSB.append('\n');
        for(MucPaz paz : pazs.values()){
            zoneSB.append(paz.getId());
            zoneSB.append(',');
            zoneSB.append(paz.getHouseholds());
            zoneSB.append(',');
            zoneSB.append(paz.getJobsByType().get("Agri"));
            zoneSB.append(',');
            zoneSB.append(paz.getJobsByType().get("Mnft"));
            zoneSB.append(',');
            zoneSB.append(paz.getJobsByType().get("Util"));
            zoneSB.append(',');
            zoneSB.append(paz.getJobsByType().get("Cons"));
            zoneSB.append(',');
            zoneSB.append(paz.getJobsByType().get("Retl"));
            zoneSB.append(',');
            zoneSB.append(paz.getJobsByType().get("Trns"));
            zoneSB.append(',');
            zoneSB.append(paz.getJobsByType().get("Finc"));
            zoneSB.append(',');
            zoneSB.append(paz.getJobsByType().get("Rlst"));
            zoneSB.append(',');
            zoneSB.append(paz.getJobsByType().get("Admn"));
            zoneSB.append(',');
            zoneSB.append(paz.getJobsByType().get("Serv"));
            zoneSB.append('\n');
        }

        try {
            writeToFile(output,zoneSB.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void generatePIE (String networkFile, String output) {
        final int partitionSize = (int) ((double) pazs.keySet().size() / Runtime.getRuntime().availableProcessors()) + 1;
        Iterable<List<MucPaz>> partitions = Iterables.partition(pazs.values(), partitionSize);
        logger.info( pazs.keySet().size() + " zones, " + Runtime.getRuntime().availableProcessors() + " processors, with " + partitionSize);
        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(networkFile);
        logger.info("   done network read!");
        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Runtime.getRuntime().availableProcessors());

        AtomicInteger counter = new AtomicInteger();
        FastMultiNodeDijkstraFactory fastMultiNodeDijkstraFactory = new FastMultiNodeDijkstraFactory(true);

        //write header
        StringBuilder pieSB = new StringBuilder();
        pieSB.append("zoneId,pop,Agri,Mnft,Util,Cons,Retl,Trns,Finc,Rlst,Admn,Serv");
        pieSB.append('\n');
        for (final List<MucPaz> partition : partitions) {
            MultiNodePathCalculator pathCalculator = (MultiNodePathCalculator) fastMultiNodeDijkstraFactory.createPathCalculator(network, new DistanceAsTravelDisutility(), (link, v, person, vehicle) -> link.getLength());
            executor.addTaskToQueue(() -> {
                try {
                    int id = counter.incrementAndGet();
                    int counterr = 0;
                    for (MucPaz origin : partition) {

                        if(LongMath.isPowerOfTwo(counterr)) {
                            logger.info(counterr + " in " + id);
                        };

                        Map<MucPaz, InitialNode> toNodes = new HashMap<>();
                        Coord originCoord = CoordUtils.createCoord(((Geometry)origin.getFeature().getDefaultGeometry()).getCoordinate());
                        Node originNode = NetworkUtils.getNearestNode(network, originCoord);
                        Collection<MucPaz> destinationZones = zoneSearchTree.getDisk(originCoord.getX(),originCoord.getY(),BUFFER_AREA);

                        for(MucPaz destination : destinationZones){
                            Coord destinationCoord = CoordUtils.createCoord(((Geometry)destination.getFeature().getDefaultGeometry()).getCoordinate());
                            Node destinationNode = NetworkUtils.getNearestNode(network, destinationCoord);
                            toNodes.put(destination,new InitialNode(destinationNode, 0., 0.));
                        }


                        ImaginaryNode aggregatedToNodes = MultiNodeDijkstra.createImaginaryNode(toNodes.values());
                        pathCalculator.calcLeastCostPath(originNode, aggregatedToNodes, 8*3600, null, null);

                        for (MucPaz destination : destinationZones) {
                            Coord destinationCoord = CoordUtils.createCoord(((Geometry)destination.getFeature().getDefaultGeometry()).getCoordinate());
                            Node destinationNode = NetworkUtils.getNearestNode(network, destinationCoord);
                            int travelDistance = (int) pathCalculator.constructPath(originNode, destinationNode, 8*3600).travelTime;
                            if(travelDistance<=BUFFER_AREA){
                                origin.householdWithinBuffer.getAndAdd(destination.getHouseholds());
                                for(String type:origin.jobsInBufferByType.keySet()){
                                    origin.jobsInBufferByType.get(type).getAndAdd(destination.getJobsByType().get(type));
                                }
                            }
                        }

                        synchronized (pieSB){
                            pieSB.append(origin.getId());
                            pieSB.append(',');
                            pieSB.append(origin.householdWithinBuffer.get());
                            pieSB.append(',');
                            pieSB.append(origin.jobsInBufferByType.get("Agri"));
                            pieSB.append(',');
                            pieSB.append(origin.jobsInBufferByType.get("Mnft"));
                            pieSB.append(',');
                            pieSB.append(origin.jobsInBufferByType.get("Util"));
                            pieSB.append(',');
                            pieSB.append(origin.jobsInBufferByType.get("Cons"));
                            pieSB.append(',');
                            pieSB.append(origin.jobsInBufferByType.get("Retl"));
                            pieSB.append(',');
                            pieSB.append(origin.jobsInBufferByType.get("Trns"));
                            pieSB.append(',');
                            pieSB.append(origin.jobsInBufferByType.get("Finc"));
                            pieSB.append(',');
                            pieSB.append(origin.jobsInBufferByType.get("Rlst"));
                            pieSB.append(',');
                            pieSB.append(origin.jobsInBufferByType.get("Admn"));
                            pieSB.append(',');
                            pieSB.append(origin.jobsInBufferByType.get("Serv"));
                            pieSB.append('\n');
                        }
                        counterr++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
                return null;
            });
        }
        executor.execute();
        try {
            writeToFile(output,pieSB.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static void zonesReader(String input) {
        setZoneSearchTree(input);
        for (SimpleFeature feature: ShapeFileReader.getAllFeatures(input)) {
            int zoneId = Integer.parseInt(feature.getAttribute("zoneID").toString());
            int pop = Integer.parseInt(feature.getAttribute("Einwohner").toString());
            Point centroid = ((Geometry)feature.getDefaultGeometry()).getCentroid();
            MucPaz paz = new MucPaz(zoneId);
            paz.setHouseholds(pop==-1?0:pop);
            paz.setFeature(feature);
            zoneSearchTree.put(centroid.getX(),centroid.getY(),paz);
            pazQuadTree.insert(((Geometry)(feature.getDefaultGeometry())).getEnvelopeInternal(),paz);
            pazs.put(zoneId,paz);
        }
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

    static MucPaz locateJobToPAZ(Coordinate coordinate){
        GeometryFactory gf = new GeometryFactory();
        Point point = gf.createPoint(coordinate);
        List<MucPaz> pazs = pazQuadTree.query(point.getEnvelopeInternal());

        for (MucPaz paz : pazs){
            if(((Geometry)paz.getFeature().getDefaultGeometry()).contains(point)){
                return paz;
            }
        }

        //logger.warn("Coordinate x: " + coordinate.x + ", y: " + coordinate.y + " can not be located to a moped zone.");
        failedJobMatchingCounter.incrementAndGet();
        return zoneSearchTree.getClosest(coordinate.x,coordinate.y);
    }

    public static void writeToFile(String path, String building) throws FileNotFoundException {
        PrintWriter bd = new PrintWriter(new FileOutputStream(path, true));
        bd.write(building);
        bd.close();
    }

//    public static void main(String arg[]) {
//
//        DataSet ds = null;
//
//        Envelope bounds = loadEnvelope();
//        double minX = ((ReferencedEnvelope) bounds).getMinX() - 500;
//        double minY = ((ReferencedEnvelope) bounds).getMinY() - 500;
//        double maxX = ((ReferencedEnvelope) bounds).getMaxX() + 500;
//        double maxY = ((ReferencedEnvelope) bounds).getMaxY() + 500;
//
//
//        jobTree = new QuadTree<>(minX, minY, maxX, maxY);
//        popTree = new QuadTree<>(minX, minY, maxX, maxY);
//        transitTree = new QuadTree<>(minX, minY, maxX, maxY);
//
//
//        CRSFactory factory = new CRSFactory();
//        CoordinateReferenceSystem srcCrs = factory.createFromName("EPSG:31468");
//        CoordinateReferenceSystem dstCrs = factory.createFromName("EPSG:3035");
//        BasicCoordinateTransform transform = new BasicCoordinateTransform(srcCrs, dstCrs);
//
//
//        PopulationReader populationReader = new PopulationReader(ds);
//        JobReader employmentReader = new JobReader(ds, transform);
//        TransitReader transitReader = new TransitReader(ds, transform);
//
//        populationReader.read();
//        employmentReader.read();
//        transitReader.read();
//
//        calculateJobsWithinBuffer();
//        calculatePopWithinBuffer();
//        calculateTransitsWithinBuffer();
//
//        calculatePIE();
//
//        writeOut();
//
//    }
//
//    private static Envelope loadEnvelope() {
//        //TODO: Remove minX,minY,maxX,maxY when implementing study area shapefile in Geodata 09 Oct QZ'
//        //File schoolsShapeFile = new File("");
//        String urlAsString = "file:/F:/Qin/MoPeD/MunichPIE/shapefiles/mucZone3035.shp";
//        URL url = null;
//        try {
//            url = new URL(urlAsString);
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        }
//        try {
//            FileDataStore dataStore = FileDataStoreFinder.getDataStore(url);
//            return dataStore.getFeatureSource().getBounds();
//        } catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//    }
//
//    private static void calculatePIE() {
//
//
//    }
//
//    private static void calculatePopWithinBuffer() {
//        int sumPopulation;
//
//        for(BlockGroup mucZone : zoneMap.values()){
//
//            sumPopulation = 0;
//
//            Collection<BlockGroup> popList = popTree.getDisk(mucZone.getCoord().x,mucZone.getCoord().y,searchRadius);
//
//            for (BlockGroup zone : popList){
//                sumPopulation += zone.getPopulation();
//            }
//
//            mucZone.setPopulationWithinBuffer(sumPopulation);
//        }
//
//    }
//
//    private static void calculateJobsWithinBuffer() {
//
//        double sumTotalJob;
//        double sumUliJob;
//
//        for(BlockGroup mucZone : zoneMap.values()){
//
//            sumTotalJob = 0.0;
//            sumUliJob = 0.0;
//
//            Collection<Job> jobList = jobTree.getDisk(mucZone.getCoord().x,mucZone.getCoord().y,searchRadius);
//
//            for (Job job : jobList){
//                sumTotalJob += job.getTotalJob();
//                sumUliJob += job.getUliJob();
//            }
//
//            mucZone.setTotalJobWithinBuffer(sumTotalJob);
//            mucZone.setUliJobWithinBuffer(sumUliJob);
//        }
//
//
//    }
//
//    private static void calculateTransitsWithinBuffer() {
//        int sumTransitStops;
//
//        for(BlockGroup mucZone : zoneMap.values()){
//
//            sumTransitStops = 0;
//
//            Collection<Transit> transitList = transitTree.getDisk(mucZone.getCoord().x,mucZone.getCoord().y,searchRadius);
//
//            for (Transit transit : transitList){
//                sumTransitStops += transit.getWeightedTransitStops();
//            }
//
//            mucZone.setTransitStopsWithinBuffer(sumTransitStops);
//        }
//
//    }
//
//    public static void writeOut() {
//        String outputPath = "/F:/Qin/MoPeD/MunichPIE/data/mucPIE_job_pop.csv";
//        StringBuilder pop = new StringBuilder();
//
//        //write header
//        pop.append("id,xCoord,yCoord,population,job");
//        pop.append('\n');
//
//        //write data
//        for (PAZ mucZone : zoneMap.values()) {
//            pop.append(mucZone.getId());
//            pop.append(',');
//            pop.append(mucZone.getHouseholds());
//            pop.append(',');
//            pop.append(mucZone.getTotalJob());
//
//
//            pop.append('\n');
//        }
//
//
//        try {
//            writeToFile(outputPath, pop.toString());
//        } catch(FileNotFoundException e) {
//            e.printStackTrace();
//        }
//
//
//    }
}


