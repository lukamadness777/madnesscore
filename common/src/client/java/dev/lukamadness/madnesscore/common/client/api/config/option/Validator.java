package dev.lukamadness.madnesscore.common.client.api.config.option;

import java.util.function.Supplier;

public interface Validator<V> {
    V getValidatedValue(V value, Supplier<V> defaultValueSupplier);
}
