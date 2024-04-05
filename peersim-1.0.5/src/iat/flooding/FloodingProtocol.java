package iat.flooding;

import iat.search.Message;
import iat.search.SearchProtocol;
import peersim.core.CommonState;
import peersim.core.Linkable;
import peersim.core.Node;

public class FloodingProtocol extends SearchProtocol {
    public FloodingProtocol(String prefix) {
        super(prefix);
    }

    public void process(Message mes) {
        boolean match = this.match(mes.payload);

        if (match)
            this.notifyOriginator(mes);

        Linkable linkable = (Linkable) whoAmI.getProtocol(getLinkableID());

        int nNeighbors = CommonState.r.nextInt(linkable.degree() - 1) + 1;

        for (int i = 0; i < nNeighbors; i++) {
            Node n = (Node) linkable.getNeighbor(i);

            if (mes.originator == n || mes.hasVisited(n)) continue;

            this.forward(n, mes);
        }
    }

    public void nextCycle(Node node, int protocolID) { 
        super.nextCycle(node, protocolID);
        
        int[] data = this.pickQueryData();

        if (data != null) {
            Message m = new Message(node, Message.QRY, 0, data, ttl);

            Linkable linkable = (Linkable) node.getProtocol(getLinkableID());

            for (int i = 0; i < linkable.degree(); i++) {
                this.send((Node) linkable.getNeighbor(i), m);
            }
        }
    }
}
