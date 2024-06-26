package iat.antp2pr;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import peersim.config.Configuration;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.core.Protocol;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

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
     * Algorithm 3 in Loukos et al. 
     * 
     * @param node the current node
     * @param msg the query message to be forwarded
     */
    public void messageRouting(PheromoneProtocol pherProtocol, Node node, int pid, AntMessage msg) {
        // "pherProtocol" has to be for "node"
        // dont forget to decrease TTL 
        // Iterate through neighbors of the current node
        for (int i = 0; i < pherProtocol.degree(); i++) {
            Node neighbor = pherProtocol.getNeighbor(i);

            Double pheromone = pherProtocol.getPheromone(neighbor);
            Double low_bound = pherProtocol.getLowBound();
            Double high_bound = pherProtocol.getHighBound();

            // System.out.println("Pheromone: " + pheromone + " Low Bound: " + low_bound + " High Bound: " + high_bound);

            // Check if the neighbor is not the source of the message
            if (!neighbor.equals(msg.getSource())) {
                // Replicate the message for forwarding to the neighbor
                AntMessage forwardedMsg = msg.replicateForForwarding();

                // Update TTL of replicated message
                if (pheromone < low_bound) { forwardedMsg.setTtl(forwardedMsg.getTtl() - 1); }
                else if (pheromone > high_bound) { forwardedMsg.setTtl(forwardedMsg.getTtl() + 1); }

                // If there is remaining TTL
                if (forwardedMsg.getTtl() > 0) {
                    // Send the replicated message to the neighbor
                    Transport transport = (Transport) node.getProtocol(transportPid);
                    transport.send(node, neighbor, forwardedMsg, pid);
                }
            }
        }
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

        FileOutputStream fos = null;

        try {
            String fname = "log.txt"; // "output.txt
            fos = new FileOutputStream(fname, true);
            PrintStream pstr = new PrintStream(fos);

            pstr.println(msg.toString());

            fos.close();
        } catch (IOException e) {
            if (fos != null) {
                try { fos.close(); } 
                catch (IOException e1) { }
            }

            throw new RuntimeException(e);
        }


        // Check if linkable is of type PheromoneProtocol
        if (!(node.getProtocol(linkablePid) instanceof PheromoneProtocol)) {
            throw new IllegalArgumentException("Unexpected linkable type: " + node.getProtocol(linkablePid).getClass().getName()
                + ", must be of type PheromoneProtocol");
        }
        // PheromoneProtocol for current node
        PheromoneProtocol pherProtocol = (PheromoneProtocol) node.getProtocol(linkablePid);

         // If resource is found in current node, update query hits and pherTables
        // Only successful node knows the full path
        if (pherProtocol.hasResource(msg.getContent())) {
            // System.out.println("Has Content");
            // Increment hit count of message
            msg.incrementHitCount();
            // Get path of message
            List<Node> path = msg.getPath();

            // Iterate through nodes in path
            for (int i = 0; i < path.size(); i++) {
                Node pathNode = path.get(i); // Current node in path

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

                pathNodePherProtocol.updatePherTable(); // Update pheromone table of pathNode
                pathNodePherProtocol.normalizePherTable(); // Normalize pheromone table of pathNode
            }
        }

        // If resource isn't found in current node, forward message to other nodes
        // else {
            // System.out.println("Doesn't Have Content"); 
            // even if a hit is found, keep routing until TTL expires

            messageRouting(pherProtocol, node, pid, msg); // Algorithm 3
        // } 
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

            if (msg.inPath(node)) { return; } // Avoid loops

            if (msg.isHit()) {
                System.out.println(msg.toString());
            }
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
        try { ap = (AntProtocol) super.clone(); } 
        catch (CloneNotSupportedException e) {
            // This should not happen since AntProtocol implements Cloneable
            throw new InternalError(e.toString());
        }
        return ap;
    }
}
