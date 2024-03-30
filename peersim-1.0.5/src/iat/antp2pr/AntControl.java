package iat.antp2pr;

import peersim.config.Configuration;
import peersim.core.CommonState;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;

public class AntControl implements Control {
    
    // Constant string used as key, specifies protocol parameter
    private static final String PAR_PROTO = "protocol";

    // ID of FloodingProtocol
    private final int pid;
    
    // Constructor
    public AntControl(String prefix) {
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
        int ttl = 5; 
        
        AntMessage msg = new AntMessage(randSrc, randSrc, 6, ttl);
        ((AntProtocol) randSrc.getProtocol(pid)).forwardAnt(randSrc, pid, msg);

        return false; // keeps going until time is over
    }  
}