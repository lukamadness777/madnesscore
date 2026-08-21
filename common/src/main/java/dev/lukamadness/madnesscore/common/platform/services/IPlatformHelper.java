package dev.lukamadness.madnesscore.common.platform.services;

import java.nio.file.Path;
import java.util.Optional;

public interface IPlatformHelper {

    /**
     * Gets the name of the current platform
     *
     * @return The name of the current platform.
     */
    String getPlatformName();

    /**
     * Checks if a mod with the given id is loaded.
     *
     * @param modId The mod to check if it is loaded.
     * @return True if the mod is loaded, false otherwise.
     */
    boolean isModLoaded(String modId);

    /**
     * Check if the game is currently in a development environment.
     *
     * @return True if in a development environment, false otherwise.
     */
    boolean isDevelopmentEnvironment();

    /**
     * Gets the name of the environment type as a string.
     *
     * @return The name of the environment type.
     */
    default String getEnvironmentName() {
        return isDevelopmentEnvironment() ? "development" : "production";
    }

    /**
     * @return true si el loader actual es Fabric.
     */
    default boolean isFabric() {
        return "Fabric".equals(getPlatformName());
    }

    /**
     * @return true si el loader actual es NeoForge.
     */
    default boolean isNeoForge() {
        return "NeoForge".equals(getPlatformName());
    }

    /**
     * @return true si esta corriendo del lado cliente (physical side, no logical side).
     * Es seguro llamarlo desde codigo common: cada loader resuelve esto sin
     * cargar clases exclusivas de cliente en el server.
     */
    boolean isPhysicalClient();

    /**
     * @return true si esta corriendo en un dedicated server.
     */
    default boolean isDedicatedServer() {
        return !isPhysicalClient();
    }

    /**
     * @return el directorio de configs del juego (config/).
     */
    Path getConfigDirectory();

    /**
     * @return el directorio raiz del juego (donde vive el .minecraft / server).
     */
    Path getGameDirectory();

    /**
     * @param modId id del mod a consultar.
     * @return version del mod si esta cargado.
     */
    Optional<String> getModVersion(String modId);
}
