package dev.lukamadness.madnesscore.common.registry.helper;

import java.util.ServiceLoader;

public final class RegistryHelperLoader {
    public static final RegistryHelper INSTANCE = ServiceLoader.load(RegistryHelper.class)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException( "Cant find implementation RegistryHelper (missing META-INF/services)"));

    private RegistryHelperLoader() {}
}
