package dev.lukamadness.madnesscore.common.client.item.clothing;

import dev.lukamadness.madnesscore.common.content.tailoring.clothing.FormalSuitBootsItem;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.FormalSuitBootsItemFactory;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;

public class FormalSuitBootsItemClientFactory implements FormalSuitBootsItemFactory {
    @Override
    public FormalSuitBootsItem create(Holder<ArmorMaterial> material, Item.Properties properties) {
        return new FormalSuitBootsItemClient(material, properties);
    }
}
