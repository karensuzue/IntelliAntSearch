package iat.flooding;

import peersim.core.Node;
import java.util.ArrayList;

public class Message {
    private final Node source;
    private final Node destination;
    private final String content;
    private int ttl;
    private ArrayList<Node> visited;

    public Message(Node source, Node destination, String content, int ttl) {
        this.source = source;
        this.destination = destination;
        this.content = content;
        this.ttl = ttl;
        this.visited = new ArrayList<Node>();
    }

    public Node getSource() {
        return source;
    }

    public Node getDestination() {
        return destination;
    }

    public String getContent() {
        return content;
    }

    public int getTtl() {
        return ttl;
    }

    public void decreaseTtl() {
        ttl--;
    }

    public boolean hasVisited(Node node) {
        return visited.contains(node);
    }

    public void updateVisited(Node node) {
        visited.add(node);
    }

}
