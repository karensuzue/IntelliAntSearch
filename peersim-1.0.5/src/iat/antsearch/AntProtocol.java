package iat.antsearch;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import example.isearch.SMessage;
import example.isearch.SearchProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Node;

public class AntProtocol extends SearchProtocol {
    private static final String PAR_Q1 = "q1";
    private static final String PAR_Q2 = "q2";
    private static final String PAR_HIGH = "high";
    private static final String PAR_LOW = "low";

    // For AntP2PR implementation
    // <neighbor index (in neighbors), pheromone value>
    protected Map<Node, Double> pherTable = new HashMap<>(); 
    // <neighbor index, query hit count>
    protected Map<Node, Integer> queryHitCount = new HashMap<>();

    // Parameters for update function
    protected double q1, q2, low, high; 


    public AntProtocol(String prefix) {
        super(prefix);

        q1 = Configuration.getDouble(prefix + "." + PAR_Q1);
        q2 = Configuration.getDouble(prefix + "." + PAR_Q2);
        low = Configuration.getDouble(prefix + "." + PAR_LOW);
        high = Configuration.getDouble(prefix + "." + PAR_HIGH);
    }

    // Called on each node
    @Override
    public void process(SMessage msg) {
        if (this.match(msg.payload)) {
            this.notifyOriginator(msg);

            // Get path of message
            List<Node> path = msg.path;

            // Iterate through nodes in path
            for (int i = 0; i < path.size(); i++) {
                Node pathNode = path.get(i); // Current node in path

                // Pheromone protocol of pathNode
                AntProtocol pathNodeProtocol = 
                    (AntProtocol) pathNode.getProtocol(pid);
                
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
        } else {
            for (Node m : view) {
                if (msg.hasVisited(m)) { continue; }

                if (msg.ttl <= 0) { continue; }

                SMessage replicatedMsg = (SMessage) msg.copy();

                Double pheromone = this.getPheromone(m);
                Double low_bound = this.low;
                Double high_bound = this.high;

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

    

    @Override
    public void nextCycle(Node node, int protocolID) {
        // Calls Process method
        super.nextCycle(node, protocolID);

        int[] data = this.pickQueryData();

        if (data != null) {
            SMessage m = new SMessage(node, SMessage.QRY, 0, data, ttl);

            for (int i = 0; i < this.degree(); i++) {
                this.send((Node) this.getNeighbor(i), m);
            }
        }
    }
    

  


    /**
     * Algorithm 1 in Loukos et al. 2010
     * Update entire pheromone table
     * This is performed after processing a successful message
     */
    public void updatePherTable() {
        // Iterate through the pherTable map
        for (Map.Entry<Node, Double> entry : pherTable.entrySet()) {
            Node neighbor = entry.getKey();
            Double pheromone = entry.getValue();
            int queryHit = queryHitCount.get(neighbor);

                    
            // Update the value (for example, increment by 1)
            Double delta = q1 * Math.pow(Math.E, q2 * queryHit);

            // System.out.println("Query Hit: " + queryHit + " for " + neighbor.getID() + " with pheromone " + pheromone + " and delta " + delta);

                    
            // Update the value in pherTable
            pherTable.put(neighbor, pheromone + delta);
        }
    }

    /**
     * Algorithm 2 in Loukos et al. 2010
     * Normalizes values in pheromone table within range [0,1]
     * This is performed after updating the pheromone table
     */
    public void normalizePherTable() {
        Double sum = 0.0;
        for (Map.Entry<Node, Double> entry : pherTable.entrySet()) {
            sum = sum + entry.getValue();
        }

        for (Map.Entry<Node, Double> entry : pherTable.entrySet()) {
            pherTable.put(entry.getKey(), entry.getValue() / sum );
        } 
    }

    public Double getPheromone(Node node) {
        if (pherTable.containsKey(node)) {
            // Retrieve and return the pheromone value associated with the node
            return pherTable.get(node);
        } else {
            // If the node is not found in the pherTable
            throw new IllegalArgumentException("Node not found in the pherTable");
        }   
    }

    // ----------------------------------------------------------
    // Query Hit Table Methods
    // ----------------------------------------------------------

    /** Increment query hit count for neighbor */
    public void incrementQueryHit(Node neighbor) {
        queryHitCount.put(neighbor, queryHitCount.getOrDefault(neighbor, 0) + 1);
    }

    @Override
    public boolean addNeighbor(Node neighbor) {
        // HashMap dynamically resizes itself
        pherTable.put(neighbor, 1.0);

        normalizePherTable();

        // Initialize query hit count as 0
        queryHitCount.put(neighbor, 0);
        return super.addNeighbor(neighbor);
    }

    @Override
    public Object clone() {
        AntProtocol ap = (AntProtocol) super.clone();
        ap.pherTable = new HashMap<>(pherTable);
        ap.queryHitCount = new HashMap<>(queryHitCount);

        return ap;
    }

}
