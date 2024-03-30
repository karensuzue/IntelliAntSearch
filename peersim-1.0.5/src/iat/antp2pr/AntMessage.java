package iat.antp2pr;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import peersim.core.Node;

public class AntMessage {
    private final Node source;
    private final Node destination;
    private int content;
    private int ttl;
    private int hitCount; // if hitCount > 1, success
    private List<Node> path; 

    public AntMessage(Node source, Node destination, int content, int ttl) {
        this.source = source;
        this.destination = destination;
        this.content = content;
        this.ttl = ttl;
        this.hitCount = 0;
        this.path = new ArrayList<Node>();
    }

    // ----------------------------------------------------------
    // Source and Destination
    // ----------------------------------------------------------

    // Getter for source
    public Node getSource() {
        return source;
    }
    // do we need a getdestination?
    public Node getDestination() {
        return destination;
    }

    // ----------------------------------------------------------
    // Message Content
    // ----------------------------------------------------------

    // Getter for content
    public int getContent() {
        return content;
    }
    // Setter for content
    public void setContent(int content) {
        this.content = content;
    }

    // ----------------------------------------------------------
    // Time To Live
    // ----------------------------------------------------------

    // Getter for TTL
    public int getTtl() {
        return ttl;
    }
    // Setter for TTL
    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    // ----------------------------------------------------------
    // Hits
    // ----------------------------------------------------------

    // Getter for hitCount
    public int getHitCount() {
        return hitCount;
    }
    // Increment hitCount
    public void incrementHitCount() {
        this.hitCount++;
    }
    // Is it successful?
    public boolean isHit() {
        return hitCount > 0;
    }

    // ----------------------------------------------------------
    // Path Traveled
    // ----------------------------------------------------------

    public void addToPath(Node node) {
        path.add(node);
    }
    public List<Node> getPath() {
        return new LinkedList<>(path);
    }

    public boolean inPath(Node node) {
        return path.contains(node);
    }

    // ----------------------------------------------------------
    // Other
    // ----------------------------------------------------------

    // Method to replicate this ant for sending to another node, with deep copy of path
    public AntMessage replicateForForwarding() {
        AntMessage replicatedAnt = new AntMessage(this.source, this.destination, this.content, this.ttl - 1);
        replicatedAnt.hitCount = this.hitCount;
        replicatedAnt.path = new ArrayList<>(path);// Deep copy of path
        return replicatedAnt;
    }

    @Override
    public String toString() {
        return "AntMessage{" +
                "source=" + source +
                ", content='" + content + '\'' +
                ", ttl=" + ttl +
                ", hitCount=" + hitCount +
                ", path=" + path +
                '}';
    }
}
