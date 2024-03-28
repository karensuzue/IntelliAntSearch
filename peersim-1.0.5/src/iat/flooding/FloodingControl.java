package iat.flooding;


import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;
import peersim.core.Node;
import peersim.core.CommonState;
import peersim.edsim.EDSimulator; 

public class FloodingControl implements Control {

    // Constant string used as key, specifies protocol parameter
    private static final String PAR_PROTO = "protocol";

    // ID of FloodingProtocol
    private final int pid;
    private final int interval;

    // Constructor
    public FloodingControl(String prefix) {
        // No FastConfig here
        pid = Configuration.getPid(prefix + "." + PAR_PROTO);
        // Default interval of 10 time units
        interval = Configuration.getInt(prefix + ".interval", 10); 
    }

    /**
     * Overrides Control's execute()
     * In Cycle-Driven simulations, execute() is invoked once per cycle (WIP)
     * In Event-Driven simulations, execute() is invoked once at the beginning
     */
    public boolean execute() {
        // Trigger the flooding protocol from a random node
        Node randSrc = Network.get(CommonState.r.nextInt(Network.size()));
        // Time to live for messages
        int ttl = 5; 
        // Create a message for flooding
        Message msg = new Message(randSrc, randSrc, "Hello from " + randSrc.getID(), ttl);
        // Deliver the message to the protocol for processing
        ((FloodingProtocol) randSrc.getProtocol(pid)).processEvent(randSrc, pid, msg);
        
        return false;
    }

    
}