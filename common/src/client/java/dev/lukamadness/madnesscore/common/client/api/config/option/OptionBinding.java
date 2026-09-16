package dev.lukamadness.madnesscore.common.client.api.config.option;

public interface OptionBinding<V> {
    void save(V value);

    V load();
}
