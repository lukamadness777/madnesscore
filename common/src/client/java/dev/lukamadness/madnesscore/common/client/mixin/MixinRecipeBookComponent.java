package dev.lukamadness.madnesscore.common.client.mixin;

import dev.lukamadness.madnesscore.common.client.slots.ui.SlotHoverManager;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeBookComponent.class)
public class MixinRecipeBookComponent {
    @Inject(method = "hasClickedOutside", at = @At("HEAD"), cancellable = true)
    private void madnesscore$onHasClickedOutside(double mouseX, double mouseY, int x, int y, int width, int height, int p_100304_,
            CallbackInfoReturnable<Boolean> cir) {
        if (SlotHoverManager.isClickInsideBounds(mouseX, mouseY)) {
            cir.setReturnValue(false);
        }
    }
}
