package pie.useCase;


import org.matsim.core.utils.collections.QuadTree;
import pie.data.Transit;
import pie.data.employmentAllocation.BlockGroup;

import java.util.HashMap;
import java.util.Map;


public class MucPIECaculator {

   public static Map<Integer, BlockGroup> zoneMap = new HashMap<>();
    public static QuadTree<Transit> transitTree;
    public static QuadTree<BlockGroup> popTree;
    public static final double searchRadius = 500;


/*
    public static void main(String arg[]){

        DataSet ds = null;

        Envelope bounds = loadEnvelope();
        double minX = ((ReferencedEnvelope)bounds).getMinX()-500;
        double minY = ((ReferencedEnvelope)bounds).getMinY()-500;
        double maxX = ((ReferencedEnvelope)bounds).getMaxX()+500;
        double maxY = ((ReferencedEnvelope)bounds).getMaxY()+500;


        jobTree = new QuadTree<>(minX,minY,maxX,maxY);
        popTree = new QuadTree<>(minX,minY,maxX,maxY);
        transitTree = new QuadTree<>(minX,minY,maxX,maxY);



        CRSFactory factory = new CRSFactory();
        CoordinateReferenceSystem srcCrs = factory.createFromName("EPSG:31468");
        CoordinateReferenceSystem dstCrs = factory.createFromName("EPSG:3035");
        BasicCoordinateTransform transform = new BasicCoordinateTransform(srcCrs, dstCrs);


        PopulationReader populationReader = new PopulationReader(ds);
        JobReader employmentReader = new JobReader(ds,transform);
        TransitReader transitReader = new TransitReader(ds,transform);

        populationReader.read();
        employmentReader.read();
        transitReader.read();

        calculateJobsWithinBuffer();
        calculatePopWithinBuffer();
        calculateTransitsWithinBuffer();

        calculatePIE();

        writeOut();

    }

    private static Envelope loadEnvelope() {
        //TODO: Remove minX,minY,maxX,maxY when implementing study area shapefile in Geodata 09 Oct QZ'
        //File schoolsShapeFile = new File("");
        String urlAsString = "file:/F:/Qin/MoPeD/MunichPIE/shapefiles/mucZone3035.shp";
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

    private static void calculatePIE() {

        double area = Math.PI * 0.5 * 0.5;

        for(BlockGroup mucZone : zoneMap.values()){
            //double popDensity = ((mucZone.getPopulationWithinBuffer()+ mucZone.getTotalJobWithinBuffer())/area);
            double popDensity = mucZone.getPopulationWithinBuffer()/area;
            double uli = mucZone.getUliJobWithinBuffer();
            double transit = mucZone.getTransitStopsWithinBuffer();
            mucZone.setPopDensity(popDensity);
            double pie = 4.615*popDensity + 3.120*uli + 3.529*transit;
            mucZone.setPie(pie);
        }
    }



    private static void calculatePopWithinBuffer() {
        int sumPopulation;

        for(BlockGroup mucZone : zoneMap.values()){

            sumPopulation = 0;

            Collection<BlockGroup> popList = popTree.getDisk(mucZone.getCoord().x,mucZone.getCoord().y,searchRadius);

            for (BlockGroup zone : popList){
                sumPopulation += zone.getPopulation();
            }

            mucZone.setPopulationWithinBuffer(sumPopulation);
        }

    }

    private static void calculateJobsWithinBuffer() {

        double sumTotalJob;
        double sumUliJob;

        for(BlockGroup mucZone : zoneMap.values()){

            sumTotalJob = 0.0;
            sumUliJob = 0.0;

            Collection<Job> jobList = jobTree.getDisk(mucZone.getCoord().x,mucZone.getCoord().y,searchRadius);

            for (Job job : jobList){
                sumTotalJob += job.getTotalJob();
                sumUliJob += job.getUliJob();
            }

            mucZone.setTotalJobWithinBuffer(sumTotalJob);
            mucZone.setUliJobWithinBuffer(sumUliJob);
        }


    }

    private static void calculateTransitsWithinBuffer() {
        int sumTransitStops;

        for(BlockGroup mucZone : zoneMap.values()){

            sumTransitStops = 0;

            Collection<Transit> transitList = transitTree.getDisk(mucZone.getCoord().x,mucZone.getCoord().y,searchRadius);

            for (Transit transit : transitList){
                sumTransitStops += transit.getWeightedTransitStops();
            }

            mucZone.setTransitStopsWithinBuffer(sumTransitStops);
        }

    }

    public static void writeOut(){
        String outputPath = "/F:/Qin/MoPeD/MunichPIE/data/mucPIE_onlyPop_noZero.csv";
        StringBuilder pop = new StringBuilder();

        //write header
        pop.append("id,xCoord,yCoord,population,popInBuffer,totalJobInBuffer,popDensity,uliJobInBuffer,transitInBuffer,mucPIE");
        pop.append('\n');

        //write data
        for (BlockGroup mucZone : zoneMap.values()){
            if(mucZone.getPie() != 0.0) {
                pop.append(mucZone.getId());
                pop.append(',');
                pop.append((int) mucZone.getCoord().x);
                pop.append(',');
                pop.append((int) mucZone.getCoord().y);
                pop.append(',');
                pop.append(mucZone.getPopulation());
                pop.append(',');
                pop.append(mucZone.getPopulationWithinBuffer());
                pop.append(',');
                pop.append(mucZone.getTotalJobWithinBuffer());
                pop.append(',');
                pop.append(mucZone.getPopDensity());
                pop.append(',');
                pop.append(mucZone.getUliJobWithinBuffer());
                pop.append(',');
                pop.append(mucZone.getTransitStopsWithinBuffer());
                pop.append(',');
                pop.append(mucZone.getPie());
                pop.append('\n');
            }
        }

        try {
            writeToFile(outputPath,pop.toString());
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }*/



}
