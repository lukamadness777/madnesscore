package dev.lukamadness.madnesscore.common.client.mixin.accessor;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Slot.class)
public interface SlotAccessor {
    @Mutable
    @Accessor("x")
    void madnesscore$setX(int x);

    @Mutable
    @Accessor("y")
    void madnesscore$setY(int y);
}
