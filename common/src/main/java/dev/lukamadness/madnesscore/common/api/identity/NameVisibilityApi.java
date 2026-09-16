package dev.lukamadness.madnesscore.common.api.identity;

import com.mojang.datafixers.util.Pair;
import dev.lukamadness.madnesscore.common.api.slots.SlotComponent;
import dev.lukamadness.madnesscore.common.api.slots.SlotReference;
import dev.lukamadness.madnesscore.common.api.slots.SlotsApi;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

/**
 * Punto de entrada único para saber si el nombre (nametag / label) de una entidad debe mostrarse
 * oculto ahora mismo. Es el reemplazo genérico del {@code HideNametagMixin} que tenía beyond-the-sea:
 * en vez de que cada mod hardcodee sus propios items y sus propios chequeos de Trinkets/Accessories,
 * cualquier item (vanilla armor o slot dinámico de madnesscore, de cualquier mod) que implemente
 * {@link NameHidingItem} es detectado automáticamente acá.
 * <p>
 * Server-safe: solo lee ItemStacks equipados, no toca nada de render. Lo usa tanto el mixin de
 * {@code LivingEntityRenderer#hasLabel} (cliente) como cualquier integración externa (ej. Jade) que
 * quiera decidir si mostrar "???" en vez del nombre real.
 */
public final class NameVisibilityApi {
    private NameVisibilityApi() {
    }

    public static boolean isNameHidden(LivingEntity entity) {
        if (entity == null) {
            return false;
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.HUMANOID_ARMOR) {
                continue;
            }
            if (hides(entity.getItemBySlot(slot))) {
                return true;
            }
        }

        Optional<SlotComponent> component = SlotsApi.getSlotComponent(entity);
        if (component.isPresent()) {
            List<Pair<SlotReference, ItemStack>> equipped = component.get().getAllEquipped();
            for (Pair<SlotReference, ItemStack> pair : equipped) {
                if (hides(pair.getSecond())) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean hides(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof NameHidingItem hiding && hiding.hidesName(stack);
    }
}
