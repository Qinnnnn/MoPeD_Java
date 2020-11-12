package pie;

import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.contrib.accessibility.utils.MergeNetworks;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;
import org.matsim.core.utils.geometry.CoordinateTransformation;
import org.matsim.core.utils.geometry.transformations.TransformationFactory;
import org.matsim.core.utils.io.OsmNetworkReader;

public class pedNetworkCreator {


//        public static void main(String[] args) {
//            Network network = NetworkUtils.createNetwork();
//            CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, "EPSG:6852");
//            final OsmNetworkReader osmNetworkReader = new OsmNetworkReader(network, ct, true, true);
//            osmNetworkReader.setKeepPaths(false);
//            osmNetworkReader.setHighwayDefaults(8, "pedestrian", 1, 5./3.6, 1, 300);
//            osmNetworkReader.parse("F:/models/matSim/network/portland/oregonOSMNetwork_noMotorway.osm");
//            new NetworkWriter(network).writeV2("C:/Users/qin/Desktop/pedNetwork.xml.gz");
//            Network pedNetwork = NetworkUtils.createNetwork();
//            new MatsimNetworkReader(pedNetwork).readFile("C:/Users/qin/Desktop/pedNetwork.xml.gz");
//            new org.matsim.core.network.algorithms.NetworkCleaner().run(pedNetwork);
//
//            new NetworkWriter(pedNetwork).writeV2("C:/Users/qin/Desktop/pedNetwork_cleaned.xml.gz");
//        }

    public static void main(String[] args) {
        Network pedNetworkOregon = NetworkUtils.createNetwork();
        CoordinateTransformation ct = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, "EPSG:6852");
        final OsmNetworkReader osmNetworkReader = new OsmNetworkReader(pedNetworkOregon, ct, true, true);
        osmNetworkReader.setKeepPaths(false);
        osmNetworkReader.setHighwayDefaults(8, "pedestrian", 1, 5./3.6, 1, 300);
        osmNetworkReader.parse("F:/models/matSim/network/portland/oregonOSMNetwork_withMotorway.osm");
        new org.matsim.core.network.algorithms.NetworkCleaner().run(pedNetworkOregon);

        Network pedNetworkWashi = NetworkUtils.createNetwork();
        CoordinateTransformation ct2 = TransformationFactory.getCoordinateTransformation(TransformationFactory.WGS84, "EPSG:6852");
        final OsmNetworkReader osmNetworkReader2 = new OsmNetworkReader(pedNetworkWashi, ct2, true, true);
        osmNetworkReader2.setKeepPaths(false);
        osmNetworkReader2.setHighwayDefaults(8, "pedestrian", 1, 5./3.6, 1, 300);
        osmNetworkReader2.parse("F:/models/matSim/network/portland/washiOSMNetwork_withMotorway.osm");
        new org.matsim.core.network.algorithms.NetworkCleaner().run(pedNetworkWashi);


        MyMergeNetworks.merge(pedNetworkOregon,"wa",pedNetworkWashi);
        new NetworkWriter(pedNetworkOregon).writeV2("C:/Users/qin/Desktop/pedNetwork_withClark_withMotorway.xml.gz");

    }

        //merge oregon and washi
//    public static void main(String[] args) {
//        Network pedNetworkOregon = NetworkUtils.createNetwork();
//        new MatsimNetworkReader(pedNetworkOregon).readFile("C:/Users/qin/Desktop/pedNetwork_cleaned.xml.gz");
//        Network pedNetworkWashi = NetworkUtils.createNetwork();
//        new MatsimNetworkReader(pedNetworkWashi).readFile("C:/Users/qin/Desktop/pedNetwork_cleaned2.xml.gz");
//        MyMergeNetworks.merge(pedNetworkOregon,"wa",pedNetworkWashi);
//        new NetworkWriter(pedNetworkOregon).writeV2("C:/Users/qin/Desktop/pedNetwork_withClark.xml.gz");
//
//    }

}
