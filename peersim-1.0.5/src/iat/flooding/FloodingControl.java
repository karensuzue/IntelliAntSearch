package example.antsearch;


import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.core.CommonState;

public class FloodingControl implements Control {

    private static final String PAR_PROTO = "protocol";

    private final int pid;

    public FloodingControl(String prefix) {
        pid = Configuration.getPid(prefix + "." + PAR_PROTO);
    }

    public boolean execute() {
        // Trigger the flooding protocol from a random node
        Node randomNode = Network.get(CommonState.r.nextInt(Network.size()));
        int ttl = 10; // Time to live for messages

        Message msg = new Message(randomNode.getIndex(), randomNode.getIndex(), "Hello from " + randomNode.getID(), ttl);

        ((FloodingProtocol) randomNode.getProtocol(pid)).floodMessage(randomNode, pid, msg);
        return false;
    }
}