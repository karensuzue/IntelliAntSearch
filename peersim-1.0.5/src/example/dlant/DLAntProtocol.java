package example.dlant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import peersim.config.Configuration;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;

public class DLAntProtocol implements EDProtocol {

    // Pheromone-related parameters
    private double alpha; // Importance of the pheromone trail
    private double evaporation; // Rate of pheromone evaporation
    private final int tid; // Transport layer ID

    // Constructor that takes the transport layer ID
    public DLAntProtocol(int tid) {
        // Initialize protocol parameters from the configuration
        String prefix = "protocol.dlant"; // Use the protocol name as defined in the config file
        alpha = Configuration.getDouble(prefix + ".alpha", 1.0);
        evaporation = Configuration.getDouble(prefix + ".evaporation", 0.1);
        this.tid = tid; // Assign the passed transport layer ID
    }

    // Method to start the ant search
    public void startAntSearch(Node startNode) {
        // Create an ant message with some initial pheromone level
        AntMessage msg = new AntMessage(startNode.getIndex(), "Search Start", alpha);
        // Schedule the ant message as an event for the node itself
        EDSimulator.add(0, msg, startNode, tid);
    }

    // Event processing method
    @Override
    public void processEvent(Node node, int pid, Object event) {
        if (event instanceof AntMessage) {
            AntMessage msg = (AntMessage) event;

            // Evaporate pheromone
            msg.setPheromoneLevel(msg.getPheromoneLevel() * (1 - evaporation));

            // Forward the message to the best neighbor based on the pheromone level
            Node bestNeighbor = selectBestNeighbor(node);

            if (bestNeighbor != null) {
                // Send the message to the selected neighbor
                ((Transport) node.getProtocol(tid)).send(node, bestNeighbor, replicateAntMessage(msg), pid);
            }
        }
    }

    private Node selectBestNeighbor(Node node) {
        List<Node> neighbors = getNeighbors(node);
        Node bestNeighbor = null;
        double maxPheromoneLevel = Double.MIN_VALUE;

        for (Node neighbor : neighbors) {
            double pheromoneLevel = getPheromoneLevelTo(node, neighbor);
            if (pheromoneLevel > maxPheromoneLevel) {
                maxPheromoneLevel = pheromoneLevel;
                bestNeighbor = neighbor;
            }
        }

        return bestNeighbor;
    }

    private List<Node> getNeighbors(Node node) {
    Linkable linkable = (Linkable) node.getProtocol(linkablePid); // Assume linkablePid is the protocol ID for the Linkable protocol
    List<Node> neighbors = new ArrayList<>();

    for (int i = 0; i < linkable.degree(); i++) {
        neighbors.add(linkable.getNeighbor(i));
    }

    return neighbors;
}
Map<Node, Double> pheromoneLevels = new HashMap<>();
    private double getPheromoneLevelTo(Node from, Node to) {
        // Check if the pheromone level for the 'to' node is stored in the 'from' node's map
    if (pheromoneLevels.containsKey(to)) {
        return pheromoneLevels.get(to);
    }
    // Return a default pheromone level if no level is stored
    return defaultValue; // defaultValue should be defined based on your simulation's requirements
    }

    private AntMessage replicateAntMessage(AntMessage original) {
        // Creates a deep copy of the original AntMessage to be forwarded
        AntMessage copy = new AntMessage(original.getSource(), original.getQuery(), original.getPheromoneLevel(), original.getTtl());
        // Additional fields and logic might be copied here as well
        return copy;
    }
    
    // Clone method for creating instances of the protocol for each node
    @Override
    public Object clone() {
        // Use the protocol name as defined in the config file and get its pid
        int pid = Configuration.lookupPid("protocol.dlant");
        return new DLAntProtocol(pid); // Pass the pid to the constructor
    }
}
