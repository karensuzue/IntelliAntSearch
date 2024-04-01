package example.isearch;

import peersim.cdsim.CDSimulator;
import peersim.cdsim.CDState;
import peersim.core.CommonState;
import peersim.core.Node;

public class FloodProtocol extends SearchProtocol {
    public FloodProtocol(String prefix) {
        super(prefix);
    }

    public void process(SMessage mes) {
        boolean match = this.match(mes.payload);
        if (match)
            this.notifyOriginator(mes);

        for (int i = 0; i < CommonState.r.nextInt(this.degree()) + 1; i++) {
            Node n = this.getRNDNeighbor();

            if (mes.originator == n || mes.hasVisited(n)) continue;

            this.forward(n, mes);
        }
    }

    public void nextCycle(Node node, int protocolID) { 
        super.nextCycle(node, protocolID);
        
        int[] data = this.pickQueryData();

        if (data != null) {
            SMessage m = new SMessage(node, SMessage.QRY, 0, data, ttl);

            for (int i = 0; i < this.degree(); i++) {
                this.send((Node) this.getNeighbor(i), m);
            }
        }
        
    }
}
