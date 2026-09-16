package dev.lukamadness.madnesscore.neoforge.registry;

import dev.lukamadness.madnesscore.common.registry.blockentity.BlockEntityFactory;
import dev.lukamadness.madnesscore.common.registry.helper.RegistryHelper;
import dev.lukamadness.madnesscore.common.registry.menu.MenuFactory;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
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
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.Holder;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public class NeoForgeRegistryHelper implements RegistryHelper {
    private static IEventBus MOD_EVENT_BUS;

    private static final Map<String, DeferredRegister<Block>> BLOCK_REGISTRIES = new ConcurrentHashMap<>();
    private static final Map<String, DeferredRegister<Item>> ITEM_REGISTRIES = new ConcurrentHashMap<>();
    private static final Map<String, DeferredRegister<CreativeModeTab>> CREATIVE_TAB_REGISTRIES = new ConcurrentHashMap<>();
    private static final Map<String, DeferredRegister<BlockEntityType<?>>> BLOCK_ENTITY_REGISTRIES = new ConcurrentHashMap<>();
    private static final Map<String, DeferredRegister<ArmorMaterial>> ARMOR_MATERIAL_REGISTRIES = new ConcurrentHashMap<>();
    private static final Map<String, DeferredRegister<EntityType<?>>> ENTITY_REGISTRIES = new ConcurrentHashMap<>();
    private static final Map<String, DeferredRegister<MenuType<?>>> MENU_REGISTRIES = new ConcurrentHashMap<>();
    private static final Map<String, DeferredRegister<SoundEvent>> SOUND_REGISTRIES = new ConcurrentHashMap<>();
    private static final Map<String, DeferredRegister<RecipeType<?>>> RECIPE_TYPE_REGISTRIES = new ConcurrentHashMap<>();
    private static final Map<String, DeferredRegister<RecipeSerializer<?>>> RECIPE_SERIALIZER_REGISTRIES = new ConcurrentHashMap<>();
    private static final Map<String, DeferredRegister.DataComponents> DATA_COMPONENT_REGISTRIES = new ConcurrentHashMap<>();

    private static IEventBus requireEventBus() {
        if (MOD_EVENT_BUS == null) {
            throw new IllegalStateException(
                    "NeoForgeRegistryHelper.registerToBus(modEventBus) tiene que llamarse antes de registrar cualquier contenido"
            );
        }
        return MOD_EVENT_BUS;
    }

    public static void registerToBus(IEventBus eventBus) {
        MOD_EVENT_BUS = eventBus;
    }

    private static DeferredRegister<Block> blocksFor(String namespace) {
        return BLOCK_REGISTRIES.computeIfAbsent(namespace, ns -> {
            DeferredRegister<Block> register = DeferredRegister.create(BuiltInRegistries.BLOCK, ns);
            register.register(requireEventBus());
            return register;
        });
    }

    private static DeferredRegister<Item> itemsFor(String namespace) {
        return ITEM_REGISTRIES.computeIfAbsent(namespace, ns -> {
            DeferredRegister<Item> register = DeferredRegister.create(BuiltInRegistries.ITEM, ns);
            register.register(requireEventBus());
            return register;
        });
    }

    private static DeferredRegister<CreativeModeTab> creativeTabsFor(String namespace) {
        return CREATIVE_TAB_REGISTRIES.computeIfAbsent(namespace, ns -> {
            DeferredRegister<CreativeModeTab> register = DeferredRegister.create(BuiltInRegistries.CREATIVE_MODE_TAB, ns);
            register.register(requireEventBus());
            return register;
        });
    }

    private static DeferredRegister<BlockEntityType<?>> blockEntitiesFor(String namespace) {
        return BLOCK_ENTITY_REGISTRIES.computeIfAbsent(namespace, ns -> {
            DeferredRegister<BlockEntityType<?>> register = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, ns);
            register.register(requireEventBus());
            return register;
        });
    }

    private static DeferredRegister<ArmorMaterial> armorMaterialsFor(String namespace) {
        return ARMOR_MATERIAL_REGISTRIES.computeIfAbsent(namespace, ns -> {
            DeferredRegister<ArmorMaterial> register = DeferredRegister.create(BuiltInRegistries.ARMOR_MATERIAL, ns);
            register.register(requireEventBus());
            return register;
        });
    }

    private static DeferredRegister<EntityType<?>> entitiesFor(String namespace) {
        return ENTITY_REGISTRIES.computeIfAbsent(namespace, ns -> {
            DeferredRegister<EntityType<?>> register = DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, ns);
            register.register(requireEventBus());
            return register;
        });
    }

    private static DeferredRegister<MenuType<?>> menusFor(String namespace) {
        return MENU_REGISTRIES.computeIfAbsent(namespace, ns -> {
            DeferredRegister<MenuType<?>> register = DeferredRegister.create(BuiltInRegistries.MENU, ns);
            register.register(requireEventBus());
            return register;
        });
    }

    private static DeferredRegister<SoundEvent> soundsFor(String namespace) {
        return SOUND_REGISTRIES.computeIfAbsent(namespace, ns -> {
            DeferredRegister<SoundEvent> register = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, ns);
            register.register(requireEventBus());
            return register;
        });
    }

    private static DeferredRegister<RecipeType<?>> recipeTypesFor(String namespace) {
        return RECIPE_TYPE_REGISTRIES.computeIfAbsent(namespace, ns -> {
            DeferredRegister<RecipeType<?>> register = DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, ns);
            register.register(requireEventBus());
            return register;
        });
    }

    private static DeferredRegister<RecipeSerializer<?>> recipeSerializersFor(String namespace) {
        return RECIPE_SERIALIZER_REGISTRIES.computeIfAbsent(namespace, ns -> {
            DeferredRegister<RecipeSerializer<?>> register = DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, ns);
            register.register(requireEventBus());
            return register;
        });
    }

    private static DeferredRegister.DataComponents dataComponentsFor(String namespace) {
        return DATA_COMPONENT_REGISTRIES.computeIfAbsent(namespace, ns -> {
            DeferredRegister.DataComponents register = DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ns);
            register.register(requireEventBus());
            return register;
        });
    }

    @Override
    public <T extends Block> Supplier<T> registerBlock(String namespace, String id, Supplier<T> block) {
        DeferredHolder<Block, T> holder = blocksFor(namespace).register(id, block);
        return holder;
    }

    @Override
    public <T extends Item> Supplier<T> registerItem(String namespace, String id, Supplier<T> item) {
        DeferredHolder<Item, T> holder = itemsFor(namespace).register(id, item);
        return holder;
    }

    @Override
    public Holder<ArmorMaterial> registerArmorMaterial(String namespace, String id, Supplier<ArmorMaterial> material) {
        return armorMaterialsFor(namespace).register(id, material);
    }

    @Override
    public Supplier<CreativeModeTab> registerCreativeTab(String namespace, String id, Supplier<CreativeModeTab.Builder> tabBuilder) {
        DeferredHolder<CreativeModeTab, CreativeModeTab> holder =
                creativeTabsFor(namespace).register(id, () -> tabBuilder.get().build());
        return holder;
    }

    @Override
    public CreativeModeTab.Builder creativeTabBuilder() {
        return CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0);
    }

    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(
            String namespace,
            String id,
            BlockEntityFactory<T> factory,
            Supplier<Block[]> validBlocks
    ) {
        DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> holder = blockEntitiesFor(namespace).register(
                id,
                () -> BlockEntityType.Builder.of(factory::create, validBlocks.get()).build(null)
        );
        return holder;
    }

    @Override
    public <T extends Entity> Supplier<EntityType<T>> registerEntity(
            String namespace,
            String id,
            EntityType.EntityFactory<T> factory,
            MobCategory category,
            UnaryOperator<EntityType.Builder<T>> builderOperator
    ) {
        DeferredHolder<EntityType<?>, EntityType<T>> holder = entitiesFor(namespace).register(
                id,
                () -> builderOperator.apply(EntityType.Builder.of(factory, category)).build(namespace + ":" + id)
        );
        return holder;
    }

    @Override
    public <T extends AbstractContainerMenu> Supplier<MenuType<T>> registerMenu(String namespace, String id, MenuFactory<T> factory) {
        DeferredHolder<MenuType<?>, MenuType<T>> holder = menusFor(namespace).register(
                id,
                () -> new MenuType<>(factory::create, net.minecraft.world.flag.FeatureFlags.VANILLA_SET)
        );
        return holder;
    }

    @Override
    public Supplier<SoundEvent> registerSound(String namespace, String id) {
        DeferredHolder<SoundEvent, SoundEvent> holder = soundsFor(namespace).register(
                id,
                () -> SoundEvent.createVariableRangeEvent(RegistryHelper.id(namespace, id))
        );
        return holder;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Recipe<?>> Supplier<RecipeType<T>> registerRecipeType(String namespace, String id) {
        DeferredHolder<RecipeType<?>, RecipeType<?>> holder = recipeTypesFor(namespace).register(
                id,
                () -> new RecipeType<T>() {
                    @Override public String toString() { return namespace + ":" + id; }
                }
        );
        return () -> (RecipeType<T>) holder.get();
    }

    @Override
    public <T extends RecipeSerializer<?>> Supplier<T> registerRecipeSerializer(String namespace, String id, Supplier<T> serializer) {
        DeferredHolder<RecipeSerializer<?>, T> holder = recipeSerializersFor(namespace).register(id, serializer::get);
        return holder;
    }

    @Override
    public <T> Supplier<DataComponentType<T>> registerDataComponentType(
            String namespace,
            String id,
            UnaryOperator<DataComponentType.Builder<T>> builderOperator
    ) {
        DeferredHolder<DataComponentType<?>, DataComponentType<T>> holder =
                dataComponentsFor(namespace).registerComponentType(id, builderOperator);
        return holder;
    }
}