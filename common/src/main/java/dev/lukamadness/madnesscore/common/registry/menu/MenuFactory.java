package dev.lukamadness.madnesscore.common.registry.menu;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

@FunctionalInterface
public interface MenuFactory<T extends AbstractContainerMenu> {
    T create(int windowId, Inventory playerInventory);
}
