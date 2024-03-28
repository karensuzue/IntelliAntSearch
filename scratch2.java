import java.util.*;

// Represents a node in the network
class Node {
    int id;
    List<Node> neighbors;
    List<String> availablePackages;

    public Node(int id) {
        this.id = id;
        this.neighbors = new ArrayList<>();
        this.availablePackages = new ArrayList<>();
    }

    public void addNeighbor(Node neighbor) {
        neighbors.add(neighbor);
    }

    public void addPackage(String packageName) {
        availablePackages.add(packageName);
    }

    // Simulates sending a query for a package to all neighbors
    public void sendQuery(String packageName) {
        System.out.println("Node " + id + " sends query for package: " + packageName);
        for (Node neighbor : neighbors) {
            neighbor.respondToQuery(packageName);
        }
    }

    // Simulates responding to a query for a package
    public void respondToQuery(String packageName) {
        if (availablePackages.contains(packageName)) {
            System.out.println("Node " + id + " responds with package: " + packageName);
        } else {
            System.out.println("Node " + id + " does not have package: " + packageName);
        }
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

    // Adds a package to a node
    public void addPackage(int nodeId, String packageName) {
        Node node = nodes.get(nodeId);
        node.addPackage(packageName);
    }

    // Initiates querying for a package from a given node
    public void startPackageQuery(int startNodeId, String packageName) {
        Node startNode = nodes.get(startNodeId);
        startNode.sendQuery(packageName);
    }
}

public class PackageQuerySimulation {
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

        // Add packages to nodes
        network.addPackage(0, "PackageA");
        network.addPackage(1, "PackageB");
        network.addPackage(2, "PackageC");
        network.addPackage(3, "PackageD");
        network.addPackage(4, "PackageE");

        // Start package queries from node 0
        network.startPackageQuery(0, "PackageC");
        network.startPackageQuery(0, "PackageF"); // Querying for a non-existent package
    }
}
