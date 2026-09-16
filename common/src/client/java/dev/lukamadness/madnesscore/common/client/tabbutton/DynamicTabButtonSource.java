package dev.lukamadness.madnesscore.common.client.tabbutton;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Map;

public interface DynamicTabButtonSource {
    String sourceId();

    Map<String, ItemStack> getActiveInstances(LocalPlayer player);

    Component label();

    ItemStack buttonIcon();

    void onClick(String instanceKey);
}
