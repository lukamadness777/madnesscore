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

/**
 * Componente de slots adjuntado a una entidad. La implementacion concreta
 * ({@link dev.lukamadness.madnesscore.common.slots.LivingEntitySlotComponent})
 * vive una unica vez por entidad; el mecanismo de adjuntado varia por loader (mixin en Fabric,
 * Data Attachment en NeoForge) - ver {@code ISlotAttachment} en platform/services.
 * <p>
 * Portado de dev.emi.trinkets.api.TrinketComponent.
 */
public interface SlotComponent {

    LivingEntity getEntity();

    /**
     * @return los grupos de slots disponibles para la entidad, por nombre de grupo.
     */
    Map<String, SlotGroup> getGroups();

    /**
     * @return grupo -> slot -> inventario, para la entidad. Los inventarios respetan
     * modificadores de atributo de cantidad de slot.
     */
    Map<String, Map<String, SlotInventory>> getInventory();

    /**
     * Reconstruye los grupos/inventarios en base a los datos actuales de {@code SlotsApi} para el
     * tipo de entidad, preservando contenido e inventario existentes cuando sea posible.
     */
    void update();

    void addTemporaryModifiers(Multimap<String, AttributeModifier> modifiers);

    void addPersistentModifiers(Multimap<String, AttributeModifier> modifiers);

    void removeModifiers(Multimap<String, AttributeModifier> modifiers);

    void clearModifiers();

    Multimap<String, AttributeModifier> getModifiers();

    /**
     * @return si algun slot disponible para la entidad contiene un stack que matchea el predicado.
     */
    boolean isEquipped(Predicate<ItemStack> predicate);

    default boolean isEquipped(Item item) {
        return isEquipped(stack -> stack.getItem() == item);
    }

    /**
     * @return todos los slots que matchean el predicado, junto a su stack.
     */
    List<Pair<SlotReference, ItemStack>> getEquipped(Predicate<ItemStack> predicate);

    default List<Pair<SlotReference, ItemStack>> getEquipped(Item item) {
        return getEquipped(stack -> stack.getItem() == item);
    }

    default List<Pair<SlotReference, ItemStack>> getAllEquipped() {
        return getEquipped(stack -> !stack.isEmpty());
    }

    /**
     * Itera sobre todos los slots disponibles para la entidad.
     */
    void forEach(BiConsumer<SlotReference, ItemStack> consumer);

    Set<SlotInventory> getTrackingUpdates();

    void clearCachedModifiers();

    void readFromNbt(CompoundTag tag, HolderLookup.Provider lookup);

    void writeToNbt(CompoundTag tag, HolderLookup.Provider lookup);
}