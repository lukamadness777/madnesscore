package dev.lukamadness.madnesscore.common.client.mixin;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.client.slots.SlotUiState;
import dev.lukamadness.madnesscore.common.slots.gui.PlayerDynamicSlot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Dibuja el fondo de icono/blank de cada slot dinámico detrás del item (equivalente al parche de
 * z-order de Trinkets, simplificado: en vez de reordenar manualmente las capas de dibujo, esta
 * clase dibuja el fondo ANTES de que vainilla dibuje el resto del slot en la misma llamada, así
 * el orden natural de dibujo ya deja el item por encima sin necesitar tocar la matriz de
 * profundidad), y oculta los slots "extra" (no ancla) de un grupo mientras no está expandido.
 * <p>
 * Portado de dev.emi.trinkets.mixin.HandledScreenMixin, con el truco de
 * {@code drawSlotHighlightFront} (z-fighting del highlight de foco) simplificado a un relleno de
 * color liso en vez de reusar la sprite de highlight vainilla (ver NOTA en render de highlight,
 * más abajo) porque el nombre exacto de esa sprite en mappings oficiales 1.21.1 no se pudo
 * confirmar desde este entorno.
 */
@Mixin(net.minecraft.client.gui.screens.inventory.AbstractContainerScreen.class)
public abstract class MixinAbstractContainerScreen extends Screen {

    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @Shadow
    protected AbstractContainerMenu menu;

    @Shadow
    private boolean isHovering(Slot pSlot, double pMouseX, double pMouseY) {
        throw new AssertionError();
    }

    @Unique
    private static final ResourceLocation MORE_SLOTS =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "textures/gui/more_slots.png");

    /**
     * Fondo "vacío" que se dibuja detrás de un slot dinámico que YA tiene un ítem, en vez de no
     * dibujar nada. El asset ya estaba en resources (assets/madnesscore/textures/gui/blank_back.png)
     * pero nunca se usaba desde código. Portado de dev.emi.trinkets.mixin.HandledScreenMixin#BLANK_BACK.
     */
    @Unique
    private static final ResourceLocation BLANK_BACK =
            ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "textures/gui/blank_back.png");

    protected MixinAbstractContainerScreen(net.minecraft.network.chat.Component title) {
        super(title);
    }

    /**
     * FIX (bug de iconos invisibles en el inventario creativo): en supervivencia, el {@link Slot}
     * que llega ac&aacute; es directamente un {@link PlayerDynamicSlot}. Pero en la pesta&ntilde;a
     * "Inventory" del inventario creativo, vainilla envuelve TODOS los slots de
     * {@code player.inventoryMenu} (incluidos los nuestros) en
     * {@code CreativeModeInventoryScreen.SlotWrapper} antes de agregarlos a {@code menu.slots} -
     * el objeto que realmente se itera y se pasa a {@code renderSlot} es ese wrapper, no nuestro
     * {@link PlayerDynamicSlot} original. Como el {@code instanceof PlayerDynamicSlot} de abajo
     * fallaba contra el wrapper, esta clase directamente se salteaba (sin cancelar) el dibujo del
     * fondo/&iacute;cono para CUALQUIER slot dinamico en creativo, dejandolos en blanco.
     * <p>
     * Reconoce el mismo patron que ya usa {@code MixinCreativeModeInventoryScreen#madnesscore$repositionDynamicSlots}
     * via {@link SlotWrapperAccessor}. Devuelve el {@link PlayerDynamicSlot} real (para leer
     * grupo/tipo/ancla/icono), pero el llamador sigue usando el {@code slot} original (wrapper o
     * no) para las coordenadas de pantalla, porque en creativo esas coordenadas ya fueron
     * corregidas sobre el wrapper mismo, no sobre el slot envuelto.
     */
    @Unique
    private static PlayerDynamicSlot madnesscore$unwrap(Slot slot) {
        if (slot instanceof PlayerDynamicSlot ds) {
            return ds;
        }
        if (slot instanceof SlotWrapperAccessor wrapper && wrapper.madnesscore$getTarget() instanceof PlayerDynamicSlot ds) {
            return ds;
        }
        return null;
    }

    /**
     * Oculta los slots "no ancla" de un grupo colapsado (no interfiere con el item/lógica: el
     * slot sigue existiendo y sincronizado, solo no se dibuja). El slot ancla (offset 0, en la
     * posición vainilla del grupo) siempre se dibuja, igual que una pieza de armadura normal.
     * <p>
     * NOTA: desde que {@code MixinPlayerDynamicSlotActive} existe, vainilla ya ni siquiera llama a
     * este método para un slot inactivo (ver {@code Slot#isActive()}), así que el cancel de acá
     * abajo pasó a ser un guard defensivo/redundante en vez de la única barrera - se deja igual
     * por las dudas (ej. algún otro mod que llame a renderSlot manualmente).
     * <p>
     * También empuja el z-order del fondo+ítem por delante del muñeco 3D que dibuja
     * {@code InventoryScreen} (que usa depth-test real, así que el orden de llamada NO alcanza
     * para ganarle - hace falta trasladar la matriz, igual que dev.emi.trinkets.mixin.HandledScreenMixin#changeZ).
     * Se balancea con {@link #madnesscore$onRenderSlotTail}.
     */
    @Inject(method = "renderSlot", at = @At("HEAD"), cancellable = true)
    private void madnesscore$onRenderSlotHead(GuiGraphics graphics, Slot slot, CallbackInfo ci) {
        PlayerDynamicSlot ds = madnesscore$unwrap(slot);
        if (ds == null) {
            return;
        }
        boolean groupOpen = SlotUiState.activeGroup == ds.madnesscore$getGroup();
        if (!ds.madnesscore$isAnchor() && !groupOpen) {
            ci.cancel();
            return;
        }
        if (!ds.madnesscore$isAnchor() && SlotUiState.activeType != null
                && SlotUiState.activeType != ds.madnesscore$getType()) {
            ci.cancel();
            return;
        }

        // Empuja Z ANTES de dibujar: como no cancelamos, vainilla sigue ejecutando el resto de
        // este mismo método (drawSlot original) inmediatamente después de este HEAD injection, así
        // que el ítem/tooltip que dibuja vainilla también queda dentro de la matriz trasladada.
        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, 300);

        // Si el slot tiene ítem, se dibuja un fondo "vacío" (BLANK_BACK) en vez de no dibujar
        // nada, para no perder el respaldo visual del panel al equipar algo (antes el fondo
        // directamente desaparecía).
        ResourceLocation background = slot.hasItem() ? BLANK_BACK : ds.madnesscore$getBackground();
        if (background == null) {
            background = BLANK_BACK;
        }
        graphics.blit(background, slot.x, slot.y, 0, 0, 16, 16, 16, 16);
        if (!ds.madnesscore$isAnchor()) {
            graphics.blit(MORE_SLOTS, slot.x - 1, slot.y - 1, 4, 4, 18, 18, 256, 256);
        }
        if (this.hoveredSlot == slot && groupOpen) {
            graphics.fill(slot.x - 1, slot.y - 1, slot.x + 17, slot.y + 17, 0x80FFFFFF);
        }
    }

    /**
     * Balancea el {@code pushPose()} de {@link #madnesscore$onRenderSlotHead}. Si ese método
     * canceló el render (slot colapsado), esta inyección en TAIL nunca se alcanza -tampoco hace
     * falta, porque en ese caso nunca se hizo push-, así que el stack de matrices siempre queda
     * balanceado.
     */
    @Inject(method = "renderSlot", at = @At("TAIL"))
    private void madnesscore$onRenderSlotTail(GuiGraphics graphics, Slot slot, CallbackInfo ci) {
        if (madnesscore$unwrap(slot) != null) {
            graphics.pose().popPose();
        }
    }

    /**
     * FIX (bug #3 - "el tooltip aparece atras del item"): {@code GuiGraphics#renderTooltip}
     * vainilla siempre dibuja el tooltip trasladando {@code +400} en Z desde la pose ACTUAL en el
     * momento en que se lo llama - un valor fijo, sin relacion con el z-push de
     * {@link #madnesscore$onRenderSlotHead}. Ese push existe solo mientras dura la llamada a
     * {@code renderSlot} (se hace pop en {@link #madnesscore$onRenderSlotTail} de arriba), y el
     * tooltip se dibuja DESPUES, en una llamada aparte, ya sin ese push activo. El icono de un
     * slot dinamico normalmente termina compitiendo en Z contra el propio +400 del tooltip -
     * vainilla ya reserva ese margen para item vs tooltip en un slot comun, pero acá el item
     * arranca +300 mas arriba de lo normal (para poder verse por encima del muñeco 3D real, que sí
     * usa depth-test), asi que ese margen deja de alcanzar y el tooltip puede terminar quedando
     * por atras.
     * <p>
     * La solucion no es afinar el numero a mano (dependeria de en que Z exacto vainilla dibuja
     * normalmente el icono de un slot, que no se pudo confirmar en este entorno): en vez de eso,
     * cuando el slot bajo el mouse es uno de los nuestros, este método sube el tooltip ANTES de
     * que vainilla lo dibuje con el MISMO push (+300) que ya usa el item - así el tooltip vuelve a
     * quedar +400 por encima del item, sea cual sea el Z base real, exactamente como pasa siempre
     * en un slot vainilla comun.
     */
    @Inject(method = "renderTooltip(Lnet/minecraft/client/gui/GuiGraphics;II)V", at = @At("HEAD"))
    private void madnesscore$onRenderTooltipHead(GuiGraphics graphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.hoveredSlot != null && madnesscore$unwrap(this.hoveredSlot) != null) {
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 300);
        }
    }

    @Inject(method = "renderTooltip(Lnet/minecraft/client/gui/GuiGraphics;II)V", at = @At("TAIL"))
    private void madnesscore$onRenderTooltipTail(GuiGraphics graphics, int mouseX, int mouseY, CallbackInfo ci) {
        if (this.hoveredSlot != null && madnesscore$unwrap(this.hoveredSlot) != null) {
            graphics.pose().popPose();
        }
    }

    /**
     * FIX (bug #2 - "click afuera del inventario tira el item del slot"): {@code hasClickedOutside}
     * vainilla decide "afuera" comparando el mouse SOLO contra el rectangulo del panel principal
     * ({@code leftPos}/{@code topPos}/{@code imageWidth}/{@code imageHeight}) - no le importa si
     * hay un {@link Slot} bajo el cursor. Nuestro panel flotante de slots dinamicos se dibuja
     * fuera de ese rectangulo, asi que un click ahi siempre da {@code hasClickedOutside = true}.
     * <p>
     * El problema es que {@code mouseClicked} SI encuentra el slot (via {@code findSlot}, que
     * recorre {@code menu.slots} sin importar el rectangulo del panel) y se lo pasa de todos
     * modos a {@code slotClicked(slot, -999, boton, ClickType.THROW)} - y {@code slotClicked}
     * vainilla, si el {@code Slot} no es null, PISA ese -999 con {@code slot.index} antes de
     * mandarlo al servidor. Resultado neto: el click llega al servidor como
     * {@code ClickType.THROW} sobre el slot real - exactamente la accion de "Q" para tirar el
     * item del slot - en vez de como un {@code PICKUP} normal.
     * <p>
     * El fix: si el punto clickeado cae sobre uno de nuestros slots dinamicos activos, nunca es
     * "afuera", sin importar donde este el panel principal.
     */
    @Inject(method = "hasClickedOutside", at = @At("HEAD"), cancellable = true)
    private void madnesscore$hasClickedOutside(double mouseX, double mouseY, int guiLeft, int guiTop, int mouseButton, CallbackInfoReturnable<Boolean> cir) {
        for (Slot slot : this.menu.slots) {
            if (slot.isActive() && madnesscore$unwrap(slot) != null && this.isHovering(slot, mouseX, mouseY)) {
                cir.setReturnValue(false);
                return;
            }
        }
    }

    /**
     * Mientras un grupo está expandido, solo sus propios slots (o el inventario normal, para
     * poder arrastrar un item hacia adentro) responden al mouse — el resto del inventario queda
     * "congelado", igual que al tener abierto el panel flotante de Trinkets.
     */
    @Inject(method = "isHovering(Lnet/minecraft/world/inventory/Slot;DD)Z", at = @At("HEAD"), cancellable = true)
    private void madnesscore$isHovering(Slot slot, double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
        if (SlotUiState.activeGroup == null) {
            return;
        }
        PlayerDynamicSlot ds = madnesscore$unwrap(slot);
        if (ds != null) {
            boolean visible = ds.madnesscore$isAnchor() || SlotUiState.activeGroup == ds.madnesscore$getGroup();
            if (!visible) {
                cir.setReturnValue(false);
            }
        }
    }
}