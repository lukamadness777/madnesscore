package dev.lukamadness.madnesscore.common.client.mixin;

import dev.lukamadness.madnesscore.common.client.bundledtabs.BundledTab;
import dev.lukamadness.madnesscore.common.client.bundledtabs.BundledTabGroup;
import dev.lukamadness.madnesscore.common.client.bundledtabs.BundledTabsAPI;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(CreativeModeTab.class)
public class MixinCreativeModeTabBundledItems {
    @Inject(method = "buildContents", at = @At("TAIL"))
    private void madnesscore$appendBundledTabItems(CreativeModeTab.ItemDisplayParameters parameters,
                                                   CallbackInfo ci) {
        CreativeModeTab self = (CreativeModeTab) (Object) this;
        BundledTabGroup group = BundledTabsAPI.getGroup(self);
        if (group == null) {
            return;
        }

        Collection<ItemStack> displayItems = self.getDisplayItems();
        Collection<ItemStack> searchItems = self.getSearchTabDisplayItems();

        for (BundledTab bundle : group.getTabs()) {
            bundle.populate(parameters.holders());
            for (ItemStack stack : bundle.getDisplayItems()) {
                if (!displayItems.contains(stack)) {
                    displayItems.add(stack.copy());
                    searchItems.add(stack.copy());
                }
            }
        }
    }
}
