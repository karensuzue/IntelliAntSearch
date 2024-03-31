package iat.antp2pr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.core.Protocol;


/**
 * This protocol contains the neighbor, resource, pheromone, 
 * and query hit tables for a single node in the AntP2PR routing scheme, 
 * as well as provides operations for updating them. Similar use to 
 * IdleProtocol, but with extra features. Should work with
 * WireGraph protocols, because it implements Linkable. 
 */
public class PheromoneProtocol implements Protocol, Linkable {

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
    // <neighbor index (in neighbors), pheromone value>
    protected Map<Node, Double> pherTable = new HashMap<>(); 
    // <neighbor index, query hit count>
    protected Map<Node, Integer> queryHitCount = new HashMap<>();
    // Resources are abstracted as unique integers
    protected List<Integer> resources = new ArrayList<>();
    // Parameters for update function
    protected double q1, q2, low, high; 

    // For Linkable implementation
    protected Node[] neighbors;
    // Length of neighbors, pherTable, and queryHitCount
    protected int len;
    
    private static final int DEFAULT_INITIAL_CAPACITY = 10; // default neighbor capacity
    private static final int MIN_RES_CAPACITY = 1; // min resource capacity
    private static final int MAX_RES_CAPACITY = 10; // max resource capacity

    private static final int POSSIBLE_RESOURCES = 50;

    private Random random = new Random();

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

        int resource_capacity = random.nextInt(MAX_RES_CAPACITY - MIN_RES_CAPACITY + 1) + MIN_RES_CAPACITY;

        // Initialize resources
        for (int i = 0; i < resource_capacity; i++) {
            addResource(nextRandomResource());
        }
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
        queryHitCount.put(neighbor, queryHitCount.getOrDefault(neighbor, 0) + 1);
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

    // @Override
    // public void processEvent(Node node, int pid, Object event) {
        //// when receive success message, run updatepheromone()
    // }

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

    public Node[] getNeighbors() {
        return neighbors;
    }

    /** 
     * Adds given node if it is not already in the network. 
     * There is no limit to the number of nodes that can be 
     * added. 
     * Called by OverlayGraph to instantiate network.
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
     * Return a clone of the protocol. Used to instantiate nodes. 
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

        // Clone resources table, but randomize
        pp.resources = new ArrayList<>();

        int resource_capacity = random.nextInt(MAX_RES_CAPACITY - MIN_RES_CAPACITY + 1) + MIN_RES_CAPACITY;

        // Initialize resources
        for (int i = 0; i < resource_capacity; i++) {
            pp.addResource(nextRandomResource());
        }

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


    // ----------------------------------------------------------
    // Utililty Methods
    // ----------------------------------------------------------


    public static int nextRandomResource() {
        Random random = new Random();
        return random.nextInt(POSSIBLE_RESOURCES);
    }
}