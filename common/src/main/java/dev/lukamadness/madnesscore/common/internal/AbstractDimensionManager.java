package dev.lukamadness.madnesscore.common.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

import dev.lukamadness.madnesscore.common.api.dimension.DynamicDimensionApi;
import dev.lukamadness.madnesscore.common.api.dimension.UnregisterDimensionEvent;
import dev.lukamadness.madnesscore.common.mixin.accessor.*;
import dev.lukamadness.madnesscore.common.network.UpdateDimensionsPacket;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.mojang.serialization.Lifecycle;

import net.minecraft.core.BlockPos;
import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.RegistryAccess.ImmutableRegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.DerivedLevelData;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;
import net.minecraft.world.level.storage.WorldData;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractDimensionManager implements DynamicDimensionApi
{
	private static final RegistrationInfo DIMENSION_REGISTRATION_INFO = new RegistrationInfo(Optional.empty(), Lifecycle.stable());
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Set<ResourceKey<Level>> VANILLA_LEVELS = Set.of(Level.OVERWORLD, Level.NETHER, Level.END);

	private Set<ResourceKey<Level>> levelsPendingUnregistration = new HashSet<>();

	protected AbstractDimensionManager() {}

	protected abstract void onLevelLoaded(ServerLevel level);

	protected abstract void onLevelUnloaded(ServerLevel level);

	protected abstract void notifyClientsOfDimensionChange(MinecraftServer server, UpdateDimensionsPacket packet);

	protected void onLevelMapChanged(MinecraftServer server) {}

	@Override
	public ServerLevel getOrCreateLevel(final MinecraftServer server, final ResourceKey<Level> levelKey, final Supplier<LevelStem> dimensionFactory)
	{
		Map<ResourceKey<Level>, ServerLevel> map = ((MinecraftServerAccessor)(Object)server).madnesscore$getLevels();
		@Nullable ServerLevel existingLevel = map.get(levelKey);

		return existingLevel == null
				? this.createAndRegisterLevel(server, map, levelKey, dimensionFactory)
				: existingLevel;
	}

	@Override
	public void markDimensionForUnregistration(final MinecraftServer server, final ResourceKey<Level> levelToRemove)
	{
		if (!VANILLA_LEVELS.contains(levelToRemove))
		{
			ServerLevel level = server.getLevel(levelToRemove);
			if (level != null)
			{
				level.save(null, true, false);
				this.levelsPendingUnregistration.add(levelToRemove);
			}
		}
	}

	@Override
	public Set<ResourceKey<Level>> getLevelsPendingUnregistration()
	{
		return ImmutableSet.copyOf(this.levelsPendingUnregistration);
	}

	private ServerLevel createAndRegisterLevel(final MinecraftServer server, final Map<ResourceKey<Level>, ServerLevel> map, final ResourceKey<Level> levelKey, Supplier<LevelStem> dimensionFactory)
	{
		final MinecraftServerAccessor serverAccessor = (MinecraftServerAccessor)(Object)server;
		final ServerLevel overworld = server.getLevel(Level.OVERWORLD);

		final ResourceKey<LevelStem> dimensionKey = ResourceKey.create(Registries.LEVEL_STEM, levelKey.location());
		final LevelStem dimension = dimensionFactory.get();

		final ChunkProgressListener chunkProgressListener = serverAccessor.madnesscore$getProgressListenerFactory().create(11);
		final Executor executor = serverAccessor.madnesscore$getExecutor();
		final LevelStorageAccess anvilConverter = serverAccessor.madnesscore$getStorageSource();
		final WorldData worldData = server.getWorldData();
		final DerivedLevelData derivedLevelData = new DerivedLevelData(worldData, worldData.overworldData());

		Registry<LevelStem> dimensionRegistry = server.registryAccess().registryOrThrow(Registries.LEVEL_STEM);
		if (dimensionRegistry instanceof MappedRegistry<LevelStem> writableRegistry)
		{
			((MappedRegistryAccessor)(Object)writableRegistry).madnesscore$setFrozen(false);
			writableRegistry.register(dimensionKey, dimension, DIMENSION_REGISTRATION_INFO);
		}
		else
		{
			throw new IllegalStateException(String.format("Unable to register dimension %s -- dimension registry not writable", dimensionKey.location()));
		}

		final ServerLevel newLevel = new ServerLevel(
				server,
				executor,
				anvilConverter,
				derivedLevelData,
				levelKey,
				dimension,
				chunkProgressListener,
				worldData.isDebugWorld(),
				overworld.getSeed(),
				List.of(),
				false,
				(RandomSequences)null
		);

		overworld.getWorldBorder().addListener(new BorderChangeListener.DelegateBorderChangeListener(newLevel.getWorldBorder()));

		map.put(levelKey, newLevel);
		this.onLevelMapChanged(server);

		this.onLevelLoaded(newLevel);

		this.notifyClientsOfDimensionChange(server, new UpdateDimensionsPacket(Set.of(levelKey), true));

		return newLevel;
	}

	public void unregisterScheduledDimensions(final MinecraftServer server)
	{
		if (this.levelsPendingUnregistration.isEmpty())
			return;

		final Set<ResourceKey<Level>> keysToRemove = this.levelsPendingUnregistration;
		this.levelsPendingUnregistration = new HashSet<>();

		final Registry<LevelStem> oldRegistry = server.registryAccess().registryOrThrow(Registries.LEVEL_STEM);
		if (!(oldRegistry instanceof MappedRegistry<LevelStem> oldMappedRegistry))
		{
			LOGGER.warn("Cannot unload dimensions: dimension registry not an instance of MappedRegistry. There may be another mod causing incompatibility with MadnessCore, or MadnessCore may need to be updated for your version of Minecraft.");
			return;
		}
		final MinecraftServerAccessor serverAccessor = (MinecraftServerAccessor)(Object)server;
		LayeredRegistryAccess<RegistryLayer> layeredRegistryAccess = serverAccessor.madnesscore$getRegistries();
		LayeredRegistryAccessAccessor layeredRegistryAccessAccessor = (LayeredRegistryAccessAccessor)(Object)layeredRegistryAccess;
		RegistryAccess.Frozen composite = layeredRegistryAccessAccessor.madnesscore$getComposite();
		if (!(composite instanceof ImmutableRegistryAccess immutableRegistryAccess))
		{
			LOGGER.warn("Cannot unload dimensions: composite registry not an instance of ImmutableRegistryAccess. There may be another mod causing incompatibility with MadnessCore, or MadnessCore may need to be updated for your version of Minecraft.");
			return;
		}

		final Set<ResourceKey<Level>> removedLevelKeys = new HashSet<>();
		final ServerLevel overworld = server.getLevel(Level.OVERWORLD);
		final Map<ResourceKey<Level>, ServerLevel> levelMap = serverAccessor.madnesscore$getLevels();

		for (final ResourceKey<Level> levelKeyToRemove : keysToRemove)
		{
			final @Nullable ServerLevel levelToRemove = server.getLevel(levelKeyToRemove);
			if (levelToRemove == null)
				continue;

			boolean allowUnregister = UnregisterDimensionEvent.fire(levelToRemove);
			if (!allowUnregister)
				continue;

			final @Nullable ServerLevel removedLevel = levelMap.remove(levelKeyToRemove);

			if (removedLevel != null)
			{
				for (final ServerPlayer player : Lists.newArrayList(removedLevel.players()))
				{
					ResourceKey<Level> respawnKey = player.getRespawnDimension();
					if (keysToRemove.contains(respawnKey))
					{
						respawnKey = Level.OVERWORLD;
						player.setRespawnPosition(respawnKey, null, 0, false, false);
					}
					if (respawnKey == null)
					{
						respawnKey = Level.OVERWORLD;
					}
					@Nullable ServerLevel destinationLevel = server.getLevel(respawnKey);
					if (destinationLevel == null)
					{
						destinationLevel = overworld;
					}

					@Nullable
					BlockPos destinationPos = player.getRespawnPosition();
					if (destinationPos == null)
					{
						destinationPos = destinationLevel.getSharedSpawnPos();
					}

					final float respawnAngle = player.getRespawnAngle();
					player.teleportTo(destinationLevel, destinationPos.getX(), destinationPos.getY(), destinationPos.getZ(), respawnAngle, 0F);
				}
				removedLevel.save(null, false, removedLevel.noSave());

				this.onLevelUnloaded(removedLevel);

				final WorldBorder overworldBorder = overworld.getWorldBorder();
				final WorldBorder removedWorldBorder = removedLevel.getWorldBorder();
				final List<BorderChangeListener> listeners = ((WorldBorderAccessor)(Object)overworldBorder).madnesscore$getListeners();
				BorderChangeListener targetListener = null;
				for (BorderChangeListener listener : listeners)
				{
					if (listener instanceof BorderChangeListener.DelegateBorderChangeListener delegate
							&& removedWorldBorder == ((DelegateBorderChangeListenerAccessor)(Object)delegate).madnesscore$getWorldBorder())
					{
						targetListener = listener;
						break;
					}
				}
				if (targetListener != null)
				{
					overworldBorder.removeListener(targetListener);
				}

				removedLevelKeys.add(levelKeyToRemove);
			}
		}

		if (!removedLevelKeys.isEmpty())
		{
			final MappedRegistry<LevelStem> newRegistry = new MappedRegistry<>(Registries.LEVEL_STEM, oldMappedRegistry.registryLifecycle());

			for (final var entry : oldRegistry.entrySet())
			{
				final ResourceKey<LevelStem> oldKey = entry.getKey();
				final ResourceKey<Level> oldLevelKey = ResourceKey.create(Registries.DIMENSION, oldKey.location());
				final LevelStem dimension = entry.getValue();
				if (oldKey != null && dimension != null && !removedLevelKeys.contains(oldLevelKey))
				{
					newRegistry.register(oldKey, dimension, oldRegistry.registrationInfo(oldKey).orElse(DIMENSION_REGISTRATION_INFO));
				}
			}

			List<RegistryAccess.Frozen> newRegistryAccessList = new ArrayList<>();
			for (RegistryLayer layer : RegistryLayer.values())
			{
				if (layer == RegistryLayer.DIMENSIONS)
				{
					newRegistryAccessList.add(new ImmutableRegistryAccess(List.of(newRegistry)).freeze());
				}
				else
				{
					newRegistryAccessList.add(layeredRegistryAccess.getLayer(layer));
				}
			}
			Map<ResourceKey<? extends Registry<?>>, Registry<?>> newRegistryMap = new HashMap<>();
			for (var registryAccess : newRegistryAccessList)
			{
				var registries = registryAccess.registries().toList();
				for (var registryEntry : registries)
				{
					newRegistryMap.put(registryEntry.key(), registryEntry.value());
				}
			}
			layeredRegistryAccessAccessor.madnesscore$setValues(List.copyOf(newRegistryAccessList));
			((ImmutableRegistryAccessAccessor)(Object)immutableRegistryAccess).madnesscore$setRegistries(newRegistryMap);

			this.onLevelMapChanged(server);

			this.notifyClientsOfDimensionChange(server, new UpdateDimensionsPacket(removedLevelKeys, false));
		}
	}

	public void clearPendingUnregistrations()
	{
		this.levelsPendingUnregistration = new HashSet<>();
	}
}
