package iat.antp2ped;

import java.util.List;

import iat.search.Message;
import iat.search.SearchProtocol;
import peersim.config.Configuration;
import peersim.core.Linkable;
import peersim.core.Node;

public class AntProtocol extends SearchProtocol {

    // ----------------------------------------------------------
    // Config Parameters
    // ----------------------------------------------------------

    /**
     * User-defined for experimental control
     * - smart: determines selection of neighbors
     */
     
    private static final String PAR_SMART = "smart";

    private static final String PAR_ANTS = "ants";


    // ----------------------------------------------------------
    // Fields
    // ----------------------------------------------------------

    private boolean smart = false;
    private int ants = 3;

    public AntProtocol(String prefix) {
        super(prefix);

        smart = Configuration.getBoolean(prefix + "." + PAR_SMART, false);
        ants = Configuration.getInt(prefix + "." + PAR_ANTS, 3);
    }

    @Override
    public void process(Message msg) {
        if (this.match(msg.payload)) { // Hit
            this.notifyOriginator(msg);

            processSuccess(msg);
        } else {
            processMiss(msg);

        }


    }

    @Override
    public void nextCycle(Node node, int protocolID) {
        super.nextCycle(node, protocolID);

        int[] data = this.pickQueryData();

        if (data != null) {
            Message m = new Message(node, Message.QRY, 0, data, ttl);

            Linkable linkable = (Linkable) node.getProtocol(getLinkableID());

            // choose ants number of neighbors to send the message to
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

        Node prevNode = whoAmI;

        // Iterate through nodes in path
        for (int i = 0; i < path.size(); i++) {
            Node pathNode = path.get(i); // Current node in path

            // Pheromone protocol of pathNode
            PheromoneProtocol pathNodeProtocol = 
                (PheromoneProtocol) pathNode.getProtocol(getLinkableID());

            pathNodeProtocol.incrementQueryHit(prevNode); // Increment query hit of pathNode

            pathNodeProtocol.updatePherTable(); // Update pheromone table of pathNode
            pathNodeProtocol.normalizePherTable(); // Normalize pheromone table of pathNode

            prevNode = pathNode; // Update previous node
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

        if (msg.ttl <= 0) return;

        for (int i = 0; i < pherProtocol.degree(); i++) {
            Node m = (Node) pherProtocol.getNeighbor(i);

            if (msg.hasVisited(m)) { continue; }

            Message replicatedMsg = (Message) msg.copy();

            Double pheromone = pherProtocol.getPheromone(m);
            Double low_bound = pherProtocol.low;
            Double high_bound = pherProtocol.high;

            int qh = pherProtocol.queryHitCount.get(m.getIndex());
            if (qh > 0) {
                            // System.out.println(msg + "(" + qh+ ")" + " | " + "Pheromone: " + pheromone + " Low: " + low_bound + " High: " + high_bound);

            }


            // Check if the neighbor is not the source of the message
            if (!m.equals(msg.originator)) {          
                // Update TTL of replicated message
                if (pheromone < low_bound) {  replicatedMsg.ttl--; }
                else if (pheromone > high_bound) {  
                    replicatedMsg.ttl++;    
                }

                forward(m, replicatedMsg);
            }
        }             
    }


    @Override
    public Node selectFreeNeighbor(Message mes) {
        if (!smart) {
            return super.selectFreeNeighbor(mes);
        }


        Node bestNeighbor = null;
        double maxPheromone = -1;
        PheromoneProtocol link = (PheromoneProtocol) whoAmI.getProtocol(getLinkableID());
        
        for (int i = 0; i < link.degree(); i++) {
            Node neighbor = link.getNeighbor(i);
            
            Double pheromoneLevel = link.pherTable.getOrDefault(neighbor, 0.0);

            if (pheromoneLevel > maxPheromone && !messageTable.containsKey(mes)) {
                maxPheromone = pheromoneLevel;
                bestNeighbor = neighbor;
            }
        }


        return bestNeighbor != null ? bestNeighbor : super.selectFreeNeighbor(mes);
    }
}

