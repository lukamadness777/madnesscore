package dev.lukamadness.madnesscore.common.mixin;

import dev.lukamadness.madnesscore.common.slots.VanillaEquipmentMirror;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public abstract class MixinLivingEntityEquipmentMirror {
    @Inject(method = "getItemBySlot", at = @At("RETURN"), cancellable = true)
    private void madnesscore$mirrorSlotArmor(EquipmentSlot slot, CallbackInfoReturnable<ItemStack> cir) {
        if (!cir.getReturnValue().isEmpty()) {
            return;
        }

        LivingEntity self = (LivingEntity) (Object) this;

        ItemStack mirrored = VanillaEquipmentMirror.getMirroredStack(self, slot);
        if (!mirrored.isEmpty()) {
            cir.setReturnValue(mirrored);
        }
    }

    /**
     * Impide equipar directamente en el slot vanilla real (armadura del personaje, dispensador,
     * comandos, etc.) mientras el slot espejado de MadnessCore ya tenga puesta cualquier pieza,
     * sin importar si el stack entrante tiene los mismos componentes (dos piezas distintas con
     * la misma configuración por defecto igual deben tratarse como piezas distintas). Desequipar
     * (stack vacío) siempre se permite.
     */
    @Inject(method = "setItemSlot", at = @At("HEAD"), cancellable = true)
    private void madnesscore$blockVanillaEquipIfMirrored(EquipmentSlot slot, ItemStack stack, CallbackInfo ci) {
        if (stack.isEmpty()) {
            return;
        }

        LivingEntity self = (LivingEntity) (Object) this;
        ItemStack mirrored = VanillaEquipmentMirror.getMirroredStack(self, slot);

        if (!mirrored.isEmpty()) {
            ci.cancel();
        }
    }
}