package iat.dlantisearch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.IdleProtocol;
import peersim.core.Node;


/**
 * This protocol contains the neighbor, resource, pheromone, 
 * and query hit tables for a single node in the AntP2PR routing scheme, 
 * as well as provides operations for updating them. Similar use to 
 * IdleProtocol, but with extra features. Should work with
 * WireGraph protocols, because it implements Linkable. 
 * NOTE: This is the version that extends IdleProtocol
 * TODO: Merge this version with the original? (which implements Protocol, Linkable)
 */
public class PheromoneProtocol extends IdleProtocol {

    // ----------------------------------------------------------
    // Config Parameters
    // ----------------------------------------------------------

    /**
     * User-defined for experimental control
     * - q1, q2 determines aggressiveness of pheromone updates
     * - high and low represents pheromone thresholds during message routing
     * - capacity is how many neighbors can we store
     */
     
    private static final String PAR_Q1 = "q1";
    private static final String PAR_Q2 = "q2";
    private static final String PAR_HIGH = "high";
    private static final String PAR_LOW = "low";

    // ----------------------------------------------------------
    // Fields
    // ----------------------------------------------------------

    // For AntP2PR implementation
    // <neighbor index (in neighbors), pheromone value>
    protected Map<Integer, Double> pherTable = new HashMap<>(); 
    // <neighbor index, query hit count>
    protected Map<Integer, Integer> queryHitCount = new HashMap<>();
    // Resources are abstracted as unique integers
    protected List<Integer> resources = new ArrayList<>();
    // Parameters for update function
    protected double q1, q2, low, high; 

    private final double uniformPheromone = 1.0;
    
    // ----------------------------------------------------------
    // Initialization
    // ----------------------------------------------------------

    // Constructor
    public PheromoneProtocol(String prefix) {
        super(prefix);

        // User-defined values
        q1 = Configuration.getDouble(prefix + "." + PAR_Q1);
        q2 = Configuration.getDouble(prefix + "." + PAR_Q2);
        low = Configuration.getDouble(prefix + "." + PAR_LOW);
        high = Configuration.getDouble(prefix + "." + PAR_HIGH);
    }

    

    // ----------------------------------------------------------
    // Pheromone Table Methods
    // ----------------------------------------------------------

    /**
     * Algorithm 1 in Loukos et al. 2010
     * Update entire pheromone table
     * This is performed after processing a successful message
     */
    public void updatePherTable() {
        // Iterate through the pherTable map
        for (Map.Entry<Integer, Double> entry : pherTable.entrySet()) {
            Integer neighborIdx = entry.getKey();
            Double pheromone = entry.getValue();
            int queryHit = queryHitCount.get(neighborIdx);

            // Update the value (for example, increment by 1)
            Double delta = q1 * Math.pow(Math.E, q2 * queryHit);

            // System.out.println("Neighbor: " + neighbor.getID() + " Pheromone: " + pheromone + " Query Hit: " + queryHit + " Delta: " + delta);
                    
            // Update the value in pherTable
            pherTable.put(neighborIdx, pheromone + delta);
        }

    }

    /**
     * Algorithm 2 in Loukos et al. 2010
     * Normalizes values in pheromone table within range [0,1]
     * This is performed after updating the pheromone table
     */
    public void normalizePherTable() {
        // Find the sum of all pheromone values
        Double sum = 0.0;

        for (Double pheromone : pherTable.values()) {
            sum += pheromone;
        }

        HashMap<Integer, Double> normalizedPherTable = new HashMap<>();

        for (Map.Entry<Integer, Double> pherEntry : pherTable.entrySet()) {
            Integer neighborIdx = pherEntry.getKey();
            Double pheromone = pherEntry.getValue();

            normalizedPherTable.put(neighborIdx, pheromone / sum);
        }


        pherTable = normalizedPherTable;
    }

    public Double getPheromone(Node node) {
        if (pherTable.containsKey(node.getIndex())) {
            // Retrieve and return the pheromone value associated with the node
            return pherTable.get(node.getIndex());
        } else {
            // If the node is not found in the pherTable
            throw new IllegalArgumentException("Node not found in the pherTable");
        }   
    }

    public Double getLowBound() {
        return low;
    }

    public Double getHighBound() {
        return high;
    }

    // ----------------------------------------------------------
    // Query Hit Table Methods
    // ----------------------------------------------------------

    /** Increment query hit count for neighbor */
    public void incrementQueryHit(Node neighbor) {
        queryHitCount.put(neighbor.getIndex(), queryHitCount.getOrDefault(neighbor.getIndex(), 0) + 1);
    }

   
    // ----------------------------------------------------------
    // Linkable Implementation
    // ----------------------------------------------------------

    /** 
     * Adds given node if it is not already in the network. 
     * There is no limit to the number of nodes that can be 
     * added. 
     * Called by OverlayGraph to instantiate network.
     */
    @Override
    public boolean addNeighbor(Node neighbor) {       
        super.addNeighbor(neighbor);

        // Give neighbor uniform random pheromone value
        // HashMap dynamically resizes itself
        
        // If we're in the adding phase i.e. all neighbors should have uniform pheromone
        if (CommonState.getIntTime() == 0) {
            // Set pheromone value to be similar to others in the table
            
            Double value = pherTable.isEmpty() ? uniformPheromone : pherTable.entrySet().iterator().next().getValue();
            pherTable.put(neighbor.getIndex(), value);
        } else {
            pherTable.put(neighbor.getIndex(), uniformPheromone);
        }

        normalizePherTable();

        // Initialize query hit count as 0
        queryHitCount.put(neighbor.getIndex(), 0);

        return true;
    }

   
    /** 
     * Return a clone of the protocol. Used to instantiate nodes. 
     * Invoked at any time during the simulation.
     */
    @Override 
    public Object clone() {
	    PheromoneProtocol pp = (PheromoneProtocol) super.clone();

        // Clone pheromone table
        pp.pherTable = new HashMap<Integer, Double>();
        for (Map.Entry<Integer, Double> entry : this.pherTable.entrySet()) {
            pp.pherTable.put(entry.getKey(), entry.getValue());
        }

        // Clone query hit count table
        pp.queryHitCount = new HashMap<Integer, Integer>();
        for (Map.Entry<Integer, Integer> entry : this.queryHitCount.entrySet()) {
            pp.queryHitCount.put(entry.getKey(), entry.getValue());
        }
        
	    return pp;
    }

}