package dev.lukamadness.madnesscore.common.mixin.accessor;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.core.LayeredRegistryAccess;
import net.minecraft.core.RegistryAccess;

@Mixin(LayeredRegistryAccess.class)
public interface LayeredRegistryAccessAccessor
{
	@Accessor("values")
	@Mutable
	void madnesscore$setValues(List<RegistryAccess.Frozen> values);

	@Accessor("composite")
	RegistryAccess.Frozen madnesscore$getComposite();
}
