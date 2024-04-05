/*
 * Copyright (c) 2003-2005 The BISON Project
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License version 2 as
 * published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 */

package iat.search;

import java.util.ArrayList;
import java.util.List;

import peersim.cdsim.CDState;
import peersim.core.CommonState;
import peersim.core.Node;
import peersim.edsim.EDSimulator;

/**
 * 
 * @author Gian Paolo Jesi
 */
public class Message implements Cloneable {

    // ---------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------

    public static final int QRY = 0;

    public static final int FWD = 1;

    public static final int HIT = 2;

    private static int seq_generator = 0;

    public int hops, type, seq, start, ttl, hits;

    public Node originator; // the query producer

    public int[] payload; // an array of keys

    public List<Node> path; // the path followed by the message

    // ---------------------------------------------------------------------
    // Initialization
    // ---------------------------------------------------------------------

    public Message(Node originator, int type, int hops, int[] payload, int ttl) {
        this.originator = originator;
        this.path = new ArrayList<>();
        this.type = type;
        this.hops = hops;
        this.ttl = ttl;
        this.payload = payload;
        this.seq = ++seq_generator;
        this.start = SearchUtils.getCycle();
        this.hits = 0;
        this.path.add(originator);
    }

    public Object clone() throws CloneNotSupportedException {
        Message msg = (Message) super.clone();

        msg.path = new ArrayList<>(path);
        msg.hits = hits;

        return msg;
    }

    public Message copy() {
        try {
            return (Message) this.clone();
        } catch (CloneNotSupportedException e) {
            Message msg =  new Message(originator, type, hops, payload, ttl);

            msg.path = new ArrayList<>(path);

            return msg;
        }
    }

    // ---------------------------------------------------------------------
    // Methods
    // ---------------------------------------------------------------------

    public void addToPath(Node node) {
        path.add(node);
    }

    public boolean hasVisited(Node node) {
        return path.contains(node);
    }

    public int hashCode() {
        return seq;
    }

    public boolean equals(Object obj) {
        return (obj instanceof Message) && (((Message) obj).seq == this.seq);
    }

    public String toString() {
        return "SMessage[" + seq + "] hops=" + hops;
    }
}
