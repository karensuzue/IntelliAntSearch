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

    // Current node's protocol
    PheromoneProtocol pherProtocol;

    public DLAntProtocol(String prefix) {
        super(prefix);
        transportID = Configuration.getPid(prefix + "." + PAR_TRANSPORT);
        pherProtocol = (PheromoneProtocol) whoAmI.getProtocol(getLinkableID());
    }

    /*
     * Necessary for CD simulation (and ED?)
     * Called in SearchProtocol every time message arrives to a node 
     */
    @Override
    public void process(Message msg) {
        if (this.match(msg.payload)) { // Hit
            this.notifyOriginator(msg); // notify query originator

            pherUpdate(msg);
        }

        processMiss(msg);
        // check for ttl condition here 
        // TODO: how to ensure that send always start from originator, forward always for intermediary
        // TODO: make sure that works for both cycle and event
    }

    /*
     * Overrides CDProtocol's and SearchProtocol's nextCycle
     * Implement for compatibility to cycle-driven simulations
     * TODO: IGNORE FOR NOW, FOCUS ON EVENT DRIVEN 
     */
    @Override
    public void nextCycle(Node node, int protocolID) {
        super.nextCycle(node, protocolID); // invoke superclass method (SearchProtocol)

        int[] data = this.pickQueryData();

        if (data != null) {
            Message m = new Message(node, Message.QRY, 0, data, ttl); // shouldn't QRY be FWD?

            Linkable linkable = (Linkable) node.getProtocol(getLinkableID());

            for (int i = 0; i < linkable.degree(); i++) {
                send((Node) linkable.getNeighbor(i), m); // I guess send is okay?
            }
        }
    }
    
    /**
     * Algorithm 1 and 2 in Loukos et al. 2010
     * Updates query hit table of nodes in path. Updates pheromone table of nodes in path 
     * and normalizes pheromone table of nodes in path.
     * @param msg Message object
     */
    private void pherUpdate(Message msg) {
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
     * Adjusts TTL of a message message based on low and high pheromone bounds.
     * @param msg Message object
     */
    private void ttlAdjust(Message msg, Double pher, Double low, Double high) {
        if (pher < low) { msg.ttl--; }
        else if (pher > high) { msg.ttl++; }        
    }


    /**
     * TODO: Call this in processmiss
     * TODO: Can we override SearchProtocol's send and forward instead? 
     * Maybe that's reserved for CD simulations
     * 
     * Figure 3 in Ahmadi et al. 2016
     * Used by originator. Floods a subset of neighbors with highest pheromone values
     */
    private void srcSend(Message msg) {
        // generate random number r between 0 and 1
        // select any neighbors (m) with (should be normalized) pheromone values > r
        // make m copies of query
        // determine adaptive value of ant ttl??? huh (do i have to use loukos algorithm 3??)
        // forward query or ant to those m neighbors

        // Random "r" between 0.0 and 1.0
        Double pherThreshold = random.nextDouble(); 

        // Iterate through current node's neighbors
        for (int i = 0; i < pherProtocol.degree(); i++) {
            Node neighbor = (Node) pherProtocol.getNeighbor(i);

            // Neighbor's pheromone value
            Double pheromone = pherProtocol.getPheromone(neighbor);

            // If neighbor's pheromone value > "r"
            if (pheromone > pherThreshold) {
                // Duplicate message
                Message replicatedMsg = (Message) msg.copy();
                 // TTL update parameters
                Double low_bound = pherProtocol.low; 
                Double high_bound = pherProtocol.high;

                ttlAdjust(replicatedMsg, pheromone, low_bound, high_bound);

                // send(neighbor, replicatedMsg);
                // Can't use SearchProtocol's send, copies message multiple times 
                // and does it's own TTL update
                // Use transport directly
                Transport tr = (Transport) neighbor.getProtocol(this.transportID);
                tr.send(whoAmI, neighbor, replicatedMsg, pid); // pid defined in SearchProtocol
            } 
        }
    }

    /**
     * TODO: Call this in processmiss
     * Figure 4 in Ahmadi et al. 2016
     * Used by intermediate nodes to forward message to a single neighbor with highest pheromone value.
     */
    private void intForward(Message msg) {

        // if message ttl > 0
            // search for object, if found increment hit count in message, send back results to source
            // choose neighbor with best pher probability
            // copy message
            // determine adaptive ttl of message
            // forward message to that single neighbor use (forward)
            
        // else if ttl = 0 
            // search over
            // update pheromone according to antp2pr protocol

        // TODO: TTL Condition check is done in process(), not here

        Double highestPher = 0.0;

        for (int i = 0; i < pherProtocol.degree(); i++) {
            Node neighbor = (Node) pherProtocol.getNeighbor(i);

            // Move to next neighbor if current neighbor is visited
            if (msg.hasVisited(neighbor)) { continue; }





        }

    }


     /**
     * Process a missed query hit. 
     * If node is not query originator, select a neighbor using pheromone as probabilities
     * If node is query originator, flood to a small % of neighbors with highest pheromone
     */
    private void processMiss(Message msg) {
        // Obtain current node's PheromoneProtocol
        PheromoneProtocol pherProtocol = (PheromoneProtocol) whoAmI.getProtocol(getLinkableID());

        // Iterate through current node's neighbors
        for (int i = 0; i < pherProtocol.degree(); i++) {
            Node neighbor = (Node) pherProtocol.getNeighbor(i);

            // Move to next neighbor if current neighbor is visited
            if (msg.hasVisited(neighbor)) { continue; }

            // Move to next neighbor if message TTL is expired
            // TODO: What, should be return? 
            if (msg.ttl <= 0) { continue; }

            Message replicatedMsg = (Message) msg.copy();

            Double pheromone = pherProtocol.getPheromone(neighbor);
            // TTL update parameters
            Double low_bound = pherProtocol.low; 
            Double high_bound = pherProtocol.high;

            // Check if the neighbor is not the source of the message, just in case?
            // TODO: is this necessary? 
            if (!neighbor.equals(msg.originator)) {          
                // Update TTL of replicated message
                // if (pheromone < low_bound) {  replicatedMsg.ttl--; }
                // else if (pheromone > high_bound) {  replicatedMsg.ttl++; }
                ttlAdjust(replicatedMsg, pheromone, low_bound, high_bound);

                forward(neighbor, replicatedMsg);
            }
        }             
    }

}
