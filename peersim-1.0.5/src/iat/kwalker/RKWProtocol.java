package iat.kwalker;

import iat.search.Message;
import iat.search.SearchProtocol;
import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;

public class RKWProtocol extends SearchProtocol {
    // ---------------------------------------------------------------------
    // Parameters
    // ---------------------------------------------------------------------

    /**
     * Parameter for the number of walkers at the query initiation. It must be <
     * then view size. Default is 1.
     */
    public static final String PAR_WALKERS = "walkers";

    // ---------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------

    protected int walkers;

    /** Creates a new instance of RWProtocol */
    public RKWProtocol(String prefix) {
        super(prefix);
        walkers = Configuration.getInt(prefix + "." + PAR_WALKERS, 1);
    }

    // "Passive" behaviour implementation: process key similarity and notifies
    // any match and forwards messages.
    public void process(Message mes) {
        // checks for hits and notifies originator if any:
        boolean match = this.match(mes.payload);

        if (match)
            this.notifyOriginator(mes);

        // forwards the message to a random neighbor:
        Node neighbor = this.getRandomNeighbor();
        
        forward(neighbor, mes);
    }

    @Override
    public void nextCycle(Node node, int protocolID) {
        super.nextCycle(node, protocolID);

        int[] data = this.pickQueryData(); // if we have to produce a query...

        // System.out.println("RKWProtocol.nextCycle" + data + " " + CommonState.getTime());

        if (data != null) {
            Message m = new Message(node, Message.QRY, 0, data, ttl);

            Linkable linkable = (Linkable) node.getProtocol(getLinkableID());
            
            // produces the specified number of walkers:
            for (int i = 0; i < this.walkers && i < linkable.degree(); i++) {
                send(linkable.getNeighbor(i), m);
            }
        }
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {
        // System.out.println("RKWProtocol.processEvent" + event.toString() + " " + CommonState.getTime());

        super.processEvent(node, pid, event);
    }
}
