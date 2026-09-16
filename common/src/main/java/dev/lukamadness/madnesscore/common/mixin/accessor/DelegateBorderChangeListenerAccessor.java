package dev.lukamadness.madnesscore.common.mixin.accessor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.level.border.BorderChangeListener;
import net.minecraft.world.level.border.WorldBorder;

@Mixin(BorderChangeListener.DelegateBorderChangeListener.class)
public interface DelegateBorderChangeListenerAccessor
{
	@Accessor("worldBorder")
	WorldBorder madnesscore$getWorldBorder();
}
