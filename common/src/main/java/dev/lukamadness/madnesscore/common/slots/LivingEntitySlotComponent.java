package dev.lukamadness.madnesscore.common.slots;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mojang.datafixers.util.Pair;
import dev.lukamadness.madnesscore.common.api.slots.SlotComponent;
import dev.lukamadness.madnesscore.common.api.slots.SlotGroup;
import dev.lukamadness.madnesscore.common.api.slots.SlotInventory;
import dev.lukamadness.madnesscore.common.api.slots.SlotReference;
import dev.lukamadness.madnesscore.common.api.slots.SlotType;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Implementacion de {@link SlotComponent} para cualquier {@link LivingEntity}. No depende de
 * ningun framework de "entity components" externo (a diferencia del Trinkets original, que usa
 * Cardinal Components API y por eso es exclusivo de Fabric): es un POJO plano al que cada loader
 * decide como adjuntarse (ver {@code ISlotAttachment}).
 * <p>
 * Portado de dev.emi.trinkets.api.LivingEntityTrinketComponent.
 */
public class LivingEntitySlotComponent implements SlotComponent {

    private Map<String, Map<String, SlotInventory>> inventory = new HashMap<>();
    private final Set<SlotInventory> trackingUpdates = new HashSet<>();
    private final Map<String, SlotGroup> groups = new HashMap<>();
    private final LivingEntity entity;

    /**
     * Ultimo stack visto en cada slot ("grupo/slot/indice" -> stack), usado por {@link SlotTicker}
     * para detectar equipar/desequipar entre ticks. Estado interno, no se persiste.
     */
    final Map<String, ItemStack> lastEquipped = new HashMap<>();

    /**
     * FIX (issue #4 - "al unirte con algo equipado, vuelve a sonar el equipamiento"): tanto al
     * cargar la entidad desde disco ({@code readAdditionalSaveData}) como al recibir el paquete
     * de sync inicial en el cliente, esta instancia de {@link LivingEntitySlotComponent} es
     * NUEVA - se construye en blanco y recien despues {@link #readFromNbt} le carga los items
     * dentro de los {@code SlotInventory}. Como {@link #lastEquipped} arranca vacio, el primer
     * {@link SlotTicker#tick} que corre despues del load ve cada item ya equipado como "recien
     * equipado" (oldStack=EMPTY, newStack=item cargado) y dispara TODO el flujo de equipar - lo
     * cual es correcto para los modificadores de atributo transitorios (no se persisten, hay que
     * reaplicarlos) pero NO para el sonido (el jugador no acaba de equiparse nada, solo se
     * reconecto). Este flag deja que {@link SlotTicker} distinga ambos casos: se prende aca, al
     * terminar de cargar el NBT, y {@code SlotTicker} lo apaga despues de procesar ese primer
     * tick, saltando unicamente la reproduccion de sonido mientras esta prendido.
     */
    boolean justLoaded = false;

    public LivingEntitySlotComponent(LivingEntity entity) {
        this.entity = entity;
        this.update();
    }

    @Override
    public LivingEntity getEntity() {
        return this.entity;
    }

    @Override
    public Map<String, SlotGroup> getGroups() {
        return this.groups;
    }

    @Override
    public Map<String, Map<String, SlotInventory>> getInventory() {
        return inventory;
    }

    @Override
    public void update() {
        Map<String, SlotGroup> entitySlots = SlotsApi.getEntitySlots(this.entity);
        groups.clear();
        Map<String, Map<String, SlotInventory>> newInventory = new HashMap<>();
        for (Map.Entry<String, SlotGroup> group : entitySlots.entrySet()) {
            String groupKey = group.getKey();
            SlotGroup groupValue = group.getValue();
            Map<String, SlotInventory> oldGroup = this.inventory.get(groupKey);
            groups.put(groupKey, groupValue);
            for (Map.Entry<String, SlotType> slot : groupValue.getSlots().entrySet()) {
                SlotInventory inv = new SlotInventory(slot.getValue(), this, e -> this.trackingUpdates.add(e));
                if (oldGroup != null) {
                    SlotInventory oldInv = oldGroup.get(slot.getKey());
                    if (oldInv != null) {
                        inv.copyFrom(oldInv);
                        for (int i = 0; i < oldInv.getContainerSize(); i++) {
                            ItemStack stack = oldInv.getItem(i).copy();
                            if (i < inv.getContainerSize()) {
                                inv.setItem(i, stack);
                            } else if (!stack.isEmpty()) {
                                if (this.entity instanceof Player player) {
                                    player.getInventory().placeItemBackInInventory(stack);
                                } else if (this.entity.level() instanceof ServerLevel serverLevel) {
                                    this.entity.spawnAtLocation(stack);
                                }
                            }
                        }
                    }
                }
                newInventory.computeIfAbsent(group.getKey(), k -> new HashMap<>()).put(slot.getKey(), inv);
            }
        }
        this.inventory = newInventory;
    }

    @Override
    public void clearCachedModifiers() {
        for (Map<String, SlotInventory> group : this.getInventory().values()) {
            for (SlotInventory inv : group.values()) {
                inv.clearCachedModifiers();
            }
        }
    }

    @Override
    public Set<SlotInventory> getTrackingUpdates() {
        return this.trackingUpdates;
    }

    @Override
    public void addTemporaryModifiers(Multimap<String, AttributeModifier> modifiers) {
        forEachTargetInventory(modifiers, (inv, modifier) -> inv.addModifier(modifier));
    }

    @Override
    public void addPersistentModifiers(Multimap<String, AttributeModifier> modifiers) {
        forEachTargetInventory(modifiers, (inv, modifier) -> inv.addPersistentModifier(modifier));
    }

    @Override
    public void removeModifiers(Multimap<String, AttributeModifier> modifiers) {
        forEachTargetInventory(modifiers, (inv, modifier) -> inv.removeModifier(modifier.id()));
    }

    private void forEachTargetInventory(Multimap<String, AttributeModifier> modifiers, BiConsumer<SlotInventory, AttributeModifier> action) {
        for (Map.Entry<String, Collection<AttributeModifier>> entry : modifiers.asMap().entrySet()) {
            String[] keys = entry.getKey().split("/");
            if (keys.length < 2) {
                continue;
            }
            String group = keys[0];
            String slot = keys[1];
            Map<String, SlotInventory> groupInv = this.inventory.get(group);
            if (groupInv != null) {
                SlotInventory inv = groupInv.get(slot);
                if (inv != null) {
                    for (AttributeModifier modifier : entry.getValue()) {
                        action.accept(inv, modifier);
                    }
                }
            }
        }
    }

    @Override
    public Multimap<String, AttributeModifier> getModifiers() {
        Multimap<String, AttributeModifier> result = HashMultimap.create();
        for (Map.Entry<String, Map<String, SlotInventory>> group : this.getInventory().entrySet()) {
            for (Map.Entry<String, SlotInventory> slotType : group.getValue().entrySet()) {
                result.putAll(group.getKey() + "/" + slotType.getKey(), slotType.getValue().getModifiers().values());
            }
        }
        return result;
    }

    @Override
    public void clearModifiers() {
        for (Map<String, SlotInventory> group : this.getInventory().values()) {
            for (SlotInventory inv : group.values()) {
                inv.clearModifiers();
            }
        }
    }

    @Override
    public void readFromNbt(CompoundTag tag, HolderLookup.Provider lookup) {
        NonNullList<ItemStack> dropped = NonNullList.create();
        for (String groupKey : tag.getAllKeys()) {
            CompoundTag groupTag = tag.getCompound(groupKey);
            Map<String, SlotInventory> groupSlots = this.inventory.get(groupKey);
            for (String slotKey : groupTag.getAllKeys()) {
                CompoundTag slotTag = groupTag.getCompound(slotKey);
                SlotInventory inv = groupSlots != null ? groupSlots.get(slotKey) : null;

                if (inv != null) {
                    inv.fromTag(slotTag.getCompound("Metadata"));
                    NonNullList<ItemStack> items = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
                    ContainerHelper.loadAllItems(slotTag.getCompound("Items"), items, lookup);
                    for (int i = 0; i < items.size(); i++) {
                        inv.setItem(i, items.get(i));
                    }
                } else {
                    NonNullList<ItemStack> items = NonNullList.create();
                    ContainerHelper.loadAllItems(slotTag.getCompound("Items"), items, lookup);
                    dropped.addAll(items);
                }
            }
        }
        if (this.entity.level() instanceof ServerLevel serverLevel) {
            for (ItemStack stack : dropped) {
                if (!stack.isEmpty()) {
                    this.entity.spawnAtLocation(stack);
                }
            }
        }
        // Ver el javadoc de "justLoaded": el proximo SlotTicker#tick va a ver estos items como
        // "recien equipados" contra un lastEquipped vacio; que reaplique modificadores esta bien,
        // pero no tiene que sonar como si el jugador hubiera equipado algo de nuevo.
        this.justLoaded = true;
    }

    @Override
    public void writeToNbt(CompoundTag tag, HolderLookup.Provider lookup) {
        for (Map.Entry<String, Map<String, SlotInventory>> group : this.getInventory().entrySet()) {
            CompoundTag groupTag = new CompoundTag();
            for (Map.Entry<String, SlotInventory> slot : group.getValue().entrySet()) {
                CompoundTag slotTag = new CompoundTag();
                SlotInventory inv = slot.getValue();
                CompoundTag itemsTag = new CompoundTag();
                NonNullList<ItemStack> items = NonNullList.withSize(inv.getContainerSize(), ItemStack.EMPTY);
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    items.set(i, inv.getItem(i));
                }
                ContainerHelper.saveAllItems(itemsTag, items, lookup);
                slotTag.put("Metadata", inv.toTag());
                slotTag.put("Items", itemsTag);
                groupTag.put(slot.getKey(), slotTag);
            }
            tag.put(group.getKey(), groupTag);
        }
    }

    @Override
    public boolean isEquipped(Predicate<ItemStack> predicate) {
        for (Map<String, SlotInventory> group : this.getInventory().values()) {
            for (SlotInventory inv : group.values()) {
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    if (predicate.test(inv.getItem(i))) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public List<Pair<SlotReference, ItemStack>> getEquipped(Predicate<ItemStack> predicate) {
        List<Pair<SlotReference, ItemStack>> list = new ArrayList<>();
        forEach((slotReference, itemStack) -> {
            if (predicate.test(itemStack)) {
                list.add(new Pair<>(slotReference, itemStack));
            }
        });
        return list;
    }

    @Override
    public void forEach(BiConsumer<SlotReference, ItemStack> consumer) {
        for (Map<String, SlotInventory> group : this.getInventory().values()) {
            for (SlotInventory inv : group.values()) {
                for (int i = 0; i < inv.getContainerSize(); i++) {
                    consumer.accept(new SlotReference(inv, i), inv.getItem(i));
                }
            }
        }
    }
}