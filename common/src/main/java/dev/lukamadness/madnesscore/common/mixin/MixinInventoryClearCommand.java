package dev.lukamadness.madnesscore.common.mixin;

import dev.lukamadness.madnesscore.common.api.slots.SlotsApi;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(Inventory.class)
public abstract class MixinInventoryClearCommand {
    @Inject(method = "clearOrCountMatchingItems", at = @At("RETURN"), cancellable = true)
    private void madnesscore$clearOrCountSlotsToo(Predicate<ItemStack> stackPredicate, int maxCount,
                                                   Container inventory, CallbackInfoReturnable<Integer> cir) {
        Player player = ((Inventory) (Object) this).player;
        int removedSoFar = cir.getReturnValue();
        boolean simulate = maxCount == 0;

        int[] extra = {0};
        SlotsApi.getSlotComponent(player).ifPresent(component ->
                component.getInventory().values().forEach(byType ->
                        byType.values().forEach(slotInventory -> {
                            int remaining = maxCount < 0 ? -1 : maxCount - removedSoFar - extra[0];
                            if (maxCount >= 0 && remaining <= 0) {
                                return;
                            }
                            extra[0] += ContainerHelper.clearOrCountMatchingItems(
                                    slotInventory, stackPredicate, remaining, simulate);
                        })));

        if (extra[0] > 0) {
            cir.setReturnValue(removedSoFar + extra[0]);
        }
    }
}
