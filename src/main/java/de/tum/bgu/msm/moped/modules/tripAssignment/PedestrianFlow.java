package de.tum.bgu.msm.moped.modules.tripAssignment;

import com.google.common.collect.Iterables;
import com.google.common.math.LongMath;
import de.tum.bgu.msm.moped.data.DataSet;
import de.tum.bgu.msm.moped.util.MoPeDUtil;
import de.tum.bgu.msm.moped.util.concurrent.ConcurrentExecutor;
import org.apache.log4j.Logger;
import org.locationtech.jts.geom.Geometry;
import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.Node;
import org.matsim.contrib.dvrp.router.DistanceAsTravelDisutility;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.router.FastDijkstraFactory;
import org.matsim.core.router.util.LeastCostPathCalculator;
import org.matsim.core.utils.geometry.CoordUtils;

import java.io.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PedestrianFlow {

    private static Logger logger = Logger.getLogger(PedestrianFlow.class);
    public  Map<String,Double> linkVolume = new HashMap<>();
    public  DataSet dataSet;

    public PedestrianFlow(DataSet dataSet) {
        this.dataSet = dataSet;
    }

    public  synchronized void setLinkVolume(String id, double volume) {
        linkVolume.put(id, volume);
    }


    public Map<String,Double> getPedestrianFlow (String networkFile,String odPairs) {

        ArrayList<ODPair> pairs = readCentroidList(odPairs);

        final int partitionSize = (int) ((double) pairs.size() / Runtime.getRuntime().availableProcessors()) + 1;
        Iterable<List<ODPair>> partitions = Iterables.partition(pairs, partitionSize);
        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(networkFile);
        logger.info(partitionSize);

        ConcurrentExecutor<Void> executor = ConcurrentExecutor.fixedPoolService(Runtime.getRuntime().availableProcessors());

        AtomicInteger counter = new AtomicInteger();

        for (final List<ODPair> partition : partitions) {
            LeastCostPathCalculator dijkstra = new FastDijkstraFactory(false)
                    .createPathCalculator(network, new DistanceAsTravelDisutility(), (link, v, person, vehicle) -> link.getLength());


            executor.addTaskToQueue(() -> {
                try {
                    int id = counter.incrementAndGet();
                    int counterr = 0;
                    for (ODPair pair : partition) {

                        if(LongMath.isPowerOfTwo(counterr)) {
                            logger.info(counterr + " in " + id);
                        };

                        //generate toNodes vector (toSuperPAZ) and calculate Least Cost Path to all destination nodes
                        Coord originCoord = CoordUtils.createCoord(((Geometry)(dataSet.getZone(pair.getOrigin()).getShapeFeature().getDefaultGeometry())).getCentroid().getCoordinate());
                        Node originNode = NetworkUtils.getNearestNode(network, originCoord);

                        Coord destinationCoord = CoordUtils.createCoord(((Geometry)(dataSet.getZone(pair.getDestination()).getShapeFeature().getDefaultGeometry())).getCentroid().getCoordinate());
                        Node destinationNode = NetworkUtils.getNearestNode(network, destinationCoord);
                        LeastCostPathCalculator.Path path = dijkstra.calcLeastCostPath(originNode,destinationNode,8 * 3600,null,null);

                        for(Link link : path.links){
                            double volume = pair.getCount();
                            if (linkVolume.containsKey(link.getId().toString())){
                                volume = volume + linkVolume.get(link.getId().toString());
                            }
                            setLinkVolume(link.getId().toString(),volume);
                        }
                        counterr++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.warn(e.getLocalizedMessage());
                    throw new RuntimeException(e);
                }
                return null;
            });
        }
        executor.execute();
        return linkVolume;
    }

    public Map<String,Double> getPedestrianFlow_nonconcurrent (String networkFile,String odPairs) {

        ArrayList<ODPair> pairs = readCentroidList(odPairs);

        Network network = NetworkUtils.createNetwork();
        new MatsimNetworkReader(network).readFile(networkFile);

        LeastCostPathCalculator dijkstra = new FastDijkstraFactory(false)
                    .createPathCalculator(network, new DistanceAsTravelDisutility(), (link, v, person, vehicle) -> link.getLength());

        int counterr = 0;
        logger.info("size: " + pairs.size());
        logger.info("zone " + dataSet.getZones().size());
        for (ODPair pair : pairs) {

            if(LongMath.isPowerOfTwo(counterr)) {
                logger.info(counterr);
            };

            //generate toNodes vector (toSuperPAZ) and calculate Least Cost Path to all destination nodes
            Coord originCoord = CoordUtils.createCoord(((Geometry)(dataSet.getZone(pair.getOrigin()).getShapeFeature().getDefaultGeometry())).getCentroid().getCoordinate());
            Node originNode = NetworkUtils.getNearestNode(network, originCoord);

            Coord destinationCoord = CoordUtils.createCoord(((Geometry)(dataSet.getZone(pair.getDestination()).getShapeFeature().getDefaultGeometry())).getCentroid().getCoordinate());
            Node destinationNode = NetworkUtils.getNearestNode(network, destinationCoord);
            LeastCostPathCalculator.Path path = dijkstra.calcLeastCostPath(originNode,destinationNode,8 * 3600,null,null);

            for(Link link : path.links){
                double volume = pair.getCount();
                if (linkVolume.containsKey(link.getId().toString())){
                    volume = volume + linkVolume.get(link.getId().toString());
                }
                setLinkVolume(link.getId().toString(),volume);
            }
            counterr++;
        }

        return linkVolume;
    }

    class ODPair {

        private int origin;
        private int destination;
        private double count;

        public ODPair(int origin, int destination, double count) {
            this.origin = origin;
            this.destination = destination;
            this.count = count;
        }

        public int getOrigin() {
            return origin;
        }

        public int getDestination() {
            return destination;
        }

        public double getCount() {
            return count;
        }
    }



    public  ArrayList<ODPair> readCentroidList(String fileName) {
         int posOrigin;
         int posDestination;
         int posCount;

        BufferedReader bufferReader = null;
        ArrayList<ODPair> pairList = new ArrayList<>();

        try {
            String line;
            bufferReader = new BufferedReader(new FileReader(fileName));

            String headerLine = bufferReader.readLine();
            String[] header = headerLine.split(",");
            posOrigin = MoPeDUtil.findPositionInArray("origin", header);
            posDestination = MoPeDUtil.findPositionInArray("destination", header);
            posCount = MoPeDUtil.findPositionInArray("trips", header);

            while ((line = bufferReader.readLine()) != null ) {
                ODPair pair = CSVtoLocation(line,posOrigin,posDestination,posCount);
                if (pair!=null) {
                    pairList.add(pair);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferReader != null) bufferReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return pairList;

    }

    public  ODPair CSVtoLocation(String csvLine, int posOrigin, int posDestination, int posCount) {
        int origin;
        int destination;
        double count;

        try {
            String[] splitData = csvLine.split(",");
            origin = Integer.parseInt(splitData[posOrigin]);
            destination = Integer.parseInt(splitData[posDestination]);
            count = Double.parseDouble(splitData[posCount]);

            ODPair pair = new ODPair(origin, destination, count);
            return pair;
        } catch (Exception e){
            return null;
        }
    }
}
