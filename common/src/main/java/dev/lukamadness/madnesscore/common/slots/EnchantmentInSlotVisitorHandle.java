package dev.lukamadness.madnesscore.common.slots;

import net.minecraft.core.Holder;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.Enchantment;

public interface EnchantmentInSlotVisitorHandle {
    void accept(Holder<Enchantment> holder, int level, EnchantedItemInUse use);
}
