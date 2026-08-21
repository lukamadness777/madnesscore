package dev.lukamadness.madnesscore.common.client.mixin;

import dev.lukamadness.madnesscore.common.client.slots.SlotUiState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Vuelve los botones/widgets no interactuables mientras hay un grupo de slots expandido (mismo
 * motivo que Trinkets: evita que, por ejemplo, un botón que quedó debajo del panel desplegado
 * reaccione al mouse/click).
 * <p>
 * Simplificado respecto del original: en vez de apuntar al sitio exacto (PUTFIELD) donde vainilla
 * asigna el campo "hovered" dentro de {@code render(...)} (Yarn), esta versión fuerza el campo a
 * {@code false} al HEAD del método, con el mismo efecto neto y sin depender de un punto de
 * inyección tan frágil. NOTA: confirmar en el IDE que el campo se llama "isHovered" para esta
 * versión (1.21.1); si no compila, buscar en {@code AbstractWidget} el único {@code boolean} que
 * se lee en {@code isHoveredOrFocused()}/se usa para dibujar el resalte al pasar el mouse.
 * <p>
 * Portado de dev.emi.trinkets.mixin.ClickableWidgetMixin.
 */
@Mixin(AbstractWidget.class)
public abstract class MixinAbstractWidget {

    @Shadow
    private boolean isHovered;

    @Inject(method = "render", at = @At("HEAD"))
    private void madnesscore$onRenderHead(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (SlotUiState.activeGroup != null) {
            this.isHovered = false;
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void madnesscore$mouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (SlotUiState.activeGroup != null) {
            cir.setReturnValue(false);
        }
    }
}
