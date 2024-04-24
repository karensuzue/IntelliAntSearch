package iat.dlantisearch;

import java.util.List;
import java.util.Random;

import iat.search.Message;
import iat.search.SearchProtocol;
import peersim.config.Configuration;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.transport.Transport;

/*
 * Requires PheromoneProtocol as Linkable, use in place of IdleProtocol
 */
public class DLAntProtocol extends SearchProtocol {
    Random random = new Random();

    // Can't access SearchProtocol's
    // TODO: Could change SearchProtocol's, but dependencies exist
    private static final String PAR_TRANSPORT = "transport";
    private int transportID; 

    PheromoneProtocol pherProtocol;
    
    Double pherThreshold;

    public DLAntProtocol(String prefix) {
        super(prefix);
        transportID = Configuration.getPid(prefix + "." + PAR_TRANSPORT);
    }

    // ----------------------------------------------------------
    // Override
    // ----------------------------------------------------------

    /*
     * Called in SearchProtocol every time message arrives to a node 
     * Necessary for ED simulation
     */
    @Override
    public void process(Message msg) {
        if (this.match(msg.payload)) { // Hit at current node
            this.notifyOriginator(msg); // notify query originator

            processSuccess(msg);
        }

        // else { processMiss(msg); }    
        processMiss(msg);    
    }

    /*
     * Overrides CDProtocol's and SearchProtocol's nextCycle
     * A CDProtocol method, but is used to generate new queries for our ED simulation 
     * (would require CDscheduler in config)
     * @param node Originator node
     * @param protocolID 
     */
    @Override
    public void nextCycle(Node node, int protocolID) {
        super.nextCycle(node, protocolID); // invoke superclass method (from SearchProtocol)

        // Generate data for query
        int[] data = this.pickQueryData();

        if (data != null) {
            Message m = new Message(node, Message.QRY, 0, data, ttl); // QRY, because initiate message
            this.messageTable.put(m, Integer.valueOf(1)); // originator seen message, THIS CHANGES HIT RATE SIGNIFICANTLY.
            // If you include this line hit rate will dramatically decrease
            
            // Random "r" between 0.0 and 1.0
            pherThreshold = random.nextDouble(); 
            pherProtocol = (PheromoneProtocol) whoAmI.getProtocol(getLinkableID());

            // Iterate through current node's neighbors
            for (int i = 0; i < pherProtocol.degree(); i++) {
                Node neighbor = (Node) pherProtocol.getNeighbor(i);
                send(neighbor, m);
            }
        }
    }

      /**
     * Figure 3 in Ahmadi et al. 2016
     * Used by originator. Floods a subset of neighbors with highest pheromone values
     * @param node Node being communicated with
     * @param msg Message object
     */
    @Override
    public void send(Node neighbor, Message msg) {
        // Neighbor's pheromone value
        Double pheromone = pherProtocol.getPheromone(neighbor);
        // If neighbor's pheromone value > "r"
        if (pheromone > pherThreshold) { // TODO: If pheromones start out uniform...
            // Duplicate message
            Message replicatedMsg = (Message) msg.copy();
             // TTL update parameters
            Double low_bound = pherProtocol.low; 
            Double high_bound = pherProtocol.high;

            // Adjust TTL of message
            ttlAdjust(replicatedMsg, pheromone, low_bound, high_bound);

            // Add neighbor to path
            replicatedMsg.addToPath(neighbor); 
            replicatedMsg.hops++;

            Transport tr = (Transport) neighbor.getProtocol(this.transportID);
            tr.send(whoAmI, neighbor, replicatedMsg, pid); // pid defined in SearchProtocol
            

            updateRoutingTable(neighbor, replicatedMsg);
        }
    }

    /**
     * Part I of Figure 4 in Ahmadi et al. 2016
     * Used by intermediate nodes to forward message to a single neighbor with highest pheromone value.
     * @param n Node being forwarded to
     * @param msg Message being forwarded
     */
    @Override
    public void forward(Node n, Message msg) {
        Double pheromone = pherProtocol.getPheromone(n);

        // Duplicate message
        Message replicatedMsg = (Message) msg.copy();
        // TTL update parameters
        Double low_bound = pherProtocol.low; 
        Double high_bound = pherProtocol.high;
       
        ttlAdjust(replicatedMsg, pheromone, low_bound, high_bound);
        replicatedMsg.addToPath(n); // Add neighbor to path
        replicatedMsg.hops++;

        Transport tr = (Transport) n.getProtocol(this.transportID);
        tr.send(whoAmI, n, replicatedMsg, pid); // pid defined in SearchProtocol

        updateRoutingTable(n, replicatedMsg);
    }

    // ----------------------------------------------------------
    // Process Query
    // ----------------------------------------------------------
        
    /**
     * Algorithm 1 and 2 in Loukos et al. 2010
     * Updates query hit table of nodes in path. Updates pheromone table of nodes in path 
     * and normalizes pheromone table of nodes in path.
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

            // Increment query hit of pathNode
            pathNodeProtocol.incrementQueryHit(prevNode); // For previous node in path
            

            pathNodeProtocol.updatePherTable(); // Update pheromone table of pathNode
            pathNodeProtocol.normalizePherTable(); // Normalize pheromone table of pathNode

            prevNode = pathNode; // Update previous node
        }
    }

    /**
     * Algorithm 3 in Loukos et al. 2010
     * Adjusts TTL of a message message based on low and high pheromone bounds.
     * @param msg Message object
     */
    private void ttlAdjust(Message msg, Double pher, Double low, Double high) {
        if (pher < low) { msg.ttl--; }
        else if (pher > high) { msg.ttl++; }        
    }

     /**
      * Part II of Figure 4 in Ahmadi et al. 2016
      * Process a missed query hit. Intermediate nodes only.
      * Nodes select and forward message to a single neighbor with highest pheromone
      * @param msg Message object
      */
    private void processMiss(Message msg) {
        if (msg.ttl <= 0) {
            if (msg.hits == 0) { this.failedTable.add(msg); }
            return;
        }

        // pherProtocol = (PheromoneProtocol) whoAmI.getProtocol(getLinkableID());

        // Find neighbor with highest pheromone
        Node highestNeighbor = whoAmI; // Should not be current node by the time this is done
        Double highestPher = 0.0;

        for (int i = 0; i < pherProtocol.degree(); i++) {
            Node neighbor = (Node) pherProtocol.getNeighbor(i);

            // Move to next neighbor if current neighbor is visited
            if (msg.hasVisited(neighbor)) { continue; }

            // Obtain neighbor's pheromone value
            Double neighborPher = pherProtocol.getPheromone(neighbor);

            // Update highest pheromone
            if (neighborPher > highestPher) {
                highestPher = neighborPher;
                highestNeighbor = neighbor;
            }
        }

        forward(highestNeighbor, msg);
    } 

}
