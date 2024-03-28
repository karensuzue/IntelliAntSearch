package iat.flooding;

import java.util.ArrayList;

import peersim.cdsim.CDProtocol;
import peersim.config.FastConfig;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;
import peersim.vector.SingleValueHolder;

public class FloodingProtocol extends SingleValueHolder implements CDProtocol {
    private ArrayList<Object> incomingQueue = new ArrayList<>();

    public FloodingProtocol(String prefix) {
        super(prefix);
    }

    public Object clone() {
        return super.clone();
    }

    public void nextCycle(Node node, int protocolID) {
        // TODO Auto-generated method stub
        
    }



    public void floodMessage(Node node, int pid, Message msg) {
        if (msg.getTtl() <= 0) {
            return;
        }

        msg.decreaseTtl();

        for (int i = 0; i < Network.size(); i++) {
            Node peer = Network.get(i);
            
            if (peer != null && peer.getID() != node.getID()) {
                Transport tr = (Transport) peer.getProtocol(FastConfig.getTransport(pid));

                tr.send(node, peer, msg, pid); // Pass transportId instead of the protocol instance
            }
        }
    }

    public void processEvent(Node node, int pid, Object event) {
        // Process received message
        // Flood the message to all neighbors
        // floodMessage(node, pid, event);
        if (event instanceof Message) {
            Message msg = (Message) event;
            
            System.out.println("Node " + node.getID() + " received message: " + msg.getContent());

            if (msg.getTtl() > 0) {
                floodMessage(node, pid, msg);
            }
        }
    }
}