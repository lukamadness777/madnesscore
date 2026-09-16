package dev.lukamadness.madnesscore.fabric.mixin;

import dev.lukamadness.madnesscore.common.slots.network.SlotNetworking;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerEntity.class)
public abstract class MixinServerEntityTracking {
    @Shadow
    @Final
    private Entity entity;

    @Inject(method = "addPairing", at = @At("TAIL"))
    private void madnesscore$onStartTracking(ServerPlayer player, CallbackInfo ci) {
        if (this.entity instanceof LivingEntity livingEntity) {
            SlotNetworking.sendFullSyncTo(livingEntity, player);
        }
    }
}
