package dev.lukamadness.madnesscore.common.api.slots.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public final class SlotEventBus<T> {
    private final List<T> listeners = new ArrayList<>();
    private final Function<List<T>, T> invokerFactory;
    private T invoker;

    public SlotEventBus(Function<List<T>, T> invokerFactory) {
        this.invokerFactory = invokerFactory;
        this.invoker = invokerFactory.apply(this.listeners);
    }

    public void addListener(T listener) {
        this.listeners.add(listener);
    }

    public T invoker() {
        return this.invoker;
    }
}
