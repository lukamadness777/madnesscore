package dev.lukamadness.madnesscore.neoforge.mixin;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.slots.gui.PlayerSlotMenu;
import dev.lukamadness.madnesscore.common.slots.network.RequestSlotResyncPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.neoforged.neoforge.network.PacketDistributor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Same root cause as the Trinkets/Accessories crash: a mod that dynamically resizes the
 * player's container (madnesscore's own PlayerSlotMenu slots, in this case) can end up with
 * the client holding fewer/more slots than the server for a brief window (right after a
 * teleport, dimension change, waystone, respawn, etc). When that happens, a
 * ClientboundContainerSetSlotPacket/ClientboundContainerSetContentPacket referencing an index
 * the client doesn't have yet throws IndexOutOfBoundsException and kills the connection with
 * "Network Protocol Error".
 * <p>
 * Instead of letting that exception propagate, we bounds-check the packet before vanilla
 * touches {@code AbstractContainerMenu#slots}. If it doesn't fit, we drop just that packet,
 * speculatively rebuild our local dynamic slots, and ask the server for an authoritative
 * resync (see {@link RequestSlotResyncPayload}) instead of disconnecting the player.
 */
@Mixin(ClientPacketListener.class)
public abstract class MixinClientPacketListener {

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
        PacketDistributor.sendToServer(new RequestSlotResyncPayload());
    }
}