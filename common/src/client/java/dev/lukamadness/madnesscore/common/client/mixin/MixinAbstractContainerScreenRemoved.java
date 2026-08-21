package dev.lukamadness.madnesscore.common.client.mixin;

import dev.lukamadness.madnesscore.common.client.slots.SlotHoverManager;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * "removed" está sobreescrito en AbstractContainerScreen (manda el packet de cierre de
 * contenedor al server), no en InventoryScreen directamente — mismo motivo que
 * AbstractContainerScreenAccessor lee "menu"/"hoveredSlot" desde acá y no desde el target
 * original.
 */
@Mixin(AbstractContainerScreen.class)
public abstract class MixinAbstractContainerScreenRemoved {

    @Inject(method = "removed", at = @At("HEAD"))
    private void madnesscore$onRemoved(CallbackInfo ci) {
        if (!((Object) this instanceof InventoryScreen)) return;
        SlotHoverManager.close();
        SlotHoverManager.removeSelections();
    }
}