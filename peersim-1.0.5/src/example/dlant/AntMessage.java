package example.dlant;

import java.util.LinkedList;
import java.util.List;

public class AntMessage {
    private final int source;
    private String content;
    private double pheromoneLevel;
    private int ttl;
    private int hitCount;
    private List<Integer> path; 

    public AntMessage(int source, String content, double initialPheromone, int ttl) {
        this.source = source;
        this.content = content;
        this.pheromoneLevel = initialPheromone;
        this.ttl = ttl;
        this.hitCount = 0;
        this.path = new LinkedList<>();
    }

    // Getter for source
    public int getSource() {
        return source;
    }

    // Getter and Setter for content
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    // Getter and Setter for pheromoneLevel
    public double getPheromoneLevel() {
        return pheromoneLevel;
    }

    public void setPheromoneLevel(double pheromoneLevel) {
        this.pheromoneLevel = pheromoneLevel;
    }

    // Getter and Setter for TTL
    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    // Getter and Setter for hitCount
    public int getHitCount() {
        return hitCount;
    }

    public void incrementHitCount() {
        this.hitCount++;
    }

    // Method for path (formerly routeHistory)
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
        AntMessage replicatedAnt = new AntMessage(this.source, this.content, this.pheromoneLevel, this.ttl - 1);
        replicatedAnt.hitCount = this.hitCount;
        replicatedAnt.path = new LinkedList<>(this.path); // Deep copy of path
        return replicatedAnt;
    }

    @Override
    public String toString() {
        return "AntMessage{" +
                "source=" + source +
                ", content='" + content + '\'' +
                ", pheromoneLevel=" + pheromoneLevel +
                ", ttl=" + ttl +
                ", hitCount=" + hitCount +
                ", path=" + path +
                '}';
    }
}