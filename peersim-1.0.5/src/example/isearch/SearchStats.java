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

package example.isearch;

import peersim.core.Network;

/**
 * A class of this type is instantiated for each time a dinstinct packet is
 * discovered. It stores statistics for each packet.
 * 
 */
public class SearchStats {

    // ---------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------

    /** Number of dinstinct nodes having seen the query */
    private int nbSeen;

    /** Number of hits */
    private int nbHits;

    /** Number of messages sent on behalf of this query */
    public int nbMessages;

    /** Number of extra probes sent on behalf of this query */
    // private int nbExtraProbes;
    /** Sequence number of the message */
    private int seq;

    /** Age of this query */
    public int age;

    /** Time to live of this query */
    public int ttl;

    public double hitRate;

    public double networkLoad;

    public double missedRate;

    // ---------------------------------------------------------------------
    // Initialization
    // ---------------------------------------------------------------------

    public SearchStats(int seq, int age, int ttl) {
        this.nbSeen = 0;
        this.nbHits = 0;
        this.hitRate = 0.0;
        this.networkLoad = 0.0;
        this.missedRate = 0.0;
        this.nbMessages = 0;
        this.seq = seq;
        this.age = age;
        this.ttl = ttl;
    }

    // ---------------------------------------------------------------------
    // Methods
    // ---------------------------------------------------------------------

    public void update(int msgs, int hits) {
        ++nbSeen;
        nbHits += hits;
        nbMessages += msgs;
        
        // Update the hit rate
        this.hitRate = (double) nbHits / (double) nbSeen;

        // Update the network load
        this.networkLoad = (double) nbSeen / (double) Network.size();

        this.missedRate = nbMessages - nbHits;

    }

    public String toString() {
        return "Seq: " + seq + " Age: " + age + " #Seen: " + nbSeen + " #Hits: " + nbHits + " #Messages: " + nbMessages + " HitRate: " + hitRate + " NetworkLoad: " + networkLoad;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public int getNbSeen() {
        return nbSeen;
    }
}
