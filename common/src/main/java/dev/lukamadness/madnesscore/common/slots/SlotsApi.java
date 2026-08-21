package dev.lukamadness.madnesscore.common.slots;

import com.google.common.collect.ImmutableMap;
import dev.lukamadness.madnesscore.common.api.slots.SlotItem;
import dev.lukamadness.madnesscore.common.platform.Services;
import dev.lukamadness.madnesscore.common.api.slots.SlotGroup;
import dev.lukamadness.madnesscore.common.api.slots.SlotReference;
import dev.lukamadness.madnesscore.common.api.slots.SlotType;
import dev.lukamadness.madnesscore.common.api.slots.Slottable;
import dev.lukamadness.madnesscore.common.api.slots.SlotComponent;
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

/**
 * Punto de entrada publico al sistema de slots data-driven de Madness Core (fusiona lo que en
 * Trinkets serian {@code TrinketsApi} + {@code EntitySlotLoader}).
 * <p>
 * Los slots se definen via datapack:
 * <ul>
 *     <li>{@code data/<namespace>/slots/<grupo>/group.json} - metadata del grupo (slot_id, order)</li>
 *     <li>{@code data/<namespace>/slots/<grupo>/<slot>.json} - definicion de un slot individual</li>
 *     <li>{@code data/<namespace>/slot_assignments/*.json} - asigna slots a tipos de entidad (o tags con "#")</li>
 * </ul>
 * Cada lado (cliente/servidor) mantiene su propia copia recargable de forma independiente, igual que
 * el sistema original de Trinkets, para no depender de que el lado contrario ya haya sincronizado datos.
 * <p>
 * Ademas expone el registro de comportamiento ({@link Slottable}) por item y el acceso al
 * {@link SlotComponent} de una entidad (delegado al servicio de plataforma {@code ISlotAttachment}).
 */
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

    // ---------------------------------------------------------------------------------------
    // Datos (grupos / asignacion a entidades) - portado de TrinketSlotsApi (Fase 1)
    // ---------------------------------------------------------------------------------------

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

    // ---------------------------------------------------------------------------------------
    // Comportamiento de items (Slottable) - Fase 2
    // ---------------------------------------------------------------------------------------

    /**
     * Registra el comportamiento de un item equipable en un slot. {@link SlotItem}
     * lo hace automaticamente en su constructor; si tu item extiende otra clase, llama esto a mano
     * (por ejemplo desde el registro del item, pasandote a ti mismo si implementas {@link Slottable}).
     */
    public static void registerSlottable(Item item, Slottable slottable) {
        SLOTTABLES.put(item, slottable);
    }

    public static Slottable getSlottable(Item item) {
        return SLOTTABLES.getOrDefault(item, DEFAULT_SLOTTABLE);
    }

    public static Slottable getDefaultSlottable() {
        return DEFAULT_SLOTTABLE;
    }

    // ---------------------------------------------------------------------------------------
    // Componente por entidad - delega al servicio de plataforma (mixin en Fabric / Data Attachment en NeoForge)
    // ---------------------------------------------------------------------------------------

    /**
     * @return el {@link SlotComponent} de la entidad, si el loader ya lo adjunto (siempre deberia
     * estar presente para toda {@link LivingEntity}; se expone como Optional por si se llama en un
     * momento donde la entidad todavia no fue inicializada del todo).
     */
    public static Optional<SlotComponent> getSlotComponent(LivingEntity entity) {
        return Optional.ofNullable(Services.SLOT_ATTACHMENT.getOrCreate(entity));
    }

    /**
     * Notifica que un item equipado en un slot dinamico acaba de romperse (llegar a 0 de
     * durabilidad), para sincronizar el efecto de rotura (sonido + particulas) a los clientes que
     * ven a la entidad. Portado de {@code TrinketsApi#onTrinketBroken}; llamar SOLO del lado
     * servidor, tipicamente desde el callback de {@code ItemStack#hurtAndBreak(int, LivingEntity,
     * EquipmentSlot)} (o la sobrecarga equivalente) de un item que implementa {@link Slottable} y
     * tiene durabilidad.
     */
    public static void onSlotItemBroken(ItemStack stack, SlotReference ref, LivingEntity entity) {
        dev.lukamadness.madnesscore.common.slots.network.SlotNetworking.sendBreak(entity, ref);
    }

    // ---------------------------------------------------------------------------------------
    // Predicados data-driven (quick_move_predicates / validator_predicates / tooltip_predicates)
    // ---------------------------------------------------------------------------------------

    @FunctionalInterface
    public interface SlotPredicate {
        /**
         * @return TRUE si el predicado aprueba explicitamente, FALSE si lo rechaza explicitamente,
         * o null si es indiferente (se sigue evaluando el resto del set, en orden).
         */
        Boolean test(ItemStack stack, SlotReference ref, LivingEntity entity);
    }

    public static void registerSlotPredicate(ResourceLocation id, SlotPredicate predicate) {
        PREDICATES.put(id, predicate);
    }

    public static Optional<SlotPredicate> getSlotPredicate(ResourceLocation id) {
        return Optional.ofNullable(PREDICATES.get(id));
    }

    /**
     * Evalua un set de predicados en orden hasta que uno devuelva un resultado no-nulo (TRUE/FALSE).
     * Si ninguno se pronuncia, el resultado por defecto es {@code false}.
     */
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

        // Por defecto, un item entra en un slot si esta en el tag data/<namespace>/tags/item/<grupo>/<slot>.json
        // (namespace del propio slot) o en el tag "<namespace>:all" de items.
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

    /**
     * @return si {@code type} tiene, ahora mismo, al menos un item que pueda llegar a entrar en
     * el. Usado para ocultar/deshabilitar (ver {@code MixinPlayerDynamicSlotActive}) slot types
     * "vacios" - definidos en el datapack pero sin ningun tag de items cargado todavia (ej:
     * "head/face", "legs/belt" - no tienen {@code data/madnesscore/tags/item/<grupo>/<tipo>.json},
     * a diferencia de "head/hat", "chest/back" o "feet/shoes" que si).
     * <p>
     * Solo evaluamos esto para slot types cuyo {@code validator_predicates} use el predicado
     * data-driven por defecto ({@code madnesscore:tag}) - es el unico caso donde "esta vacio" se
     * puede saber de antemano mirando UN tag, sin depender de la entidad ni del stack. Un slot con
     * un predicado distinto (custom, o {@code madnesscore:all}) puede aceptar items sin depender
     * de ningun tag, asi que ahi no hay forma segura de decidir esto sin ejecutar el predicado
     * item por item contra todo el registro - en ese caso preferimos NO ocultar nada (mismo
     * comportamiento que antes de este metodo) antes que arriesgarnos a esconder un slot que en
     * realidad si funciona.
     */
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