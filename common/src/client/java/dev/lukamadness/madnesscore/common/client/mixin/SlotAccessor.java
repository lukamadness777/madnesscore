package dev.lukamadness.madnesscore.common.client.mixin;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * {@link Slot#x} y {@link Slot#y} son {@code final} en vainilla (se fijan una sola vez en el
 * constructor). Los necesitamos mutables para poder reposicionar, después de creados, los
 * {@code CreativeModeInventoryScreen.SlotWrapper} que vainilla arma automáticamente para la
 * pestaña "Inventory" del inventario creativo (ver {@code MixinCreativeModeInventoryScreen}):
 * ese wrapping copia el índice de cada slot de {@code player.inventoryMenu} (incluidos nuestros
 * slots dinámicos) pero calcula su posición en pantalla con una fórmula que no conoce nuestro
 * sistema de grupos, así que hace falta corregirla después de que vainilla ya construyó el
 * wrapper. {@code @Mutable} le indica a Mixin que está bien generar un setter para un campo
 * declarado final.
 */
@Mixin(Slot.class)
public interface SlotAccessor {

    @Mutable
    @Accessor("x")
    void madnesscore$setX(int x);

    @Mutable
    @Accessor("y")
    void madnesscore$setY(int y);
}