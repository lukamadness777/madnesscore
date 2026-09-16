package dev.lukamadness.madnesscore.common.registry.item;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.registry.helper.RegistryHelperLoader;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;
import java.util.Map;

public class ModArmorMaterials {
    public static final Holder<ArmorMaterial> CLOTHES_MATERIAL = RegistryHelperLoader.INSTANCE.registerArmorMaterial(
            MadnessCoreCommon.MOD_ID, "clothes", () -> new ArmorMaterial(
                    Map.of(ArmorItem.Type.CHESTPLATE, 1),
                    5,
                    SoundEvents.ARMOR_EQUIP_LEATHER,
                    () -> Ingredient.EMPTY,
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "clothes"))),
                    0.0F,
                    0.0F
            ));

    public static final Holder<ArmorMaterial> BOOTS_MATERIAL = RegistryHelperLoader.INSTANCE.registerArmorMaterial(
            MadnessCoreCommon.MOD_ID, "clothes_boots", () -> new ArmorMaterial(
                    Map.of(ArmorItem.Type.BOOTS, 1),
                    5,
                    SoundEvents.ARMOR_EQUIP_LEATHER,
                    () -> Ingredient.EMPTY,
                    List.of(new ArmorMaterial.Layer(ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "clothes_boots"))),
                    0.0F,
                    0.0F
            ));

    public static void init() {
    }
}
