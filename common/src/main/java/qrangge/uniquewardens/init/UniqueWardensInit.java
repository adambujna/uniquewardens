package qrangge.uniquewardens.init;

import qrangge.uniquewardens.Constants;
import qrangge.uniquewardens.services.Services;

public class UniqueWardensInit {
    public static void init() {
        // Accessing Services here forces the ServiceLoader to run and will
        // throw early (at startup) if an implementation is missing.
        Constants.LOG.info("[{}] Initializing on {}",
                Constants.MOD_NAME,
                Services.PLATFORM.getPlatformName());
    }
}
