package dev.lukamadness.madnesscore.common.slots.gui;

import dev.lukamadness.madnesscore.common.slots.SlotEquipLogic;
import dev.lukamadness.madnesscore.common.slots.SlotsApi;
import dev.lukamadness.madnesscore.common.api.slots.SlotGroup;
import dev.lukamadness.madnesscore.common.api.slots.SlotInventory;
import dev.lukamadness.madnesscore.common.api.slots.SlotReference;
import dev.lukamadness.madnesscore.common.api.slots.SlotType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Slot de GUI para un slot dinámico dentro del inventario del jugador (armado por
 * {@code MixinInventoryMenu}). Vive en el sourceSet "main" (no "client") porque, igual que un
 * slot vainilla, participa en la lógica real de items en ambos lados: la validación de
 * inserción/extracción corre en el servidor tanto como en la copia predictiva del cliente.
 * <p>
 * A diferencia de {@code dev.emi.trinkets.SurvivalTrinketSlot}, este slot NO decide por sí mismo
 * si está "enfocado" (esa es una decisión puramente visual, calculada del lado cliente por
 * {@code SlotHoverManager}/{@code MixinAbstractContainerScreen} a partir de los datos que expone
 * este slot vía {@link DynamicSlot}) — así el módulo common/main queda 100% válido en un servidor
 * dedicado, sin depender nunca de clases del sourceSet "client". Portado de
 * dev.emi.trinkets.SurvivalTrinketSlot.
 */
public class PlayerDynamicSlot extends Slot implements DynamicSlot {

    private final SlotGroup group;
    private final SlotType type;
    private final int offset;
    private final boolean anchor;
    private final SlotInventory slotInventory;

    public PlayerDynamicSlot(SlotInventory inventory, int index, int x, int y, SlotGroup group, SlotType type,
                              int offset, boolean anchor) {
        super(inventory, index, x, y);
        this.group = group;
        this.type = type;
        this.offset = offset;
        this.anchor = anchor;
        this.slotInventory = inventory;
    }

    private SlotReference reference() {
        return new SlotReference(this.slotInventory, this.offset);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return SlotEquipLogic.canInsert(stack, reference(), this.slotInventory.getComponent().getEntity());
    }

    @Override
    public boolean mayPickup(Player player) {
        ItemStack stack = this.getItem();
        if (stack.isEmpty()) {
            return true;
        }
        return SlotsApi.getSlottable(stack.getItem()).canUnequip(stack, reference(), player);
    }

    public SlotReference madnesscore$getReference() {
        return reference();
    }

    public SlotGroup madnesscore$getGroup() {
        return this.group;
    }

    public int madnesscore$getOffset() {
        return this.offset;
    }

    public boolean madnesscore$isAnchor() {
        return this.anchor;
    }

    @Override
    public SlotType madnesscore$getType() {
        return this.type;
    }

    @Override
    public ResourceLocation madnesscore$getBackground() {
        return this.type.getIcon();
    }
}
