package pie;

import org.matsim.api.core.v01.Coord;
import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkWriter;
import org.matsim.api.core.v01.network.Node;
import org.matsim.contrib.accessibility.utils.NetworkUtil;
import org.matsim.core.network.LinkFactory;
import org.matsim.core.network.NetworkUtils;
import org.matsim.core.network.io.MatsimNetworkReader;

import java.util.ArrayList;
import java.util.List;

public class PedNetworkCreatorBaseYear {

    public static void main(String[] args) {
        Network pedNetwork = NetworkUtils.createNetwork();
        new MatsimNetworkReader(pedNetwork).readFile("C:/Users/qin/Desktop/pedNetwork_withClark_pedestrian.xml.gz");

        //add 2 new nodes
        for(int i = 0; i < 2; i++){
            int x = Integer.parseInt(args[i].split(",")[0]);
            int y = Integer.parseInt(args[i].split(",")[1]);
            String newNodeId = "baseNode" + (i+1);
            Node newNode = pedNetwork.getFactory().createNode(Id.create(newNodeId, Node.class),new Coord(x,y));
            pedNetwork.addNode(newNode);
        }

        //add new links
        int count = 0;
        for(int i = 2; i < args.length; i++){
            count++;

            Node fromNode = pedNetwork.getNodes().get(Id.create(args[i].split(",")[0], Node.class));
            Node toNode = pedNetwork.getNodes().get(Id.create(args[i].split(",")[1], Node.class));
            String linkType = args[i].split(",")[2];
            String newLinkId = "baseLink" + count;
            double linkLength = NetworkUtils.getEuclideanDistance(fromNode.getCoord(),toNode.getCoord());

            Link l = pedNetwork.getFactory().createLink(Id.create(newLinkId, Link.class), fromNode, toNode);
            l.setLength(linkLength);
            l.setFreespeed(5./3.6);
            l.setCapacity(600);
            l.setNumberOfLanes(1);
            NetworkUtils.setType(l, linkType);
            pedNetwork.addLink(l);



            String newLinkOppositeId = "baseLink" + i + "oppo";
            Link l_opposite = pedNetwork.getFactory().createLink(Id.create(newLinkOppositeId, Link.class), toNode, fromNode);
            l_opposite.setLength(linkLength);
            l_opposite.setFreespeed(5./3.6);
            l_opposite.setCapacity(600);
            l_opposite.setNumberOfLanes(1);
            NetworkUtils.setType(l_opposite, linkType);
            pedNetwork.addLink(l_opposite);
        }

        //remove tillikum crossing from base year
        pedNetwork.removeLink(Id.create("177996",Link.class));
        pedNetwork.removeLink(Id.create("178080",Link.class));
        pedNetwork.removeLink(Id.create("166376",Link.class));
        pedNetwork.removeLink(Id.create("178024",Link.class));

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

        new NetworkWriter(pedNetwork).writeV2("C:/Users/qin/Desktop/pedNetwork_withClark_pedestrian_baseYear.xml.gz");
    }

}
