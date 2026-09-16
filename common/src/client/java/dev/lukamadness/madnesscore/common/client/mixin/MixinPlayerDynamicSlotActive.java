package dev.lukamadness.madnesscore.common.client.mixin;

import dev.lukamadness.madnesscore.common.client.slots.ui.SlotUiState;
import dev.lukamadness.madnesscore.common.api.slots.SlotsApi;
import dev.lukamadness.madnesscore.common.api.slots.SlotGroup;
import dev.lukamadness.madnesscore.common.slots.gui.PlayerDynamicSlot;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class MixinPlayerDynamicSlotActive {
    @Inject(method = "isActive", at = @At("HEAD"), cancellable = true)
    private void madnesscore$isActive(CallbackInfoReturnable<Boolean> cir) {
        if (!((Object) this instanceof PlayerDynamicSlot self)) {
            return;
        }

        if (self.madnesscore$isAnchor()) {
            cir.setReturnValue(SlotsApi.hasReadyItems(self.madnesscore$getType()));
            return;
        }

        SlotGroup group = self.madnesscore$getGroup();
        boolean groupOpen = SlotUiState.activeGroup == group;
        if (groupOpen && SlotUiState.activeType != null) {
            groupOpen = SlotUiState.activeType == self.madnesscore$getType();
        }
        cir.setReturnValue(groupOpen && SlotsApi.hasReadyItems(self.madnesscore$getType()));
    }
}
