package example.dlant;
import peersim.core.Node;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.CommonState; // Import CommonState

public class DLAntControl implements Control {

    private final int pid;

    public DLAntControl(String prefix) {
        pid = Configuration.getPid(prefix + ".protocol");
    }

    @Override
    public boolean execute() {
        int size = Network.size();

        if (size == 0) {
            return true;
        }

        int startIndex = CommonState.r.nextInt(size);
        Node startNode = Network.get(startIndex);

        DLAntProtocol protocol = (DLAntProtocol) startNode.getProtocol(pid);
        protocol.startAntSearch(startNode, "resource", pid); 

        return false;
    }
}
