package dev.lukamadness.madnesscore.common.client.slots;

import dev.lukamadness.madnesscore.common.api.slots.SlotGroup;
import dev.lukamadness.madnesscore.common.api.slots.SlotType;

/**
 * Estado (puramente visual, cliente) de qué grupo/tipo está actualmente "abierto" bajo el mouse
 * en el inventario. Portado de los campos estáticos de dev.emi.trinkets.TrinketsClient
 * (activeGroup/activeType/quickMoveGroup/quickMoveType/quickMoveTimer).
 */
public final class SlotUiState {

    private SlotUiState() {
    }

    public static SlotGroup activeGroup;
    public static SlotType activeType;

    /**
     * NOTA: Trinkets también trackea un "quickMoveGroup/Type/Timer" para hacer parpadear
     * brevemente el grupo destino tras un shift-click. Se dejó afuera de este port porque
     * requeriría que el mixin de servidor ({@code MixinInventoryMenu}, sourceSet "main") le
     * avisara al cliente qué grupo eligió el quick-move, y ese mixin no puede depender de este
     * paquete "client" (ver el comentario en MixinInventoryMenu) sin romper la compatibilidad
     * con servidor dedicado. Si se quiere esa animación en el futuro, hay que agregar un evento
     * de red S2C liviano para avisarle al cliente qué slot recibió el quick-move.
     */
    public static void clear() {
        activeGroup = null;
        activeType = null;
    }
}
