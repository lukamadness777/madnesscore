package dev.lukamadness.madnesscore.neoforge.dimension;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.internal.AbstractDimensionManager;
import dev.lukamadness.madnesscore.common.network.UpdateDimensionsPacket;
import dev.lukamadness.madnesscore.neoforge.MadnessCoreNeoForge;
import dev.lukamadness.madnesscore.neoforge.dimension.QuietPacketDistributors;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public final class DimensionManager extends AbstractDimensionManager
{
	private DimensionManager() {}

	public static final DimensionManager INSTANCE = new DimensionManager();

	@Override
	protected void onLevelLoaded(ServerLevel level)
	{
		NeoForge.EVENT_BUS.post(new LevelEvent.Load(level));
	}

	@Override
	protected void onLevelUnloaded(ServerLevel level)
	{
		NeoForge.EVENT_BUS.post(new LevelEvent.Unload(level));
	}

	@Override
	protected void notifyClientsOfDimensionChange(MinecraftServer server, UpdateDimensionsPacket packet)
	{
		QuietPacketDistributors.sendToAll(server, packet);
	}

	@Override
	protected void onLevelMapChanged(MinecraftServer server)
	{
		server.markWorldsDirty();
	}

	@EventBusSubscriber(modid = MadnessCoreCommon.MOD_ID)
	private static class NeoForgeEventHandler
	{
		@SubscribeEvent(priority = EventPriority.LOWEST)
		public static void onServerTick(final ServerTickEvent.Post event)
		{
			MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
			if (server != null)
			{
				DimensionManager.INSTANCE.unregisterScheduledDimensions(server);
			}
		}

		@SubscribeEvent
		public static void onServerStopped(final ServerStoppedEvent event)
		{
			DimensionManager.INSTANCE.clearPendingUnregistrations();
		}
	}
}
