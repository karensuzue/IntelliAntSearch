package iat.dlantisearch;

import java.util.Random;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import iat.antp2pr.AntMessage;
import iat.antp2pr.PheromoneProtocol;

public class DLAntControl implements Control {

    // Constant string used as key, specifies protocol parameter
    private static final String PAR_PROTO = "protocol";

    // ID of FloodingProtocol
    private final int pid;

    Random random = new Random();

    // Constructor
    public DLAntControl(String prefix) {
        // No FastConfig here
        pid = Configuration.getPid(prefix + "." + PAR_PROTO);
    }

    /**
     * In Cycle-Driven simulations, execute() is invoked once per cycle (WIP)
     * In Event-Driven simulations, execute() is invoked once at the beginning?
     */
    @Override
    public boolean execute() {
        // Trigger the flooding protocol from a random node
        Node randSrc = Network.get(CommonState.r.nextInt(Network.size()));
        // Time to live for messages
        int ttl = 3; 
        
        AntMessage msg = new AntMessage(randSrc, randSrc, PheromoneProtocol.nextRandomResource(), ttl);
        ((DLAntProtocol) randSrc.getProtocol(pid)).forwardAnt(randSrc, pid, msg);

        return false;
    }

}
