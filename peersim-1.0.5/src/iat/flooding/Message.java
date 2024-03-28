package iat.flooding;

import peersim.core.Node;

public class Message {
    private final Node source;
    private final Node destination;
    private final String content;
    private int ttl;

    public Message(Node source, Node destination, String content, int ttl) {
        this.source = source;
        this.destination = destination;
        this.content = content;
        this.ttl = ttl;
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

}
