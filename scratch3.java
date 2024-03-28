
import peersim.core.CommonState;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.cdsim.CDProtocol;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;
import peersim.vector.SingleValueHolder;

/**
 * This class provides an implementation for the flooding technique.
 * When a pair of nodes interact, their values are
 * averaged. The class subclasses {@link SingleValueHolder} in
 * order to provide a consistent access to the averaging variable value.
 *
 * Note that this class does not override the clone method, because it does
 * not have any state other than what is inherited from
 * {@link SingleValueHolder}.
 */
public class FloodingProtocol extends SingleValueHolder implements CDProtocol, EDProtocol{
    

    public FloodingProtocol(String prefix) {
        super(prefix);
    }

    /**
     * Using a {@link Linkable} protocol, send message to a random neighbor
     * from a specified node. 
     * Overrides EDProtocol's processEvent(), standard method invoked by
     * scheduler to deliver events to FloodingProtocol. 
     * 
     * @param node
     *            the node on which this component is run.
     * @param protocolID
     *            the id of this protocol in the protocol array.
     * @param message 
     *            the delivered message.
     */
    public void floodMessage(Node node, int protocolID, Message message) {
         // Obtain ID of linkable object used by a FloodingProtocol
        int linkableID = FastConfig.getLinkable(protocolID);
        // Obtain specific node linkable protocol, i.e. it's neighbors list
        Linkable linkable = node.getProtocol(linkableID);

        // If node has neighbors
        if (linkable.degree() > 0) {
            // Randomly select neighbor
            // CommonState is the common state of the simulation all objects see
            // CommonState.r refers to a ExtendedRandom object, source of randomness, basically retrieve random ID
            Node peer = linkable.getNeighbor(CommonState.r.nextInt(linkable.degree()));
            // If selected neighbor isn't functioning properly, return
            if (!peer.isUp()) {
                return;
            }

            // Return message if time to live expires
            if (message.getTtl() <= 0) {
                return;
            }
            // Decrease time to live
            message.decreaseTtl();
            // Obtain ID of transport protocol used by a FloodingProtocol
            int transportID = FastConfig.getTransport(protocolID);
            // Obtain transport protocol specific to node 
            Transport transport = node.getProtocol(transportID);
            // Send message to peer
            transport.send(node, peer, message, transportID);
        }
    }

    /**
     * Flood messages to all neighbors. 
     * Overrides EDProtocol's processEvent(), standard method invoked by
     * scheduler to deliver events to FloodingProtocol. 
     * 
     * @param node
     *            the node on which this component is run.
     * @param protocolID
     *            the id of this protocol in the protocol array.
     * @param event 
     *            the delivered event
     */
    public void processEvent(Node node, int protocolID, Object event) {
        if (event instanceof Message) {
            Message msg = (Message) event;
            
            System.out.println("Node " + node.getID() + " received message: " + msg.getContent());

            if (msg.getTtl() > 0) {
                floodMessage(node, pid, msg);
            }
        }
    }

    /**
     * Flood messages to all neighbors.
     * Overrides CDProtocol's nextCycle(), standard method to define
     * periodic activity for cycle-driven simulations. 
     * 
     * @param node
     *            the node on which this component is run.
     * @param protocolID
     *            the id of this protocol in the protocol array.
     */
    public void nextCycle(Node node, int protocolID) {
        floodMessage();
    }
}

package iat.flooding;

import peersim.core.Node;
import peersim.core.Linkable;

/**
 * This class helps get neighbors of a node .
 */
public class NeighborHelper {

    // Method to get neighbors of a node excluding a specific neighbor
    public static Node[] getNeighborsExcluding(Node node, Node excludedNeighbor) {
        Linkable linkable = (Linkable) node.getProtocol(FastConfig.getLinkable(CommonState.getPid()));
        int numNeighbors = linkable.degree();
        Node[] neighbors = new Node[numNeighbors - 1]; // Array to hold neighbors excluding the excludedNeighbor
        int index = 0;

        // Iterate over all neighbors
        for (int i = 0; i < numNeighbors; i++) {
            Node neighbor = linkable.getNeighbor(i);
            // Exclude the specified neighbor
            if (!neighbor.equals(excludedNeighbor)) {
                neighbors[index++] = neighbor;
            }
        }

        return neighbors;
    }
}


    /**
     * Helper function to get neighbors of a node excluding a specific neighbor.
     * 
     * @param node
     *            the node on which this component is run.
     * @param protocolID
     *            the id of this protocol in the protocol array.
     */
    private static Node[] getNeighborsExcluding(Node node, Node excludedNeighbor, Linkable linkable) {
        int numNeighbors = linkable.degree();
        // Array to hold neighbors excluding the excludedNeighbor
        Node[] neighbors = new Node[numNeighbors - 1]; 

         // Iterate over all neighbors
        for (int i = 0; i < numNeighbors; i++) {
            Node neighbor = linkable.getNeighbor(i);
            // Exclude the specified neighbor

        }
        return neighbors;
    }