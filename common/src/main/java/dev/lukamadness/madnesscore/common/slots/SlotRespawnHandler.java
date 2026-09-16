package dev.lukamadness.madnesscore.common.slots;

import dev.lukamadness.madnesscore.common.api.slots.SlotInventory;
import dev.lukamadness.madnesscore.common.platform.Services;
import dev.lukamadness.madnesscore.common.slots.gui.PlayerSlotMenu;
import dev.lukamadness.madnesscore.common.slots.network.SlotNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SlotRespawnHandler {
    private static final Set<UUID> pendingDimensionResync = ConcurrentHashMap.newKeySet();
    private static final Map<UUID, Long> lastClientRequestedResync = new ConcurrentHashMap<>();
    private static final long CLIENT_RESYNC_COOLDOWN_MS = 1000L;

    private SlotRespawnHandler() {
    }

    /**
     * Called when the client tells us (via RequestSlotResyncPayload) that it dropped a
     * container packet because its local slot count didn't match ours. Rate-limited per
     * player so a rapid burst of desynced packets only triggers one real resync.
     */
    public static void handleClientResyncRequest(ServerPlayer player) {
        long now = System.currentTimeMillis();
        Long last = lastClientRequestedResync.get(player.getUUID());
        if (last != null && now - last < CLIENT_RESYNC_COOLDOWN_MS) {
            return;
        }
        lastClientRequestedResync.put(player.getUUID(), now);
        fullResync(player);
    }

    public static void onRespawn(Player oldPlayer, Player newPlayer) {
        SlotInventory.copyFrom(oldPlayer, newPlayer);
    }

    public static void markPendingDimensionResync(ServerPlayer player) {
        pendingDimensionResync.add(player.getUUID());
    }

    public static boolean hasPendingDimensionResync(ServerPlayer player) {
        return pendingDimensionResync.contains(player.getUUID());
    }

    public static void resolvePendingDimensionResync(ServerPlayer player) {
        if (!pendingDimensionResync.remove(player.getUUID())) {
            return;
        }

        fullResync(player);
    }

    public static void fullResync(ServerPlayer player) {
        if (player.inventoryMenu instanceof PlayerSlotMenu menu) {
            menu.madnesscore$updateSlots(true);
        }

        Services.SLOT_NETWORK.sendToPlayer(player, SlotNetworking.buildDefinitionsPayload());

        SlotNetworking.sendFullSyncTo(player, player);

        player.inventoryMenu.sendAllDataToRemote();
    }
}