package dev.lukamadness.madnesscore.common.slots;

import dev.lukamadness.madnesscore.common.api.slots.DropRule;
import dev.lukamadness.madnesscore.common.api.slots.SlotInventory;
import dev.lukamadness.madnesscore.common.api.slots.event.SlotDropCallback;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;

/**
 * Logica compartida de que pasa con los items equipados en slots cuando una entidad muere, segun
 * la {@link DropRule} calculada para cada uno (KEEP/DROP/DESTROY/DEFAULT). Cada loader debe
 * invocar {@link #dropOnDeath(LivingEntity, ServerLevel)} una unica vez por muerte, del lado del
 * servidor:
 * <ul>
 *     <li>Fabric: inject TAIL en {@code LivingEntity#die(DamageSource)} via mixin.</li>
 *     <li>NeoForge: listener de {@code LivingDeathEvent}, solo si no fue cancelado.</li>
 * </ul>
 * Portado de la parte de drop de dev.emi.trinkets.mixin.LivingEntityMixin#dropInventory.
 */
public final class SlotDeathHandler {

    private SlotDeathHandler() {
    }

    public static void dropOnDeath(LivingEntity entity, ServerLevel level) {
        boolean keepInventory = level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY);

        SlotsApi.getSlotComponent(entity).ifPresent(slots -> slots.forEach((ref, stack) -> {
            if (stack.isEmpty()) {
                return;
            }

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
                    // Portado de dev.emi.trinkets.mixin.LivingEntityMixin#dropInventory: un item con
                    // Maldicion de Desvanecimiento (o cualquier encantamiento con el mismo efecto) se
                    // destruye en vez de soltarse al morir.
                    dropRule = DropRule.DESTROY;
                } else {
                    dropRule = DropRule.DROP;
                }
            }

            switch (dropRule) {
                case DROP -> {
                    dropFromEntity(entity, level, stack);
                    inventory.setItem(ref.index(), ItemStack.EMPTY);
                }
                case DESTROY -> inventory.setItem(ref.index(), ItemStack.EMPTY);
                default -> {
                    // KEEP: no se toca el stack.
                }
            }
        }));
    }

    private static void dropFromEntity(LivingEntity entity, ServerLevel level, ItemStack stack) {
        // Imita el comportamiento de drop de un jugador solo para jugadores; para el resto de
        // entidades, spawnea el item directamente en el mundo.
        if (entity instanceof Player player) {
            player.drop(stack, true, false);
        } else {
            ItemEntity itemEntity = new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), stack);
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }
    }
}
