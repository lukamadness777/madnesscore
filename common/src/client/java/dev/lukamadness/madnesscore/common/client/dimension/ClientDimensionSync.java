package dev.lukamadness.madnesscore.common.client.dimension;

import dev.lukamadness.madnesscore.common.network.UpdateDimensionsPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Set;
import java.util.function.Consumer;

public class ClientDimensionSync {
    public static void applyUpdateDimensions(UpdateDimensionsPacket packet)
    {
        @SuppressWarnings("resource")
        final LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return;

        final Set<ResourceKey<Level>> dimensionList = player.connection.levels();
        if (dimensionList == null)
            return;

        Consumer<ResourceKey<Level>> keyConsumer = packet.add()
                ? dimensionList::add
                : dimensionList::remove;

        packet.keys().forEach(keyConsumer);
    }
}
