package iat.antp2pr;

import java.util.HashMap;

import peersim.core.IdleProtocol;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;


/**
 * This protocol contains the pheromone and query hit tables for a node in the
 * AntP2PR routing scheme, as well as provides operations for updating
 * them.
 */
public class PheromoneProtocol implements EDProtocol, Linkable{

    // Config parameters, user-defined for experimental control
    // q1, q2 determines aggressiveness of pheromone updates
    // qh is number of query hits
    // high and low represents pheromone thresholds during message routing
    private static final String PAR_Q1 = "q1";
    private static final String PAR_Q2 = "q2";
    private static final String PAR_QH = "qh";
    private static final String PAR_HIGH = "high";
    private static final String PAR_LOW = "low";

    // For AntP2PR implementation
    protected HashMap<Node, Integer> pherTable = new HashMap<>();
    protected HashMap<Node, Integer> queryHitCount = new HashMap();
    // Parameters for update function
    protected int q1, q2, qh, low, high;

    // For Linkable implementation
    protected Node[] neighbors;
    // Length of neighbors, pherTable, and queryHitCount
    protected int len;

    public PheromoneProtocol() {
        // initialize pherTable with uniform distributed random values 
        // also do the config thing 
        // for the message routing protocol to access the parameters, they will need a config parameter for a linkable (which is this)
    }

    public void updatePheromones() {
        // algo 1 of antp2pr
        // maybe also algo 2?
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {
        // when receive success message, run updatepheromone()
    }

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

    @Override
    public boolean addNeighbor(Node neighbour) {
        // add to neighbors
        // add to phertable and queryhitcount
        // upadte len
        // new random value for pheromone inserted
    }

    @Override
    public boolean contains(Node neighbor) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'contains'");
    }

    @Override
    public void pack() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'pack'");
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'processEvent'");
    }
}

// can we do multiple linkables in the config file, idleprotocol and this?