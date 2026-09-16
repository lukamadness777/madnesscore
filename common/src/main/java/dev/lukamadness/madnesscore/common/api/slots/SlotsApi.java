package dev.lukamadness.madnesscore.common.api.slots;

import com.google.common.collect.ImmutableMap;
import dev.lukamadness.madnesscore.common.platform.Services;
import dev.lukamadness.madnesscore.common.slots.data.EntitySlotReloadListener;
import dev.lukamadness.madnesscore.common.slots.data.SlotGroupReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class SlotsApi {
    private static final SlotGroupReloadListener SERVER_GROUPS = new SlotGroupReloadListener();
    private static final EntitySlotReloadListener SERVER_ENTITIES = new EntitySlotReloadListener(SERVER_GROUPS);

    private static final SlotGroupReloadListener CLIENT_GROUPS = new SlotGroupReloadListener();
    private static final EntitySlotReloadListener CLIENT_ENTITIES = new EntitySlotReloadListener(CLIENT_GROUPS);

    private static final Map<Item, Slottable> SLOTTABLES = new HashMap<>();
    private static final Slottable DEFAULT_SLOTTABLE = new Slottable() {
    };

    private static final Map<ResourceLocation, SlotPredicate> PREDICATES = new HashMap<>();

    private SlotsApi() {
    }

    public static SlotGroupReloadListener getServerGroupLoader() {
        return SERVER_GROUPS;
    }

    public static EntitySlotReloadListener getServerEntityLoader() {
        return SERVER_ENTITIES;
    }

    public static SlotGroupReloadListener getClientGroupLoader() {
        return CLIENT_GROUPS;
    }

    public static EntitySlotReloadListener getClientEntityLoader() {
        return CLIENT_ENTITIES;
    }

    public static Map<String, SlotGroup> getEntitySlots(EntityType<?> type, boolean clientSide) {
        return clientSide ? CLIENT_ENTITIES.getEntitySlots(type) : SERVER_ENTITIES.getEntitySlots(type);
    }

    public static Map<String, SlotGroup> getEntitySlots(Level level, EntityType<?> type) {
        return getEntitySlots(type, level.isClientSide());
    }

    public static Map<String, SlotGroup> getEntitySlots(Entity entity) {
        if (entity == null) {
            return ImmutableMap.of();
        }
        return getEntitySlots(entity.level(), entity.getType());
    }

    public static void registerSlottable(Item item, Slottable slottable) {
        SLOTTABLES.put(item, slottable);
    }

    public static Slottable getSlottable(Item item) {
        return SLOTTABLES.getOrDefault(item, DEFAULT_SLOTTABLE);
    }

    public static Slottable getDefaultSlottable() {
        return DEFAULT_SLOTTABLE;
    }

    public static Optional<SlotComponent> getSlotComponent(LivingEntity entity) {
        return Optional.ofNullable(Services.SLOT_ATTACHMENT.getOrCreate(entity));
    }

    public static void clearAllSlots(LivingEntity entity) {
        getSlotComponent(entity).ifPresent(component ->
                component.getInventory().values().forEach(byType ->
                        byType.values().forEach(SlotInventory::clearContent)));
    }

    public static void onSlotItemBroken(ItemStack stack, SlotReference ref, LivingEntity entity) {
        dev.lukamadness.madnesscore.common.slots.network.SlotNetworking.sendBreak(entity, ref);
    }

    @FunctionalInterface
    public interface SlotPredicate {
        Boolean test(ItemStack stack, SlotReference ref, LivingEntity entity);
    }

    public static void registerSlotPredicate(ResourceLocation id, SlotPredicate predicate) {
        PREDICATES.put(id, predicate);
    }

    public static Optional<SlotPredicate> getSlotPredicate(ResourceLocation id) {
        return Optional.ofNullable(PREDICATES.get(id));
    }

    public static boolean evaluatePredicateSet(Set<ResourceLocation> set, ItemStack stack, SlotReference ref, LivingEntity entity) {
        for (ResourceLocation id : set) {
            Optional<SlotPredicate> predicate = getSlotPredicate(id);
            if (predicate.isPresent()) {
                Boolean result = predicate.get().test(stack, ref, entity);
                if (result != null) {
                    return result;
                }
            }
        }
        return false;
    }

    static {
        registerSlotPredicate(id("all"), (stack, ref, entity) -> true);
        registerSlotPredicate(id("none"), (stack, ref, entity) -> false);

        registerSlotPredicate(id("tag"), (stack, ref, entity) -> {
            SlotType slot = ref.inventory().getSlotType();
            TagKey<Item> slotTag = TagKey.create(Registries.ITEM, id(slot.getId()));
            TagKey<Item> allTag = TagKey.create(Registries.ITEM, id("all"));

            if (stack.is(slotTag) || stack.is(allTag)) {
                return true;
            }
            return null;
        });
    }

    public static boolean hasReadyItems(SlotType type) {
        if (!type.getValidatorPredicates().contains(id("tag"))) {
            return true;
        }
        TagKey<Item> slotTag = TagKey.create(Registries.ITEM, id(type.getId()));
        TagKey<Item> allTag = TagKey.create(Registries.ITEM, id("all"));
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.getTag(slotTag).map(set -> set.size() > 0).orElse(false)
                || net.minecraft.core.registries.BuiltInRegistries.ITEM.getTag(allTag).map(set -> set.size() > 0).orElse(false);
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(dev.lukamadness.madnesscore.common.MadnessCoreCommon.MOD_ID, path);
    }
}
