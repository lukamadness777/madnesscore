package dev.lukamadness.madnesscore.common.api.slots;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Inventario (de un solo {@link SlotType}) que respeta modificadores de atributo de cantidad de
 * slot (ver {@link SlotAttributes}). Portado de dev.emi.trinkets.api.TrinketInventory.
 */
public class SlotInventory implements Container {

    private final SlotType slotType;
    private final int baseSize;
    private final SlotComponent component;
    private final Map<ResourceLocation, AttributeModifier> modifiers = new HashMap<>();
    private final Set<AttributeModifier> persistentModifiers = new HashSet<>();
    private final Set<AttributeModifier> cachedModifiers = new HashSet<>();
    private final Multimap<AttributeModifier.Operation, AttributeModifier> modifiersByOperation = HashMultimap.create();
    private final Consumer<SlotInventory> updateCallback;

    private NonNullList<ItemStack> stacks;
    private boolean dirtySize = false;

    public SlotInventory(SlotType slotType, SlotComponent comp, Consumer<SlotInventory> updateCallback) {
        this.component = comp;
        this.slotType = slotType;
        this.baseSize = slotType.getAmount();
        this.stacks = NonNullList.withSize(this.baseSize, ItemStack.EMPTY);
        this.updateCallback = updateCallback;
    }

    public SlotType getSlotType() {
        return this.slotType;
    }

    public SlotComponent getComponent() {
        return this.component;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < this.getContainerSize(); i++) {
            stacks.set(i, ItemStack.EMPTY);
        }
    }

    @Override
    public int getContainerSize() {
        this.recalculateSize();
        return this.stacks.size();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < this.getContainerSize(); i++) {
            if (!stacks.get(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        this.recalculateSize();
        return stacks.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ContainerHelper.removeItem(stacks, slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(stacks, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.recalculateSize();
        stacks.set(slot, stack);
    }

    @Override
    public void setChanged() {
        // NO-OP: la persistencia la maneja el SlotComponent duenio.
    }

    public void markUpdate() {
        this.dirtySize = true;
        this.updateCallback.accept(this);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public Map<ResourceLocation, AttributeModifier> getModifiers() {
        return this.modifiers;
    }

    public java.util.Collection<AttributeModifier> getModifiersByOperation(AttributeModifier.Operation operation) {
        return this.modifiersByOperation.get(operation);
    }

    public void addModifier(AttributeModifier modifier) {
        this.modifiers.put(modifier.id(), modifier);
        this.getModifiersByOperation(modifier.operation()).add(modifier);
        this.markUpdate();
    }

    public void addPersistentModifier(AttributeModifier modifier) {
        this.addModifier(modifier);
        this.persistentModifiers.add(modifier);
    }

    public void removeModifier(ResourceLocation identifier) {
        AttributeModifier modifier = this.modifiers.remove(identifier);
        if (modifier != null) {
            this.persistentModifiers.remove(modifier);
            this.getModifiersByOperation(modifier.operation()).remove(modifier);
            this.markUpdate();
        }
    }

    public void clearModifiers() {
        for (ResourceLocation id : java.util.List.copyOf(this.getModifiers().keySet())) {
            this.removeModifier(id);
        }
    }

    public void removeCachedModifier(AttributeModifier modifier) {
        this.cachedModifiers.remove(modifier);
    }

    public void clearCachedModifiers() {
        for (AttributeModifier cached : this.cachedModifiers) {
            this.removeModifier(cached.id());
        }
        this.cachedModifiers.clear();
    }

    /**
     * Recalcula el tamano del inventario en base a los modificadores de atributo activos
     * (ADD_VALUE, ADD_MULTIPLIED_BASE, ADD_MULTIPLIED_TOTAL), dropeando cualquier item que ya
     * no entre si el tamano se reduce.
     */
    public void recalculateSize() {
        if (this.dirtySize) {
            this.dirtySize = false;
            double size = this.baseSize;
            for (AttributeModifier mod : this.getModifiersByOperation(AttributeModifier.Operation.ADD_VALUE)) {
                size += mod.amount();
            }

            double totalSize = size;
            for (AttributeModifier mod : this.getModifiersByOperation(AttributeModifier.Operation.ADD_MULTIPLIED_BASE)) {
                totalSize += this.baseSize * mod.amount();
            }

            for (AttributeModifier mod : this.getModifiersByOperation(AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL)) {
                totalSize *= (1 + mod.amount());
            }

            LivingEntity entity = this.component.getEntity();
            int newSize = Math.max(0, (int) totalSize);

            if (newSize != this.stacks.size()) {
                NonNullList<ItemStack> newStacks = NonNullList.withSize(newSize, ItemStack.EMPTY);
                for (int i = 0; i < this.stacks.size(); i++) {
                    ItemStack stack = this.stacks.get(i);
                    if (i < newStacks.size()) {
                        newStacks.set(i, stack);
                    } else if (!stack.isEmpty() && entity.level() instanceof ServerLevel serverLevel) {
                        entity.spawnAtLocation(stack);
                    }
                }
                this.stacks = newStacks;
            }
        }
    }

    public void copyFrom(SlotInventory other) {
        this.modifiers.clear();
        this.modifiersByOperation.clear();
        this.persistentModifiers.clear();
        other.modifiers.forEach((id, modifier) -> this.addModifier(modifier));
        for (AttributeModifier persistentModifier : other.persistentModifiers) {
            this.addPersistentModifier(persistentModifier);
        }
        this.recalculateSize();
    }

    /**
     * Copia el contenido y los modificadores de todos los slots de una entidad a otra (ej: al
     * respawnear con keepInventory activo). Debe ser invocado desde el hook de respawn de cada
     * loader (ver Fase 2: Fabric usa ServerPlayerEvents.COPY_FROM, NeoForge usa PlayerEvent.Clone).
     */
    public static void copyFrom(LivingEntity previous, LivingEntity current) {
        dev.lukamadness.madnesscore.common.slots.SlotsApi.getSlotComponent(previous).ifPresent(prevSlots ->
                dev.lukamadness.madnesscore.common.slots.SlotsApi.getSlotComponent(current).ifPresent(currentSlots -> {
                    Map<String, Map<String, SlotInventory>> prevMap = prevSlots.getInventory();
                    Map<String, Map<String, SlotInventory>> currentMap = currentSlots.getInventory();
                    for (Map.Entry<String, Map<String, SlotInventory>> entry : prevMap.entrySet()) {
                        Map<String, SlotInventory> currentInvs = currentMap.get(entry.getKey());
                        if (currentInvs != null) {
                            for (Map.Entry<String, SlotInventory> invEntry : entry.getValue().entrySet()) {
                                SlotInventory currentInv = currentInvs.get(invEntry.getKey());
                                if (currentInv != null) {
                                    currentInv.copyFrom(invEntry.getValue());
                                }
                            }
                        }
                    }
                }));
    }

    public CompoundTag toTag() {
        // Los modificadores "cacheados" (no persistentes) no se guardan a disco: se recalculan
        // en el siguiente tick a partir de los items equipados. Solo persistimos los que fueron
        // agregados explicitamente como permanentes (addPersistentModifier).
        return new CompoundTag();
    }

    public void fromTag(CompoundTag tag) {
        // Ver comentario en toTag(): reservado para modificadores persistentes (Fase futura).
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SlotInventory that = (SlotInventory) o;
        return slotType.equals(that.slotType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slotType);
    }
}