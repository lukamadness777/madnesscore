package dev.lukamadness.madnesscore.common.client.render.clothing.model;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.FormalSuitBootsItem;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class FormalSuitBootsModel extends GeoModel<FormalSuitBootsItem> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "geo/armor/formal_suit_boots.geo.json");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "textures/armor/formal_suit.png");

    @Override
    public ResourceLocation getModelResource(FormalSuitBootsItem animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(FormalSuitBootsItem animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(FormalSuitBootsItem animatable) {
        return null;
    }
}
