package iat.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import peersim.cdsim.CDProtocol;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;

public abstract class SearchProtocol implements CDProtocol, Linkable {

    public Hashtable<Object, Integer> messageTable;
    public HashSet<Object> hitTable;
    public ArrayList<Object> incomingQueue;
    public ArrayList<Node> view;
    public HashMap<Integer, Integer> keyStorage;
    public HashMap<Integer, Object> queryDistro;

    protected boolean andMatch = false;
    protected int ttl = 10;

    public SearchProtocol(String prefix, Object obj) {
        this.messageTable = new Hashtable<Object, Integer>();
        this.hitTable = new HashSet<Object>();
        this.incomingQueue = new ArrayList<Object>();
        this.view = new ArrayList<Node>();
        this.keyStorage = new HashMap<Integer, Integer>();
        this.queryDistro = new HashMap<Integer, Object>();
    }


    public void nextCycle(Node node, int protocolID) {
        int currentTime = CommonState.getIntTime();

        Iterator<Object> iter = incomingQueue.iterator();

        while (iter.hasNext()) {
            SMessage msg = (SMessage) iter.next();

            if (msg.hops == (currentTime - msg.start + 1)) {
                continue;
            }

            Integer actual = (Integer) this.messageTable.get(msg);

            int index = (actual != null ? actual.intValue() + 1 : 1);

            this.messageTable.put(msg,  Integer.valueOf(index));

            this.process(msg, protocolID);

            iter.remove();
        }    
    }

    public abstract void process(SMessage msg, int protocolID);

    public void send(Node n, SMessage mes, int pid) {
        try {
            SMessage copy = (SMessage) mes.clone();
            copy.hops++;
            this.messageTable.put(mes, Integer.valueOf(1));
            SearchProtocol sp = (SearchProtocol) n.getProtocol(pid);
            sp.incomingQueue.add(copy);
        } catch (CloneNotSupportedException ex) {
            ex.printStackTrace();
        }
    }

    public void forward(Node n, SMessage mes, int pid) {
        if (mes.hops < ttl) {
            try {
                SMessage copy = (SMessage) mes.clone();

                copy.hops++;

                SearchProtocol sp = (SearchProtocol) n.getProtocol(pid);

                copy.type = SMessage.FWD; // sets FWD type

                sp.incomingQueue.add(copy);
            } catch (CloneNotSupportedException cnse) {
                System.out.println("Troubles with message cloning...!");
            }
        }
    }

    public void addQueryData(int cycle, int[] keys) {
        this.queryDistro.put(Integer.valueOf(cycle), (Object) keys);
    }
    
    public void addKeyStorage(Map entry) {
        this.keyStorage.putAll(entry);
    }

    public void notifyOriginator(SMessage mes, int pid) {
        Node originator = mes.originator;
        SearchProtocol sp = (SearchProtocol) originator.getProtocol(pid);
        sp.hitTable.add(mes);
    }

    public int[] pickQueryData() {
        int[] result = null;

        int currentTime = CommonState.getIntTime();

        Integer cycle = Integer.valueOf(currentTime);

        if (this.queryDistro.containsKey(cycle)) {
            result = (int[]) this.queryDistro.get(cycle);
        }

        return result;
    }

    public int degree() {
        return this.view.size();
    }

    public Node getNeighbor(int i) {
        return (Node) this.view.get(i);
    }

    public boolean addNeighbor(Node neighbor) {
        if (!this.view.contains(neighbor)) {
            this.view.add(neighbor);
            return true;
        }

        return false;
    }

    public boolean contains(Node neighbor) {
        return this.view.contains(neighbor);
    }

    public void pack() {
        // do nothing
    }

    public void onKill() {
        this.messageTable.clear();
        this.hitTable.clear();
        this.incomingQueue.clear();
        this.view.clear();
        this.keyStorage.clear();
        this.queryDistro.clear();
    }

    protected int[] matches(int[] keys) {
        int[] result = null;

        ArrayList<Integer> temp = new ArrayList<>();

        for (int i = 0; i < keys.length; i++) {
            if (this.keyStorage.containsKey(Integer.valueOf(keys[i]))) {
                temp.add(Integer.valueOf(keys[i]));
            }
        }

        if (temp.size() > 0) {
            result = new int[temp.size()];
            for (int i = 0; i < temp.size(); i++) {
                result[i] = ((Integer) temp.get(i)).intValue();
            }
        }

        return result;
    }

    protected boolean match(int keys[]) {
        int[] result = this.matches(keys);

        if (result != null) {
            return true;
        }

        return false;
    }

    

    public Object clone() {
        SearchProtocol copy = null;

        try { copy=(SearchProtocol)super.clone(); }
        catch( CloneNotSupportedException e ) {} // never happens

        copy.messageTable = (Hashtable<Object, Integer>) this.messageTable.clone();
        copy.hitTable = (HashSet<Object>) this.hitTable.clone();
        copy.incomingQueue = (ArrayList<Object>) this.incomingQueue.clone();
        copy.view = (ArrayList<Node>) this.view.clone();
        copy.keyStorage = (HashMap<Integer, Integer>) this.keyStorage.clone();
        copy.queryDistro = (HashMap<Integer, Object>) this.queryDistro.clone();

        return copy;
    }
}