package pie;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;

import java.util.ArrayList;
import java.util.List;

public class PedNetworkCreatorBridgeScenario {

    public static void main(String[] args) {
        Network pedNetwork = NetworkUtils.createNetwork();
        new MatsimNetworkReader(pedNetwork).readFile("C:/Users/qin/Desktop/pedNetwork_withClark_pedestrian_scenario_eastAndWestAndLlyodAndNorth.xml.gz");

        //add type for new links
        for(Link l : pedNetwork.getLinks().values()){
            if(l.getAttributes().getAttribute("type")==null){
                NetworkUtils.setType(l, "residential");
            }
        }

        //modify capacity 300 to 600?
        for(Link l : pedNetwork.getLinks().values()){
            if(l.getCapacity()==300){
                l.setCapacity(600);
            }
        }

        //add reverse link for all links
        List<Link> reverselinks = new ArrayList<>();

        for(Link l : pedNetwork.getLinks().values()){
            Link oppositeLink = NetworkUtils.findLinkInOppositeDirection(l);
            if(oppositeLink==null){
                String newLinkOppositeId = l.getId().toString() + "_oppo";
                Link l_opposite = pedNetwork.getFactory().createLink(Id.create(newLinkOppositeId, Link.class), l.getToNode(), l.getFromNode());
                l_opposite.setLength(l.getLength());
                l_opposite.setFreespeed(l.getFreespeed());
                l_opposite.setCapacity(l.getCapacity());
                l_opposite.setNumberOfLanes(l.getNumberOfLanes());
                NetworkUtils.setType(l_opposite, l.getAttributes().getAttribute("type").toString());
                reverselinks.add(l_opposite);
            }
        }

        for(Link l : reverselinks){
            pedNetwork.addLink(l);
        }

        new org.matsim.core.network.algorithms.NetworkCleaner().run(pedNetwork);
        new NetworkWriter(pedNetwork).writeV2("C:/Users/qin/Desktop/pedNetwork_withClark_pedestrian_BridgeScenario_final.xml.gz");


        //calculate total link kms added
        double totalKMScenario = 0.;
        for(Link l : pedNetwork.getLinks().values()){
            totalKMScenario += l.getLength();
        }

        Network pedNetworkBase = NetworkUtils.createNetwork();
        new MatsimNetworkReader(pedNetworkBase).readFile("C:/Users/qin/Desktop/pedNetwork_withClark_pedestrian_baseYear.xml.gz");
        double totalKMBase = 0.;
        for(Link l : pedNetworkBase.getLinks().values()){
            totalKMBase += l.getLength();
        }

        System.out.println("Bridge scenario total kms: " + totalKMScenario/1000.);
        System.out.println("Base year total kms: " + totalKMBase/1000.);
        System.out.println("New network kms: " + (totalKMScenario-totalKMBase)/1000.);
        System.out.println("New network kms (one-way): " + (totalKMScenario-totalKMBase)/1000./2);
    }

}
