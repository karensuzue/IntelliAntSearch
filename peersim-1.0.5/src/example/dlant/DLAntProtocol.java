package example.dlant;

import peersim.config.Configuration;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class DLAntProtocol implements EDProtocol {

    // Constants
    private static final String PAR_TRANSPORT = ".transport";
    private static final String PAR_EVAPORATION = ".evaporation";
    private static final String PAR_ALPHA = ".alpha";
    private static final String PAR_LINKABLE = ".linkable";
    private static final double q1 = 0.01;
    private static final double q2 = 0.5;

<<<<<<< Updated upstream
    // Pheromone-related parameters
    private final double alpha; // Importance of the pheromone trail
    private final double evaporation; // Rate of pheromone evaporation
    private final int transportPid; // Transport layer ID
    private final int linkablePid; // Linkable layer ID
    private final Map<Node, Double> pheromoneLevels; // Pheromone levels to neighbors
=======
    // Protocol parameters
    private final double alpha;
    private final double evaporation;
    private final int transportPid;
    private final int linkablePid;
    private final Map<Node, Double> pheromoneLevels;

    // Resources and query hit count
    private Map<Node, Integer> queryHitCount;
    private ArrayList<Integer> resources;
>>>>>>> Stashed changes

    public DLAntProtocol(String prefix) {
<<<<<<< Updated upstream
        this.transportPid = Configuration.getPid(prefix + PAR_TRANSPORT);
        this.linkablePid = Configuration.getPid(prefix + PAR_LINKABLE);
        this.alpha = Configuration.getDouble(prefix + PAR_ALPHA, 1.0);
        this.evaporation = Configuration.getDouble(prefix + PAR_EVAPORATION, 0.1);
        this.pheromoneLevels = new HashMap<>();
=======
        transportPid = Configuration.getPid(prefix + PAR_TRANSPORT);
        linkablePid = Configuration.getPid(prefix + PAR_LINKABLE);
        alpha = Configuration.getDouble(prefix + PAR_ALPHA, 1.0);
        evaporation = Configuration.getDouble(prefix + PAR_EVAPORATION, 0.1);
        pheromoneLevels = new HashMap<>();
        resources = new ArrayList<>();
        queryHitCount = new HashMap<>();
>>>>>>> Stashed changes
    }

    public void startAntSearch(Node startNode, String objectToSearch, int pid) {
        AntMessage msg = new AntMessage(startNode.getIndex(), objectToSearch, alpha, calculateInitialTTL());
        EDSimulator.add(0, msg, startNode, pid);
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {
        if (event instanceof AntMessage) {
            AntMessage msg = (AntMessage) event;

<<<<<<< Updated upstream
            // Pheromone evaporation
            pheromoneLevels.forEach((key, value) -> pheromoneLevels.put(key, value * (1 - evaporation)));

            // Update pheromone levels upon successful discovery
            if (msg.isHit()) {
                updatePheromones(msg, node);
            }

            // Forward the ant to neighbors based on the pheromone level
=======
            if (msg.getPath().contains(node.getIndex())) {
                return;
            }

            msg.addToPath(node.getIndex());

            boolean hitOccurred = this.resources.contains(msg.getContent());
            if (hitOccurred) {
                msg.incrementHitCount();
                if (!msg.getPath().isEmpty()) {
                    int lastNeighborIndex = msg.getPath().size() - 2;
                    if (lastNeighborIndex >= 0) {
                        Node lastNeighbor = Network.get(msg.getPath().get(lastNeighborIndex));
                        registerHit(lastNeighbor);
                    }
                }
            }
            updatePheromones(msg, node, hitOccurred);

>>>>>>> Stashed changes
            if (msg.getTtl() > 0) {
                forwardAnt(msg, node);
            }
        }
    }

    private void forwardAnt(AntMessage msg, Node currentNode) {
        Linkable linkable = (Linkable) currentNode.getProtocol(linkablePid);
<<<<<<< Updated upstream
        if (linkable.degree() > 0 && msg.getTtl() > 0) {
            // Decrement TTL by 1 as a base case
            msg.setTtl(msg.getTtl() - 1);
    
            for (int i = 0; i < linkable.degree(); i++) {
                Node neighbor = linkable.getNeighbor(i);
    
                // Check if the pheromone level is above a threshold before forwarding
                double pheromoneLevel = pheromoneLevels.getOrDefault(neighbor, 0.0);
                if (pheromoneLevel > alpha) {
                    AntMessage replicatedMsg = msg.replicateForForwarding();
    
                    // Adjust TTL based on pheromone level or other criteria if necessary
                    // For example, you might increase TTL if pheromoneLevel is exceptionally high,
                    // indicating a highly promising path.
    
                    Transport transport = (Transport) currentNode.getProtocol(transportPid);
                    transport.send(currentNode, neighbor, replicatedMsg, transportPid);
=======
        if (linkable.degree() > 0) {
            IntStream.range(0, linkable.degree()).forEach(i -> {
                Node neighbor = linkable.getNeighbor(i);
                double pheromoneLevel = pheromoneLevels.getOrDefault(neighbor, 1.0);
                if (pheromoneLevel >= alpha) {
                    Transport transport = (Transport) currentNode.getProtocol(transportPid);
                    transport.send(currentNode, neighbor, msg.replicateForForwarding(), pid);
>>>>>>> Stashed changes
                }
            }
        }
    }

    private void updatePheromones(AntMessage msg, Node currentNode, boolean hit) {
        pheromoneLevels.forEach((neighbor, currentLevel) -> {
            double delta = hit ? q1 * Math.exp(q2 * queryHitCount.getOrDefault(neighbor, 0)) : q1;
            pheromoneLevels.put(neighbor, currentLevel + delta);
        });
        pheromoneLevels.forEach((neighbor, currentLevel) -> {
            pheromoneLevels.put(neighbor, currentLevel * (1 - evaporation));
        });
    }

    public void registerHit(Node neighbor) {
        queryHitCount.merge(neighbor, 1, Integer::sum);
    }

    private int calculateInitialTTL() {
        return Configuration.getInt("simulation.initialTTL", 5);
    }

    @Override
    public Object clone() {
        return new DLAntProtocol(DLAntProtocol.class.getSimpleName());
    }
}
