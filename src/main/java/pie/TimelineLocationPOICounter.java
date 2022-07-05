package pie;

import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.matsim.api.core.v01.Coord;
import org.matsim.core.utils.collections.QuadTree;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.gis.ShapeFileReader;
import org.opengis.feature.simple.SimpleFeature;
import pie.data.employmentAllocation.OSMObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static de.tum.bgu.msm.moped.io.output.OutputWriter.writeToFile;

public class TimelineLocationPOICounter {

    public static final double searchRadius = 1000;
    private static Map<OSMType, QuadTree<OSMObject>>  osmAmenityTreeMap = new HashMap<>();
    private static Map<String, Coordinate> locationMap = new HashMap<>();
    private static Map<String, Map<OSMType,Integer>> locationPOIMap = new HashMap<>();
    private static double minX;
    private static double minY;
    private static double maxX;
    private static double maxY;

    public static void main(String arg[]){
        String boundary = "F:\\models\\timelinedata\\rawData\\untilMay\\builtEnvironmentData/de_amenity.shp";
        String osmAmenityFile = "F:\\models\\timelinedata\\rawData\\untilMay\\builtEnvironmentData/de_amenity_31468.shp";
        String timelineLocationFile = "F:\\models\\timelinedata\\rawData\\untilMay\\builtEnvironmentData/tripDestination_31468.shp";
        String outputFile = "F:\\models\\timelinedata\\rawData\\untilMay\\builtEnvironmentData/tripDestinationPOI.csv";
        CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, TransformationFactory.DHDN_GK4);

        ReferencedEnvelope bounds = loadEnvelope(boundary);

        Coord min = ct.transform(new Coord(bounds.getMinX(),bounds.getMinY()));
        Coord max = ct.transform(new Coord(bounds.getMaxX(),bounds.getMaxY()));
        minX = min.getX();
        minY = min.getY();
        maxX = max.getX();
        maxY = max.getY();

        for (OSMType type : OSMType.values()){
            QuadTree<OSMObject> osmAmenityTree = new QuadTree<>(minX,minY,maxX,maxY);
            osmAmenityTreeMap.put(type,osmAmenityTree);
        }

        readOSMObjects(osmAmenityFile);
        readTimelineLocation(timelineLocationFile);

        countPOIWithinBuffer();
        writeOutPOIs(outputFile);


    }

    private static void writeOutPOIs(String outputPath) {
        StringBuilder poi = new StringBuilder();

        //write header
        poi.append("uid,x,y");
        for (OSMType type : OSMType.values()){
            poi.append(",");
            poi.append(type.name());
        }
        poi.append('\n');

        //write data
        for (String uid: locationPOIMap.keySet()){
            poi.append(uid);
            poi.append(',');
            poi.append(locationMap.get(uid).x);
            poi.append(',');
            poi.append(locationMap.get(uid).y);
            for (OSMType type : OSMType.values()){
                poi.append(",");
                poi.append(locationPOIMap.get(uid).get(type));
            }
            poi.append('\n');
        }

        try {
            writeToFile(outputPath,poi.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    private static void countPOIWithinBuffer() {

        for(String uid : locationMap.keySet()){
            for(OSMType type : OSMType.values()){
                int count = osmAmenityTreeMap.get(type).getDisk(locationMap.get(uid).x,locationMap.get(uid).y,searchRadius).size();
                if(locationPOIMap.get(uid)==null){
                    Map<OSMType,Integer> poiMap = new HashMap<>();
                    locationPOIMap.put(uid, poiMap);
                }
                locationPOIMap.get(uid).put(type,count);
            }
        }

    }

    private static void readTimelineLocation(String timelineLocationFile) {
        if (timelineLocationFile == null) {
            throw new RuntimeException("No shape file found!");
        }

        for (SimpleFeature feature : ShapeFileReader.getAllFeatures(timelineLocationFile)) {
            String uid = feature.getAttribute("trip_uid").toString();
            Geometry geometry = (Geometry)feature.getAttribute("the_geom");
            double x = geometry.getCentroid().getX();
            double y = geometry.getCentroid().getY();
            locationMap.put(uid,new Coordinate(x,y));
        }
    }



    private static void readOSMObjects(String osmAmenityFile) {

        if (osmAmenityFile == null) {
            throw new RuntimeException("No shape file found!");
        }
        int counter = 0;
        int infCount = 0;
        int outOfBoundCount = 0;
        int count = 0;

        for (SimpleFeature feature : ShapeFileReader.getAllFeatures(osmAmenityFile)) {

            long osmId = Long.parseLong(feature.getAttribute("osm_id").toString());
            String osmTag = feature.getAttribute("other_tags").toString();
            Geometry geometry = (Geometry)feature.getAttribute("the_geom");
            double x = geometry.getCentroid().getX();
            double y = geometry.getCentroid().getY();
            if(x==Double.POSITIVE_INFINITY|y==Double.POSITIVE_INFINITY){
                infCount++;
                continue;
            }

            if(x>maxX|x<minX|y>maxY|y<minY){
                outOfBoundCount++;
                continue;
            }


            OSMType amenityType;
            if (osmTag.contains("amenity")){
                amenityType = OSMType.amenity;
            }else if (osmTag.contains("club")){
                amenityType = OSMType.club;
            }else if (osmTag.contains("government")){
                amenityType = OSMType.government;
            }else if (osmTag.contains("healthcare")){
                amenityType = OSMType.healthcare;
            }else if (osmTag.contains("historic")){
                amenityType = OSMType.historic;
            }else if (osmTag.contains("leisure")){
                amenityType = OSMType.leisure;
            }else if (osmTag.contains("shop")){
                amenityType = OSMType.shop;
            }else if (osmTag.contains("social_facility")){
                amenityType = OSMType.socialFacility;
            }else if (osmTag.contains("sport")){
                amenityType = OSMType.sport;
            }else if (osmTag.contains("tourism")){
                amenityType = OSMType.tourism;
            }else{
                amenityType = null;
                System.out.println("Warning: no keywords found in osm other_tags!");
                counter++;
            }

            if (amenityType != null){
                OSMObject osmObject = new OSMObject(osmId);
                osmObject.setGeometry(geometry);
                osmObject.setAmenityType(amenityType);
                osmAmenityTreeMap.get(amenityType).put(x,y,osmObject);
            }

        }

        System.out.println("osm objects have no amenity type: " + counter);
        System.out.println("infinite points: " + infCount);
        System.out.println("out of boundary points: " + outOfBoundCount);
    }

    private static ReferencedEnvelope loadEnvelope(String path) {
        File poiFile = new File(path);
        try {
            FileDataStore dataStore = FileDataStoreFinder.getDataStore(poiFile);
            return dataStore.getFeatureSource().getBounds();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public enum OSMType {
        amenity,club,government,healthcare,historic,leisure,shop,socialFacility,sport,tourism
    }
}
