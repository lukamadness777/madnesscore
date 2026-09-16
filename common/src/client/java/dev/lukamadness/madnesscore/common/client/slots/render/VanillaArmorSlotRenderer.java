package dev.lukamadness.madnesscore.common.client.slots.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.lukamadness.madnesscore.common.api.slots.SlotReference;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;

public class VanillaArmorSlotRenderer implements SlotRenderer<LivingEntity> {
    private static HumanoidModel<LivingEntity> innerModel;
    private static HumanoidModel<LivingEntity> outerModel;

    private static HumanoidModel<LivingEntity> innerModel() {
        if (innerModel == null) {
            innerModel = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_INNER_ARMOR));
        }
        return innerModel;
    }

    private static HumanoidModel<LivingEntity> outerModel() {
        if (outerModel == null) {
            outerModel = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR));
        }
        return outerModel;
    }

    @Override
    public void render(ItemStack stack, SlotReference slotReference, EntityModel<LivingEntity> contextModel,
                        PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, LivingEntity entity,
                        float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                        float netHeadYaw, float headPitch) {
        if (!(stack.getItem() instanceof ArmorItem armorItem) || !(contextModel instanceof HumanoidModel<LivingEntity> contextHumanoid)) {
            return;
        }

        EquipmentSlot slot = armorItem.getEquipmentSlot();
        boolean useInner = slot == EquipmentSlot.LEGS;
        HumanoidModel<LivingEntity> model = useInner ? innerModel() : outerModel();

        contextHumanoid.copyPropertiesTo(model);
        setPartVisibility(model, slot);

        ResourceLocation texture = getArmorTexture(armorItem, useInner);
        if (texture == null) {
            return;
        }

        float r = 1.0F, g = 1.0F, b = 1.0F;
        if (stack.has(DataComponents.DYED_COLOR)) {
            int rgb = DyedItemColor.getOrDefault(stack, DyedItemColor.LEATHER_COLOR);
            r = ((rgb >> 16) & 0xFF) / 255.0F;
            g = ((rgb >> 8) & 0xFF) / 255.0F;
            b = (rgb & 0xFF) / 255.0F;
        }

        var vertexConsumer = bufferSource.getBuffer(RenderType.armorCutoutNoCull(texture));
        model.renderToBuffer(poseStack, vertexConsumer, packedLight, net.minecraft.client.renderer.LightTexture.FULL_BRIGHT);
    }

    private static void setPartVisibility(HumanoidModel<LivingEntity> model, EquipmentSlot slot) {
        model.setAllVisible(false);
        switch (slot) {
            case HEAD -> {
                model.head.visible = true;
                model.hat.visible = true;
            }
            case CHEST -> {
                model.body.visible = true;
                model.rightArm.visible = true;
                model.leftArm.visible = true;
            }
            case LEGS -> {
                model.body.visible = true;
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
            }
            case FEET -> {
                model.rightLeg.visible = true;
                model.leftLeg.visible = true;
            }
            default -> {
            }
        }
    }

    private static ResourceLocation getArmorTexture(ArmorItem armorItem, boolean innerLayer) {
        ArmorMaterial material = armorItem.getMaterial().value();
        String materialName = getMaterialName(material);
        if (materialName == null) {
            return null;
        }
        String layer = innerLayer ? "_layer_2" : "_layer_1";
        return ResourceLocation.withDefaultNamespace("textures/models/armor/" + materialName + layer + ".png");
    }

    private static String getMaterialName(ArmorMaterial material) {
        if (material == ArmorMaterials.LEATHER.value()) return "leather";
        if (material == ArmorMaterials.CHAIN.value()) return "chainmail";
        if (material == ArmorMaterials.IRON.value()) return "iron";
        if (material == ArmorMaterials.GOLD.value()) return "gold";
        if (material == ArmorMaterials.DIAMOND.value()) return "diamond";
        if (material == ArmorMaterials.NETHERITE.value()) return "netherite";
        if (material == ArmorMaterials.TURTLE.value()) return "turtle";
        return null;
    }
}
