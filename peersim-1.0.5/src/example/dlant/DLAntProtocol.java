package example.dlant;

import peersim.config.Configuration;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class DLAntProtocol implements EDProtocol {

    private static final String PAR_TRANSPORT = ".transport";
    private static final String PAR_EVAPORATION = ".evaporation";
    private static final String PAR_ALPHA = ".alpha";
    private static final String PAR_LINKABLE = ".linkable";
    private static final double q1 = 80; 
    private static final double q2 = -0.2; 

    private final double alpha;
    private final double evaporation;
    private final int transportPid;
    private final int linkablePid;
    private final Map<Node, Double> pheromoneLevels;
    private final Map<Node, Integer> queryHitCount;
    protected ArrayList<Integer> resources;

    public DLAntProtocol(String prefix) {
        transportPid = Configuration.getPid(prefix + PAR_TRANSPORT);
        linkablePid = Configuration.getPid(prefix + PAR_LINKABLE);
        alpha = Configuration.getDouble(prefix + PAR_ALPHA, 1.0);
        evaporation = Configuration.getDouble(prefix + PAR_EVAPORATION, 0.1);
        pheromoneLevels = new HashMap<>();
        queryHitCount = new HashMap<>();
        resources = new ArrayList<>();
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
            boolean hitOccurred = this.resources.contains(msg.getContent());
            if (hitOccurred) {
                msg.incrementHitCount();
                queryHitCount.put(node, queryHitCount.getOrDefault(node, 0) + 1);
            }
            updatePheromones(msg, node, hitOccurred);
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

    private void updatePheromones(AntMessage msg, Node currentNode, boolean hit) {
        if (hit) {
            // Increase pheromones on the reverse path
            for (int i = msg.getPath().size() - 2; i >= 0; i--) {
                Node prevNode = Network.get(msg.getPath().get(i));
                double delta = q1 * Math.exp(q2 * queryHitCount.getOrDefault(prevNode, 0));
                pheromoneLevels.merge(prevNode, delta, Double::sum);
            }
        }
        // Evaporation
        pheromoneLevels.replaceAll((node, pheromone) -> pheromone * (1 - evaporation));
    }

    private int calculateInitialTTL() {
        return Configuration.getInt("simulation.initialTTL", 2);
    }

    @Override
    public Object clone() {
        return new DLAntProtocol("protocol.dlant");
    }
}
