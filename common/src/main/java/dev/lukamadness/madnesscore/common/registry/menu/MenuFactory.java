package dev.lukamadness.madnesscore.common.registry.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/**
 * Reemplazo 100% comun de {@code MenuType.MenuSupplier}, por el mismo motivo
 * que {@link dev.lukamadness.madnesscore.common.registry.blockentity.BlockEntityFactory}:
 * la interfaz vanilla es package-private y solo NeoForge (no el modulo
 * common) la vuelve publica.
 */
@FunctionalInterface
public interface MenuFactory<T extends AbstractContainerMenu> {
    T create(int windowId, Inventory playerInventory);
}