package dev.lukamadness.madnesscore.common.registry.fluid;

import java.util.function.Supplier;

/**
 * Supplier mutable interno usado unicamente para resolver las referencias
 * circulares entre el fluido source y el fluido flowing (cada uno necesita
 * poder devolver al otro via getSource()/getFlowing(), pero ambos objetos
 * se instancian antes de que el registro termine). No usar fuera del
 * paquete de registro de fluidos.
 */
public final class MutableSupplier<T> implements Supplier<T> {
    private T value;
    private Supplier<T> delegate;

    public void set(T value) {
        this.value = value;
    }

    public void bind(Supplier<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public T get() {
        if (delegate != null) {
            return delegate.get();
        }
        return value;
    }
}
