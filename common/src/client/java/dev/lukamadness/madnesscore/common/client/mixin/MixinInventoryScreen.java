package dev.lukamadness.madnesscore.common.client.mixin;

import dev.lukamadness.madnesscore.common.client.slots.SlotHoverManager;
import dev.lukamadness.madnesscore.common.client.slots.SlotHoverScreen;
import dev.lukamadness.madnesscore.common.api.slots.SlotGroup;
import dev.lukamadness.madnesscore.common.slots.gui.Point;
import dev.lukamadness.madnesscore.common.slots.gui.PlayerSlotMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Reemplaza al viejo botón + {@code SlotsMenu}/{@code SlotsScreen} separados: engancha
 * {@link SlotHoverManager} directamente sobre el inventario de supervivencia vainilla, para que
 * los slots dinámicos (ya inyectados server-side por {@code MixinInventoryMenu}) se dibujen
 * anclados alrededor del ícono del jugador y se expandan al pasar el mouse por encima, en vez de
 * abrir una pantalla aparte.
 * <p>
 * "extends Screen" es solo un stub para que javac deje llamar métodos heredados de Screen
 * directamente vía "this"; Mixin lo descarta al mergear con el target real (InventoryScreen).
 * Mismo truco que el resto de los mixins de este paquete.
 * <p>
 * "hoveredSlot" y "menu" NO se pueden @Shadow-ear acá: están declarados en
 * {@code AbstractContainerScreen}, no en {@code InventoryScreen} (que ni siquiera hereda
 * directo de ella: en el medio está {@code EffectRenderingInventoryScreen}) — Mixin @Shadow
 * solo resuelve campos declarados directamente en la clase target. Por eso se leen a través de
 * {@link AbstractContainerScreenAccessor}, exactamente igual que ya se hace acá mismo con
 * leftPos/topPos.
 * <p>
 * Portado de dev.emi.trinkets.mixin.InventoryScreenMixin.
 */
@Mixin(InventoryScreen.class)
public abstract class MixinInventoryScreen extends Screen implements SlotHoverScreen {

    protected MixinInventoryScreen(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"))
    private void madnesscore$onInit(CallbackInfo ci) {
        SlotHoverManager.init(this);
    }

//    @Inject(method = "tick", at = @At("TAIL"))
//    private void madnesscore$onTick(CallbackInfo ci) {
//        // Reservado para futuras animaciones (ver NOTA en SlotUiState); sin timer propio no hay
//        // nada que decrementar por ahora, pero se deja el hook para no tener que volver a tocar
//        // el mixin cuando se agregue.
//    }

    @Inject(method = "render", at = @At("HEAD"))
    private void madnesscore$onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        SlotHoverManager.update(mouseX, mouseY);
    }

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void madnesscore$onRenderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        // El panel lateral para grupos "flotantes" extra (más de 4 columnas junto al ícono del
        // jugador) se dejó afuera de este port inicial; ver MIGRATION_NOTES.md.
    }

    @Inject(method = "renderLabels", at = @At("TAIL"))
    private void madnesscore$onRenderLabels(GuiGraphics graphics, int mouseX, int mouseY, CallbackInfo ci) {
        SlotHoverManager.drawActiveGroup(graphics);
    }

//    @Inject(method = "removed", at = @At("HEAD"))
//    private void madnesscore$onRemoved(CallbackInfo ci) {
//        SlotHoverManager.close();
//        SlotHoverManager.removeSelections();
//    }

    @Override
    public PlayerSlotMenu madnesscore$getMenu() {
        return (PlayerSlotMenu) ((AbstractContainerScreenAccessor) this).madnesscore$getMenu();
    }

    @Override
    public Rect2i madnesscore$getGroupRect(SlotGroup group) {
        Point pos = this.madnesscore$getMenu().madnesscore$getGroupPos(group);
        if (pos != null) {
            return new Rect2i(pos.x() - 1, pos.y() - 1, 17, 17);
        }
        return new Rect2i(0, 0, 0, 0);
    }

    @Override
    public Slot madnesscore$getHoveredSlot() {
        return ((AbstractContainerScreenAccessor) this).madnesscore$accessorHoveredSlot();
    }

    @Override
    public int madnesscore$getX() {
        return ((AbstractContainerScreenAccessor) this).madnesscore$getLeftPos();
    }

    @Override
    public int madnesscore$getY() {
        return ((AbstractContainerScreenAccessor) this).madnesscore$getTopPos();
    }
}