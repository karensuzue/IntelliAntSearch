package example.dlant;

import peersim.core.Node;
import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.CommonState;

public class DLAntControl implements Control {

    private final int pid;

    public DLAntControl(String prefix) {
        pid = Configuration.getPid(prefix + ".protocol");
    }

    @Override
    public boolean execute() {
        int size = Network.size();

        if (size == 0) {
            System.out.println("Network is empty, skipping control execution.");
            return true; // Simulation should stop if there are no nodes
        }

        int startIndex = CommonState.r.nextInt(size);
        Node startNode = Network.get(startIndex);

        System.out.println("Starting ant search from node " + startIndex);

        DLAntProtocol protocol = (DLAntProtocol) startNode.getProtocol(pid);
        if (protocol != null) {
            protocol.startAntSearch(startNode, "resource", pid);
        } else {
            System.out.println("DLAntProtocol not found on node " + startIndex + ", skipping.");
        }

        return false; // Return false to indicate that the simulation should not stop
    }
}
