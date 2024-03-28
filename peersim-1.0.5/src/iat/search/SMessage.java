package iat.search;

import peersim.core.Node;

public class SMessage implements Cloneable {
    public static final int QRY = 0;
    public static final int FWD = 1;
    public static final int HIT = 2;

    private static int seq_generator = 0;

    public int hops, type, seq, start;

    public Node originator; // the query producer

    public int[] payload; // an array of keys

    public SMessage(Node originator, int type, int hops, int[] payload) {
        this.originator = originator;
        this.type = type;
        this.payload = payload;
        this.seq = seq_generator++;
        this.hops = hops;
    }

    public Object clone() throws CloneNotSupportedException {
        SMessage m = (SMessage) super.clone();
        
        return m;
    }

    public int hashCode() {
        return seq;
    }
    
    public boolean equals(Object obj) {
        return (obj instanceof SMessage) && (((SMessage) obj).

        seq == this.seq);
    }
}
