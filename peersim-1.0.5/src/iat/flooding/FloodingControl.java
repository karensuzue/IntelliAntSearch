package iat.flooding;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.core.CommonState;

public class FloodingControl implements Control {

    // Constant string used as key, specifies protocol parameter
    private static final String PAR_PROTO = "protocol";

    // ID of FloodingProtocol
    private final int pid;
    
    // Constructor
    public FloodingControl(String prefix) {
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
        
        Message msg = new Message(randSrc, randSrc, "Hello from " + randSrc.getID(), ttl);
        ((FloodingProtocol) randSrc.getProtocol(pid)).floodMessage(randSrc, pid, msg);
        // Schedule the flooding event to occur
        // EDSimulator.add(interval, msg, randSrc, pid);
        return false;
    }
}