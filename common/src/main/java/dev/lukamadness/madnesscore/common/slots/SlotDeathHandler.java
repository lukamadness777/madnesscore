package dev.lukamadness.madnesscore.common.slots;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.api.slots.DropRule;
import dev.lukamadness.madnesscore.common.api.slots.SlotInventory;
import dev.lukamadness.madnesscore.common.api.slots.SlotsApi;
import dev.lukamadness.madnesscore.common.api.slots.event.SlotDropCallback;
import dev.lukamadness.madnesscore.common.slots.network.SlotNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;

public final class SlotDeathHandler {
    private SlotDeathHandler() {
    }

    public static void dropOnDeath(LivingEntity entity, ServerLevel level) {
        boolean keepInventory = level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);

        boolean[] changed = {false};

        SlotsApi.getSlotComponent(entity).ifPresent(slots -> {
            slots.forEach((ref, stack) -> {
                if (stack.isEmpty()) {
                    return;
                }

                boolean mirrors = ref.inventory().getSlotType().mirrorsVanillaEquipment();

                DropRule dropRule = SlotsApi.getSlottable(stack.getItem()).getDropRule(stack, ref, entity);
                dropRule = SlotDropCallback.EVENT.invoker().drop(dropRule, stack, ref, entity);

                SlotInventory inventory = ref.inventory();

                if (dropRule == DropRule.DEFAULT) {
                    dropRule = inventory.getSlotType().getDropRule();
                }

                if (dropRule == DropRule.DEFAULT) {
                    if (keepInventory && entity.getType() == EntityType.PLAYER) {
                        dropRule = DropRule.KEEP;
                    } else if (EnchantmentHelper.has(stack, EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
                        dropRule = DropRule.DESTROY;
                    } else {
                        dropRule = DropRule.DROP;
                    }
                }

                switch (dropRule) {
                    case DROP -> {
                        dropFromEntity(entity, level, stack);
                        inventory.setItem(ref.index(), ItemStack.EMPTY);
                        changed[0] = true;
                    }
                    case DESTROY -> {
                        inventory.setItem(ref.index(), ItemStack.EMPTY);
                        changed[0] = true;
                    }
                    default -> {}
                }

                if (mirrors && (dropRule == DropRule.DROP || dropRule == DropRule.DESTROY)) {
                    VanillaEquipmentMirror.resolveEquipmentSlot(slots, inventory)
                            .ifPresent(equipmentSlot -> entity.setItemSlot(equipmentSlot, ItemStack.EMPTY));
                }
            });
        });

        if (changed[0]) {
            SlotNetworking.syncToTrackers(entity);

        }
    }

    private static void dropFromEntity(LivingEntity entity, ServerLevel level, ItemStack stack) {
        if (entity instanceof Player player) {
            player.drop(stack, true, false);
        } else {
            ItemEntity itemEntity = new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), stack);
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }
    }
}
