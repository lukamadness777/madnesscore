package dev.lukamadness.madnesscore.common.mixin;

import com.mojang.logging.LogUtils;
import dev.lukamadness.madnesscore.common.slots.VanillaEquipmentMirror;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * El slot de armadura vanilla (dentro del inventario del jugador) usa como contenedor el propio
 * {@code Inventory}, cuyo {@code setItem} escribe directo a la lista interna de armadura sin pasar
 * nunca por {@code LivingEntity#setItemSlot}. Por eso el mixin sobre {@code setItemSlot} (ver
 * {@link MixinLivingEntityEquipmentMirror}) no alcanza para bloquear el drag-and-drop normal en el
 * inventario: hay que interceptar {@code ArmorSlot#mayPlace} directamente, que es el método real
 * que vanilla usa para decidir si el ítem puede entrar en ese slot.
 */
@Mixin(targets = "net.minecraft.world.inventory.ArmorSlot")
public abstract class MixinArmorSlot {
    // TODO: log temporal para diagnosticar, sacar después.
    @Unique
    private static final Logger MADNESSCORE$LOG = LogUtils.getLogger();

    @Shadow
    @Final
    private LivingEntity owner;

    @Shadow
    @Final
    private EquipmentSlot slot;

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void madnesscore$blockIfMirrored(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.isEmpty()) {
            return;
        }

        ItemStack mirrored = VanillaEquipmentMirror.getMirroredStack(this.owner, this.slot);
        MADNESSCORE$LOG.info("[madnesscore-debug] ArmorSlot#mayPlace slot={} stack={} mirrored={}",
                this.slot, stack, mirrored);
        if (!mirrored.isEmpty()) {
            cir.setReturnValue(false);
        }
    }
}