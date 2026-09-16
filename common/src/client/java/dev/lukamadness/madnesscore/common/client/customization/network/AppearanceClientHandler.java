package dev.lukamadness.madnesscore.common.client.customization.network;

import dev.lukamadness.madnesscore.common.client.customization.CustomizationConfig;
import dev.lukamadness.madnesscore.common.client.customization.render.AppearanceMaskTexture;
import dev.lukamadness.madnesscore.common.network.AppearanceConfigPayload;
import dev.lukamadness.madnesscore.common.platform.Services;
import net.minecraft.client.Minecraft;

public final class AppearanceClientHandler {
    private AppearanceClientHandler() {}

    public static void sendToServer() {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        AppearanceConfigPayload payload = AppearanceConfigPayloadHelper.fromConfig(
                client.player.getUUID(), CustomizationConfig.get());

        Services.APPEARANCE_NETWORK.sendToServer(payload);
    }

    public static void handleReceived(AppearanceConfigPayload payload) {
        AppearanceCache.put(payload.owner(), payload);
        AppearanceMaskTexture.invalidate(payload.owner());
    }

    public static void clear() {
        AppearanceCache.clear();
    }
}