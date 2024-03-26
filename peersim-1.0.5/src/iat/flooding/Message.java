package iat.flooding;

public class Message {
    private final int source;
    private final int destination;
    private final String content;
    private int ttl;

    public Message(int source, int destination, String content, int ttl) {
        this.source = source;
        this.destination = destination;
        this.content = content;
        this.ttl = ttl;
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

    public int getTtl() {
        return ttl;
    }

    public void decreaseTtl() {
        ttl--;
    }

}
