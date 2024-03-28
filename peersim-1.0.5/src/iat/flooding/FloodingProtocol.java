package iat.flooding;

import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.cdsim.CDProtocol;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;
import peersim.vector.SingleValueHolder;

/**
 * This class provides an implementation for the flooding protocol.
 * FloodingProtocol will be contained in each node. 
 */
public class FloodingProtocol extends SingleValueHolder implements CDProtocol, EDProtocol{
    
    private static final String PAR_LINKABLE = "linkable";
    private static final String PAR_TRANSPORT = "transport";

    // Constructor
    public FloodingProtocol(String prefix) {
        super(prefix);
    }

    /**
     * Using a {@link Linkable} protocol, send message to neighbors of
     * a specified node. 
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
        // Return if TTL expires
        if (message.getTtl() <= 0) { return; }
        
        // Message has arrived at node, update as visited
        message.updateVisited(node);

        // Obtain ID of linkable object used by a FloodingProtocol
        int linkableID = FastConfig.getLinkable(protocolID);
        // Obtain specific node linkable protocol, i.e. it's neighbors list
        Linkable linkable = (Linkable) node.getProtocol(linkableID);

        // Obtain ID of transport protocol used by a FloodingProtocol
        int transportID = FastConfig.getTransport(protocolID);

        // Obtain source node that sent message to current node
        // Node source = message.getSource();
        // assert(message.getDestination().equals(node)); // SANITY CHECK - is message's destination truly current node?

        // If node has neighbors
        if (linkable.degree() > 0) {
            // Send message to all neighbors, except for nodes it has already visited
            for (int i = 0; i < linkable.degree(); i++) {
                Node peer = linkable.getNeighbor(i);

                // If peer is not active, continue
                if (!peer.isUp()) { continue; }

                // If peer is the one sending current node this message, continue
                // if (peer.equals(source)) { continue; }

                // If message has visited peer, continue
                if (message.hasVisited(peer)) { continue; }

                // Create copy of current message, update source and destination
                Message newMsg = new Message(node, peer, message.getContent(), message.getTtl());
                // Decrease TTL
                newMsg.decreaseTtl();
                
                // Obtain transport of peer
                Transport transport = (Transport) peer.getProtocol(transportID);
                // Send message to peer, ED Simulator listens to transport
                transport.send(node, peer, newMsg, transportID);

                System.out.println("Node " + node.getID() + " sent message to node " + peer.getID() + ": " + newMsg.getContent());
            }
        }

        else {
            System.out.println(node.getID() + " has no neighbors");
        }
    }

    /**
     * Execute floodMessage() for Event-Driven simulations
     * Overrides EDProtocol's processEvent()
     * Processes incoming messages (in the form of events)
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
                floodMessage(node, protocolID, msg);
            }
        } 
        else {
            // Handle other types of events if necessary
            System.err.println("Unexpected event type: " + event.getClass().getName());
        }
    }

    /**
     * WIP
     * Execute floodMessage() for Cycle-Driven simulations
     * Overrides CDProtocol's nextCycle(), performs periodically
     * Needs "inbox" data structure to keep track all of the messages?
     * Flooding probably easier to implement with Event-Driven mode
     * 
     * @param node
     *            the node on which this component is run.
     * @param protocolID
     *            the id of this protocol in the protocol array.
     */
    public void nextCycle(Node node, int protocolID) {
        // if (msg.getTtl() > 0) {
            // floodMessage(node, protocolID, msg);
        // }
        return;
    }


}