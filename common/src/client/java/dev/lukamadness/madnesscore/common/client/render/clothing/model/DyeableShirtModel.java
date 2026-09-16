package dev.lukamadness.madnesscore.common.client.render.clothing.model;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.DyeableShirtItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DyeableShirtModel extends GeoModel<DyeableShirtItem> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "geo/armor/dyeable_shirt.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "textures/armor/dyeable_shirt.png");

    @Override
    public ResourceLocation getModelResource(DyeableShirtItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(DyeableShirtItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(DyeableShirtItem animatable) {
        return null;
    }
}
