import java.util.*;

// Represents a node in the network
class Node {
    int id;
    List<Node> neighbors;

    public Node(int id) {
        this.id = id;
        this.neighbors = new ArrayList<>();
    }

    public void addNeighbor(Node neighbor) {
        neighbors.add(neighbor);
    }

    // Simulates sending a message to all neighbors
    public void sendMessage(String message) {
        System.out.println("Node " + id + " sends message: " + message);
        for (Node neighbor : neighbors) {
            neighbor.receiveMessage(message);
        }
    }

    // Simulates receiving a message
    public void receiveMessage(String message) {
        System.out.println("Node " + id + " receives message: " + message);
    }
}

// Represents the network
class Network {
    List<Node> nodes;

    public Network(int size) {
        nodes = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            nodes.add(new Node(i));
        }
    }

    // Connects two nodes in the network
    public void connect(int nodeId1, int nodeId2) {
        Node node1 = nodes.get(nodeId1);
        Node node2 = nodes.get(nodeId2);
        node1.addNeighbor(node2);
        node2.addNeighbor(node1);
    }

    // Initiates flooding from a given node
    public void startFlooding(int startNodeId, String message) {
        Node startNode = nodes.get(startNodeId);
        startNode.sendMessage(message);
    }
}

public class FloodingAlgorithm {
    public static void main(String[] args) {
        // Create a network with 5 nodes
        Network network = new Network(5);
        // Connect nodes in the network
        network.connect(0, 1);
        network.connect(0, 2);
        network.connect(1, 2);
        network.connect(1, 3);
        network.connect(2, 3);
        network.connect(3, 4);

        // Start flooding from node 0 with a message
        network.startFlooding(0, "Hello from node 0!");
    }
}
