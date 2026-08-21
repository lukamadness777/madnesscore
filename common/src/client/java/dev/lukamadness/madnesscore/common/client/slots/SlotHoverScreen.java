package dev.lukamadness.madnesscore.common.client.slots;

import dev.lukamadness.madnesscore.common.api.slots.SlotGroup;
import dev.lukamadness.madnesscore.common.slots.gui.PlayerSlotMenu;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.world.inventory.Slot;

/**
 * Implementado (vía mixin) por las pantallas de inventario que soportan el hover dinámico de
 * slots. Portado de dev.emi.trinkets.TrinketScreen.
 */
public interface SlotHoverScreen {

    PlayerSlotMenu madnesscore$getMenu();

    Rect2i madnesscore$getGroupRect(SlotGroup group);

    Slot madnesscore$getHoveredSlot();

    int madnesscore$getX();

    int madnesscore$getY();
}
