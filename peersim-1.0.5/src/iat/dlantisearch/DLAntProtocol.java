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

    public DLAntProtocol(String prefix) {
        super(prefix);
        transportID = Configuration.getPid(prefix + "." + PAR_TRANSPORT);
    }

    /*
     * Called in SearchProtocol every time message arrives to a node 
     * Necessary for ED simulation
     */
    @Override
    public void process(Message msg) {
        System.out.println(msg.path);
        if (this.match(msg.payload)) { // Hit
            this.notifyOriginator(msg); // notify query originator

            processSuccess(msg);
        }

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

            send(node, m);
        }
    }
    
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

            pathNodeProtocol.incrementQueryHit(prevNode); // Increment query hit of pathNode

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
     * Figure 3 in Ahmadi et al. 2016
     * Used by originator. Floods a subset of neighbors with highest pheromone values
     * @param node Originator node
     * @param msg Message object
     */
    @Override
    public void send(Node node, Message msg) {
        msg.addToPath(node); // Add originator to path

        Integer actual = (Integer) this.messageTable.get(msg);
        int index = (actual != null ? actual.intValue() + 1 : 1);
        // messageTable stores the number of times a node has seen a packet/message
        this.messageTable.put(msg, Integer.valueOf(index)); 

        PheromoneProtocol pherProt = (PheromoneProtocol) node.getProtocol(getLinkableID());

        // Random "r" between 0.0 and 1.0
        Double pherThreshold = random.nextDouble(); 

        // Iterate through current node's neighbors
        for (int i = 0; i < pherProt.degree(); i++) {
            Node neighbor = (Node) pherProt.getNeighbor(i);

            // Neighbor's pheromone value
            Double pheromone = pherProt.getPheromone(neighbor);

            // If neighbor's pheromone value > "r"
            if (pheromone > pherThreshold) {
                // Duplicate message
                Message replicatedMsg = (Message) msg.copy();
                 // TTL update parameters
                Double low_bound = pherProt.low; 
                Double high_bound = pherProt.high;

                ttlAdjust(replicatedMsg, pheromone, low_bound, high_bound);

                replicatedMsg.addToPath(neighbor); // Add neighbor to path
                replicatedMsg.hops++;

                // send(neighbor, replicatedMsg);
                // Can't use SearchProtocol's send() as it would result in too many message copies 
                // send() also does its own TTL update
                // We use transport directly
                Transport tr = (Transport) neighbor.getProtocol(this.transportID);
                tr.send(node, neighbor, replicatedMsg, pid); // pid defined in SearchProtocol

                updateRoutingTable(neighbor, replicatedMsg);
            } 
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

        pherProtocol = (PheromoneProtocol) whoAmI.getProtocol(getLinkableID());

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
