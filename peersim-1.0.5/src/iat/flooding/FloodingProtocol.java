package iat.flooding;

import java.util.ArrayList;

import peersim.cdsim.CDProtocol;
import peersim.config.FastConfig;
import peersim.core.Linkable;
import peersim.core.Network;
import peersim.core.Node;
import peersim.edsim.EDProtocol;
import peersim.transport.Transport;
import peersim.vector.SingleValueHolder;

public class FloodingProtocol extends SingleValueHolder implements EDProtocol {
    public FloodingProtocol(String prefix) {
        super(prefix);
    }

    public void floodMessage(Node node, int pid, Message msg) {
        if (msg.getTtl() <= 0 || msg.hasVisited((int)node.getID())) {
            return;
        }

        Message newMsg = msg.replicateForSending(((int)node.getID()));

        Linkable linkable = (Linkable) node.getProtocol(FastConfig.getLinkable(pid));

        for (int i = 0; i < linkable.degree(); i++) {
            Node peer = linkable.getNeighbor(i);

            if (newMsg.hasVisited((int)peer.getID())) {
                continue;
            }

            Transport tr = (Transport) peer.getProtocol(FastConfig.getTransport(pid));

            tr.send(node, peer, newMsg, pid);

            System.out.println("Node " + node.getID() + " sent message to node " + peer.getID() + ": " + newMsg.getContent());
        }

       
    }

    public void processEvent(Node node, int pid, Object event) {
        // Process received message
        // Flood the message to all neighbors
        // floodMessage(node, pid, event);
        if (event instanceof Message) {
            Message msg = (Message) event;
            
            System.out.println("Node " + node.getID() + " received message: " + msg.getContent());

            floodMessage(node, pid, msg);
        }
    }
}