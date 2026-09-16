package dev.lukamadness.madnesscore.common.client.render.clothing.model;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.DyeableJacketItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DyeableJacketModel extends GeoModel<DyeableJacketItem> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "geo/armor/dyeable_jacket.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "textures/armor/dyeable_jacket.png");

    @Override
    public ResourceLocation getModelResource(DyeableJacketItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(DyeableJacketItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(DyeableJacketItem animatable) {
        return null;
    }
}
