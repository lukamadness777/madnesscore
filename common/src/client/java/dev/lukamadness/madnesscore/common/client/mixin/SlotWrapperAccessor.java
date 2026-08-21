package dev.lukamadness.madnesscore.common.client.mixin;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * {@code CreativeModeInventoryScreen.SlotWrapper#target} (el {@link Slot} real que envuelve,
 * p.ej. un slot de nuestro sistema dentro de {@code player.inventoryMenu}) es de paquete privado.
 * Lo necesitamos para poder identificar, entre los slots ya envueltos por vainilla en la pestaña
 * "Inventory" del creativo, cuáles son en realidad nuestros {@code PlayerDynamicSlot} y así poder
 * reposicionarlos (ver {@code MixinCreativeModeInventoryScreen}).
 * <p>
 * {@code CreativeModeInventoryScreen.SlotWrapper} en sí (la clase target) también es de paquete
 * privado en vainilla, así que no se puede nombrar con un {@code .class} literal desde nuestro
 * paquete (error de compilación de javac, no una limitación de Mixin) — de ahí el
 * {@code targets = "..."} con el nombre binario en vez de {@code @Mixin(...SlotWrapper.class)}.
 * Una vez mergeada, cualquier instancia de esa clase implementa esta interfaz pública nuestra, así
 * que en el resto del mod alcanza con {@code instanceof SlotWrapperAccessor} para reconocerla sin
 * tener que nombrar el tipo vainilla en ningún lado.
 */
@Mixin(targets = "net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen$SlotWrapper")
public interface SlotWrapperAccessor {

    @Accessor("target")
    Slot madnesscore$getTarget();
}