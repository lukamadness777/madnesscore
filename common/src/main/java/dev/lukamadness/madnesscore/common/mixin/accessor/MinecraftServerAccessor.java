package dev.lukamadness.madnesscore.common.mixin.accessor;

import java.util.Map;
import java.util.concurrent.Executor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.RegistryLayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.ChunkProgressListenerFactory;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelStorageSource.LevelStorageAccess;

@Mixin(MinecraftServer.class)
public interface MinecraftServerAccessor
{
	@Accessor("levels")
	Map<ResourceKey<Level>, ServerLevel> madnesscore$getLevels();

	@Accessor("executor")
	Executor madnesscore$getExecutor();

	@Accessor("storageSource")
	LevelStorageAccess madnesscore$getStorageSource();

	@Accessor("progressListenerFactory")
	ChunkProgressListenerFactory madnesscore$getProgressListenerFactory();

	@Accessor("registries")
	LayeredRegistryAccess<RegistryLayer> madnesscore$getRegistries();
}
