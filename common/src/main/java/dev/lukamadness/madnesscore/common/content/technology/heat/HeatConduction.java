package dev.lukamadness.madnesscore.common.content.technology.heat;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public final class HeatConduction {
    private HeatConduction() {}

    public static final double NEIGHBOR_CONDUCTION_RATE = 0.02;

    public static double gatherFromHotterNeighbors(Level level, BlockPos pos, ModHeatStorage storage) {
        double totalDelta = 0.0;
        for (Direction direction : Direction.values()) {
            BlockEntity neighbor = level.getBlockEntity(pos.relative(direction));
            if (neighbor instanceof HeatReceiver receiver) {
                double neighborTemp = receiver.getHeatStorage().getTemperature();
                double diff = neighborTemp - storage.getTemperature();
                if (diff > 0) {
                    totalDelta += diff * NEIGHBOR_CONDUCTION_RATE;
                }
            }
        }
        return totalDelta;
    }
}
