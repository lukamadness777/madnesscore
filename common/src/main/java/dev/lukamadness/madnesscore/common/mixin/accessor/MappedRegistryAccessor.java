package dev.lukamadness.madnesscore.common.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.core.MappedRegistry;

@Mixin(MappedRegistry.class)
public interface MappedRegistryAccessor
{
	@Accessor("frozen")
	@Mutable
	void madnesscore$setFrozen(boolean frozen);
}
