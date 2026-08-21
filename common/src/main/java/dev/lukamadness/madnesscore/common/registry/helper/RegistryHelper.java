package dev.lukamadness.madnesscore.common.registry.helper;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.registry.blockentity.BlockEntityFactory;
import dev.lukamadness.madnesscore.common.registry.menu.MenuFactory;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ServiceLoader;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * API de registro general multiplataforma (Fabric + NeoForge, sin Architectury).
 * Cada loader provee su propia implementacion via ServiceLoader
 * (ver META-INF/services/dev.lukamadness.madnesscore.common.registry.helper.RegistryHelper).
 */
public interface RegistryHelper {

    RegistryHelper INSTANCE = ServiceLoader.load(RegistryHelper.class)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                    "No se encontro una implementacion de RegistryHelper (falta el archivo META-INF/services)"));

    <T extends Block> Supplier<T> registerBlock(String id, Supplier<T> block);

    <T extends Item> Supplier<T> registerItem(String id, Supplier<T> item);

    Supplier<CreativeModeTab> registerCreativeTab(String id, Supplier<CreativeModeTab.Builder> tabBuilder);

    // Cada loader devuelve un Builder ya inicializado a su manera
    CreativeModeTab.Builder creativeTabBuilder();

    /**
     * Registra un BlockEntityType. validBlocks es perezoso (Supplier) para poder
     * referenciar Suppliers de ModBlocks que todavia no terminaron de resolverse
     * en el momento en que se declara el campo estatico.
     */
    <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(
            String id,
            BlockEntityFactory<T> factory,
            Supplier<Block[]> validBlocks
    );

    <T extends Entity> Supplier<EntityType<T>> registerEntity(
            String id,
            EntityType.EntityFactory<T> factory,
            MobCategory category,
            UnaryOperator<EntityType.Builder<T>> builderOperator
    );

    <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenu(
            String id,
            MenuFactory<T> factory
    );

    Supplier<SoundEvent> registerSound(String id);

    <T extends net.minecraft.world.item.crafting.Recipe<?>> Supplier<RecipeType<T>> registerRecipeType(String id);

    <T extends RecipeSerializer<?>> Supplier<T> registerRecipeSerializer(String id, Supplier<T> serializer);

    static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, path);
    }
}