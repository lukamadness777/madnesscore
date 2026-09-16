package dev.lukamadness.madnesscore.common.api.identity;

import net.minecraft.world.item.ItemStack;

/**
 * Implementá esta interfaz en cualquier {@link net.minecraft.world.item.Item} (armadura vanilla,
 * item de slot dinámico de madnesscore, o de cualquier addon como beyond-the-sea) para que, mientras
 * ese ItemStack esté equipado y {@link #hidesName(ItemStack)} devuelva true, {@link NameVisibilityApi}
 * reporte que el nombre del portador está oculto.
 * <p>
 * No hace falta registrar nada a mano: {@link NameVisibilityApi#isNameHidden(net.minecraft.world.entity.LivingEntity)}
 * recorre los slots vanilla (armadura) y los slots dinámicos de madnesscore de la entidad buscando
 * items que implementen esta interfaz.
 * <p>
 * Ejemplo típico (prenda con capucha, como la Hoodie de beyond-the-sea): guardar un flag "HoodUp" con
 * {@link ItemToggleFlag} y devolver ese flag acá:
 * <pre>{@code
 * public class HoodieItem extends ArmorItem implements NameHidingItem {
 *     private static final String HOOD_UP = "HoodUp";
 *
 *     public static boolean isHoodUp(ItemStack stack) {
 *         return ItemToggleFlag.get(stack, HOOD_UP);
 *     }
 *
 *     public static void toggleHood(ItemStack stack) {
 *         ItemToggleFlag.toggle(stack, HOOD_UP);
 *     }
 *
 *     @Override
 *     public boolean hidesName(ItemStack stack) {
 *         return isHoodUp(stack);
 *     }
 * }
 * }</pre>
 */
public interface NameHidingItem {
    /**
     * @param stack el ItemStack equipado tal cual está puesto en el slot (vanilla o dinámico).
     * @return true si, en el estado actual de este stack (ej. capucha puesta), el nombre del portador
     * debe ocultarse.
     */
    boolean hidesName(ItemStack stack);
}
