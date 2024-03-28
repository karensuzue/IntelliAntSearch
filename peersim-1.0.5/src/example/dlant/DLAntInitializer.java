package example.dlant;

import java.util.Random;

import peersim.config.Configuration;
import peersim.core.Control;
import peersim.core.Network;

public class DLAntInitializer implements Control {
    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The protocol to operate on.
     * 
     * @config
     */
    private static final String PAR_PROT = "protocol";

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    /** Protocol identifier; obtained from config property {@link #PAR_PROT}. */
    private final int pid;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Creates a new instance and read parameters from the config file.
     */
    public DLAntInitializer(String prefix) {
        pid = Configuration.getPid(prefix + "." + PAR_PROT);
    }

    @Override
    public boolean execute() {
        // TODO Auto-generated method stub

        for (int i = 0; i < Network.size(); i++) {
            DLAntProtocol prot = (DLAntProtocol) Network.get(i).getProtocol(pid);

            int resourceAmount = new Random().nextInt(1, 5);

            for (int j = 0; j < resourceAmount; j++) {
                int value = new Random().nextInt(1, Network.size() + 1);

                prot.addResource(value);
            }
        }
        
        

        return false;
    }
}
