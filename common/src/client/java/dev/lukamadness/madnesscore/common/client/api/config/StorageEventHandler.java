package dev.lukamadness.madnesscore.common.client.api.config;

@FunctionalInterface
public interface StorageEventHandler {
    void afterSave();
}
