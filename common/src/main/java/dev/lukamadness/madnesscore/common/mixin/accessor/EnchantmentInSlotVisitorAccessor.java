package dev.lukamadness.madnesscore.common.mixin.accessor;

import dev.lukamadness.madnesscore.common.slots.EnchantmentInSlotVisitorHandle;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(
        targets = "net/minecraft/world/item/enchantment/EnchantmentHelper$EnchantmentInSlotVisitor",
        remap = true
)
public interface EnchantmentInSlotVisitorAccessor extends EnchantmentInSlotVisitorHandle {
}
