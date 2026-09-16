package dev.lukamadness.madnesscore.common.client.slots.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lukamadness.madnesscore.common.api.slots.SlotReference;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface SlotRenderer<T extends LivingEntity> {
    void render(ItemStack stack, SlotReference slotReference, EntityModel<T> contextModel,
                PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity,
                float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                float netHeadYaw, float headPitch);

    static <E extends LivingEntity> void followBodyRotations(EntityModel<E> contextModel, PoseStack poseStack) {
        if (contextModel instanceof HumanoidModel<?> humanoid) {
            humanoid.body.translateAndRotate(poseStack);
        }
    }

    static <E extends LivingEntity> void translateToHand(HumanoidArm arm, EntityModel<E> contextModel, PoseStack poseStack) {
        if (!(contextModel instanceof HumanoidModel<?> humanoid)) {
            return;
        }
        humanoid.body.translateAndRotate(poseStack);
        (arm == HumanoidArm.RIGHT ? humanoid.rightArm : humanoid.leftArm).translateAndRotate(poseStack);
    }

    static <E extends LivingEntity> void translateToHead(EntityModel<E> contextModel, PoseStack poseStack) {
        if (contextModel instanceof HumanoidModel<?> humanoid) {
            humanoid.head.translateAndRotate(poseStack);
        }
    }

    static <E extends LivingEntity> void translateToFace(EntityModel<E> contextModel, PoseStack poseStack) {
        translateToHead(contextModel, poseStack);
        poseStack.translate(0.0D, -0.25D, -0.35D);
    }
}
