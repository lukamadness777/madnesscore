package dev.lukamadness.madnesscore.common.mixin;

import dev.lukamadness.madnesscore.common.api.slots.SlotsApi;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Inventory.class)
public abstract class MixinInventoryClear {
    @Inject(method = "clearContent", at = @At("TAIL"))
    private void madnesscore$clearSlotsToo(CallbackInfo ci) {
        Player player = ((Inventory) (Object) this).player;
        if (player != null) {
            SlotsApi.clearAllSlots(player);
        }
    }
}
