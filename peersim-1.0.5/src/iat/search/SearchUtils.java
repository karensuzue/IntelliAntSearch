package iat.search;

import peersim.config.Configuration;
import peersim.core.CommonState;

public class SearchUtils {
    public static int getCycle() {
        int time = CommonState.getIntTime() / cycleLength();

        return time;
    }

    public static int cycleLength() {
        return Configuration.getInt("CYCLE");
    }
}
