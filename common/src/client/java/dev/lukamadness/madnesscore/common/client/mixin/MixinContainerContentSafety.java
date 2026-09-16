package dev.lukamadness.madnesscore.common.client.mixin;

import dev.lukamadness.madnesscore.common.mixin.accessor.ContainerMenuAccessor;
import dev.lukamadness.madnesscore.common.slots.gui.PlayerSlotMenu;
import net.minecraft.core.NonNullList;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(AbstractContainerMenu.class)
public abstract class MixinContainerContentSafety {
    @Shadow
    private int stateId;

    @Inject(at = @At("HEAD"), method = "initializeContents", cancellable = true)
    private void madnesscore$guardContentSize(int stateId, List<ItemStack> items, ItemStack carried, CallbackInfo info) {
        if (!(this instanceof PlayerSlotMenu menu)) {
            return;
        }
        AbstractContainerMenu self = (AbstractContainerMenu) (Object) this;
        int slotCount = self.slots.size();
        if (items.size() <= slotCount) {
            return;
        }

        menu.madnesscore$updateSlots(true);
        slotCount = self.slots.size();
        if (items.size() <= slotCount) {
            return;
        }

        this.stateId = stateId;
        NonNullList<ItemStack> lastSlots = ((ContainerMenuAccessor) (Object) this).madnesscore$getLastSlots();
        for (int i = 0; i < slotCount; i++) {
            ItemStack stack = items.get(i);
            self.getSlot(i).set(stack);
            if (i < lastSlots.size()) {
                lastSlots.set(i, stack.copy());
            }
        }
        self.setCarried(carried);
        info.cancel();
    }
}
