package dev.lukamadness.madnesscore.common.network;

import dev.lukamadness.madnesscore.common.platform.Services;
import net.minecraft.server.level.ServerPlayer;

public final class AppearanceServerHandler {
    private AppearanceServerHandler() {}

    public static void handleReceived(ServerPlayer sender, AppearanceConfigPayload payload) {
        AppearanceConfigPayload verified = new AppearanceConfigPayload(
                sender.getUUID(),
                payload.skinColor(),
                payload.eyeOffsetX(), payload.eyeOffsetY(), payload.eyeWidth(), payload.eyeHeight(),
                payload.eyeColor(), payload.scleraColor(),
                payload.hairType(), payload.hairColor(),
                payload.hairPixels(), payload.eyePixels(),
                payload.hairColorMode(), payload.eyeColorMode()
        );

        AppearanceStore.put(sender.getUUID(), verified);

        Services.APPEARANCE_NETWORK.sendToAll(sender, verified);
    }

    public static void onPlayerJoin(ServerPlayer joined) {
        for (AppearanceConfigPayload existing : AppearanceStore.getAll().values()) {
            if (existing.owner().equals(joined.getUUID())) continue;
            Services.APPEARANCE_NETWORK.sendToPlayer(joined, existing);
        }
    }

    public static void onPlayerDisconnect(ServerPlayer player) {
        AppearanceStore.remove(player.getUUID());
    }
}
