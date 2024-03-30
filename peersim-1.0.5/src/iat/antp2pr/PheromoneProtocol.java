package iat.antp2pr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;

/**
 * This protocol contains the neighbor, resource, pheromone, 
 * and query hit tables for a single node in the AntP2PR routing scheme, 
 * as well as provides operations for updating them. Similar use to 
 * IdleProtocol, but with extra features. Should work with
 * WireGraph protocols, because it implements Linkable. 
 */
public class PheromoneProtocol implements EDProtocol, Linkable{

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
    private static final String PAR_INITCAP = "capacity"; // optional, default defined below

    // ----------------------------------------------------------
    // Fields
    // ----------------------------------------------------------

    // For AntP2PR implementation
    protected Map<Node, Double> pherTable = new HashMap<>();
    protected Map<Node, Integer> queryHitCount = new HashMap<>();
    // Resources are abstracted as unique integers
    protected List<Integer> resources = new ArrayList<>();
    // Parameters for update function
    protected double q1, q2, low, high; 

    // For Linkable implementation
    protected Node[] neighbors;
    // Length of neighbors, pherTable, and queryHitCount
    protected int len;

    Random random = new Random();
    
    private static final int DEFAULT_INITIAL_CAPACITY = 10; // default neighbor capacity

    // ----------------------------------------------------------
    // Initialization
    // ----------------------------------------------------------

    // Constructor
    public PheromoneProtocol(String prefix) {
        // User-defined values
        q1 = Configuration.getDouble(prefix + "." + PAR_Q1);
        q2 = Configuration.getDouble(prefix + "." + PAR_Q2);
        low = Configuration.getDouble(prefix + "." + PAR_LOW);
        high = Configuration.getDouble(prefix + "." + PAR_HIGH);
        // Create neighbors table of defined capacity
        neighbors = new Node[Configuration.getInt(prefix + "." + PAR_INITCAP,
			DEFAULT_INITIAL_CAPACITY)];
	    len = 0; // neighbor count
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
        for (Map.Entry<Node, Double> entry : pherTable.entrySet()) {
            Node neighbor = entry.getKey();
            Double pheromone = entry.getValue();
            int queryHit = queryHitCount.get(neighbor);
                    
            // Update the value (for example, increment by 1)
            Double delta = q1 * Math.pow(Math.E, q2 * queryHit);
                    
            // Update the value in pherTable
            pherTable.put(neighbor, pheromone + delta);
        }
    }

    /**
     * Algorithm 2 in Loukos et al. 2010
     * Normalizes values in pheromone table
     * This is performed after updating the pheromone table
     */
    public void normalizePherTable() {
        Double sum = 0.0;
        for (Map.Entry<Node, Double> entry : pherTable.entrySet()) {
            sum = sum + entry.getValue();
        }

        for (Map.Entry<Node, Double> entry : pherTable.entrySet()) {
            pherTable.put(entry.getKey(), pherTable.get(entry) / sum );
        } 
    }

    // ----------------------------------------------------------
    // Query Hit Table Methods
    // ----------------------------------------------------------

    /** Update query hit count for neighbor */
    public void updateQueryHit(int neighbor) {

    }

    // ----------------------------------------------------------
    // Resource Table Methods
    // ----------------------------------------------------------

    /** 
     * Add resources to the node's resource table 
     * Resources are abstracted as unique numbers
     */
    public void addResource(int resource) {
        resources.add(resource);
    }

    /** Does this node have this resource? */
    public boolean hasResource(int resource) {
        return resources.contains(resource);
    }

    // ----------------------------------------------------------
    // EDProtocol Implementation
    // ----------------------------------------------------------

    @Override
    public void processEvent(Node node, int pid, Object event) {
        // when receive success message, run updatepheromone()
    }

    // ----------------------------------------------------------
    // Linkable Implementation
    // ----------------------------------------------------------

    @Override
    public void onKill() {
        neighbors = null;
        len = 0;
        pherTable.clear();
        queryHitCount.clear();
    }

    @Override
    public int degree() {
        return len;
    }

    @Override
    public Node getNeighbor(int i) {
        return neighbors[i];
    }

    /** 
     * Adds given node if it is not already in the network. 
     * There is no limit to the number of nodes that can be 
     * added. 
     * Called by OverlayGraph
     */
    @Override
    public boolean addNeighbor(Node neighbor) {       
        // If neighbor already included, don't add
        for (int i = 0; i < len; i++) {
            if (neighbors[i] == neighbor)
                return false;
        }
        
        // If neighbors list has reached max capacity, resize list
        if (len == neighbors.length) {
            Node[] temp = new Node[3 * neighbors.length / 2];
            System.arraycopy(neighbors, 0, temp, 0, neighbors.length);
            neighbors = temp;
        }

        // Add neighbor to list
        neighbors[len] = neighbor;

        // Give neighbor random pheromone value 
        // HashMap dynamically resizes itself
        pherTable.put(neighbor, random.nextDouble());
        normalizePherTable();

        // Initialize query hit count as 0
        queryHitCount.put(neighbor, 0);

        len++; // Update current neighbor count

        return true;
    }

    /** Does neighbor exist? */
    @Override
    public boolean contains(Node neighbor) {
        for (int i = 0; i < len; i++) {
            if (neighbors[i] == neighbor)
                return true;
        }
        return false;
    }

    @Override
    public void pack() {
        if (len == neighbors.length) { return; }
	    Node[] temp = new Node[len];
	    System.arraycopy(neighbors, 0, temp, 0, len);
	    neighbors = temp;
    }

    @Override
    public String toString() {
	    if( neighbors == null ) return "DEAD!";
	    StringBuffer buffer = new StringBuffer();
	    buffer.append("len=" + len + " maxlen=" + neighbors.length + " [");
	    for (int i = 0; i < len; ++i) {
		    buffer.append(neighbors[i].getIndex() + " ");
	    }
	    return buffer.append("]").toString();
    }

    /** 
     * Return a clone of the Linkable protocol. Used to instantiate nodes. 
     * Invoked at any time during the simulation.
     */
    @Override 
    public Object clone() {
	    PheromoneProtocol pp = null;
	    try { pp = (PheromoneProtocol) super.clone(); }
	    catch( CloneNotSupportedException e ) {} // never happens

        // Clone neighbors table
	    pp.neighbors = new Node[neighbors.length];
	    System.arraycopy(neighbors, 0, pp.neighbors, 0, len);

        // Clone length
	    pp.len = len;

        // Clone pheromone table
        pp.pherTable = new HashMap<Node, Double>();
        for (Map.Entry<Node, Double> entry : this.pherTable.entrySet()) {
            pp.pherTable.put(entry.getKey(), entry.getValue());
        }

        // Clone query hit count table
        pp.queryHitCount = new HashMap<Node, Integer>();
        for (Map.Entry<Node, Integer> entry : this.queryHitCount.entrySet()) {
            pp.queryHitCount.put(entry.getKey(), entry.getValue());
        }
        
	    return pp;
    }
}