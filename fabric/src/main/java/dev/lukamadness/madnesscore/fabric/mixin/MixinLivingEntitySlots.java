package dev.lukamadness.madnesscore.fabric.mixin;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.slots.LivingEntitySlotComponent;
import dev.lukamadness.madnesscore.common.slots.SlotDeathHandler;
import dev.lukamadness.madnesscore.common.slots.SlotTicker;
import dev.lukamadness.madnesscore.fabric.slots.SlotComponentHolder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntitySlots implements SlotComponentHolder {
    @Unique
    private static final String MADNESSCORE$NBT_KEY = MadnessCoreCommon.MOD_ID + ":slots";

    @Unique
    private LivingEntitySlotComponent madnesscore$slotComponent;

    @Override
    public LivingEntitySlotComponent madnesscore$getSlotComponent() {
        if (this.madnesscore$slotComponent == null) {
            this.madnesscore$slotComponent = new LivingEntitySlotComponent((LivingEntity) (Object) this);
        }
        return this.madnesscore$slotComponent;
    }

    @Override
    public void madnesscore$setSlotComponent(LivingEntitySlotComponent component) {
        this.madnesscore$slotComponent = component;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void madnesscore$tick(CallbackInfo ci) {
        SlotTicker.tick((LivingEntity) (Object) this);
    }

    @Inject(method = "dropAllDeathLoot", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;dropEquipment()V"))
    private void madnesscore$dropSlotsOnDeath(ServerLevel level, DamageSource damageSource, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        SlotDeathHandler.dropOnDeath(self, level);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void madnesscore$writeSlots(CompoundTag tag, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        HolderLookup.Provider lookup = self.level().registryAccess();
        CompoundTag slotsTag = new CompoundTag();
        this.madnesscore$getSlotComponent().writeToNbt(slotsTag, lookup);
        tag.put(MADNESSCORE$NBT_KEY, slotsTag);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void madnesscore$readSlots(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains(MADNESSCORE$NBT_KEY)) {
            LivingEntity self = (LivingEntity) (Object) this;
            HolderLookup.Provider lookup = self.level().registryAccess();
            this.madnesscore$getSlotComponent().readFromNbt(tag.getCompound(MADNESSCORE$NBT_KEY), lookup);
        }
    }
}
