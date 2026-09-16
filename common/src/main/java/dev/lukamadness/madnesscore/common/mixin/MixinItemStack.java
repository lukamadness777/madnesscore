package dev.lukamadness.madnesscore.common.mixin;

import com.google.common.collect.Multimap;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.DyeableJacketItem;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.DyeableLegginsItem;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.DyeableShirtItem;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.FormalColors;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.FormalSuitBootsItem;
import dev.lukamadness.madnesscore.common.content.tailoring.clothing.FormalSuitItem;
import dev.lukamadness.madnesscore.common.registry.item.ModItems;
import dev.lukamadness.madnesscore.common.slots.SlotEquipLogic;
import dev.lukamadness.madnesscore.common.api.slots.SlotsApi;
import dev.lukamadness.madnesscore.common.api.slots.SlotAttributes;
import dev.lukamadness.madnesscore.common.api.slots.SlotInventory;
import dev.lukamadness.madnesscore.common.api.slots.SlotReference;
import dev.lukamadness.madnesscore.common.api.slots.SlotType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {
    private static final DecimalFormat MADNESSCORE$DECIMAL_FORMAT = new DecimalFormat("#.##");

    @Inject(method = "getTooltipLines", at = @At(value = "RETURN", ordinal = 1), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void madnesscore$onGetTooltipLines(Item.TooltipContext context, Player player, TooltipFlag flag,
                                               CallbackInfoReturnable<List<Component>> cir, List<Component> list) {
        if (list == null) {
            return;
        }
        ItemStack self = (ItemStack) (Object) this;

        madnesscore$addDyeStateTooltip(self, list);

        if (player == null) {
            return;
        }

        SlotsApi.getSlotComponent(player).ifPresent(component -> {
            boolean canEquipAnywhere = true;
            int slotTypeCount = 0;
            Set<SlotType> slots = new LinkedHashSet<>();
            Map<SlotType, Multimap<Holder<Attribute>, AttributeModifier>> modifiersBySlot = new HashMap<>();
            Multimap<Holder<Attribute>, AttributeModifier>[] defaultModifiersHolder = new Multimap[1];
            boolean[] allModifiersSameHolder = {true};

            for (Map<String, SlotInventory> group : component.getInventory().values()) {
                for (SlotInventory inv : group.values()) {
                    SlotType slotType = inv.getSlotType();
                    slotTypeCount++;
                    boolean anywhereButHidden = false;
                    boolean matchedThisType = false;

                    for (int i = 0; i < inv.getContainerSize(); i++) {
                        SlotReference ref = new SlotReference(inv, i);
                        boolean tooltipVisible = SlotsApi.evaluatePredicateSet(
                                slotType.getTooltipPredicates(), self, ref, player);
                        boolean canInsert = SlotEquipLogic.canInsert(self, ref, player);

                        if (tooltipVisible && canInsert) {
                            slots.add(slotType);
                            Multimap<Holder<Attribute>, AttributeModifier> map = SlotEquipLogic.getModifiers(self, ref, player);

                            if (defaultModifiersHolder[0] == null) {
                                defaultModifiersHolder[0] = map;
                            } else if (allModifiersSameHolder[0]) {
                                allModifiersSameHolder[0] = madnesscore$areMapsEqual(defaultModifiersHolder[0], map);
                            }

                            boolean duplicate = false;
                            for (Map.Entry<SlotType, Multimap<Holder<Attribute>, AttributeModifier>> entry : modifiersBySlot.entrySet()) {
                                if (entry.getKey() == slotType && madnesscore$areMapsEqual(entry.getValue(), map)) {
                                    duplicate = true;
                                    break;
                                }
                            }
                            if (!duplicate) {
                                modifiersBySlot.put(slotType, map);
                            }
                            matchedThisType = true;
                            break;
                        } else if (canInsert) {
                            anywhereButHidden = true;
                        }
                    }

                    if (!matchedThisType && !anywhereButHidden) {
                        canEquipAnywhere = false;
                    }
                }
            }

            if (canEquipAnywhere && slotTypeCount > 1) {
                list.add(Component.translatable("madnesscore.tooltip.slots.any").withStyle(ChatFormatting.GRAY));
            } else if (slots.size() > 1) {
                list.add(Component.translatable("madnesscore.tooltip.slots.list").withStyle(ChatFormatting.GRAY));
                for (SlotType slotType : slots) {
                    list.add(slotType.getTranslation().withStyle(ChatFormatting.BLUE));
                }
            } else if (slots.size() == 1) {
                for (SlotType slotType : slots) {
                    list.add(Component.translatable("madnesscore.tooltip.slots.single",
                            slotType.getTranslation().withStyle(ChatFormatting.BLUE)).withStyle(ChatFormatting.GRAY));
                }
            }

            if (!modifiersBySlot.isEmpty()) {
                if (allModifiersSameHolder[0]) {
                    if (defaultModifiersHolder[0] != null && !defaultModifiersHolder[0].isEmpty()) {
                        list.add(Component.translatable("madnesscore.tooltip.attributes.all").withStyle(ChatFormatting.GRAY));
                        madnesscore$addAttributes(list, defaultModifiersHolder[0]);
                    }
                } else {
                    for (Map.Entry<SlotType, Multimap<Holder<Attribute>, AttributeModifier>> entry : modifiersBySlot.entrySet()) {
                        list.add(Component.translatable("madnesscore.tooltip.attributes.single",
                                entry.getKey().getTranslation().withStyle(ChatFormatting.BLUE)).withStyle(ChatFormatting.GRAY));
                        madnesscore$addAttributes(list, entry.getValue());
                    }
                }
            }
        });
    }

    private void madnesscore$addDyeStateTooltip(ItemStack self, List<Component> list) {
        Item item = self.getItem();

        if (item == ModItems.FORMAL_SUIT.get()) {
            FormalColors colors = FormalSuitItem.getColors(self);
            list.add(Component.translatable("madnesscore.tooltip.formal_suit.shirt_color",
                    madnesscore$hexComponent(colors.shirtColor())).withStyle(ChatFormatting.GRAY));
            list.add(Component.translatable("madnesscore.tooltip.formal_suit.suit_color",
                    madnesscore$hexComponent(colors.suitColor())).withStyle(ChatFormatting.GRAY));
            list.add(Component.translatable("madnesscore.tooltip.formal_suit.tie_color",
                    madnesscore$hexComponent(colors.tieColor())).withStyle(ChatFormatting.GRAY));
            list.add(Component.translatable(colors.tieVisible()
                            ? "madnesscore.tooltip.formal_suit.tie_shown"
                            : "madnesscore.tooltip.formal_suit.tie_hidden")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        Integer color = null;
        if (item == ModItems.DYEABLE_SHIRT.get()) {
            color = DyeableShirtItem.getColor(self);
        } else if (item == ModItems.DYEABLE_JACKET.get()) {
            color = DyeableJacketItem.getColor(self);
        } else if (item == ModItems.DYEABLE_LEGGINS.get()) {
            color = DyeableLegginsItem.getColor(self);
        } else if (item == ModItems.FORMAL_SUIT_BOOTS.get()) {
            color = FormalSuitBootsItem.getColor(self);
        }

        if (color != null) {
            list.add(Component.translatable("madnesscore.tooltip.dye.color", madnesscore$hexComponent(color))
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    private static String madnesscore$hex(int rgb) {
        return String.format("#%06X", rgb & 0xFFFFFF);
    }

    private static Component madnesscore$hexComponent(int rgb) {
        int clean = rgb & 0xFFFFFF;
        return Component.literal(madnesscore$hex(clean)).setStyle(Style.EMPTY.withColor(TextColor.fromRgb(clean)));
    }

    private void madnesscore$addAttributes(List<Component> list, Multimap<Holder<Attribute>, AttributeModifier> map) {
        if (map.isEmpty()) {
            return;
        }
        for (Map.Entry<Holder<Attribute>, AttributeModifier> entry : map.entries()) {
            Holder<Attribute> attribute = entry.getKey();
            AttributeModifier modifier = entry.getValue();
            double amount = modifier.amount();

            if (modifier.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                    && modifier.operation() != AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL) {
                if (attribute.equals(Attributes.KNOCKBACK_RESISTANCE)) {
                    amount *= 10.0D;
                }
            } else {
                amount *= 100.0D;
            }

            Component text = Component.translatable(attribute.value().getDescriptionId());
            if (attribute.value() instanceof SlotAttributes.SlotEntityAttribute) {
                text = Component.translatable("madnesscore.tooltip.attributes.slots", text);
            }

            if (amount > 0.0D) {
                list.add(Component.translatable("attribute.modifier.plus." + modifier.operation().id(),
                        MADNESSCORE$DECIMAL_FORMAT.format(amount), text).withStyle(ChatFormatting.BLUE));
            } else if (amount < 0.0D) {
                amount *= -1.0D;
                list.add(Component.translatable("attribute.modifier.take." + modifier.operation().id(),
                        MADNESSCORE$DECIMAL_FORMAT.format(amount), text).withStyle(ChatFormatting.RED));
            }
        }
    }

    private boolean madnesscore$areMapsEqual(Multimap<Holder<Attribute>, AttributeModifier> map1, Multimap<Holder<Attribute>, AttributeModifier> map2) {
        if (map1.size() != map2.size()) {
            return false;
        }
        for (Holder<Attribute> attribute : map1.keySet()) {
            if (!map2.containsKey(attribute)) {
                return false;
            }
            Collection<AttributeModifier> col1 = map1.get(attribute);
            Collection<AttributeModifier> col2 = map2.get(attribute);
            if (col1.size() != col2.size()) {
                return false;
            }
            Iterator<AttributeModifier> iter = col2.iterator();
            for (AttributeModifier modifier : col1) {
                AttributeModifier other = iter.next();
                if (!modifier.operation().equals(other.operation())) {
                    return false;
                }
                if (modifier.amount() != other.amount()) {
                    return false;
                }
            }
        }
        return true;
    }
}
