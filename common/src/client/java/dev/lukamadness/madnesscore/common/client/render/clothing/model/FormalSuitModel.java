package dev.lukamadness.madnesscore.common.client.render.clothing.model;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.FormalSuitItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FormalSuitModel extends GeoModel<FormalSuitItem> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "geo/armor/formal_suit_chest.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "textures/armor/formal_suit.png");

    @Override
    public ResourceLocation getModelResource(FormalSuitItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(FormalSuitItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(FormalSuitItem animatable) {
        return null;
    }
}
