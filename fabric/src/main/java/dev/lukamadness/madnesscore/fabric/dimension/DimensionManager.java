package dev.lukamadness.madnesscore.fabric.dimension;

import dev.lukamadness.madnesscore.common.internal.AbstractDimensionManager;
import dev.lukamadness.madnesscore.common.network.UpdateDimensionsPacket;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public final class DimensionManager extends AbstractDimensionManager
{
	private DimensionManager() {}

	public static final DimensionManager INSTANCE = new DimensionManager();

	@Override
	protected void onLevelLoaded(ServerLevel level)
	{
		ServerWorldEvents.LOAD.invoker().onWorldLoad(level.getServer(), level);
	}

	@Override
	protected void onLevelUnloaded(ServerLevel level)
	{
		ServerWorldEvents.UNLOAD.invoker().onWorldUnload(level.getServer(), level);
	}

	@Override
	protected void notifyClientsOfDimensionChange(MinecraftServer server, UpdateDimensionsPacket packet)
	{
		for (ServerPlayer player : server.getPlayerList().getPlayers())
		{
			if (ServerPlayNetworking.canSend(player, UpdateDimensionsPacket.TYPE))
			{
				ServerPlayNetworking.send(player, packet);
			}
		}
	}
}
