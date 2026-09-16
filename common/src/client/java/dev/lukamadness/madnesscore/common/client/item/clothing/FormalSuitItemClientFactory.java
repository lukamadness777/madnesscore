package dev.lukamadness.madnesscore.common.client.item.clothing;

import dev.lukamadness.madnesscore.common.content.tailoring.clothing.FormalSuitItem;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.FormalSuitItemFactory;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;

public class FormalSuitItemClientFactory implements FormalSuitItemFactory {
    @Override
    public FormalSuitItem create(Holder<ArmorMaterial> material, Item.Properties properties) {
        return new FormalSuitItemClient(material, properties);
    }
}
