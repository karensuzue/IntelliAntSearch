package iat.antp2pr;

import peersim.core.Node;
import peersim.edsim.EDProtocol;

/**
 * This protocol handles query routing in AntP2PR. 
 * Requires PheromoneProtocol as a Linkable to work. 
 */
public class AntProtocol implements EDProtocol {
    // must be a pheromoneprotocol
    private static final String PAR_LINKABLE = "linkable";
    private static final String PAR_TRANSPORT = "transport";

    public AntProtocol() {
         // initialize pherTable with uniform distributed random values, same size as nodes 
    }

    @Override
    public void processEvent(Node node, int pid, Object event) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'processEvent'");
    }
    
}
