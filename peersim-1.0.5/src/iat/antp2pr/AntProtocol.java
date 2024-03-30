package iat.antp2pr;

import java.util.List;

import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.core.Protocol;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;
import peersim.vector.SingleValueComparator;

/**
 * This protocol handles query routing in AntP2PR. 
 * Requires PheromoneProtocol as a Linkable to work. 
 */
public class AntProtocol implements EDProtocol {
    private static final String PAR_LINKABLE = "linkable";
    private static final String PAR_TRANSPORT = "transport";

    private final int linkablePid;
    private final int transportPid;

    // Constructor
    public AntProtocol(String prefix) {
        transportPid = Configuration.getPid(prefix + "." + PAR_TRANSPORT);
        linkablePid = Configuration.getPid(prefix + "." + PAR_LINKABLE);
    }

    /**
     * Algorithm 3 in Loukos et al. 2010
     * Forward a query message to neighbor nodes
     */
    public void messageRouting() {

    }

    /**
     * Forward "ant" from current node. Once successful and TTL expires,
     * update pheromone values of nodes traveled along the path. 
     * 
     * @param node
     *            the node on which this component is run.
     * @param protocolID
     *            the id of this protocol in the protocol array.
     * @param message 
     *            the delivered message.
     */
    public void forwardAnt(Node node, int pid, AntMessage msg) {
        // Return if TTL expires
        if (msg.getTtl() <= 0) { return; }

        // Message has arrived at current node, add to path
        msg.addToPath(node);

        // Check to see if linkable is of type PheromoneProtocol
        PheromoneProtocol pherProtocol = null;
        Protocol tempProtocol = node.getProtocol(linkablePid);
        if (node.getProtocol(linkablePid) instanceof PheromoneProtocol) {
            pherProtocol = (PheromoneProtocol) tempProtocol;
        }
        else {
            System.err.println("Unexpected linkable type: " + tempProtocol.getClass().getName()
                + ", must be of type PheromoneProtocol");
        }
        // Obtain node's transport protocol
        Transport transport = (Transport) node.getProtocol(transportPid);

        // If resource is found in current node, update query hits and pherTables
        // Only successful node knows the full path
        // Don't forward message regardless of TTL
        if (pherProtocol.hasResource(msg.getContent())) {

            // Increment hit count of message
            msg.incrementHitCount();
        
            List<Node> path = msg.getPath();

            // Iterate through nodes in path
            for (int i = 0; i < path.size(); i++) {
                Node pathNode = path.get(i);

                // Pheromone protocol of pathNode
                PheromoneProtocol pathNodePherProtocol = 
                    (PheromoneProtocol) pathNode.getProtocol(linkablePid);
                
                for (Node p : path) {
                    // Update query hit table of path node
                    // If other nodes in path are immediate neighbors of pathNode, increment
                    if (pathNodePherProtocol.contains(p)) {
                        pathNodePherProtocol.incrementQueryHit(p);
                    }
                }
            }
        }

        // If resource isn't found in current node, forward message to other nodes
        else {
            messageRouting();
        } 
    }


    /**
     * Execute floodMessage() for Event-Driven simulations
     * Processes incoming messages (in the form of events)
     * 
     * @param node
     *            the node on which this component is run.
     * @param protocolID
     *            the id of this protocol in the protocol array.
     * @param event 
     *            the delivered event
     */
    @Override
    public void processEvent(Node node, int pid, Object event) {
        AntMessage msg;
        if (event instanceof AntMessage) {
            msg = (AntMessage) event;

            forwardAnt(node, pid, msg);
        }
        else {
            // Handle other types of events if necessary
            System.err.println("Unexpected event type: " + event.getClass().getName());
        }
    }


    /** WIP
     * Return a clone of the protocol. Used to instantiate nodes. 
     * Invoked at any time during the simulation.
     */
    @Override
    public Object clone() {
        AntProtocol ap = null;
        return ap;

    }
    
}
