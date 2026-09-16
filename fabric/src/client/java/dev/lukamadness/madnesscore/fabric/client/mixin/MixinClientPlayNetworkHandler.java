package dev.lukamadness.madnesscore.fabric.client.mixin;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.slots.gui.PlayerSlotMenu;
import dev.lukamadness.madnesscore.common.slots.network.RequestSlotResyncPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric counterpart of the NeoForge MixinClientPacketListener fix (same Mojmap classes, since
 * this project compiles Fabric against official mappings too). Guards the two vanilla packet
 * handlers that index straight into AbstractContainerMenu#slots. If a packet references a slot
 * index / item count that doesn't match what the client currently has (because madnesscore's
 * own dynamic slots rebuilt at a different moment than the server - e.g. right after a
 * teleport/waystone with Trinkets, Accessories and the compat layer also installed), we drop
 * that single packet, rebuild our local dynamic slots, and ask the server for an authoritative
 * resync instead of crashing with "Network Protocol Error".
 * <p>
 * This is purely defensive: it does not read, merge, or change anything about how Trinkets,
 * Accessories or the compatibility layer show their own slots. It only stops madnesscore's own
 * slot bookkeeping from desyncing when all of them run at the same time.
 */
@Mixin(ClientPacketListener.class)
public abstract class MixinClientPlayNetworkHandler {

    @Inject(method = "handleContainerSetSlot", at = @At("HEAD"), cancellable = true)
    private void madnesscore$guardSetSlot(ClientboundContainerSetSlotPacket packet, CallbackInfo ci) {
        AbstractContainerMenu menu = madnesscore$resolveMenu(packet.getContainerId());
        if (menu == null) {
            return;
        }
        int slot = packet.getSlot();
        if (slot < 0 || slot >= menu.slots.size()) {
            MadnessCoreCommon.LOG.warn(
                    "[madnesscore] Dropped desynced ClientboundContainerSetSlotPacket (slot {} of {} in container {}); requesting resync",
                    slot, menu.slots.size(), packet.getContainerId());
            madnesscore$requestResync(menu);
            ci.cancel();
        }
    }

    @Inject(method = "handleContainerContent", at = @At("HEAD"), cancellable = true)
    private void madnesscore$guardSetContent(ClientboundContainerSetContentPacket packet, CallbackInfo ci) {
        AbstractContainerMenu menu = madnesscore$resolveMenu(packet.getContainerId());
        if (menu == null) {
            return;
        }
        if (packet.getItems().size() != menu.slots.size()) {
            MadnessCoreCommon.LOG.warn(
                    "[madnesscore] Dropped desynced ClientboundContainerSetContentPacket ({} items for {} local slots in container {}); requesting resync",
                    packet.getItems().size(), menu.slots.size(), packet.getContainerId());
            madnesscore$requestResync(menu);
            ci.cancel();
        }
    }

    @Unique
    private static AbstractContainerMenu madnesscore$resolveMenu(int containerId) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return null;
        }
        if (containerId == 0) {
            return player.inventoryMenu;
        }
        AbstractContainerMenu open = player.containerMenu;
        return open != null && open.containerId == containerId ? open : null;
    }

    @Unique
    private static void madnesscore$requestResync(AbstractContainerMenu menu) {
        if (menu instanceof PlayerSlotMenu dynamicMenu) {
            dynamicMenu.madnesscore$updateSlots(true);
        }
        ClientPlayNetworking.send(new RequestSlotResyncPayload());
    }
}