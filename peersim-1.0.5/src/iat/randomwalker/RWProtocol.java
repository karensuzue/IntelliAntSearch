package iat.randomwalker;

import iat.search.SMessage;
import iat.search.SearchProtocol;
import peersim.core.CommonState;
import peersim.core.Node;

public class RWProtocol extends SearchProtocol {
    public static final String PAR_WALKERS = "walkers";
    public static final String PAR_ANDMATCH = "andmatch";
    public static final String PAR_TTL = "ttl";

    protected int walkers;

    public RWProtocol(String prefix) {
        this(prefix, null);
    }

    public RWProtocol(String prefix, Object obj) {
        super(prefix, obj);

        int match= peersim.config.Configuration.getInt(prefix+"."+ PAR_ANDMATCH, 0);

        if (match == 1 ) this.andMatch = true;
        else this.andMatch = false;

        this.walkers = peersim.config.Configuration.getInt(prefix+"."+ PAR_WALKERS, 1);
        this.ttl = peersim.config.Configuration.getInt(prefix+"."+ PAR_TTL, 10);
    }

    public void process(SMessage mes, int protocolId) { // "Passive" behaviour implementation
        // checks for hits and notifies originator if any:
        boolean match = this.match(mes.payload);
        if (match) this.notifyOriginator(mes, protocolId);


        // forwards the message to a random neighbor:
        Node neighbor = (Node) this.view.get(CommonState.r.nextInt(degree()));

        this.forward(neighbor, mes, protocolId);
    }

    public void nextCycle(peersim.core.Node node, int protocolID) {
        super.nextCycle(node, protocolID);
        // this will handle incoming messages
        int[] data = this.pickQueryData(); // if we have to produce a query...

        if (data != null) {
            SMessage m = new SMessage(node, SMessage.QRY, 0, data);
            for (int i = 0; i < this.walkers && i < this.view.size() ; i++) {
                this.send((Node) this.view.get(i), m, protocolID);
            }
        }
    }
}
