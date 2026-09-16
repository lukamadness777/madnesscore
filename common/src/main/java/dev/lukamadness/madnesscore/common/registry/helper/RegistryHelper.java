package dev.lukamadness.madnesscore.common.registry.helper;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.registry.blockentity.BlockEntityFactory;
import dev.lukamadness.madnesscore.common.registry.menu.MenuFactory;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Holder;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public interface RegistryHelper {
    default <T extends Block> Supplier<T> registerBlock(String id, Supplier<T> block) {
        return registerBlock(MadnessCoreCommon.MOD_ID, id, block);
    }

    <T extends Block> Supplier<T> registerBlock(String namespace, String id, Supplier<T> block);

    default <T extends Item> Supplier<T> registerItem(String id, Supplier<T> item) {
        return registerItem(MadnessCoreCommon.MOD_ID, id, item);
    }

    <T extends Item> Supplier<T> registerItem(String namespace, String id, Supplier<T> item);

    default Supplier<CreativeModeTab> registerCreativeTab(String id, Supplier<CreativeModeTab.Builder> tabBuilder) {
        return registerCreativeTab(MadnessCoreCommon.MOD_ID, id, tabBuilder);
    }

    Supplier<CreativeModeTab> registerCreativeTab(String namespace, String id, Supplier<CreativeModeTab.Builder> tabBuilder);

    default Holder<ArmorMaterial> registerArmorMaterial(String id, Supplier<ArmorMaterial> material) {
        return registerArmorMaterial(MadnessCoreCommon.MOD_ID, id, material);
    }

    Holder<ArmorMaterial> registerArmorMaterial(String namespace, String id, Supplier<ArmorMaterial> material);

    CreativeModeTab.Builder creativeTabBuilder();

    default <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(
            String id,
            BlockEntityFactory<T> factory,
            Supplier<Block[]> validBlocks
    ) {
        return registerBlockEntity(MadnessCoreCommon.MOD_ID, id, factory, validBlocks);
    }

    <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(
            String namespace,
            String id,
            BlockEntityFactory<T> factory,
            Supplier<Block[]> validBlocks
    );

    default <T extends Entity> Supplier<EntityType<T>> registerEntity(
            String id,
            EntityType.EntityFactory<T> factory,
            MobCategory category,
            UnaryOperator<EntityType.Builder<T>> builderOperator
    ) {
        return registerEntity(MadnessCoreCommon.MOD_ID, id, factory, category, builderOperator);
    }

    <T extends Entity> Supplier<EntityType<T>> registerEntity(
            String namespace,
            String id,
            EntityType.EntityFactory<T> factory,
            MobCategory category,
            UnaryOperator<EntityType.Builder<T>> builderOperator
    );

    default <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenu(String id, MenuFactory<T> factory) {
        return registerMenu(MadnessCoreCommon.MOD_ID, id, factory);
    }

    <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenu(
            String namespace,
            String id,
            MenuFactory<T> factory
    );

    default Supplier<SoundEvent> registerSound(String id) {
        return registerSound(MadnessCoreCommon.MOD_ID, id);
    }

    Supplier<SoundEvent> registerSound(String namespace, String id);

    default <T extends Recipe<?>> Supplier<RecipeType<T>> registerRecipeType(String id) {
        return registerRecipeType(MadnessCoreCommon.MOD_ID, id);
    }

    <T extends Recipe<?>> Supplier<RecipeType<T>> registerRecipeType(String namespace, String id);

    default <T extends RecipeSerializer<?>> Supplier<T> registerRecipeSerializer(String id, Supplier<T> serializer) {
        return registerRecipeSerializer(MadnessCoreCommon.MOD_ID, id, serializer);
    }

    <T extends RecipeSerializer<?>> Supplier<T> registerRecipeSerializer(String namespace, String id, Supplier<T> serializer);

    default <T> Supplier<DataComponentType<T>> registerDataComponentType(
            String id,
            UnaryOperator<DataComponentType.Builder<T>> builderOperator
    ) {
        return registerDataComponentType(MadnessCoreCommon.MOD_ID, id, builderOperator);
    }

    <T> Supplier<DataComponentType<T>> registerDataComponentType(
            String namespace,
            String id,
            UnaryOperator<DataComponentType.Builder<T>> builderOperator
    );

    static ResourceLocation id(String path) {
        return id(MadnessCoreCommon.MOD_ID, path);
    }

    static ResourceLocation id(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
}