package pie;

import org.matsim.api.core.v01.Id;
import org.matsim.api.core.v01.network.Link;
import org.matsim.api.core.v01.network.Network;
import org.matsim.api.core.v01.network.NetworkFactory;
import org.matsim.api.core.v01.network.Node;

public class MyMergeNetworks {

        /**
         * Merges two networks into one, by copying all nodes and links from the addNetworks to the baseNetwork.
         *
         * @param baseNetwork
         * @param addNetwork
         */
        public static void merge(final Network baseNetwork, final String addPrefix, final Network addNetwork) {
            double capacityFactor = baseNetwork.getCapacityPeriod() / addNetwork.getCapacityPeriod();
            NetworkFactory factory = baseNetwork.getFactory();
            for (Node node : addNetwork.getNodes().values()) {
                Node node2 = (Node) factory.createNode(Id.create(addPrefix + node.getId().toString(), Node.class), node.getCoord());
                baseNetwork.addNode(node2);
            }
            for (Link link : addNetwork.getLinks().values()) {
                Id<Node> fromNodeId = Id.create(addPrefix + link.getFromNode().getId().toString(), Node.class);
                Id<Node> toNodeId = Id.create(addPrefix + link.getToNode().getId().toString(), Node.class);
                Node fromNode = baseNetwork.getNodes().get(fromNodeId);
                Node toNode = baseNetwork.getNodes().get(toNodeId);
                Link link2 = factory.createLink(Id.create(addPrefix + link.getId().toString(), Link.class),
                        fromNode, toNode);
                link2.setAllowedModes(link.getAllowedModes());
                link2.setCapacity(link.getCapacity() * capacityFactor);
                link2.setFreespeed(link.getFreespeed());
                link2.setLength(link.getLength());
                link2.setNumberOfLanes(link.getNumberOfLanes());
                link2.getAttributes().putAttribute("type",link.getAttributes().getAttribute("type"));
                link2.getAttributes().putAttribute("origid",link.getAttributes().getAttribute("origid"));

                baseNetwork.addLink(link2);
            }
        }

        /**
         * Merges two networks into one, by copying all nodes and links from the two
         * given networks to a third one.
         *
         * @param networkA
         * @param prefixA
         * @param networkB
         * @param prefixB
         * @param mergedNetwork
         */
        public static void merge(final Network networkA, final String prefixA, final Network networkB, final String prefixB, final Network mergedNetwork) {
            double capacityFactor = mergedNetwork.getCapacityPeriod() / networkA.getCapacityPeriod();
            NetworkFactory factory = mergedNetwork.getFactory();
            for (Node node : networkA.getNodes().values()) {
                Node node2 = (Node) factory.createNode(Id.create(prefixA + node.getId().toString(), Node.class), node.getCoord());
                mergedNetwork.addNode(  node2);
            }
            for (Link link : networkA.getLinks().values()) {
                Id<Node> fromNodeId = Id.create(prefixA + link.getFromNode().getId().toString(), Node.class);
                Id<Node> toNodeId = Id.create(prefixA + link.getToNode().getId().toString(), Node.class);
                Node fromNode = mergedNetwork.getNodes().get(fromNodeId);
                Node toNode = mergedNetwork.getNodes().get(toNodeId);
                Link link2 = factory.createLink(Id.create(prefixA + link.getId().toString(), Link.class),
                        fromNode, toNode);
                link2.setAllowedModes(link.getAllowedModes());
                link2.setCapacity(link.getCapacity() * capacityFactor);
                link2.setFreespeed(link.getFreespeed());
                link2.setLength(link.getLength());
                link2.setNumberOfLanes(link.getNumberOfLanes());
                mergedNetwork.addLink(link2);
                fromNode.addOutLink(link2);
                toNode.addInLink(link2);
            }
            capacityFactor = mergedNetwork.getCapacityPeriod() / networkB.getCapacityPeriod();
            for (Node node : networkB.getNodes().values()) {
                Node node2 = (Node) factory.createNode(Id.create(prefixB + node.getId().toString(), Node.class), node.getCoord());
                mergedNetwork.addNode(  node2);
            }
            for (Link link : networkB.getLinks().values()) {
                Id<Node> fromNodeId = Id.create(prefixB + link.getFromNode().getId().toString(), Node.class);
                Id<Node> toNodeId = Id.create(prefixB + link.getToNode().getId().toString(), Node.class);
                Node fromNode = mergedNetwork.getNodes().get(fromNodeId);
                Node toNode = mergedNetwork.getNodes().get(toNodeId);
                Link link2 = factory.createLink(Id.create(prefixB + link.getId().toString(), Link.class),
                        fromNode, toNode);
                link2.setAllowedModes(link.getAllowedModes());
                link2.setCapacity(link.getCapacity() * capacityFactor);
                link2.setFreespeed(link.getFreespeed());
                link2.setLength(link.getLength());
                link2.setNumberOfLanes(link.getNumberOfLanes());
                mergedNetwork.addLink(link2);
                fromNode.addOutLink(link2);
                toNode.addInLink(link2);
            }
        }
    }
