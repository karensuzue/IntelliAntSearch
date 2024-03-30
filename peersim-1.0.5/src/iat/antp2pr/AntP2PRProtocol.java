package iat.antp2pr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

import example.dlant.AntMessage;
import peersim.config.Configuration;
import peersim.core.IdleProtocol;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.edsim.EDSimulator;
import peersim.transport.Transport;

public class AntP2PRProtocol extends IdleProtocol implements EDProtocol {
     // Constants
    private static final String PAR_TRANSPORT = ".transport";
    private static final String PAR_ALPHA = ".alpha";

    private String prefix;

    // Pheromone-related parameters
    private final double alpha; // Importance of the pheromone trail
    private final int transportPid; // Transport layer ID


    private final Map<Long, Double> pherTables;
    private final Map<Long, Integer> queryHitCounts;
    private final List<Integer> resources;

    

    // Constructor that initializes the protocol's parameters
    public AntP2PRProtocol(String prefix) {
        super(prefix);

        this.prefix = prefix;

        this.transportPid = Configuration.getPid(prefix + PAR_TRANSPORT);
        this.alpha = Configuration.getDouble(prefix + PAR_ALPHA, 1.0);

        this.pherTables = new HashMap<>();
        this.queryHitCounts = new HashMap<>();
        this.resources = new ArrayList<>();

        Random rand = new Random();

        int numResources = rand.nextInt(3);

        for (int i = 0; i < numResources; i++) {
            int resource = rand.nextInt(Network.size());

            if (!resources.contains(resource)) {
                resources.add(rand.nextInt(Network.size()));
            }
        }
    }

    public boolean addNeighbor(Node n) {
        this.pherTables.put(n.getID(), 1.0);
        this.queryHitCounts.put(n.getID(), 0);

        return super.addNeighbor(n);
    }

    public boolean hasResource(int resource) {
        return resources.contains(resource);
    }

    public void startAntSearch(Node startNode, int objectToSearch, int pid) {
        AntP2PRMessage msg = new AntP2PRMessage(startNode.getIndex(), objectToSearch, alpha, calculateInitialTTL());

        EDSimulator.add(0, msg, startNode, pid);
    }

    public void processEvent(Node node, int pid, Object event) {
        if (event instanceof AntP2PRMessage) {
            AntP2PRMessage msg = (AntP2PRMessage) event;

            // Skip if the node has already been visited
            if (msg.getPath().contains(node.getIndex())) {
                return;
            }


            msg.addToPath(node.getIndex());
            
            System.out.println(msg);

            // Check if this node has the resource
            if (this.hasResource(msg.getContent())) {
                msg.incrementHitCount();

                // Add to the query hit count
                queryHitCounts.merge(node.getID(), 1, (oldValue, newValue) -> oldValue + newValue);

                // Update pheromone levels
                update();
            }

            // Forward the ant to neighbors based on the pheromone level
            forward(msg, node, pid);
        }
    }

    private void update() {
        if (this.degree() > 0) {
            IntStream.range(0, this.degree()).forEach(i -> {
                Node neighbor = this.getNeighbor(i);

                double pherValue = pherTables.getOrDefault(neighbor.getID(), 1.0);
                double qh = queryHitCounts.getOrDefault(neighbor.getID(), 0);

                double q1 = 60;
                double q2 = -0.075;

                double delta = q1 * Math.exp(q2 * qh);

                pherTables.put(neighbor.getID(), pherValue + delta);
            });

            int sum = pherTables.values().stream().mapToInt(Double::intValue).sum();

            // Normalize pheromone levels
            pherTables.forEach((key, value) -> pherTables.put(key, value / sum));
        }
    }

    private void forward(AntP2PRMessage msg, Node currentNode, int pid) {
        if (this.degree() > 0) {
            IntStream.range(0, this.degree()).forEach(i -> {
                Node neighbor = this.getNeighbor(i);
                AntP2PRMessage newMsg = msg.replicateForForwarding();

                double pherValue = pherTables.getOrDefault(neighbor.getID(), 0.0);

                System.out.println("Current (" + currentNode.getID() + ")  Pheromone value: " + pherValue + " for node " + neighbor.getID());
                System.out.println(pherTables);
                
                double lowerBound = 0.4;
                double upperBound = 0.8;

                if (pherValue < lowerBound) {
                    newMsg.setTtl(newMsg.getTtl() - 1);
                } else if (pherValue > upperBound) {
                    newMsg.setTtl(newMsg.getTtl() + 1);
                }

                if (newMsg.getTtl() > 0) {
                    Transport transport = (Transport) currentNode.getProtocol(transportPid);

                    transport.send(currentNode, neighbor, newMsg, pid);
                }
            });
        }
    }

    private int calculateInitialTTL() {
        return Configuration.getInt("simulation.initialTTL", 2);
    }

    public Object clone() {
        return new AntP2PRProtocol(prefix);
    }
}
