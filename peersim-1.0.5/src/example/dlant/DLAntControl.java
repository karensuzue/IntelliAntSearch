package example.dlant;
import peersim.core.Node;

import java.util.Random;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.CommonState; // Import CommonState

public class DLAntControl implements Control {

    private final int pid;

    public DLAntControl(String prefix) {
        pid = Configuration.getPid(prefix + ".protocol");
    }

    
    public boolean execute() {
        int size = Network.size();

        if (size == 0) {
            return true;
        }
        

        int startIndex = CommonState.r.nextInt(size);
        Node startNode = Network.get(startIndex);

        System.out.println("Starting search from node " + startNode.getIndex());

        DLAntProtocol protocol = (DLAntProtocol) startNode.getProtocol(pid);
        protocol.startAntSearch(startNode, new Random().nextInt(1, Network.size()), pid); 

        return false;
    }
}
