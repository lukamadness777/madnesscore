package dev.lukamadness.madnesscore.common.client.api.config;

import dev.lukamadness.madnesscore.common.client.api.config.structure.ConfigBuilder;

public interface ConfigEntryPoint {
    default void registerConfigEarly(ConfigBuilder builder) {
    }

    void registerConfigLate(ConfigBuilder builder);
}
