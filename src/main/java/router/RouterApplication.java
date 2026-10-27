
package router;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class RouterApplication {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(RouterApplication.class);

    private RouterApplication() {
        // Utility class: prevent instantiation.
    }

    public static void main(String[] args) {
        LOGGER.info("Java Software Router starting...");
        LOGGER.info("Java runtime version: {}", Runtime.version());
        LOGGER.info("Router foundation initialized.");
    }

    public static String applicationName() {
        return "Java Software Router";
    }
}
