package example.isearch;

import peersim.core.Node;

public class FloodProtocol extends SearchProtocol {
    public FloodProtocol(String prefix) {
        super(prefix);
    }

    public void process(SMessage mes) {
        boolean match = this.match(mes.payload);
        if (match)
            this.notifyOriginator(mes);

        // forwards the message to a random neighbor:
        Node neighbor = this.getRNDNeighbor();

        this.forward(neighbor, mes);
    }

    public void nextCycle(Node node, int protocolID) {
        super.nextCycle(node, protocolID);

        int[] data = this.pickQueryData();
        if (data != null) {
            SMessage m = new SMessage(node, SMessage.QRY, 0, data);

            for (int i = 0; i < this.degree(); i++) {
                this.send((Node) this.getNeighbor(i), m);
            }
        }
    }
}
