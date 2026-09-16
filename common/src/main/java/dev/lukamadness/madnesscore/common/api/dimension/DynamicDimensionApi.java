package dev.lukamadness.madnesscore.common.api.dimension;

import java.util.Set;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;

public interface DynamicDimensionApi
{
	static DynamicDimensionApi get()
	{
		DynamicDimensionApi instance = Holder.INSTANCE;
		if (instance == null)
		{
			throw new IllegalStateException(
				"MadnessCore.get() was called before a platform implementation registered itself. "
                        + "This currently only happens on platforms MadnessCore doesn't support yet (only NeoForge and Fabric are implemented for now).");
		}
		return instance;
	}

	static void setInstance(DynamicDimensionApi instance)
	{
		Holder.INSTANCE = instance;
	}

	final class Holder {
		private static volatile DynamicDimensionApi INSTANCE;

		private Holder() {
		}
	}

	ServerLevel getOrCreateLevel(final MinecraftServer server, final ResourceKey<Level> levelKey, final Supplier<LevelStem> dimensionFactory);

	void markDimensionForUnregistration(final MinecraftServer server, final ResourceKey<Level> levelToRemove);

	Set<ResourceKey<Level>> getLevelsPendingUnregistration();
}
