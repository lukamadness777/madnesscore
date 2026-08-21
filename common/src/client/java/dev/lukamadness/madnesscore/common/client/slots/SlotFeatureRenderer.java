package dev.lukamadness.madnesscore.common.client.slots;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import dev.lukamadness.madnesscore.common.slots.SlotsApi;
import dev.lukamadness.madnesscore.common.api.slots.SlotReference;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/**
 * Capa de render (Fase 4) que dibuja todo item equipado en un slot que tenga un {@link SlotRenderer}
 * registrado. Se agrega a TODO {@code LivingEntityRenderer} (jugadores y mobs) via mixin, ver
 * {@code MixinLivingEntityRendererSlots}. Portado de dev.emi.trinkets.TrinketFeatureRenderer,
 * adaptado a la API pre-"render state" de 1.21.1 (ver nota en {@link SlotRenderer}).
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class SlotFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    public SlotFeatureRenderer(RenderLayerParent<T, M> context) {
        super(context);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        SlotsApi.getSlotComponent(entity).ifPresent(component -> {
            List<Pair<SlotReference, ItemStack>> equipped = component.getAllEquipped();
            for (Pair<SlotReference, ItemStack> pair : equipped) {
                ItemStack stack = pair.getSecond();
                if (stack.isEmpty()) {
                    continue;
                }
                SlotRendererRegistry.getRenderer(stack.getItem()).ifPresent(renderer -> {
                    poseStack.pushPose();
                    ((SlotRenderer) renderer).render(stack, pair.getFirst(), this.getParentModel(), poseStack,
                            bufferSource, packedLight, entity, limbSwing, limbSwingAmount, partialTick,
                            ageInTicks, netHeadYaw, headPitch);
                    poseStack.popPose();
                });
            }
        });
    }
}