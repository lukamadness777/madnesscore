package dev.lukamadness.madnesscore.common.client.mixin;

import dev.lukamadness.madnesscore.common.client.slots.SlotHoverManager;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * "tick" solo está declarado en {@code Screen} (nadie lo sobreescribe en la cadena hasta
 * InventoryScreen), por eso no podía inyectarse en {@code MixinInventoryScreen}. Guardado con
 * instanceof para no afectar al resto de pantallas.
 */
@Mixin(Screen.class)
public abstract class MixinScreenTick {

    @Inject(method = "tick", at = @At("TAIL"))
    private void madnesscore$onTick(CallbackInfo ci) {
        if (!((Object) this instanceof InventoryScreen)) return;
        // Reservado para futuras animaciones (ver NOTA en SlotUiState).
    }
}