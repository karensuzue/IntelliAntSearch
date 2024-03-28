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
        // Create flooding event for chosen node
        FloodingEvent event = new FloodingEvent(randSrc, ttl);
        // Schedule the flooding event to occur with delay intervals, deliver to randSrc
        EDSimulator.add(10, event, randSrc, pid);

        return false;
    }

    private class FloodingEvent implements Control {
        private final Node node;
        private final int ttl;
        public FloodingEvent(Node node, int ttl) {
            this.node = node;
            this.ttl = ttl;
        }

        public boolean execute() {
            // Trigger flooding from the selected node
            Message msg = new Message(node, node, "Hello from " + node.getID(), this.ttl); // Example message with TTL of 5
            ((FloodingProtocol) node.getProtocol(pid)).floodMessage(node, pid, msg);
            return false; // Do not request re-execution
        }
    }
}