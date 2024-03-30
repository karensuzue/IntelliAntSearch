package iat.antp2pr;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class AntMessage {
    private final int source;
    private int content;
    private int ttl;
    private int hitCount; // if hitCount > 1, success
    private List<Integer> path; 

    public AntMessage(int source, int content, int ttl) {
        this.source = source;
        this.content = content;
        this.ttl = ttl;
        this.hitCount = 0;
        this.path = new ArrayList<Integer>();
    }

    // Getter for source
    public int getSource() {
        return source;
    }

    // Getter for content
    public int getContent() {
        return content;
    }
    
    // Setter for content
    public void setContent(int content) {
        this.content = content;
    }

    // Getter for TTL
    public int getTtl() {
        return ttl;
    }

    // Setter for TTL
    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    // Getter for hitCount
    public int getHitCount() {
        return hitCount;
    }
    // Increment hitCount
    public void incrementHitCount() {
        this.hitCount++;
    }

    // Method for path
    public void addToPath(int nodeId) {
        path.add(nodeId);
    }

    public List<Integer> getPath() {
        return new LinkedList<>(path);
    }

    // Define the isHit method
    public boolean isHit() {
        return hitCount > 0;
    }

    // Define the getPreviousNodeIndex method
    public Integer getPreviousNodeIndex() {
        if (path.isEmpty()) {
            return null;
        } else {
            return path.get(path.size() - 1);
        }
    }

    // Method to replicate this ant for sending to another node, with deep copy of path
    public AntMessage replicateForForwarding() {
        AntMessage replicatedAnt = new AntMessage(this.source, this.content, this.ttl - 1);
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
