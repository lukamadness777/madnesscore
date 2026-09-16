package dev.lukamadness.madnesscore.common.platform.services;

import java.nio.file.Path;
import java.util.Optional;

public interface IPlatformHelper {
    String getPlatformName();

    boolean isModLoaded(String modId);

    boolean isDevelopmentEnvironment();

    default String getEnvironmentName() {
        return isDevelopmentEnvironment() ? "development" : "production";
    }

    default boolean isFabric() {
        return "Fabric".equals(getPlatformName());
    }

    default boolean isNeoForge() {
        return "NeoForge".equals(getPlatformName());
    }

    boolean isPhysicalClient();

    default boolean isDedicatedServer() {
        return !isPhysicalClient();
    }

    Path getConfigDirectory();

    Path getGameDirectory();

    Optional<String> getModVersion(String modId);
}
