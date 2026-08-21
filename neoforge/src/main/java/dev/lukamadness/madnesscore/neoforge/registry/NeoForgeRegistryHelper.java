package dev.lukamadness.madnesscore.neoforge.registry;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.registry.blockentity.BlockEntityFactory;
import dev.lukamadness.madnesscore.common.registry.helper.RegistryHelper;
import dev.lukamadness.madnesscore.common.registry.menu.MenuFactory;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class NeoForgeRegistryHelper implements RegistryHelper {

    private static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(BuiltInRegistries.BLOCK, MadnessCoreCommon.MOD_ID);
    private static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(BuiltInRegistries.ITEM, MadnessCoreCommon.MOD_ID);
    private static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, MadnessCoreCommon.MOD_ID);
    private static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, MadnessCoreCommon.MOD_ID);
    private static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MadnessCoreCommon.MOD_ID);
    private static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(BuiltInRegistries.MENU, MadnessCoreCommon.MOD_ID);
    private static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, MadnessCoreCommon.MOD_ID);
    private static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, MadnessCoreCommon.MOD_ID);
    private static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, MadnessCoreCommon.MOD_ID);

    // Se llama UNA vez desde MadnessCoreNeoForge(IEventBus) antes de MadnessCoreCommon.init()
    public static void registerToBus(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        ITEMS.register(eventBus);
        CREATIVE_TABS.register(eventBus);
        BLOCK_ENTITIES.register(eventBus);
        ENTITIES.register(eventBus);
        MENUS.register(eventBus);
        SOUNDS.register(eventBus);
        RECIPE_TYPES.register(eventBus);
        RECIPE_SERIALIZERS.register(eventBus);
    }

    @Override
    public <T extends Block> Supplier<T> registerBlock(String id, Supplier<T> block) {
        DeferredHolder<Block, T> holder = BLOCKS.register(id, block);
        return holder; // DeferredHolder ya implementa Supplier<T>
    }

    @Override
    public <T extends Item> Supplier<T> registerItem(String id, Supplier<T> item) {
        DeferredHolder<Item, T> holder = ITEMS.register(id, item);
        return holder;
    }

    @Override
    public Supplier<CreativeModeTab> registerCreativeTab(String id, Supplier<CreativeModeTab.Builder> tabBuilder) {
        DeferredHolder<CreativeModeTab, CreativeModeTab> holder =
                CREATIVE_TABS.register(id, () -> tabBuilder.get().build());
        return holder;
    }

    @Override
    public CreativeModeTab.Builder creativeTabBuilder() {
        return CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0);
    }

    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(
            String id,
            BlockEntityFactory<T> factory,
            Supplier<Block[]> validBlocks
    ) {
        DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> holder = BLOCK_ENTITIES.register(
                id,
                () -> BlockEntityType.Builder.of(factory::create, validBlocks.get()).build(null)
        );
        return holder;
    }

    @Override
    public <T extends Entity> Supplier<EntityType<T>> registerEntity(
            String id,
            EntityType.EntityFactory<T> factory,
            MobCategory category,
            UnaryOperator<EntityType.Builder<T>> builderOperator
    ) {
        DeferredHolder<EntityType<?>, EntityType<T>> holder = ENTITIES.register(
                id,
                () -> builderOperator.apply(EntityType.Builder.of(factory, category)).build(id)
        );
        return holder;
    }

    @Override
    public <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenu(String id, MenuFactory<T> factory) {
        DeferredHolder<MenuType<?>, MenuType<T>> holder = MENUS.register(
                id,
                () -> new MenuType<>(factory::create, net.minecraft.world.flag.FeatureFlags.VANILLA_SET)
        );
        return holder;
    }

    @Override
    public Supplier<SoundEvent> registerSound(String id) {
        DeferredHolder<SoundEvent, SoundEvent> holder = SOUNDS.register(
                id,
                () -> SoundEvent.createVariableRangeEvent(RegistryHelper.id(id))
        );
        return holder;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Recipe<?>> Supplier<RecipeType<T>> registerRecipeType(String id) {
        DeferredHolder<RecipeType<?>, RecipeType<?>> holder = RECIPE_TYPES.register(
                id,
                () -> new RecipeType<T>() {
                    @Override public String toString() { return id; }
                }
        );
        return () -> (RecipeType<T>) holder.get();
    }

    @Override
    public <T extends RecipeSerializer<?>> Supplier<T> registerRecipeSerializer(String id, Supplier<T> serializer) {
        DeferredHolder<RecipeSerializer<?>, T> holder = RECIPE_SERIALIZERS.register(id, serializer::get);
        return holder;
    }
}