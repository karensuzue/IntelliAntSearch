package iat.search;

import peersim.core.Network;

public class SearchDataInitializer implements peersim.core.Control {
    
    /*
     * 
     * init.1.keywords 10000
init.1.query_nodes 100
init.1.query_interval 1
init.1.max_queries 1
init.1.and_keys 0
     */

    public static final String PAR_KEYWORDS = "keywords";
    public static final String PAR_QUERY_NODES = "query_nodes";
    public static final String PAR_QUERY_INTERVAL = "query_interval";
    public static final String PAR_MAX_QUERIES = "max_queries";
    public static final String PAR_AND_KEYS = "and_keys";

    private final int keywords;
    private final int queryNodes;
    private final int queryInterval;
    private final int maxQueries;
    private final int andKeys;
     

    public SearchDataInitializer(String prefix) {
        this.keywords = peersim.config.Configuration.getInt(prefix + "." + PAR_KEYWORDS);
        this.queryNodes = peersim.config.Configuration.getInt(prefix + "." + PAR_QUERY_NODES);
        this.queryInterval = peersim.config.Configuration.getInt(prefix + "." + PAR_QUERY_INTERVAL);
        this.maxQueries = peersim.config.Configuration.getInt(prefix + "." + PAR_MAX_QUERIES);
        this.andKeys = peersim.config.Configuration.getInt(prefix + "." + PAR_AND_KEYS);
    }

    public boolean execute() {
        
        return false;
    }
    
}
