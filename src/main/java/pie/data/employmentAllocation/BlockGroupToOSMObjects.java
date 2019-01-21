package pie.data.employmentAllocation;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

import com.vividsolutions.jts.index.quadtree.Quadtree;
import de.tum.bgu.msm.moped.data.DataSet;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.jblas.FloatMatrix;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;
import pie.data.FourWayIntersection;
import pie.data.OHASTrip;
import pie.data.PAZ;
import pie.readers.OHASTripReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static de.tum.bgu.msm.moped.io.output.OutputWriter.writeToFile;

public class BlockGroupToOSMObjects {

    public static List<OSMObject> osmObjectsList = new ArrayList<>();
    public static Map<Integer, PAZ> pazMap = new HashMap<>();
    public static Quadtree blockGroupQuadTree = new Quadtree();
    public static Quadtree pazQuadTree = new Quadtree();
    public static FloatMatrix productionArea;
    public static FloatMatrix blockGroupJobs;
    public static FloatMatrix blockGroupAreas;
    public static final double searchRadius = 2624.671916;
    public static QuadTree<PAZ> pazTree; //include pop and job
    public static QuadTree<FourWayIntersection> intersectionTree;
    public static List<OHASTrip> ohasTrips = new ArrayList<>();

    public static Map<Integer,Integer> tripMap = new HashMap<>();

    public static void main(String arg[]){
        productionArea = new FloatMatrix(9, 9);
        blockGroupJobs = new FloatMatrix(1500, 9);
        blockGroupAreas = new FloatMatrix(1500, 9);

        org.opengis.geometry.Envelope bounds = loadEnvelope();
        double minX = ((ReferencedEnvelope)bounds).getMinX()-searchRadius;
        double minY = ((ReferencedEnvelope)bounds).getMinY()-searchRadius;
        double maxX = ((ReferencedEnvelope)bounds).getMaxX()+searchRadius;
        double maxY = ((ReferencedEnvelope)bounds).getMaxY()+searchRadius;

        pazTree = new QuadTree<>(minX,minY,maxX,maxY);
        intersectionTree = new QuadTree<>(minX,minY,maxX,maxY);

        readProductionArea(arg);
        readBlockGroupShapes();
        readPoiPolygonShapes();
        readBuildingShapes();
        readPoiPointShapes();
        jobAllocationToOSMObject();
        readPAZShapes();
        jobAllocationToPAZ();


        readHouseholdShapes();
        readIntersectionShapes();

        calculatePieWithinBuffer();
        writeOutOSMObject();
        writeOutPAZ();

        DataSet ds = null;
        OHASTripReader ohasTripReader = new OHASTripReader(ds);
        ohasTripReader.read();
        writeOutTrips();


    }

    private static void writeOutTrips() {
        String outputPath = "/F:/Qin/MoPeD/NewPIE/data/LinkedTrips_withBE.csv";
        StringBuilder pop = new StringBuilder();

        //write header
        pop.append("tripid,pazid,MODE,INCOME,GENDER,AGE,hh,uliJob,activity,hhDensity,uliJobDensity,activityDensity,intersectionDensity,weight,PURPOSE1,distance,disable,drivingLicense,transitPass");
        pop.append('\n');

        //write data
        for (OHASTrip ohasTrip: ohasTrips){
            pop.append(ohasTrip.getId());
            pop.append(',');
            pop.append(ohasTrip.getPaz());
            pop.append(',');
            pop.append(ohasTrip.getMode());
            pop.append(',');
            pop.append(ohasTrip.getIncome());
            pop.append(',');
            pop.append(ohasTrip.getGender());
            pop.append(',');
            pop.append(ohasTrip.getAge());

            double area = Math.PI * 0.8 * 0.8;
            PAZ paz = pazMap.get(ohasTrip.getPaz());
            pop.append(',');
            pop.append(paz.getHouseholds());
            pop.append(',');
            pop.append(paz.getUliJob());
            pop.append(',');
            pop.append(paz.getHouseholds()+paz.getUliJob());
            pop.append(',');
            pop.append(paz.getHouseholdWithinBuffer()/area);
            pop.append(',');
            pop.append(paz.getUliJobWithinBuffer()/area);
            pop.append(',');
            pop.append((paz.getHouseholdWithinBuffer()+paz.getUliJobWithinBuffer())/area);
            pop.append(',');
            pop.append(paz.getIntersectionWithinBuffer());
            pop.append(',');
            pop.append(ohasTrip.getWeight());
            pop.append(',');
            pop.append(ohasTrip.getPurpose());
            pop.append(',');
            pop.append(ohasTrip.getDistance());
            pop.append(',');
            pop.append(ohasTrip.getDisable());
            pop.append(',');
            pop.append(ohasTrip.getDrivingLicense());
            pop.append(',');
            pop.append(ohasTrip.getTransitPass());
            pop.append('\n');
        }

        try {
            writeToFile(outputPath,pop.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }


    @Deprecated
    private static void writeOut() {
        String outputPath = "/F:/Qin/MoPeD/NewPIE/data/test.csv";
        StringBuilder pop = new StringBuilder();

        //write header
        pop.append("id,zoneid");
        pop.append('\n');

        //write data
        for (int id  : tripMap.keySet()){
            pop.append(id);
            pop.append(',');
            pop.append(tripMap.get(id));
            pop.append('\n');
        }

        try {
            writeToFile(outputPath,pop.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    @Deprecated
    private static void readOHASTripsShapes() {
        String urlAsString = "linkedTrip_6559.shp";
        if (urlAsString == null) {
            throw new RuntimeException("No shape file found!");
        }



        for (SimpleFeature feature : ShapeFileReader.getAllFeatures(urlAsString)) {
            int id = Integer.parseInt(feature.getAttribute("id").toString());
            Geometry geometry = (Geometry)feature.getAttribute("the_geom");
            List<PAZ> pazs = pazQuadTree.query(geometry.getEnvelopeInternal());
            int count = 0;
            for (PAZ paz : pazs){
                if (paz.getGeometry().contains(geometry)){
                    tripMap.put(id,paz.getId());
                    count++;
                }
            }

            if ((count > 1)||(count == 0)) {
                System.out.println(count);
            }
        }
    }

    private static void calculatePieWithinBuffer() {
        double sumPopulation;
        double sumTotalJob;
        double sumUliJob;

        for(PAZ paz : pazMap.values()){

            sumPopulation = 0.0;
            sumTotalJob = 0.0;
            sumUliJob = 0.0;

            Collection<PAZ> pazs = pazTree.getDisk(paz.getCoord().x,paz.getCoord().y,searchRadius);

            for (PAZ zone : pazs){
                sumPopulation += zone.getHouseholds();
                sumTotalJob += zone.getTotalJob();
                sumUliJob += zone.getUliJob();
            }

            paz.setHouseholdWithinBuffer(sumPopulation);
            paz.setTotalJobWithinBuffer(sumTotalJob);
            paz.setUliJobWithinBuffer(sumUliJob);

            Collection<FourWayIntersection> fourWayIntersections = intersectionTree.getDisk(paz.getCoord().x,paz.getCoord().y,searchRadius);
            paz.setIntersectionWithinBuffer(fourWayIntersections.size());
        }

    }

    private static void readIntersectionShapes() {
        String urlAsString = "intersections_points_6559.shp";
        if (urlAsString == null) {
            throw new RuntimeException("No shape file found!");
        }

        for (SimpleFeature feature : ShapeFileReader.getAllFeatures(urlAsString)) {
            if(feature.getAttribute("fourWay").toString().equals("true")){
                int id = Integer.parseInt(feature.getAttribute("id").toString());
                FourWayIntersection fourWayIntersection = new FourWayIntersection(id);
                fourWayIntersection.setCoord(new Coordinate(((Geometry)feature.getAttribute("the_geom")).getCoordinate().x,((Geometry)feature.getAttribute("the_geom")).getCoordinate().y));
                intersectionTree.put(fourWayIntersection.getCoord().x,fourWayIntersection.getCoord().y,fourWayIntersection);
            }
        }

    }

    private static void readHouseholdShapes() {
        String urlAsString = "households_points_6559.shp";
        if (urlAsString == null) {
            throw new RuntimeException("No shape file found!");
        }

        for (SimpleFeature feature : ShapeFileReader.getAllFeatures(urlAsString)) {
            int id = Integer.parseInt(feature.getAttribute("OBJECTID").toString());
            PAZ paz = pazMap.get(id);
            if(paz!=null) {
                paz.setHouseholds(Double.parseDouble(feature.getAttribute("HH").toString()));
            }
        }
    }

    private static void readPAZShapes() {
        String urlAsString = "PAZ_Polygons_6559.shp";
        if (urlAsString == null) {
            throw new RuntimeException("No shape file found!");
        }

        for (SimpleFeature feature : ShapeFileReader.getAllFeatures(urlAsString)) {
            int id = (int) Float.parseFloat(feature.getAttribute("PAZ").toString());
            PAZ paz = new PAZ(id);
            paz.setGeometry((Geometry)feature.getAttribute("the_geom"));
            Envelope env = paz.getGeometry().getEnvelopeInternal();
            pazQuadTree.insert(env,paz);
            pazMap.put(id,paz);
            paz.setCoord(new Coordinate(paz.getGeometry().getCentroid().getCoordinate().x,paz.getGeometry().getCentroid().getCoordinate().y));
            pazTree.put(paz.getCoord().x,paz.getCoord().y,paz);
        }
    }

    private static void jobAllocationToPAZ() {
        for (OSMObject osmObject : osmObjectsList) {
            List<PAZ> pazs = pazQuadTree.query(osmObject.getGeometry().getEnvelopeInternal());
            int count = 0;
            for (PAZ paz : pazs){
                if (paz.getGeometry().intersects(osmObject.getGeometry())){
                    Geometry geometry = paz.getGeometry().intersection(osmObject.getGeometry());
                    double areaRatio = geometry.getArea()/osmObject.getGeometry().getArea();
                    for(int type = 1; type <=8; type++){
                        double job = (osmObject.getJobsByType().get(type)* areaRatio);
                        double prevJob = 0.0;
                        if(paz.getJobsByType().get(type) != null){
                            prevJob = paz.getJobsByType().get(type);
                        }
                        paz.getJobsByType().put(type,job+prevJob);
                    }
                    count++;
                }
            }
            osmObject.setNumberOfIntersectPAZ(count);
        }

        for(PAZ paz : pazMap.values()){
            double totaljob = 0.0;
            for (int type = 1; type <= 8; type++){
                if(paz.getJobsByType().get(type)!= null){
                    totaljob += paz.getJobsByType().get(type);
                }else{
                    paz.getJobsByType().put(type,0.0);
                }
            }

            paz.setTotalJob(totaljob);
            paz.setUliJob(paz.getJobsByType().get(2)+paz.getJobsByType().get(5));
        }


    }

    private static void jobAllocationToOSMObject() {
        for (OSMObject osmObject : osmObjectsList){
            int blockGroup = osmObject.getBlockGroupId();
            for (int type = 1; type <= 8; type++){
                float jobsForThisType = osmObject.getAreasByType().get(type)*blockGroupJobs.get(blockGroup,type)/blockGroupAreas.get(blockGroup,type);
                osmObject.getJobsByType().put(type,jobsForThisType);
            }
        }
    }

    private static void readProductionArea(String arg[]) {
        for (int i = 0; i < 8; i++){
            String[] list = arg[i].split(",");
            for(int j = 0; j < 8; j++){
                productionArea.put(i+1,j+1,Float.parseFloat(list[j]));
            }
        }
    }


    private static void readBlockGroupShapes() {
        String urlAsString = "blockGroup.shp";
        if (urlAsString == null) {
            throw new RuntimeException("No shape file found!");
        }

        Map<Integer,Integer> jobsByType = new HashMap<>();

        for (SimpleFeature feature : ShapeFileReader.getAllFeatures(urlAsString)) {
            int id = Integer.parseInt(feature.getAttribute("fid").toString());
            BlockGroup blockGroup = new BlockGroup(id);
            jobsByType.put(1,(int)Double.parseDouble(feature.getAttribute("E8_OFF10").toString()));
            jobsByType.put(2,(int)Double.parseDouble(feature.getAttribute("E8_RET10").toString()));
            jobsByType.put(3,(int)Double.parseDouble(feature.getAttribute("E8_IND10").toString()));
            jobsByType.put(4,(int)Double.parseDouble(feature.getAttribute("E8_SVC10").toString()));
            jobsByType.put(5,(int)Double.parseDouble(feature.getAttribute("E8_ENT10").toString()));
            jobsByType.put(6,(int)Double.parseDouble(feature.getAttribute("E8_ED10").toString()));
            jobsByType.put(7,(int)Double.parseDouble(feature.getAttribute("E8_HLTH10").toString()));
            jobsByType.put(8,(int)Double.parseDouble(feature.getAttribute("E8_PUB10").toString()));
            blockGroup.setJobsByType(jobsByType);
            blockGroup.setGeometry((Geometry)feature.getAttribute("the_geom"));
            Envelope env = blockGroup.getGeometry().getEnvelopeInternal();
            blockGroupQuadTree.insert(env,blockGroup);
            for (int i = 1; i<=8; i++){
                blockGroupJobs.put(blockGroup.getId(),i,blockGroup.getJobsByType().get(i));
            }
        }
    }




    private static void readPoiPolygonShapes() {
        String urlAsString = "portland_employment/portland_poi_polygons_filtered.shp";
        if (urlAsString == null) {
            throw new RuntimeException("No shape file found!");
        }
        readOSMObjects(urlAsString);
    }

    private static void readBuildingShapes() {
        String urlAsString = "portland_employment/portland_building_polygons_filtered_landuseCombi_noOverlapPOI.shp";
        if (urlAsString == null) {
            throw new RuntimeException("No shape file found!");
        }

        readOSMObjects(urlAsString);

    }

    private static void readPoiPointShapes() {

        String urlAsString = "portland_employment/portland_poi_points_filtered_buffered.shp";
        if (urlAsString == null) {
            throw new RuntimeException("No shape file found!");
        }

        readOSMObjects(urlAsString);

    }

    private static void readOSMObjects(String urlAsString) {
        for (SimpleFeature feature : ShapeFileReader.getAllFeatures(urlAsString)) {
            long osmId = Long.parseLong(feature.getAttribute("osm_id").toString());
            int jobType = Integer.parseInt(feature.getAttribute("jobType").toString());
            Geometry geometry = (Geometry)feature.getAttribute("the_geom");
            List<BlockGroup> blockGroups = blockGroupQuadTree.query(geometry.getEnvelopeInternal());
            for (BlockGroup blockGroup : blockGroups){
                if (blockGroup.getGeometry().intersects(geometry)){
                    OSMObject osmObject = new OSMObject(osmId);
                    osmObject.setGeometry(blockGroup.getGeometry().intersection(geometry));
                    osmObject.setJobType(jobType);
                    osmObject.setBlockGroupId(blockGroup.getId());
                    osmObjectsList.add(osmObject);
                    for(int type = 1; type <=8; type++){
                        float area = (float) (osmObject.getGeometry().getArea()* productionArea.get(jobType,type));
                        float prevArea = blockGroupAreas.get(blockGroup.getId(),type);
                        blockGroupAreas.put(blockGroup.getId(),type,area+prevArea);
                        osmObject.getAreasByType().put(type,area);
                    }
                }
            }
        }
    }





    public static void writeOutOSMObject(){
        String outputPath = "/F:/Qin/MoPeD/NewPIE/data/osmObjectJobAllocationResult.csv";
        StringBuilder pop = new StringBuilder();

        //write header
        pop.append("osm_id,blockGroupId,jobType,job1,job2,job3,job4,job5,job6,job7,job8,intersectCount");
        pop.append('\n');

        //write data
        for (OSMObject osmObject : osmObjectsList){
            pop.append(osmObject.getId());
            pop.append(',');
            pop.append(osmObject.getBlockGroupId());
            pop.append(',');
            pop.append(osmObject.getJobType());
            pop.append(',');
            for (int type = 1; type <= 8; type++){
                pop.append(osmObject.getJobsByType().get(type));
                pop.append(',');
            }
            pop.append(osmObject.getNumberOfIntersectPAZ());
            pop.append('\n');
        }

        try {
            writeToFile(outputPath,pop.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void writeOutPAZ(){
        String outputPath = "/F:/Qin/MoPeD/NewPIE/data/pazJobAllocationResult.csv";
        StringBuilder pop = new StringBuilder();
        double area = Math.PI * 0.8 * 0.8;
        //write header
        pop.append("id,job1,job2,job3,job4,job5,job6,job7,job8,hh,ulijob,activity,hhDensity,uliJobDensity,activityDensity,fourWayIntersection");
        pop.append('\n');

        //write data
        for (PAZ paz : pazMap.values()){
            pop.append(paz.getId());
            pop.append(',');

            for (int type = 1; type <= 8; type++){
                if(paz.getJobsByType().get(type)!=null) {
                    pop.append(paz.getJobsByType().get(type));
                    pop.append(',');
                }else{
                    pop.append(0.0);
                    pop.append(',');
                }

            }
            pop.append(paz.getHouseholds());
            pop.append(',');
            pop.append(paz.getUliJob());
            pop.append(',');
            pop.append(paz.getHouseholds()+paz.getUliJob());
            pop.append(',');
            pop.append(paz.getHouseholdWithinBuffer()/area);
            pop.append(',');
            pop.append(paz.getUliJobWithinBuffer()/area);
            pop.append(',');
            pop.append((paz.getHouseholdWithinBuffer()+paz.getUliJobWithinBuffer())/area);
            pop.append(',');
            pop.append(paz.getIntersectionWithinBuffer());
            pop.append('\n');
        }

        try {
            writeToFile(outputPath,pop.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static org.opengis.geometry.Envelope loadEnvelope() {

        String urlAsString = "file:/F:/Qin/MoPeD/newPIE/shapefiles/blockGroup.shp";
        URL url = null;
        try {
            url = new URL(urlAsString);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            FileDataStore dataStore = FileDataStoreFinder.getDataStore(url);
            return dataStore.getFeatureSource().getBounds();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
