package dev.lukamadness.madnesscore.common.client.render.clothing.model;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.DyeableLegginsItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class DyeableLegginsModel extends GeoModel<DyeableLegginsItem> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "geo/armor/dyeable_leggins.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "textures/armor/dyeable_shirt.png");

    @Override
    public ResourceLocation getModelResource(DyeableLegginsItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(DyeableLegginsItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(DyeableLegginsItem animatable) {
        return null;
    }
}
