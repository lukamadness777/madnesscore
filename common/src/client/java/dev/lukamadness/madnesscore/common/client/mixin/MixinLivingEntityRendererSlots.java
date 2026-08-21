package dev.lukamadness.madnesscore.common.client.mixin;

import dev.lukamadness.madnesscore.common.client.slots.SlotFeatureRenderer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Agrega {@link SlotFeatureRenderer} a TODO renderer de entidades vivas (jugadores y mobs) al
 * construirse, igual que Trinkets hace con su TrinketFeatureRenderer. Portado (adaptado a la API
 * pre-"render state" de 1.21.1, sin el mixin adicional de LivingEntityRenderState que usa
 * Trinkets en versiones mas nuevas de MC) de dev.emi.trinkets.mixin.LivingEntityRendererMixin.
 * <p>
 * NOTA: el nombre exacto del metodo protegido usado para agregar layers ({@code addLayer}) esta
 * escrito segun mi mejor conocimiento de las mappings oficiales 1.21.1; convendria confirmarlo
 * contra el jar decompilado (podria llamarse distinto, ej. {@code addFeature}) antes de compilar.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class MixinLivingEntityRendererSlots<T extends LivingEntity, M extends EntityModel<T>> {

    @Shadow
    protected abstract boolean addLayer(RenderLayer<T, M> layer);

    @Inject(method = "<init>", at = @At("RETURN"))
    private void madnesscore$addSlotLayer(EntityRendererProvider.Context context, M model, float shadowRadius, CallbackInfo ci) {
        this.addLayer(new SlotFeatureRenderer<>((LivingEntityRenderer<T, M>) (Object) this));
    }
}