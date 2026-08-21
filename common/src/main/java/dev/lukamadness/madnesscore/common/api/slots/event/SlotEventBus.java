package dev.lukamadness.madnesscore.common.api.slots.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Bus de eventos minimo, sin dependencias externas (ni Fabric API ni NeoForge event bus), para
 * que el codigo common pueda exponer puntos de extension a otros mods/addons sin importar el
 * loader. Cada listener registrado se invoca en el orden en que fue agregado.
 *
 * @param <T> el tipo funcional del callback (ej. {@link SlotEquipCallback}).
 */
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

    /**
     * @return un objeto del tipo T que, al invocarse, dispara todos los listeners registrados.
     */
    public T invoker() {
        return this.invoker;
    }
}