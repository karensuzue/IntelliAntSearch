import java.util.ArrayList;
import java.util.List;

public class AntMessage {
    private final int source;
    private String query; // The query or the object being searched for
    private double pheromoneLevel;
    private int ttl; // Time to live for the ant
    private int hitCount; // Number of successful responses
    private List<Integer> routeHistory; // List of node IDs visited

    public AntMessage(int source, String query, double initialPheromone, int ttl) {
        this.source = source;
        this.query = query;
        this.pheromoneLevel = initialPheromone;
        this.ttl = ttl;
        this.hitCount = 0;
        this.routeHistory = new ArrayList<>();
    }

    // Getter for source
    public int getSource() {
        return source;
    }

    // Getter and Setter for query
    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
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

    public void decreaseTtl() {
        this.ttl--;
    }

    // Getter and Setter for hitCount
    public int getHitCount() {
        return hitCount;
    }

    public void incrementHitCount() {
        this.hitCount++;
    }

    // Methods for routeHistory
    public void addToRouteHistory(int nodeId) {
        routeHistory.add(nodeId);
    }

    public List<Integer> getRouteHistory() {
        return new ArrayList<>(routeHistory);
    }

    // Method to replicate this ant for sending to another node, with deep copy of routeHistory
    public AntMessage replicateForForwarding() {
        AntMessage replicatedAnt = new AntMessage(this.source, this.query, this.pheromoneLevel, this.ttl);
        replicatedAnt.hitCount = this.hitCount;
        replicatedAnt.routeHistory = new ArrayList<>(this.routeHistory); // Deep copy of route history
        return replicatedAnt;
    }

    @Override
    public String toString() {
        return "AntMessage{" +
                "source=" + source +
                ", query='" + query + '\'' +
                ", pheromoneLevel=" + pheromoneLevel +
                ", ttl=" + ttl +
                ", hitCount=" + hitCount +
                ", routeHistory=" + routeHistory +
                '}';
    }
}
