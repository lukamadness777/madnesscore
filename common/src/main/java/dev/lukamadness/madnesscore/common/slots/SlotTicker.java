package dev.lukamadness.madnesscore.common.slots;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import dev.lukamadness.madnesscore.common.api.slots.SlotAttributes;
import dev.lukamadness.madnesscore.common.api.slots.SlotInventory;
import dev.lukamadness.madnesscore.common.api.slots.SlotReference;
import dev.lukamadness.madnesscore.common.api.slots.Slottable;
import dev.lukamadness.madnesscore.common.api.slots.SlotType;
import dev.lukamadness.madnesscore.common.api.slots.event.SlotEquipCallback;
import dev.lukamadness.madnesscore.common.api.slots.event.SlotUnequipCallback;
import dev.lukamadness.madnesscore.common.slots.network.SlotNetworking;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Logica de tick compartida entre loaders: detecta cambios de equipo entre el tick anterior y el
 * actual, aplica/retira modificadores de atributo, y llama a {@link Slottable#tick}
 * y a los callbacks de equipar/desequipar.
 * <p>
 * Cada loader debe invocar {@link #tick(LivingEntity)} una vez por entidad viva y por tick:
 * <ul>
 *     <li>Fabric: inject TAIL en {@code LivingEntity#tick} via mixin.</li>
 *     <li>NeoForge: listener de {@code LivingTickEvent} en el bus de NeoForge.</li>
 * </ul>
 * La sincronizacion de red de estos cambios hacia el cliente queda para la Fase 4 (networking).
 * <p>
 * Portado de la parte no relacionada a red de dev.emi.trinkets.mixin.LivingEntityMixin#tick.
 * <p>
 * NOTA (issue #2 - "la armadura no da stats/efectos reales"): este archivo tuvo antes un
 * {@code mirrorToVanillaEquipment} que copiaba el stack equipado en un slot de trinkets hacia el
 * {@code EquipmentSlot} vainilla real de la entidad ({@code entity.setItemSlot(...)}). Eso
 * causaba un bug de duplicacion (el item quedaba a la vez en el inventario de trinkets Y en el
 * slot vainilla real, dos referencias fisicas independientes del mismo objeto). Fue eliminado.
 * La solucion correcta (que MC "trate" el item de trinkets como vainilla sin copiarlo a ningun
 * lado) vive ahora en {@link VanillaEquipmentMirror}, consultado desde
 * {@code MixinLivingEntityEquipmentMirror#getItemBySlot} - un espejo de solo lectura, nunca se
 * escribe nada en el equipo real de la entidad, asi que no hay forma de que el mismo item termine
 * existiendo en dos lados a la vez.
 */
public final class SlotTicker {

    private SlotTicker() {
    }

    public static void tick(LivingEntity entity) {
        if (entity.isRemoved()) {
            return;
        }

        SlotsApi.getSlotComponent(entity).ifPresent(component -> {
            if (!(component instanceof LivingEntitySlotComponent slots)) {
                return;
            }

            Map<String, ItemStack> newlyEquipped = new HashMap<>();
            // Array de 1 en vez de boolean suelto para poder mutarlo dentro de la lambda de abajo.
            boolean[] changedOnServer = {false};
            // FIX (issue #4): si la entidad acaba de cargar su NBT (login, cambio de dimension,
            // sync inicial al cliente), este tick va a ver todos los items ya equipados como
            // "recien equipados" contra un lastEquipped vacio. Se consume el flag ACA (una sola
            // vez, antes de procesar los slots) para que aplique a todo este tick por igual.
            boolean resyncFromLoad = slots.justLoaded;
            slots.justLoaded = false;

            slots.forEach((ref, newStack) -> {
                SlotInventory inventory = ref.inventory();
                SlotType slotType = inventory.getSlotType();
                int index = ref.index();
                String key = slotType.getGroup() + "/" + slotType.getName() + "/" + index;
                ItemStack oldStack = slots.lastEquipped.getOrDefault(key, ItemStack.EMPTY);
                ItemStack newStackCopy = newStack.copy();

                if (!ItemStack.matches(newStack, oldStack)) {
                    SlotsApi.getSlottable(oldStack.getItem()).onUnequip(oldStack, ref, entity);
                    SlotUnequipCallback.EVENT.invoker().onUnequip(oldStack, ref, entity);
                    SlotsApi.getSlottable(newStack.getItem()).onEquip(newStack, ref, entity);
                    SlotEquipCallback.EVENT.invoker().onEquip(newStack, ref, entity);

                    if (!entity.level().isClientSide()) {
                        // FIX (issues #1 y #4 - "encantamientos rotos" / "stats de armadura al
                        // doble"): si este item YA esta siendo espejado a un EquipmentSlot
                        // vainilla real (ver VanillaEquipmentMirror, mirror_vanilla_equipment:
                        // true), vainilla mismo lo detecta como "recien equipado" en
                        // LivingEntity#collectEquipmentChanges (porque ahora getItemBySlot lo
                        // devuelve) y aplica TODOS sus modificadores de atributo el solo -
                        // atributos base (Armor/Armor Toughness/Knockback Resistance) Y los que
                        // aportan encantamientos con efecto "attribute" (ej. Respiracion ->
                        // generic.oxygen_bonus). Ese es justamente el motivo de que el mixin de
                        // solo-lectura exista: "para que vainilla trate el item exactamente igual
                        // que si estuviera puesto de verdad - sin recrear cada efecto a mano".
                        // Si ADEMAS lo aplicamos aca nosotros, queda aplicado dos veces (una vez
                        // con el id original del modifier, vainilla; otra con id unico por slot,
                        // nuestro) sobre el mismo AttributeInstance - de ahi la armadura al doble,
                        // y de ahi tambien encantamientos con "attribute" quedando en un estado
                        // inconsistente (se pisan/reordenan entre ambos sistemas escribiendo y
                        // borrando el mismo tick). La solucion es dejar que UN SOLO sistema
                        // aplique stats para estos items: si esta espejado, es 100% trabajo de
                        // vainilla: no tocamos nada aca.
                        boolean oldMirrored = !oldStack.isEmpty() && isMirroredToVanilla(entity, oldStack);
                        boolean newMirrored = !newStack.isEmpty() && isMirroredToVanilla(entity, newStack);

                        if (!oldStack.isEmpty() && !oldMirrored) {
                            removeSlotModifiers(entity, slots, oldStack, ref);
                        }
                        if (!newStack.isEmpty() && !newMirrored) {
                            addSlotModifiers(entity, slots, newStack, ref);
                        }

                        // FIX (issue #1 - "hay que meter sonido de equipamiento"): antes SOLO
                        // sonaba al auto-equiparse con click derecho (ver SlotItem#equipItem), asi
                        // que arrastrar el item al slot dentro del GUI de accesorios (o
                        // quick-move/comandos) quedaba mudo. Este es el unico punto que detecta
                        // CUALQUIER cambio de equipo sin importar como se produjo, asi que es el
                        // lugar correcto para reproducir el sonido - igual que hace Trinkets en
                        // dev.emi.trinkets.mixin.LivingEntityMixin#tick.
                        // FIX (issue #4 - "al unirte con algo equipado, vuelve a sonar el
                        // equipamiento"): pero si este "cambio" es en realidad el resync del
                        // primer tick despues de cargar el NBT (ver justLoaded en
                        // LivingEntitySlotComponent), no es un equipamiento real - el jugador ya
                        // tenia el item puesto de una sesion anterior. En ese caso no suena nada.
                        if (!newStack.isEmpty() && !resyncFromLoad) {
                            SlotsApi.getSlottable(newStack.getItem()).getEquipSound(newStack, ref, entity)
                                    .ifPresent(sound -> entity.level().playSound(null,
                                            entity.getX(), entity.getY(), entity.getZ(),
                                            sound.value(), entity.getSoundSource(), 1.0F, 1.0F));
                        }

                        changedOnServer[0] = true;
                    }
                }

                SlotsApi.getSlottable(newStack.getItem()).tick(newStack, ref, entity);

                ItemStack tickedStack = inventory.getItem(index);
                // Evita re-disparar equip/unequip en items que se auto-mutan (ej: cambian de durabilidad)
                newlyEquipped.put(key, tickedStack.getItem() == newStackCopy.getItem() ? tickedStack.copy() : newStackCopy);
            });

            slots.lastEquipped.clear();
            slots.lastEquipped.putAll(newlyEquipped);

            // Fase 4: recien ahora que terminamos de procesar todos los slots del tick mandamos
            // (a lo sumo) un unico paquete con el estado completo, en vez de uno por slot que
            // cambio. syncToTrackers ya no-opea solo si entity.level() no es un ServerLevel.
            if (changedOnServer[0]) {
                SlotNetworking.syncToTrackers(entity);
            }
        });
    }

    /**
     * @return si {@code stack} es, ahora mismo, EXACTAMENTE la referencia que
     * {@code entity.getItemBySlot(...)} esta devolviendo para alguna de las 4 armor slots
     * vainilla - es decir, si {@link dev.lukamadness.madnesscore.common.mixin.MixinLivingEntityEquipmentMirror}
     * ya la esta espejando y por lo tanto vainilla ya la trata como equipada de verdad (con todo
     * lo que eso implica: atributos base + atributos de encantamiento). Comparacion por
     * referencia ({@code ==}), no por contenido - {@link VanillaEquipmentMirror} nunca copia el
     * stack, asi que es la forma correcta y barata de preguntar esto.
     */
    private static boolean isMirroredToVanilla(LivingEntity entity, ItemStack stack) {
        for (net.minecraft.world.entity.EquipmentSlot slot : new net.minecraft.world.entity.EquipmentSlot[]{
                net.minecraft.world.entity.EquipmentSlot.HEAD, net.minecraft.world.entity.EquipmentSlot.CHEST,
                net.minecraft.world.entity.EquipmentSlot.LEGS, net.minecraft.world.entity.EquipmentSlot.FEET}) {
            if (entity.getItemBySlot(slot) == stack) {
                return true;
            }
        }
        return false;
    }

    private static void removeSlotModifiers(LivingEntity entity, LivingEntitySlotComponent slots, ItemStack stack, SlotReference ref) {
        Multimap<Holder<Attribute>, AttributeModifier> map = SlotEquipLogic.getModifiers(stack, ref, entity);
        Multimap<String, AttributeModifier> slotMap = extractSlotCountModifiers(map);

        AttributeMap attributes = entity.getAttributes();
        map.asMap().forEach((attribute, modifiers) -> {
            AttributeInstance instance = attributes.getInstance(attribute);
            if (instance != null) {
                modifiers.forEach(modifier -> instance.removeModifier(modifier.id()));
            }
        });

        slots.removeModifiers(slotMap);
    }

    private static void addSlotModifiers(LivingEntity entity, LivingEntitySlotComponent slots, ItemStack stack, SlotReference ref) {
        Multimap<Holder<Attribute>, AttributeModifier> map = SlotEquipLogic.getModifiers(stack, ref, entity);
        Multimap<String, AttributeModifier> slotMap = extractSlotCountModifiers(map);

        AttributeMap attributes = entity.getAttributes();
        map.forEach((attribute, modifier) -> {
            AttributeInstance instance = attributes.getInstance(attribute);
            if (instance != null) {
                instance.removeModifier(modifier.id());
                instance.addTransientModifier(modifier);
            }
        });

        slots.addTemporaryModifiers(slotMap);
    }

    /**
     * Separa, de un multimapa de modificadores de atributo, los que en realidad apuntan a un
     * atributo "virtual" de cantidad de slot ({@link SlotAttributes.SlotEntityAttribute}) - esos
     * no se aplican como AttributeInstance real, sino que modifican el tamano de un SlotInventory.
     */
    private static Multimap<String, AttributeModifier> extractSlotCountModifiers(Multimap<Holder<Attribute>, AttributeModifier> map) {
        Multimap<String, AttributeModifier> slotMap = HashMultimap.create();
        Set<Holder<Attribute>> toRemove = new HashSet<>();
        for (Holder<Attribute> attribute : map.keySet()) {
            if (attribute.value() instanceof SlotAttributes.SlotEntityAttribute slotAttribute) {
                slotMap.putAll(slotAttribute.slot, map.get(attribute));
                toRemove.add(attribute);
            }
        }
        for (Holder<Attribute> attribute : toRemove) {
            map.removeAll(attribute);
        }
        return slotMap;
    }
}