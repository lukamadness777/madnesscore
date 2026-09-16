package dev.lukamadness.madnesscore.common.client.slots.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import dev.lukamadness.madnesscore.common.api.slots.SlotsApi;
import dev.lukamadness.madnesscore.common.api.slots.SlotReference;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@SuppressWarnings({"unchecked", "rawtypes"})
public class SlotFeatureRenderer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    public SlotFeatureRenderer(RenderLayerParent<T, M> context) {
        super(context);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        if (entity.isInvisible()) {
            return;
        }

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
