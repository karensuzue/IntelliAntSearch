package iat.search;

import peersim.config.Configuration;
import peersim.core.Network;
import peersim.core.Node;
import peersim.util.IncrementalStats;

public class SearchObserver implements peersim.core.Control {
    /*
     * 
     * observer.0.verbosity 0

     */

    public static final String PAR_VERBOSITY = "verbosity";
    public static final String PAR_PID = "protocol";

    private final int verbosity;
    private final int pid;

    public SearchObserver(String prefix) {
        verbosity = Configuration.getInt(prefix + "." + PAR_VERBOSITY);
        pid = Configuration.getPid(prefix + "." + PAR_PID);

    }

    public boolean execute() {
        long time = peersim.core.CommonState.getTime();

        IncrementalStats is = new IncrementalStats();

        for (int i = 0; i < Network.size(); i++) {
            peersim.core.Node node = Network.get(i);

            SearchProtocol prot = (SearchProtocol) node.getProtocol(pid);

            is.add(prot.messageTable.size());
        }

        System.out.println("SearchObserver" + ": " + time + " " + is);
        
        return false;
    }
    
}
