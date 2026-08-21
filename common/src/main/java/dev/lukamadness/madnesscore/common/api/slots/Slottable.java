package dev.lukamadness.madnesscore.common.api.slots;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Comportamiento de un item equipable en un slot: hooks de ciclo de vida (equipar, desequipar,
 * tick, validacion, modificadores de atributos). Portado de dev.emi.trinkets.api.Trinket.
 * <p>
 * Se implementa directamente sobre una subclase de {@link net.minecraft.world.item.Item} (ver
 * {@link SlotItem} para una base conveniente) o, si el item ya extiende otra clase, se puede
 * implementar la interfaz directamente sobre esa clase ("duck typing" al estilo Trinkets).
 */
public interface Slottable {

    /**
     * Llamado cada tick, en cliente y servidor, mientras el stack esta equipado.
     */
    default void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
    }

    /**
     * Llamado cuando una entidad equipa este item en un slot.
     */
    default void onEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
    }

    /**
     * Llamado cuando una entidad desequipa este item de un slot.
     */
    default void onUnequip(ItemStack stack, SlotReference slot, LivingEntity entity) {
    }

    /**
     * @return si la entidad puede equipar este stack en el slot indicado.
     */
    default boolean canEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        return true;
    }

    /**
     * @return si la entidad puede desequipar este stack del slot indicado. Por defecto replica el
     * comportamiento de Trinkets: si el stack tiene un encantamiento con el efecto
     * {@code PREVENT_ARMOR_CHANGE} (ej. Maldicion de Atadura), no se puede desequipar salvo que la
     * entidad sea un jugador en modo creativo.
     */
    default boolean canUnequip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        return !EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)
                || (entity instanceof Player player && player.isCreative());
    }

    /**
     * @return si este item puede auto-equiparse al primer slot disponible al usarlo (click derecho).
     */
    default boolean canEquipFromUse(ItemStack stack, LivingEntity entity) {
        return false;
    }

    /**
     * @return el sonido a reproducir al equipar este item.
     * <p>
     * NOTA: en Trinkets (que en el zip de referencia corre sobre 1.21.5) este metodo cae por
     * defecto al data component {@code minecraft:equippable} del item ({@code Equippable#equipSound()}).
     * Ese data component (clase {@code net.minecraft.world.item.component.Equippable}) recien se
     * agrego en Minecraft 1.21.2 y no existe en 1.21.1, la version de este proyecto - por eso no se
     * puede replicar ESE fallback especifico aca sin backportearlo a mano. En su lugar se usa un
     * sonido generico de equipar (igual de audible que el fallback de Trinkets, aunque no varie
     * segun el tipo de armadura del item) para que ningun SlotItem quede mudo por defecto - antes
     * de este cambio, CUALQUIER item que no sobreescribiera este metodo a mano jamas sonaba, ni
     * siquiera al auto-equiparse con click derecho via SlotItem#equipItem. Si en el futuro este mod
     * actualiza a 1.21.2+, se puede agregar el fallback real usando
     * {@code stack.get(DataComponents.EQUIPPABLE)}.
     * <p>
     * NOTA (no se puede compilar en este entorno): confirmar en el IDE que
     * {@code SoundEvents.ARMOR_EQUIP_GENERIC} es un {@code Holder<SoundEvent>} en mappings
     * oficiales 1.21.1 (deberia serlo, ya que SoundEvent es un registro dinamico desde 1.19.3); si
     * el campo resulta ser un {@code SoundEvent} plano en vez de un {@code Holder}, envolverlo con
     * {@code BuiltInRegistries.SOUND_EVENT.wrapAsHolder(...)}.
     */
    default Optional<Holder<SoundEvent>> getEquipSound(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (stack.getItem() instanceof net.minecraft.world.item.Equipable equipable) {
            return Optional.of(equipable.getEquipSound());
        }
        return Optional.of(net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_GENERIC);
    }
    /**
     * @return los modificadores de atributo que este item aporta mientras esta equipado en el
     * slot indicado. La implementacion debe ser pura (sin efectos secundarios); si los modificadores
     * no dependen del stack/slot/entidad, se recomienda cachear usando slotIdentifier como clave.
     *
     * @see SlotAttributes#addSlotModifier
     */
    default Multimap<Holder<Attribute>, AttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, ResourceLocation slotIdentifier) {
        Multimap<Holder<Attribute>, AttributeModifier> map = Multimaps.newMultimap(Maps.newLinkedHashMap(), ArrayList::new);

        if (stack.getItem() instanceof net.minecraft.world.item.Equipable equipable) {
            net.minecraft.world.entity.EquipmentSlot naturalSlot = equipable.getEquipmentSlot();

            stack.forEachModifier(naturalSlot, (attribute, modifier) -> {
                // FIX (issue #4 - "dos cascos se pisan"): el id de estos modificadores viene
                // fijo desde vainilla (ej. "minecraft:armor.helmet"), pensado para que solo
                // exista un item en ese EquipmentSlot a la vez. Acá pueden convivir varios
                // (slot real + slot de trinkets), así que hace falta un id único POR SLOT -
                // si no, AttributeInstance los confunde entre sí: se pisan al equipar y se
                // borran juntos al desequipar cualquiera de los dos. slotIdentifier ya es
                // único por SlotReference, así que lo combinamos con el id original del
                // modificador para generar uno nuevo, único por (slot, atributo).
                ResourceLocation uniqueId = ResourceLocation.fromNamespaceAndPath(
                        slotIdentifier.getNamespace(),
                        slotIdentifier.getPath() + "/" + modifier.id().getNamespace() + "_" + modifier.id().getPath());
                map.put(attribute, new AttributeModifier(uniqueId, modifier.amount(), modifier.operation()));
            });
        }

        return map;
    }

    /**
     * Llamado cuando el stack equipado se rompe (durabilidad 0). Se invoca del lado cliente, tras
     * recibir el {@code SlotBreakPayload} que dispara
     * {@link dev.lukamadness.madnesscore.common.slots.SlotsApi#onSlotItemBroken} en el
     * servidor (ver {@code SlotNetworking#handleBreakOnClient}) - exactamente igual que Trinkets,
     * cuyo efecto de rotura vainilla tambien es puramente cliente.
     * <p>
     * El default reproduce un sonido de rotura generico y unas particulas del item, imitando el
     * efecto vainilla de romper una pieza de equipo (Trinkets logra lo mismo invocando el metodo
     * protegido {@code LivingEntity#playEquipmentBreakEffects} via mixin accessor; aca se logra el
     * mismo resultado visual sin necesitar un accessor a un metodo interno, usando solo API
     * publica y estable: {@link Level#playLocalSound} + {@link Level#addParticle}).
     */
    default void onBreak(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (stack.isEmpty()) {
            return;
        }
        Level level = entity.level();
        if (!entity.isSilent()) {
            level.playLocalSound(entity.getX(), entity.getY(), entity.getZ(),
                    net.minecraft.sounds.SoundEvents.ITEM_BREAK, entity.getSoundSource(), 0.8F,
                    0.8F + level.getRandom().nextFloat() * 0.4F, false);
        }
        net.minecraft.core.particles.ItemParticleOption particle =
                new net.minecraft.core.particles.ItemParticleOption(net.minecraft.core.particles.ParticleTypes.ITEM, stack.copyWithCount(1));
        for (int i = 0; i < 5; i++) {
            double vx = (level.getRandom().nextDouble() - 0.5D) * 0.1D;
            double vy = level.getRandom().nextDouble() * 0.1D + 0.05D;
            double vz = (level.getRandom().nextDouble() - 0.5D) * 0.1D;
            level.addParticle(particle,
                    entity.getRandomX(1.0D), entity.getRandomY() + 0.5D, entity.getRandomZ(1.0D),
                    vx, vy, vz);
        }
    }

    default DropRule getDropRule(ItemStack stack, SlotReference slot, LivingEntity entity) {
        return DropRule.DEFAULT;
    }
}