package pie.useCase;


import de.tum.bgu.msm.moped.data.DataSet;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.matsim.core.utils.collections.QuadTree;
import org.opengis.geometry.Envelope;
import org.osgeo.proj4j.BasicCoordinateTransform;
import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import pie.data.FourWayIntersection;
import pie.data.employmentAllocation.BlockGroup;
import pie.readers.IntersectionReader;
import pie.readers.JobReader;
import pie.readers.PopulationReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static de.tum.bgu.msm.moped.io.output.OutputWriter.writeToFile;


public class PortlandPIECaculator {

//    public static Map<Integer, OHASTrip> tripMap = new HashMap<>();
//    public static Map<Integer, BlockGroup> zoneMap = new HashMap<>();
//    public static QuadTree<Job> jobTree;
//    public static QuadTree<BlockGroup> popTree;
//    public static QuadTree<FourWayIntersection> intersectionTree;
//    public static final double searchRadius = 500;
//
//
//
//    public static void main(String arg[]){
//
//        DataSet ds = null;
//
//        Envelope bounds = loadEnvelope();
//        double minX = ((ReferencedEnvelope)bounds).getMinX()-searchRadius;
//        double minY = ((ReferencedEnvelope)bounds).getMinY()-searchRadius;
//        double maxX = ((ReferencedEnvelope)bounds).getMaxX()+searchRadius;
//        double maxY = ((ReferencedEnvelope)bounds).getMaxY()+searchRadius;
//
//
//        jobTree = new QuadTree<>(minX,minY,maxX,maxY);
//        popTree = new QuadTree<>(minX,minY,maxX,maxY);
//        intersectionTree = new QuadTree<>(minX,minY,maxX,maxY);
//
//
//
//
//        CRSFactory factory = new CRSFactory();
//        CoordinateReferenceSystem srcCrs = factory.createFromName("EPSG:6559");
//        CoordinateReferenceSystem dstCrs = factory.createFromName("EPSG:6884");
//        BasicCoordinateTransform transform = new BasicCoordinateTransform(srcCrs, dstCrs);
//
//
//        PopulationReader populationReader = new PopulationReader(ds);
//        JobReader employmentReader = new JobReader(ds,transform);
//        IntersectionReader intersectionReader = new IntersectionReader(ds,transform);
//
//        populationReader.read();
//        employmentReader.read();
//        intersectionReader.read();
//
//        calculateJobsWithinBuffer();
//        calculatePopWithinBuffer();
//        calculateIntersectionWithinBuffer();
//
//        //calculatePIE();
//
//        writeOut();
//
//    }
//
//
//
//    private static Envelope loadEnvelope() {
//
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
//
//
//
//    private static void calculatePopWithinBuffer() {
//        int sumPopulation;
//
//        for(OHASTrip trip : tripMap.values()){
//
//            sumPopulation = 0;
//
//            Collection<BlockGroup> popList = popTree.getDisk(trip.getCoord().x,trip.getCoord().y,searchRadius);
//
//            for (BlockGroup zone : popList){
//                //sumPopulation += zone.getPopulation();
//            }
//
//            trip.setPopulationWithinBuffer(sumPopulation);
//        }
//
//    }
//
//    private static void calculateJobsWithinBuffer() {
//
//        double sumTotalJob;
//        double sumUliJob;
//
//        for(OHASTrip trip : tripMap.values()){
//
//            sumTotalJob = 0.0;
//            sumUliJob = 0.0;
//
//            Collection<Job> jobList = jobTree.getDisk(trip.getCoord().x,trip.getCoord().y,searchRadius);
//
//            for (Job job : jobList){
//                sumTotalJob += job.getTotalJob();
//                sumUliJob += job.getUliJob();
//
//            }
//
//            trip.setTotalJobWithinBuffer(sumTotalJob);
//            trip.setUliJobWithinBuffer(sumUliJob);
//        }
//
//
//    }
//
//
//
//    private static void calculateIntersectionWithinBuffer() {
//
//    }
//
//
//
//    public static void writeOut(){
//        String outputPath = "/F:/Qin/MoPeD/MunichPIE/data/mucPIE_onlyPop_noZero.csv";
//        StringBuilder pop = new StringBuilder();
//
//        //write header
//        pop.append("id,xCoord,yCoord,population,popInBuffer,totalJobInBuffer,popDensity,uliJobInBuffer,transitInBuffer,mucPIE");
//        pop.append('\n');
//
//        //write data
////        for (BlockGroup mucZone : zoneMap.values()){
////            if(mucZone.getPie() != 0.0) {
////                pop.append(mucZone.getId());
////                pop.append(',');
////                pop.append((int) mucZone.getCoord().x);
////                pop.append(',');
////                pop.append((int) mucZone.getCoord().y);
////                pop.append(',');
////                pop.append(mucZone.getPopulation());
////                pop.append(',');
////                pop.append(mucZone.getPopulationWithinBuffer());
////                pop.append(',');
////                pop.append(mucZone.getTotalJobWithinBuffer());
////                pop.append(',');
////                pop.append(mucZone.getPopDensity());
////                pop.append(',');
////                pop.append(mucZone.getUliJobWithinBuffer());
////                pop.append(',');
////                pop.append(mucZone.getTransitStopsWithinBuffer());
////                pop.append(',');
////                pop.append(mucZone.getPie());
////                pop.append('\n');
////            }
////        }
//
//        try {
//            writeToFile(outputPath,pop.toString());
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//    }



}
