package dev.lukamadness.madnesscore.common.client.customization.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

public final class SkinTextureCache {
    private SkinTextureCache() {}

    public static ResourceLocation get() {
        Minecraft client = Minecraft.getInstance();
        if (client.player instanceof AbstractClientPlayer clientPlayer) {
            return clientPlayer.getSkin().texture();
        }
        return DefaultPlayerSkin.getDefaultTexture();
    }
}
