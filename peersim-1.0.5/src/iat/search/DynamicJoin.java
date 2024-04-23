package iat.search;

import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.GeneralNode;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;

public class DynamicJoin implements Control {
    @Override
    public boolean execute() {
        
        // Randomly select a node to join the network
        int nodesToJoin = CommonState.r.nextInt(5); 

        for (int i = 0; i < nodesToJoin; i++) {
            Node node = new GeneralNode(null);
            Network.add(node);

            // Randomly select a node to connect to
            int randomNodeIndex = CommonState.r.nextInt(Network.size());
            Node randomNode = Network.get(randomNodeIndex);

            // Connect the new node to the randomly selected node
            Linkable linkable = (Linkable) randomNode.getProtocol(0);

            linkable.addNeighbor(node);

            // Connect the randomly selected node to the new node
            linkable = (Linkable) node.getProtocol(0);

            linkable.addNeighbor(randomNode);
        }

        

        
    }
}
