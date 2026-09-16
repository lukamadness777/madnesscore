package dev.lukamadness.madnesscore.common.client.item.clothing;

import dev.lukamadness.madnesscore.common.content.tailoring.clothing.DyeableShirtItem;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.DyeableShirtItemFactory;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;

public class DyeableShirtItemClientFactory implements DyeableShirtItemFactory {
    @Override
    public DyeableShirtItem create(Holder<ArmorMaterial> material, Item.Properties properties) {
        return new DyeableShirtItemClient(material, properties);
    }
}
