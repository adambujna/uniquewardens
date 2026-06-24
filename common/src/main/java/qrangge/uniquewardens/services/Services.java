package qrangge.uniquewardens.services;

import qrangge.uniquewardens.Constants;
import qrangge.uniquewardens.services.helpers.IDataHelper;
import qrangge.uniquewardens.services.helpers.IPlatformHelper;

import java.util.ServiceLoader;

public class Services {
    public static final IPlatformHelper PLATFORM = load(IPlatformHelper.class);

    public static final IDataHelper DATA = load(IDataHelper.class);

    public static <T> T load(Class<T> clazz) {

        final T loadedService = ServiceLoader.load(clazz, Services.class.getClassLoader())
                .findFirst()
                .orElseThrow(() -> new NullPointerException("Failed to load service for " + clazz.getName()));
        Constants.LOG.debug("Loaded {} for service {}", loadedService, clazz);
        return loadedService;
    }
}