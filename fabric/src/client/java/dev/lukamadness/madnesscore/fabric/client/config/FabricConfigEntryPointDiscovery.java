package dev.lukamadness.madnesscore.fabric.client.config;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.client.MadnessCoreCommonClient;
import dev.lukamadness.madnesscore.common.client.api.config.ConfigEntryPoint;
import dev.lukamadness.madnesscore.common.client.config.ConfigManager;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;

public final class FabricConfigEntryPointDiscovery {
    private FabricConfigEntryPointDiscovery() {
    }

    public static void discoverAndFinalize() {
        for (EntrypointContainer<ConfigEntryPoint> container :
                FabricLoader.getInstance().getEntrypointContainers(ConfigManager.CONFIG_ENTRY_POINT_KEY, ConfigEntryPoint.class)) {
            String modId = container.getProvider().getMetadata().getId();

            if (modId.equals(MadnessCoreCommon.MOD_ID)) {
                continue;
            }

            String displayName = container.getProvider().getMetadata().getName();
            ConfigManager.registerConfigEntryPoint(container::getEntrypoint,
                    displayName != null && !displayName.isBlank() ? displayName : modId);
        }

        MadnessCoreCommonClient.finalizeConfig();
    }
}
