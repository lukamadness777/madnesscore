package dev.lukamadness.madnesscore.common.mixin.accessor;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;

@Mixin(WorldBorder.class)
public interface WorldBorderAccessor
{
	@Accessor("listeners")
	List<BorderChangeListener> madnesscore$getListeners();
}
