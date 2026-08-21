package dev.lukamadness.madnesscore.common.slots.gui;

import dev.lukamadness.madnesscore.common.api.slots.SlotGroup;
import dev.lukamadness.madnesscore.common.api.slots.SlotType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Interfaz que {@code MixinInventoryMenu} agrega a {@code InventoryMenu} (el contenedor del
 * inventario normal del jugador, tanto supervivencia como creativo) para poder consultar, desde
 * el lado de renderizado (cliente), dónde está anclado cada {@link SlotGroup} y qué slots
 * individuales dinámicos existen dentro de él en este momento.
 * <p>
 * Portado de dev.emi.trinkets.TrinketPlayerScreenHandler.
 */
public interface PlayerSlotMenu {

    /**
     * Reconstruye los slots dinámicos: los saca todos y los vuelve a agregar en base al estado
     * actual del {@code SlotComponent} del dueño. Llamar con {@code slotsChanged = true} cuando
     * cambió qué grupos/slots existen (equipo nuevo, sincronización de red); {@code false} si
     * solo hace falta refrescar el layout (por ejemplo, tras abrir el inventario).
     */
    void madnesscore$updateSlots(boolean slotsChanged);

    /**
     * @return un número que identifica la posición del grupo: positivo y creciente para grupos
     * "flotantes" (sin slot_id, se acomodan a la izquierda del inventario en pilas de a 4);
     * negativo igual al slot_id vainilla para grupos anclados (cabeza, pecho, piernas, pies,
     * mano secundaria). 0 si el grupo no está presente.
     */
    int madnesscore$getGroupNum(SlotGroup group);

    /**
     * @return la posición (esquina superior izquierda) del slot "ancla" del grupo dentro del
     * panel del inventario, o null si el grupo no tiene slots visibles ahora mismo.
     */
    @Nullable
    Point madnesscore$getGroupPos(SlotGroup group);

    /**
     * @return para cada slot individual agregado dentro del grupo (en el orden en que se
     * agregaron), un Point cuyo x es el offset horizontal respecto al slot ancla y cuyo y es la
     * altura (cantidad de slots físicos) de ese tipo.
     */
    @NotNull
    List<Point> madnesscore$getSlotHeights(SlotGroup group);

    @Nullable
    Point madnesscore$getSlotHeight(SlotGroup group, int i);

    @NotNull
    List<SlotType> madnesscore$getSlotTypes(SlotGroup group);

    /**
     * @return cuántos SlotType distintos (con al menos un slot) tiene el grupo ahora mismo.
     */
    int madnesscore$getSlotWidth(SlotGroup group);

    /**
     * @return cuántos grupos "flotantes" (sin slot_id) exceden las primeras 4 columnas junto al
     * ícono del jugador, para saber cuánto dibujar del panel lateral extendido.
     */
    int madnesscore$getGroupCount();

    int madnesscore$getSlotRangeStart();

    int madnesscore$getSlotRangeEnd();
}
