package example.dlant;

import peersim.config.Configuration;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;
import peersim.vector.SingleValueHolder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

public class DLAntProtocol implements EDProtocol {

    // Constants
    private static final String PAR_TRANSPORT = ".transport";
    private static final String PAR_EVAPORATION = ".evaporation";
    private static final String PAR_ALPHA = ".alpha";
    private static final String PAR_LINKABLE = ".linkable";

    // Pheromone-related parameters
    private final double alpha; // Importance of the pheromone trail
    private final double evaporation; // Rate of pheromone evaporation
    private final int transportPid; // Transport layer ID
    private final int linkablePid; // Linkable layer ID
    private final Map<Node, Double> pheromoneLevels; // Pheromone levels to neighbors
    
    protected List<Integer> resources;

    // Constructor that initializes the protocol's parameters
    public DLAntProtocol(String prefix) {
        this.transportPid = Configuration.getPid(prefix + PAR_TRANSPORT);
        this.linkablePid = Configuration.getPid(prefix + PAR_LINKABLE);
        this.alpha = Configuration.getDouble(prefix + PAR_ALPHA, 1.0);
        this.evaporation = Configuration.getDouble(prefix + PAR_EVAPORATION, 0.1);
        this.pheromoneLevels = new HashMap<>();

        this.resources = new ArrayList<>();
    }

    public void addResource(int resource) {
        resources.add(resource);
    }

    public boolean hasResource(int resource) {
        return resources.contains(resource);
    }

    public ArrayList<Integer> getResources() {
        return new ArrayList<>(resources);
    }

    public void startAntSearch(Node startNode, int objectToSearch, int pid) {
        AntMessage msg = new AntMessage(startNode.getIndex(), objectToSearch, alpha, calculateInitialTTL());

        EDSimulator.add(0, msg, startNode, pid);
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {
        if (event instanceof AntMessage) {
            AntMessage msg = (AntMessage) event;

            if (msg.getPath().contains(node.getIndex())) {
                return;
            }

            msg.addToPath(node.getIndex());

            // Check if the object is found
            if (this.resources.contains(msg.getContent())) {
                msg.incrementHitCount();
            }

            // Pheromone evaporation
            pheromoneLevels.forEach((key, value) -> pheromoneLevels.put(key, value * (1 - evaporation)));
            
            System.out.println(msg);

            // Update pheromone levels upon successful discovery
            if (msg.isHit()) {
                updatePheromones(msg, node);
            }

            // Forward the ant to neighbors based on the pheromone level
            if (msg.getTtl() > 0) {
                forwardAnt(msg, node, pid);
            }
        }
    }

    private void forwardAnt(AntMessage msg, Node currentNode, int pid) {
        Linkable linkable = (Linkable) currentNode.getProtocol(linkablePid);

        if (linkable.degree() > 0) {
            IntStream.range(0, linkable.degree()).forEach(i -> {
                Node neighbor = linkable.getNeighbor(i);
                double pheromoneLevel = pheromoneLevels.getOrDefault(neighbor, 1.0);

                if (pheromoneLevel >= alpha) {
                    Transport transport = (Transport) currentNode.getProtocol(transportPid);

                    transport.send(currentNode, neighbor, msg.replicateForForwarding(), pid);
                }
            });
        }
    }

    private void updatePheromones(AntMessage msg, Node currentNode) {
        Integer previousNodeIndex = msg.getPreviousNodeIndex();
        if (previousNodeIndex != null) {
            Node previousNode = Network.get(previousNodeIndex); // Convert index to Node
            pheromoneLevels.merge(previousNode, alpha, (oldValue, newValue) -> oldValue + newValue);
        }
    }

    private int calculateInitialTTL() {
        return Configuration.getInt("simulation.initialTTL", 2);
    }

    @Override
    public Object clone() {
        return new DLAntProtocol("protocol.dlant");
    }
}