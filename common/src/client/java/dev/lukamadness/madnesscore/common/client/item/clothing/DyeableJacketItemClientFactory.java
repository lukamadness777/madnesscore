package dev.lukamadness.madnesscore.common.client.item.clothing;

import dev.lukamadness.madnesscore.common.content.tailoring.clothing.DyeableJacketItem;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.DyeableJacketItemFactory;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;

public class DyeableJacketItemClientFactory implements DyeableJacketItemFactory {
    @Override
    public DyeableJacketItem create(Holder<ArmorMaterial> material, Item.Properties properties) {
        return new DyeableJacketItemClient(material, properties);
    }
}
