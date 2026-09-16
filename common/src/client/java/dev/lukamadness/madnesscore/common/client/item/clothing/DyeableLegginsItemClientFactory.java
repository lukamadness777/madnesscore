package dev.lukamadness.madnesscore.common.client.item.clothing;

import dev.lukamadness.madnesscore.common.content.tailoring.clothing.DyeableLegginsItem;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.DyeableLegginsItemFactory;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;

public class DyeableLegginsItemClientFactory implements DyeableLegginsItemFactory {
    @Override
    public DyeableLegginsItem create(Holder<ArmorMaterial> material, Item.Properties properties) {
        return new DyeableLegginsItemClient(material, properties);
    }
}
