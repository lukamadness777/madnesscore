package dev.lukamadness.madnesscore.common.content.tailoring.clothing;

import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;

public interface DyeableLegginsItemFactory {
    DyeableLegginsItem create(Holder<ArmorMaterial> material, Item.Properties properties);
}
