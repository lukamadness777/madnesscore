package dev.lukamadness.madnesscore.common.mixin.accessor;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;

@Mixin(RegistryAccess.ImmutableRegistryAccess.class)
public interface ImmutableRegistryAccessAccessor
{
	@Accessor("registries")
	@Mutable
	void madnesscore$setRegistries(Map<? extends ResourceKey<? extends Registry<?>>, ? extends Registry<?>> registries);
}
