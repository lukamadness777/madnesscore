package dev.lukamadness.madnesscore.common.client.mixin;

import dev.lukamadness.madnesscore.common.client.slots.ui.SlotHoverManager;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractContainerScreen.class)
public abstract class MixinAbstractContainerScreenRemoved {
    @Inject(method = "removed", at = @At("HEAD"))
    private void madnesscore$onRemoved(CallbackInfo ci) {
        if (!((Object) this instanceof InventoryScreen)) return;
        SlotHoverManager.close();
        SlotHoverManager.removeSelections();
    }
}
