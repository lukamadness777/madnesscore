package dev.lukamadness.madnesscore.common.mixin;

import dev.lukamadness.madnesscore.common.slots.SlotRespawnHandler;
import dev.lukamadness.madnesscore.common.slots.gui.PlayerSlotMenu;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket;
import net.minecraft.network.protocol.game.ServerboundSetCreativeModeSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerGamePacketListenerImpl {
    @Shadow public ServerPlayer player;

    @Inject(method = "handleSetCreativeModeSlot", at = @At("HEAD"), cancellable = true)
    private void madnesscore$allowDynamicSlots(ServerboundSetCreativeModeSlotPacket packet, CallbackInfo ci) {
        if (!this.player.isCreative()) {
            return;
        }

        AbstractContainerMenu menu = this.player.inventoryMenu;
        if (!(menu instanceof PlayerSlotMenu dynamicMenu)) {
            return;
        }

        int slotId = packet.slotNum();
        ItemStack itemStack = packet.itemStack();

        int start = dynamicMenu.madnesscore$getSlotRangeStart();
        int end = dynamicMenu.madnesscore$getSlotRangeEnd();

        if (slotId >= menu.slots.size() && SlotRespawnHandler.hasPendingDimensionResync(this.player)) {
            SlotRespawnHandler.resolvePendingDimensionResync(this.player);
            start = dynamicMenu.madnesscore$getSlotRangeStart();
            end = dynamicMenu.madnesscore$getSlotRangeEnd();
        }

        if (slotId < start || slotId >= end || slotId >= menu.slots.size()) {
            return;
        }

        Slot slot = menu.getSlot(slotId);
        if (!itemStack.isEmpty() && !slot.mayPlace(itemStack)) {
            ci.cancel();
            return;
        }

        slot.setByPlayer(itemStack);
        slot.setChanged();
        menu.setRemoteSlot(slotId, itemStack);
        this.player.connection.send(new ClientboundContainerSetSlotPacket(
                menu.containerId, menu.incrementStateId(), slotId, itemStack));

        ci.cancel();
    }

    @Inject(method = "handleAcceptTeleportPacket", at = @At("TAIL"))
    private void madnesscore$resyncOnTeleportAck(ServerboundAcceptTeleportationPacket packet, CallbackInfo ci) {
        SlotRespawnHandler.resolvePendingDimensionResync(this.player);
    }
}
