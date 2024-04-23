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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import peersim.cdsim.CDProtocol;
import peersim.cdsim.CDState;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;

/**
 * 
 * @author Gian Paolo Jesi
 * @edited-by: Abdulaziz Houtari
 */
public abstract class SearchProtocol implements CDProtocol, EDProtocol {

    // ---------------------------------------------------------------------
    // Parameters
    // ---------------------------------------------------------------------

    /**
     * The Linkable enabled protocol to fetch neighbours from.
     * 
     * @config
     */
    public static final String PAR_LINKABLE = "linkable";

    
    /**
     * The transport layer to send messages.
     * 
     * @config
     */
    private static final String PAR_TRANSPORT = "transport";


    /**
     * The messages TTL size
     * 
     * @config
     */
    public static final String PAR_TTL = "ttl";

    /**
     * Parameter for the proliferation factor.
     * 
     * @config
     */
    public static final String PAR_PROLIFERATION = "proliferation";

    /**
     * Parameter to choose which key comparation approach is preferred. Default
     * is OR.
     * 
     * @config
     */
    public static final String PAR_ANDMATCH = "and_keys";

    // ---------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------

    /**
     * Stores each message that a node has seen. It may be cleaned by the an
     * observer considering how long a message can be valid (TTL).
     * 
     */
    public HashMap<Message, Integer> messageTable;

    /**
     * Stores each message that a node has seen. It may be cleaned by the an
     * observer considering how long a message can be valid (TTL).
     */
    public HashSet<Message> hitTable;

    /**
     * Stores the failed queries. It may be cleaned by the an observer
     */
    public HashSet<Message> failedTable;

    /** The local node search key storage */
    protected HashMap<Integer, Integer> keyStorage;

    /** Query distribution data structure; it holds the cycle and a key array. */
    protected TreeMap<Integer, Object> queryDistro;

    /** Counter for thecurrent node extra probing mesages. */
    protected int extraProbeCounter;

    protected int ttl, pid;

    protected boolean andMatch;

    private int linkID;
    private int transportID; 

    public Node whoAmI; // a reference the the protocol own node

    public HashMap<Message, HashMap<Node, Integer>> routingTable = null;

    // ---------------------------------------------------------------------
    // Initialization
    // ---------------------------------------------------------------------

    public SearchProtocol(String prefix) {
        this.extraProbeCounter = 0;

        ttl = Configuration.getInt(prefix + "." + PAR_TTL, 5);

        int match = peersim.config.Configuration.getInt(prefix + "."
                + PAR_ANDMATCH, 0);

        if (match == 1)
            this.andMatch = true;
        else
            this.andMatch = false;

    
        linkID = Configuration.getPid(prefix + "." + PAR_LINKABLE);
        transportID = Configuration.getPid(prefix + "." + PAR_TRANSPORT);
        
        pid = CommonState.getPid();

        messageTable = new HashMap<Message, Integer>();
        hitTable = new HashSet<Message>();
        queryDistro = new TreeMap<Integer, Object>();
        keyStorage = new HashMap<Integer, Integer>();
        routingTable = new HashMap<Message, HashMap<Node, Integer>>();
        failedTable = new HashSet<Message>();
    }

    public Object clone() {
        SearchProtocol sp = null;
        try {
            sp = (SearchProtocol) super.clone();
        } catch (CloneNotSupportedException e) {
        }
        sp.keyStorage = new HashMap<Integer, Integer>();
        sp.messageTable = new HashMap<Message, Integer>();
        sp.hitTable = new HashSet<Message>();
        sp.queryDistro = new TreeMap<Integer, Object>();
        sp.routingTable = new HashMap<Message, HashMap<Node, Integer>>();
        return sp;
    }

    // ---------------------------------------------------------------------
    // Methods
    // ---------------------------------------------------------------------

    // interface CDProtocol:
    public void nextCycle(Node node, int protocolID) {        
        if (whoAmI == null)
            whoAmI = node;
    }

    // interaface EDProtocol:
    public void processEvent(Node node, int pid, Object event) {
        if (whoAmI == null)
            whoAmI = node;

        if (event instanceof Message) {
            Message mes = (Message) event;
            
            Integer actual = (Integer) this.messageTable.get(mes);
            int index = (actual != null ? actual.intValue() + 1 : 1); // increment seen count
            this.messageTable.put(mes, Integer.valueOf(index));
            this.process(mes); // process message: hit or miss, then forward
        } else {
            throw new IllegalArgumentException("Unknown event type");
        }
    }

    /**
     * Send a message to a Node. Used by the query originator. It takes care to
     * increase the message TTL.
     * 
     * @param n
     *            The node to communicate with.
     * @param mes
     *            The message to be fsent.
     */
    public void send(Node n, Message mes) {  
        this.messageTable.put(mes, Integer.valueOf(1));

        copyAndSend(n, mes);

        // store to whom the message is sent and how many copies
        updateRoutingTable(n, mes);

    }

    /**
     * Forwards a message to a node. Used by the nodes along the message path.
     * It takes care to increase the message TTL and stops forwarding if it is
     * too high.
     * 
     * @param n
     *            The node to communicate with.
     * @param mes
     *            The message to be forwarded.
     */
    public void forward(Node n, Message mes) {
        // NOTE: it does not insert the message in the neighbor messageTable
        // because I belive that this operation has to performed by the
        // neighbor itselt; the idea is that a node has "to see" by itself.
        if (mes.ttl == 0) {
            // This is a failed query
            if (mes.hits == 0) { this.failedTable.add(mes); }

            return;
        }

        // clone message and update TTL:
        copyAndSend(n, mes);

        updateRoutingTable(n, mes);
    }

    private void copyAndSend(Node n, Message mes) {
        Message copy = mes.copy();
        
        copy.hops++;
        copy.ttl--;
        copy.addToPath(n);

        Transport tr = (Transport) n.getProtocol(transportID);

        tr.send(whoAmI, n, copy, pid);
    }

    /**
     * Store in the local routingTable the message, the destination and how many
     * time the packet has been sent to to the destination.
     * 
     * @param n
     *            The destination node.
     * @param mes
     *            The message to be sent.
     */
    protected void updateRoutingTable(Node n, Message mes) {
        HashMap<Node, Integer> map = this.routingTable.get(mes);
        if (map == null) { // 1st send of this mes
            HashMap<Node, Integer> entry = new HashMap<Node, Integer>();
            entry.put(n, 1);
            this.routingTable.put(mes, entry);
        } else {// mes already sent to somoeone
            Integer howmany = map.get(n);
            if (howmany == null) { // never sent to node n:
                map.put(n, 1);
            } else { // mes already sent to n:
                int k = howmany.intValue() + 1;
                map.put(n, k);
            }
        }

    }

    /**
     * Selects a random node from the current view. The current Linkable
     * protocol view is used according to the configuration.
     * 
     * @return A random picked node.
     */
    public Node getRandomNeighbor() {
        Node result = null;

        Linkable l = (Linkable) whoAmI.getProtocol(linkID);
        result = l.getNeighbor(CommonState.r.nextInt(l.degree()));
        
        return result;
    }

    /**
     * Select a free node from the current view. It is used by the restricted
     * protocol versions. It always return a random node even if no free node
     * are available.
     * 
     * @param mes The message for which the nodes must be "free".
     * @return A "free" node.
     */
    public Node selectFreeNeighbor(Message mes) {
        ArrayList<Node> tempList;
        
        tempList = new ArrayList<Node>();
        Linkable l = (Linkable) whoAmI.getProtocol(linkID);
        for (int i = 0; i < l.degree(); i++)
            tempList.add(l.getNeighbor(i));
        

        Collections.shuffle(tempList, CommonState.r); // same as random pick
        Node result = null;

        for (int i = 0; i < tempList.size(); i++) {
            this.extraProbeCounter++;
            Node n = (Node) tempList.get(i);
            SearchProtocol sp = (SearchProtocol) n.getProtocol(pid);
            if (!sp.messageTable.containsKey(mes)) {
                result = n;
                break;
            }
        }

        if (result == null) {
            result = (Node) tempList.get(0);
        }
        return result;
    }

    /**
     * It is the equivalent of the passive thread in real setup. It has to
     * manage incoming message requests that are available in the incomingQueue
     * data structure. BTW this method has to deal with only a single message at
     * a time and the basic nextCycle method of CDProtocol will provide the
     * available messages.
     * 
     * @param mes
     *            The message to process.
     */
    public abstract void process(Message mes);

    /**
     * Notify a hit in the current node messageTable. The absolute value
     * indicates how many times the message has been seen and the negative sign
     * indicates a succesful query hit.
     * 
     * @param mes
     *            The message for which it notifies a hit.
     */
    public void notifyOriginator(Message mes) {
        this.hitTable.add(mes);
    }

    /**
     * Performs the actual checks to compare query keys to the protocol own key
     * storage. The returning array may be NULL elements.
     * 
     * @param keys
     *            The array of keys to be checked, it is extracted from the
     *            message.
     * @return A new array of keys: the ones that has matched. It may be null if
     *         no keys has matched.
     */
    protected int[] matches(int[] keys) {
        int[] result = null;
        ArrayList<Integer> temp = new ArrayList<Integer>();
        for (int i = 0; i < keys.length; i++) {
            if (this.keyStorage.containsKey(Integer.valueOf(keys[i]))) {
                temp.add( Integer.valueOf(keys[i]));
            }
        }
        if (temp.size() > 0) {
            result = new int[temp.size()];
            for (int i = 0; i < temp.size(); i++) {
                result[i] = temp.get(i);
            }
        }
        return result;
    }

    /**
     * Performs the actual checks to compare query keys to the protocol own key
     * storage. It returns if a hit has happended according to the actual
     * comparison method (AND or OR).
     * 
     * @param keys
     *            The array of keys to be checked, it is extracted from the
     *            message.
     * @return If a hit has occurred.
     */
    protected boolean match(int[] keys) {
        boolean result = false;
        int[] matchedKeys = this.matches(keys);
        if (matchedKeys != null) {
            // and match
            if (andMatch && matchedKeys.length == keys.length)
                result = true;
            else if (!andMatch)
                result = true;
        }
        return result;
    }

    /**
     * Picks the current node query data distribution to send a message from the
     * local node distribution structure.
     * 
     * @return The set of keys in the query according to the distribution.
     */
    protected int[] pickQueryData() {
        int[] result = null;


        if (queryDistro.isEmpty())
            return null;


        Integer key = (Integer) queryDistro.firstKey();
        
        if (key.intValue() == SearchUtils.getCycle()) {
            result = (int[]) queryDistro.get(key);
            queryDistro.remove(key);
        }

        return result;
    }

    /**
     * Load the current node query data distribution structure.
     * 
     * @param cycle
     *            The cycle in which perform the query.
     * @param keys
     *            The query set.
     */
    public void addQueryData(int cycle, int[] keys) {
        this.queryDistro.put( Integer.valueOf(cycle), (Object) keys);
    }

    /**
     * Sets the node specific keys collection and their own frequency. Should be
     * called by the initializer.
     * 
     * @param entry
     *            A mapping from a key to its frequency.
     */
    public void addKeyStorage(Map<Integer, Integer> entry) {
        this.keyStorage.putAll(entry);
    }

   
    public int getTtl() {
        return ttl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public int getLinkableID() {
        return linkID;
    }
    

}