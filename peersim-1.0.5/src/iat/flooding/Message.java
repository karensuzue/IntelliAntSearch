package iat.flooding;

import java.util.ArrayList;

public class Message {
    private final int source;
    private final int destination;
    private final String content;
    private int ttl;
    private ArrayList<Integer> path;

    public Message(int source, int destination, String content, int ttl) {
        this.source = source;
        this.destination = destination;
        this.content = content;
        this.ttl = ttl;
        this.path = new ArrayList<Integer>();
    }

    public Message(int source, int destination, String content, int ttl, ArrayList<Integer> path) {
        this.source = source;
        this.destination = destination;
        this.content = content;
        this.ttl = ttl;
        this.path = path;
    }

    public int getSource() {
        return source;
    }

    public int getDestination() {
        return destination;
    }

    public String getContent() {
        return content;
    }

    public Message replicateForSending(int currentNode) {
        path.add(currentNode);

        // System.out.println("Path: " + path);

        return new Message(source, destination, content, ttl - 1, path);
    }

    public boolean hasVisited(int node) {
        return path.contains(node);
    }

    public int getTtl() {
        return ttl;
    }

    public void decreaseTtl() {
        ttl--;
    }

}
