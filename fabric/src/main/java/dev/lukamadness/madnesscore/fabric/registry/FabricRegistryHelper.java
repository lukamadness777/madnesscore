package dev.lukamadness.madnesscore.fabric.registry;

import dev.lukamadness.madnesscore.common.registry.blockentity.BlockEntityFactory;
import dev.lukamadness.madnesscore.common.registry.helper.RegistryHelper;
import dev.lukamadness.madnesscore.common.registry.menu.MenuFactory;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
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

public class FabricRegistryHelper implements RegistryHelper {
    @Override
    public <T extends Block> Supplier<T> registerBlock(String namespace, String id, Supplier<T> block) {
        T registered = Registry.register(BuiltInRegistries.BLOCK, RegistryHelper.id(namespace, id), block.get());
        return () -> registered;
    }

    @Override
    public <T extends Item> Supplier<T> registerItem(String namespace, String id, Supplier<T> item) {
        T registered = Registry.register(BuiltInRegistries.ITEM, RegistryHelper.id(namespace, id), item.get());
        return () -> registered;
    }

    @Override
    public Supplier<CreativeModeTab> registerCreativeTab(String namespace, String id, Supplier<CreativeModeTab.Builder> tabBuilder) {
        CreativeModeTab tab = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, RegistryHelper.id(namespace, id), tabBuilder.get().build());
        return () -> tab;
    }

    @Override
    public Holder<ArmorMaterial> registerArmorMaterial(String namespace, String id, Supplier<ArmorMaterial> material) {
        ResourceKey<ArmorMaterial> key = ResourceKey.create(Registries.ARMOR_MATERIAL, RegistryHelper.id(namespace, id));
        return Registry.registerForHolder(BuiltInRegistries.ARMOR_MATERIAL, key, material.get());
    }

    @Override
    public CreativeModeTab.Builder creativeTabBuilder() {
        return FabricItemGroup.builder();
    }

    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(
            String namespace,
            String id,
            BlockEntityFactory<T> factory,
            Supplier<Block[]> validBlocks
    ) {
        BlockEntityType<T> registered = Registry.register(
                BuiltInRegistries.BLOCK_ENTITY_TYPE,
                RegistryHelper.id(namespace, id),
                BlockEntityType.Builder.of(factory::create, validBlocks.get()).build(null)
        );
        return () -> registered;
    }

    @Override
    public <T extends Entity> Supplier<EntityType<T>> registerEntity(
            String namespace,
            String id,
            EntityType.EntityFactory<T> factory,
            MobCategory category,
            UnaryOperator<EntityType.Builder<T>> builderOperator
    ) {
        EntityType.Builder<T> builder = EntityType.Builder.of(factory, category);
        builder = builderOperator.apply(builder);
        ResourceKey<EntityType<?>> key = ResourceKey.create(Registries.ENTITY_TYPE, RegistryHelper.id(namespace, id));
        EntityType<T> registered = Registry.register(BuiltInRegistries.ENTITY_TYPE, key, builder.build(namespace + ":" + id));
        return () -> registered;
    }

    @Override
    public <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenu(String namespace, String id, MenuFactory<T> factory) {
        MenuType<T> registered = Registry.register(
                BuiltInRegistries.MENU,
                RegistryHelper.id(namespace, id),
                new MenuType<>(factory::create, net.minecraft.world.flag.FeatureFlags.VANILLA_SET)
        );
        return () -> registered;
    }

    @Override
    public Supplier<SoundEvent> registerSound(String namespace, String id) {
        var location = RegistryHelper.id(namespace, id);
        SoundEvent registered = Registry.register(
                BuiltInRegistries.SOUND_EVENT,
                location,
                SoundEvent.createVariableRangeEvent(location)
        );
        return () -> registered;
    }

    @Override
    public <T extends Recipe<?>> Supplier<RecipeType<T>> registerRecipeType(String namespace, String id) {
        RecipeType<T> registered = Registry.register(
                BuiltInRegistries.RECIPE_TYPE,
                RegistryHelper.id(namespace, id),
                new RecipeType<T>() {
                    @Override public String toString() { return namespace + ":" + id; }
                }
        );
        return () -> registered;
    }

    @Override
    public <T extends RecipeSerializer<?>> Supplier<T> registerRecipeSerializer(String namespace, String id, Supplier<T> serializer) {
        T registered = Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, RegistryHelper.id(namespace, id), serializer.get());
        return () -> registered;
    }

    @Override
    public <T> Supplier<DataComponentType<T>> registerDataComponentType(
            String namespace,
            String id,
            UnaryOperator<DataComponentType.Builder<T>> builderOperator
    ) {
        DataComponentType<T> registered = Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                RegistryHelper.id(namespace, id),
                builderOperator.apply(DataComponentType.builder()).build()
        );
        return () -> registered;
    }
}