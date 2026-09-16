package dev.lukamadness.madnesscore.common.mixin;

import dev.lukamadness.madnesscore.common.slots.EnchantmentInSlotVisitorHandle;
import dev.lukamadness.madnesscore.common.api.slots.SlotsApi;
import dev.lukamadness.madnesscore.common.slots.VanillaEquipmentMirror;
import it.unimi.dsi.fastutil.objects.Object2IntMap.Entry;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.EnchantedItemInUse;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(EnchantmentHelper.class)
public abstract class MixinEnchantmentHelperSlots {
    @Inject(method = "runIterationOnEquipment", at = @At("TAIL"))
    private static void madnesscore$runOnDynamicSlots(
            LivingEntity entity, @Coerce Object visitor, CallbackInfo ci) {
        EnchantmentInSlotVisitorHandle typedVisitor = (EnchantmentInSlotVisitorHandle) visitor;
        SlotsApi.getSlotComponent(entity).ifPresent(component -> component.forEach((ref, stack) -> {
            if (stack.isEmpty() || VanillaEquipmentMirror.isMirrored(entity, stack)) {
                return;
            }

            ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
            if (enchantments.isEmpty()) {
                return;
            }

            EnchantedItemInUse use = new EnchantedItemInUse(stack, null, entity,
                    item -> SlotsApi.onSlotItemBroken(stack, ref, entity));

            for (Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
                Holder<Enchantment> holder = entry.getKey();
                List<EquipmentSlotGroup> slots = holder.value().definition().slots();
                if (slots.contains(EquipmentSlotGroup.ANY) || slots.contains(EquipmentSlotGroup.ARMOR)) {
                    typedVisitor.accept(holder, entry.getIntValue(), use);
                }
            }
        }));
    }
}
