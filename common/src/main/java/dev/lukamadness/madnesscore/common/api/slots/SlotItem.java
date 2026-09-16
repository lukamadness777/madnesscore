package dev.lukamadness.madnesscore.common.api.slots;

import dev.lukamadness.madnesscore.common.slots.SlotEquipLogic;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.Optional;

public class SlotItem extends Item implements Slottable {
    public SlotItem(Properties properties) {
        super(properties);
        SlotsApi.registerSlottable(this, this);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        if (equipItem(user, stack)) {
            return InteractionResultHolder.success(stack);
        }
        return super.use(level, user, hand);
    }

    public static boolean equipItem(Player user, ItemStack stack) {
        return equipItem((LivingEntity) user, stack);
    }

    public static boolean equipItem(LivingEntity user, ItemStack stack) {
        Optional<SlotComponent> optional = SlotsApi.getSlotComponent(user);
        if (optional.isPresent()) {
            SlotComponent comp = optional.get();
            for (Map<String, SlotInventory> group : comp.getInventory().values()) {
                for (SlotInventory inv : group.values()) {
                    for (int i = 0; i < inv.getContainerSize(); i++) {
                        if (inv.getItem(i).isEmpty()) {
                            SlotReference ref = new SlotReference(inv, i);
                            if (SlotEquipLogic.canInsert(stack, ref, user)) {
                                ItemStack newStack = stack.copy();
                                inv.setItem(i, newStack);
                                Slottable slottable = SlotsApi.getSlottable(stack.getItem());
                                Optional<Holder<SoundEvent>> soundEvent = slottable.getEquipSound(stack, ref, user);
                                soundEvent.ifPresent(sound -> user.playSound(sound.value(), 1.0F, 1.0F));
                                stack.setCount(0);
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }
}
