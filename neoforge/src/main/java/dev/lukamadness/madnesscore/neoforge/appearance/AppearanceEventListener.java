package dev.lukamadness.madnesscore.neoforge.appearance;

import dev.lukamadness.madnesscore.common.network.AppearanceServerHandler;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

public class AppearanceEventListener {
    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer joined) {
            AppearanceServerHandler.onPlayerJoin(joined);
        }
    }

    @SubscribeEvent
    public void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            AppearanceServerHandler.onPlayerDisconnect(player);
        }
    }
}
