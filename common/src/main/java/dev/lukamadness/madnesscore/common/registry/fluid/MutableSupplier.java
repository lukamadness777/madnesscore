package dev.lukamadness.madnesscore.common.registry.fluid;

import java.util.function.Supplier;

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
