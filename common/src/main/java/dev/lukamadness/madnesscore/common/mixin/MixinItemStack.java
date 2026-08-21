package dev.lukamadness.madnesscore.common.mixin;

import com.google.common.collect.Multimap;
import dev.lukamadness.madnesscore.common.slots.SlotEquipLogic;
import dev.lukamadness.madnesscore.common.slots.SlotsApi;
import dev.lukamadness.madnesscore.common.api.slots.SlotAttributes;
import dev.lukamadness.madnesscore.common.api.slots.SlotInventory;
import dev.lukamadness.madnesscore.common.api.slots.SlotReference;
import dev.lukamadness.madnesscore.common.api.slots.SlotType;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
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

/**
 * Agrega al tooltip de un item, si es equipable en algun slot dinamico del jugador que esta
 * mirando el tooltip, el texto "Equippable in..." y el desglose de que atributos aporta al
 * equiparlo (usa las claves de lang {@code madnesscore.tooltip.*}, ya presentes en en_us.json).
 * Portado de dev.emi.trinkets.mixin.ItemStackMixin, adaptado a 1.21.1: esta version de Minecraft
 * no tiene {@code TooltipDisplayComponent} (rework de tooltips posterior), asi que no hay ningun
 * "gate" adicional que consultar - el desglose de atributos se muestra siempre que haya
 * modificadores, igual que hacia Trinkets antes de ese rework.
 * <p>
 * Firma y ordinal verificados contra el jar fuente real de 1.21.1
 * ({@code ItemStack#getTooltipLines(Item.TooltipContext, @Nullable Player, TooltipFlag)}): el
 * cuerpo tiene exactamente dos "return" (uno temprano con {@code List.of()} si el tooltip esta
 * oculto, y el final con la lista armada), de ahi el {@code ordinal = 1} de abajo.
 */
@Mixin(ItemStack.class)
public abstract class MixinItemStack {

    private static final DecimalFormat MADNESSCORE$DECIMAL_FORMAT = new DecimalFormat("#.##");

    @Inject(method = "getTooltipLines", at = @At(value = "RETURN", ordinal = 1), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void madnesscore$onGetTooltipLines(Item.TooltipContext context, Player player, TooltipFlag flag,
                                               CallbackInfoReturnable<List<Component>> cir, List<Component> list) {
        if (player == null || list == null) {
            return;
        }
        ItemStack self = (ItemStack) (Object) this;

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

    /**
     * Formatea y agrega al tooltip cada modificador de atributo, con el mismo criterio que
     * vainilla usa para el tooltip de armadura/armas (signo, factor x100 para modificadores
     * multiplicativos, x10 extra para knockback resistance). Portado de
     * dev.emi.trinkets.mixin.ItemStackMixin#addAttributes.
     */
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

    // `equals` no compara a fondo (las AttributeModifier de distintos slots difieren en id) -
    // portado de dev.emi.trinkets.mixin.ItemStackMixin#areMapsEqual.
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