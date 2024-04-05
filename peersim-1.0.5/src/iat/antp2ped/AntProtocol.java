package iat.antp2ped;

import java.util.List;

import iat.search.Message;
import iat.search.SearchProtocol;
import peersim.core.Linkable;
import peersim.core.Node;

public class AntProtocol extends SearchProtocol {

    public AntProtocol(String prefix) {
        super(prefix);
    }

    @Override
    public void process(Message msg) {
        if (this.match(msg.payload)) { // Hit
            this.notifyOriginator(msg);

            processSuccess(msg);
        }

        processMiss(msg);

    }

    @Override
    public void nextCycle(Node node, int protocolID) {
        super.nextCycle(node, protocolID);

        int[] data = this.pickQueryData();

        if (data != null) {
            Message m = new Message(node, Message.QRY, 0, data, ttl);

            Linkable linkable = (Linkable) node.getProtocol(getLinkableID());

            for (int i = 0; i < linkable.degree(); i++) {
                send((Node) linkable.getNeighbor(i), m);
            }
        }
    }
    

    /**
     * Process a successful query hit. Updates query hit table of nodes in path. Updates pheromone table of nodes in path 
     * and Normalizes pheromone table of nodes in path.
     * @param msg Message object
     */
    private void processSuccess(Message msg) {
        // Get path of message
        List<Node> path = msg.path;

        // Iterate through nodes in path
        for (int i = 0; i < path.size(); i++) {
            Node pathNode = path.get(i); // Current node in path

            // Pheromone protocol of pathNode
            PheromoneProtocol pathNodeProtocol = 
                (PheromoneProtocol) pathNode.getProtocol(getLinkableID());
            
            for (Node p : path) {
                // Update query hit table of path node
                // If other nodes in path are immediate neighbors of pathNode, increment
                if (pathNodeProtocol.contains(p)) {
                    pathNodeProtocol.incrementQueryHit(p);
                }
            }

            pathNodeProtocol.updatePherTable(); // Update pheromone table of pathNode
            pathNodeProtocol.normalizePherTable(); // Normalize pheromone table of pathNode
        }
    }

    /**
     * Algorithm 3 in Loukos et al. 2010
     * 
     * Process a query miss. Replicates message to neighbors of current node. Adjusts TTL of replicated message based on pheromone value 
     * of neighbor.
     * @param msg Message object
     */
    private void processMiss(Message msg) {
        PheromoneProtocol pherProtocol = (PheromoneProtocol) whoAmI.getProtocol(getLinkableID());

        for (int i = 0; i < pherProtocol.degree(); i++) {
            Node m = (Node) pherProtocol.getNeighbor(i);

            if (msg.hasVisited(m)) { continue; }

            if (msg.ttl <= 0) { continue; }

            Message replicatedMsg = (Message) msg.copy();

            Double pheromone = pherProtocol.getPheromone(m);
            Double low_bound = pherProtocol.low;
            Double high_bound = pherProtocol.high;

            // Check if the neighbor is not the source of the message
            if (!m.equals(msg.originator)) {          
                // Update TTL of replicated message
                if (pheromone < low_bound) {  replicatedMsg.ttl--; }
                else if (pheromone > high_bound) {  replicatedMsg.ttl++; }

                forward(m, replicatedMsg);
            }
        }             
    }
}

