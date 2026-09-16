package dev.lukamadness.madnesscore.common.api.slots;

import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public interface SlotComponent {
    LivingEntity getEntity();

    Map<String, SlotGroup> getGroups();

    Map<String, Map<String, SlotInventory>> getInventory();

    void update();

    void addTemporaryModifiers(Multimap<String, AttributeModifier> modifiers);

    void addPersistentModifiers(Multimap<String, AttributeModifier> modifiers);

    void removeModifiers(Multimap<String, AttributeModifier> modifiers);

    void clearModifiers();

    Multimap<String, AttributeModifier> getModifiers();

    boolean isEquipped(Predicate<ItemStack> predicate);

    default boolean isEquipped(Item item) {
        return isEquipped(stack -> stack.getItem() == item);
    }

    List<Pair<SlotReference, ItemStack>> getEquipped(Predicate<ItemStack> predicate);

    default List<Pair<SlotReference, ItemStack>> getEquipped(Item item) {
        return getEquipped(stack -> stack.getItem() == item);
    }

    default List<Pair<SlotReference, ItemStack>> getAllEquipped() {
        return getEquipped(stack -> !stack.isEmpty());
    }

    void forEach(BiConsumer<SlotReference, ItemStack> consumer);

    Set<SlotInventory> getTrackingUpdates();

    void clearCachedModifiers();

    void readFromNbt(CompoundTag tag, HolderLookup.Provider lookup);

    void writeToNbt(CompoundTag tag, HolderLookup.Provider lookup);
}
