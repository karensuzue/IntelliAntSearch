package iat.antp2pr;

import java.util.Random;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

public class AntP2PRControl implements Control {
    private final int pid;

    public AntP2PRControl(String prefix) {
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

        AntP2PRProtocol protocol = (AntP2PRProtocol) startNode.getProtocol(pid);
        protocol.startAntSearch(startNode, new Random().nextInt(Network.size()), pid); 

        return false;
    }
}
