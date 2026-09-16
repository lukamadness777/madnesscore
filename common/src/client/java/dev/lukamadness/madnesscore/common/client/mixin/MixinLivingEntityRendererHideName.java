package dev.lukamadness.madnesscore.common.client.mixin;

import dev.lukamadness.madnesscore.common.api.identity.NameVisibilityApi;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Parte "ocultar nametag" de la API de visibilidad de nombre: si {@link NameVisibilityApi#isNameHidden}
 * dice que sí, cancelamos hasLabel devolviendo false para que ni siquiera se calcule/renderice el
 * texto. La parte "mostrar nametag" es simplemente no cancelar nada (el comportamiento vanilla de
 * hasLabel sigue corriendo tal cual cuando isNameHidden es false).
 */
@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRendererHideName {

    @Inject(method = "shouldShowName", at = @At("HEAD"), cancellable = true)
    private void madnesscore$hideNameWhenHidden(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (NameVisibilityApi.isNameHidden(entity)) {
            cir.setReturnValue(false);
        }
    }
}
