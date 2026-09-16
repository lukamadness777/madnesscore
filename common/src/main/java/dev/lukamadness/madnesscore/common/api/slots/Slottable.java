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

public interface Slottable {
    default void tick(ItemStack stack, SlotReference slot, LivingEntity entity) {
    }

    default void onEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
    }

    default void onUnequip(ItemStack stack, SlotReference slot, LivingEntity entity) {
    }

    default boolean canEquip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        return true;
    }

    default boolean canUnequip(ItemStack stack, SlotReference slot, LivingEntity entity) {
        return !EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_ARMOR_CHANGE)
                || (entity instanceof Player player && player.isCreative());
    }

    default boolean canEquipFromUse(ItemStack stack, LivingEntity entity) {
        return false;
    }

    default Optional<Holder<SoundEvent>> getEquipSound(ItemStack stack, SlotReference slot, LivingEntity entity) {
        if (stack.getItem() instanceof net.minecraft.world.item.Equipable equipable) {
            return Optional.of(equipable.getEquipSound());
        }
        return Optional.of(net.minecraft.sounds.SoundEvents.ARMOR_EQUIP_GENERIC);
    }

    default Multimap<Holder<Attribute>, AttributeModifier> getModifiers(ItemStack stack, SlotReference slot, LivingEntity entity, ResourceLocation slotIdentifier) {
        Multimap<Holder<Attribute>, AttributeModifier> map = Multimaps.newMultimap(Maps.newLinkedHashMap(), ArrayList::new);

        if (stack.getItem() instanceof net.minecraft.world.item.Equipable equipable) {
            net.minecraft.world.entity.EquipmentSlot naturalSlot = equipable.getEquipmentSlot();

            stack.forEachModifier(naturalSlot, (attribute, modifier) -> {
                ResourceLocation uniqueId = ResourceLocation.fromNamespaceAndPath(
                        slotIdentifier.getNamespace(),
                        slotIdentifier.getPath() + "/" + modifier.id().getNamespace() + "_" + modifier.id().getPath());
                map.put(attribute, new AttributeModifier(uniqueId, modifier.amount(), modifier.operation()));
            });
        }

        return map;
    }

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
