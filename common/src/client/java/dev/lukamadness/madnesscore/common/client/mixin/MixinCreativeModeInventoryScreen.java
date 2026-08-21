package dev.lukamadness.madnesscore.common.client.mixin;

import dev.lukamadness.madnesscore.common.client.bundledtabs.BundledTabSelector;
import dev.lukamadness.madnesscore.common.client.slots.SlotHoverManager;
import dev.lukamadness.madnesscore.common.client.slots.SlotHoverScreen;
import dev.lukamadness.madnesscore.common.api.slots.SlotGroup;
import dev.lukamadness.madnesscore.common.slots.gui.PlayerDynamicSlot;
import dev.lukamadness.madnesscore.common.slots.gui.PlayerSlotMenu;
import dev.lukamadness.madnesscore.common.slots.gui.Point;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Engancha el {@link BundledTabSelector} a la pantalla del inventario
 * creativo en 4 puntos:
 * - init()        -> crea/inyecta los widgets iniciales.
 * - renderBg(...) -> dibuja la barra lateral + detecta cambio de pestaña.
 * - mouseScrolled -> scroll de la lista de bundle tabs.
 * - removed()     -> limpia estado al cerrar el inventario.
 * <p>
 * "extends Screen" acá es solo un stub para que javac deje llamar
 * addRenderableWidget/removeWidget (protected en Screen) vía "this"
 * directo. Mixin lo descarta al mergear con el target real
 * (CreativeModeInventoryScreen).
 * <p>
 * Además reposiciona nuestros slots dinámicos ({@code PlayerDynamicSlot}) cuando se selecciona la
 * pestaña "Inventory": vainilla ya los trae solos (envuelve TODOS los slots de
 * {@code player.inventoryMenu}, índice a índice, incluidos los nuestros, en
 * {@code CreativeModeInventoryScreen.SlotWrapper} — ver {@code selectTab}), pero calcula su
 * posición en pantalla con una fórmula que asume que solo existen los 46 slots vainilla; para
 * cualquier índice nuestro (46+) esa fórmula da coordenadas que caen encima de otros slots ya
 * existentes (típicamente la hotbar), lo que además explica que el tooltip de la hotbar dejara de
 * verse: nuestro slot vacío, al estar dibujado/registrado DESPUÉS en la lista y exactamente en el
 * mismo punto, terminaba ganando el hoveredSlot de vainilla. Ver {@code madnesscore$repositionDynamicSlots}.
 * <p>
 * FIX (issue #3 - "no me deja abrir los accesorios" en el inventario creativo): a diferencia de
 * {@code MixinInventoryScreen} (supervivencia), esta clase nunca implementaba
 * {@link SlotHoverScreen} ni enganchaba {@link SlotHoverManager} - los slots dinámicos SE
 * reposicionaban bien (ver {@code madnesscore$repositionDynamicSlots}), pero nada llamaba a
 * {@code SlotHoverManager.update()} cuadro a cuadro, así que el hover jamás detectaba que el mouse
 * estaba sobre el slot ancla y el grupo nunca se "abría": los accesorios eran visualmente
 * correctos pero completamente inertes al mouse en este inventario. Se agregan los mismos 3 hooks
 * que ya tiene {@code MixinInventoryScreen} (init/render/renderLabels) más la implementación de
 * {@link SlotHoverScreen}, calculando el rectángulo de cada grupo con la MISMA fórmula que ya usa
 * {@link #madnesscore$repositionDynamicSlots} para reubicar los slots (coordenadas creativas, no
 * las de supervivencia) y solo mientras la pestaña activa es "Inventory" (única en la que estos
 * slots existen visualmente).
 */
@Mixin(CreativeModeInventoryScreen.class)
public abstract class MixinCreativeModeInventoryScreen extends Screen implements SlotHoverScreen {

    @Unique
    private BundledTabSelector madnesscore$selector;

    protected MixinCreativeModeInventoryScreen(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("TAIL"))
    private void madnesscore$onInit(CallbackInfo ci) {
        CreativeModeInventoryScreen self = (CreativeModeInventoryScreen) (Object) this;
        this.madnesscore$selector = new BundledTabSelector(self);
        this.madnesscore$selector.injectWidgets(this::addRenderableWidget, this::removeWidget);
        SlotHoverManager.init(this);
    }

    // FIX (issue #3): equivalente a MixinInventoryScreen#madnesscore$onRender, pero enganchado en
    // "renderBg" (HEAD) en vez de "render". "render" NO se puede usar acá de forma segura: a
    // diferencia de InventoryScreen (que SI sobreescribe render() para trackear mouseX/mouseY para
    // el muñeco 3D), no hay garantia de que CreativeModeInventoryScreen declare "render" de forma
    // directa - Mixin busca el metodo target SOLO en la clase declarada en @Mixin, no en la
    // jerarquia (mismo motivo por el que MixinPlayerDynamicSlotActive tuvo que apuntar a Slot en
    // vez de a PlayerDynamicSlot, ver su javadoc). "renderBg" en cambio YA esta confirmado
    // funcionando en este archivo (ver madnesscore$onRenderBg mas abajo) y corre ANTES del loop de
    // renderSlot (igual orden relativo que HEAD de "render" en supervivencia), asi que el hover
    // sigue quedando actualizado a tiempo para que MixinAbstractContainerScreen lo lea al dibujar
    // cada slot.
    @Inject(method = "renderBg", at = @At("HEAD"))
    private void madnesscore$onUpdateHover(GuiGraphics graphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        SlotHoverManager.update(mouseX, mouseY);
    }

    // FIX (issue #3): dibuja el panel "more_slots" del grupo expandido, igual que
    // MixinInventoryScreen#madnesscore$onRenderLabels.
    // NOTA (no se puede compilar en este entorno): "renderLabels" esta declarado en
    // EffectRenderingInventoryScreen (padre comun de InventoryScreen y CreativeModeInventoryScreen),
    // no en CreativeModeInventoryScreen directamente - Mixin por defecto solo encuentra targets
    // declarados en la clase exacta puesta en @Mixin (ver el mismo problema documentado en
    // MixinPlayerDynamicSlotActive con "isActive"). Este @Inject asume que CreativeModeInventoryScreen
    // SI sobreescribe renderLabels (igual que MixinInventoryScreen ya asume lo mismo para
    // InventoryScreen, con éxito). Confirmar en el IDE; si Mixin tira
    // "could not find any targets matching 'renderLabels'" al arrancar, hay que mover este inject a
    // @Mixin(EffectRenderingInventoryScreen.class) en su lugar.
    @Inject(method = "renderLabels", at = @At("TAIL"))
    private void madnesscore$onRenderLabels(GuiGraphics graphics, int mouseX, int mouseY, CallbackInfo ci) {
        SlotHoverManager.drawActiveGroup(graphics);
    }

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void madnesscore$onRenderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        CreativeModeTab currentTab = CreativeModeInventoryScreenAccessor.madnesscore$getSelectedTab();
        this.madnesscore$selector.renderAndSync(graphics, currentTab);
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void madnesscore$onScroll(double mouseX, double mouseY, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
        CreativeModeTab currentTab = CreativeModeInventoryScreenAccessor.madnesscore$getSelectedTab();
        if (this.madnesscore$selector != null && this.madnesscore$selector.handleScroll(currentTab, mouseX, mouseY, scrollY)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "removed", at = @At("TAIL"))
    private void madnesscore$onRemoved(CallbackInfo ci) {
        if (this.madnesscore$selector != null) {
            this.madnesscore$selector.onClose();
        }
        // FIX (issue #3): limpia el estado de hover al cerrar el inventario creativo, igual que
        // MixinAbstractContainerScreenRemoved ya hace para InventoryScreen (ese mixin solo actua
        // sobre instancias de InventoryScreen, nunca cubrio esta pantalla).
        SlotHoverManager.close();
        SlotHoverManager.removeSelections();
    }

    // ---- Implementación de SlotHoverScreen (FIX issue #3) ----

    @Override
    public PlayerSlotMenu madnesscore$getMenu() {
        // Los grupos/anclas viven en player.inventoryMenu (el InventoryMenu real, mixeado por
        // MixinInventoryMenu), no en el ItemPickerMenu de esta pantalla - es la misma fuente que
        // ya usa madnesscore$repositionDynamicSlots más abajo.
        return (PlayerSlotMenu) Minecraft.getInstance().player.inventoryMenu;
    }

    @Override
    public Rect2i madnesscore$getGroupRect(SlotGroup group) {
        CreativeModeTab currentTab = CreativeModeInventoryScreenAccessor.madnesscore$getSelectedTab();
        // Nuestros slots dinámicos solo existen visualmente (reposicionados) en la pestaña
        // "Inventory" - ver madnesscore$repositionDynamicSlots. En cualquier otra pestaña no hay
        // nada que abrir.
        if (currentTab == null || currentTab.getType() != CreativeModeTab.Type.INVENTORY) {
            return new Rect2i(0, 0, 0, 0);
        }
        PlayerSlotMenu survivalMenu = this.madnesscore$getMenu();
        int groupNum = survivalMenu.madnesscore$getGroupNum(group);
        if (groupNum == 0) {
            return new Rect2i(0, 0, 0, 0);
        }
        // Misma fórmula que madnesscore$repositionDynamicSlots usa para calcular el ancla en
        // coordenadas creativas, a partir del mismo groupNum que ya expone MixinInventoryMenu.
        Point anchor = groupNum < 0
                ? madnesscore$vanillaSlotCreativePos(-groupNum)
                : new Point(8, 6 + (groupNum - 1) * 18);
        return new Rect2i(anchor.x() - 1, anchor.y() - 1, 17, 17);
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

    // Se llama al clickear cualquier pestaña de arriba del inventario
    // creativo, incluso si es la misma ya seleccionada. Vanilla repuebla
    // menu.items ahí adentro con la lista propia de esa CreativeModeTab,
    // pisando lo que nosotros hayamos puesto con updateItems(). Este hook
    // corre justo después y vuelve a imponer nuestra lista si corresponde.
    @Inject(method = "selectTab", at = @At("TAIL"), require = 1)
    private void madnesscore$onSelectTab(CreativeModeTab tab, CallbackInfo ci) {
        if (this.madnesscore$selector != null) {
            this.madnesscore$selector.onVanillaTabSelected(tab);
        }
    }

    /**
     * Corrige la posición en pantalla de nuestros slots dinámicos dentro de la pestaña
     * "Inventory" del creativo (ver nota de clase). Se ejecuta cada vez que se (re)selecciona esa
     * pestaña, después de que vainilla ya reconstruyó {@code this.menu.slots} envolviendo
     * {@code player.inventoryMenu.slots} en {@code SlotWrapper}.
     * <p>
     * Los ubicamos en una columna libre al costado izquierdo del panel (fuera del área 5x9 de
     * items ocultos y sin pisar la grilla de armadura/mano secundaria que el creativo reubica
     * arriba), reconstruyendo la posición de cada slot como
     * {@code ancla_creativo + (posición_original - ancla_supervivencia)}: así conservamos
     * exactamente el mismo patrón de apilado en zigzag que ya calculó {@code MixinInventoryMenu}
     * para el layout de supervivencia, solo que reanclado a un punto distinto.
     */
    @Inject(method = "selectTab", at = @At("TAIL"), require = 1)
    private void madnesscore$repositionDynamicSlots(CreativeModeTab tab, CallbackInfo ci) {
        if (tab.getType() != CreativeModeTab.Type.INVENTORY) {
            return;
        }
        Player player = Minecraft.getInstance().player;
        if (player == null || !(player.inventoryMenu instanceof PlayerSlotMenu survivalMenu)) {
            return;
        }
        CreativeModeInventoryScreen.ItemPickerMenu menu =
                ((CreativeModeInventoryScreen) (Object) this).getMenu();

        for (Slot wrapped : menu.slots) {
            // "SlotWrapper" (la clase real de vainilla) es package-private, así que la
            // reconocemos por la interfaz pública que le agregamos vía mixin en vez de nombrarla
            // directo (ver SlotWrapperAccessor).
            if (!(wrapped instanceof SlotWrapperAccessor sw)) {
                continue;
            }
            Slot original = sw.madnesscore$getTarget();
            if (!(original instanceof PlayerDynamicSlot ds)) {
                continue;
            }

            int groupNum = survivalMenu.madnesscore$getGroupNum(ds.madnesscore$getGroup());
            Point survivalAnchor = survivalMenu.madnesscore$getGroupPos(ds.madnesscore$getGroup());
            if (groupNum == 0 || survivalAnchor == null) {
                continue;
            }

            Point creativeAnchor = groupNum < 0
                    ? madnesscore$vanillaSlotCreativePos(-groupNum)
                    : new Point(8, 6 + (groupNum - 1) * 18);

            int newX = creativeAnchor.x() + (original.x - survivalAnchor.x());
            int newY = creativeAnchor.y() + (original.y - survivalAnchor.y());
            ((SlotAccessor) wrapped).madnesscore$setX(newX);
            ((SlotAccessor) wrapped).madnesscore$setY(newY);
        }
    }

    /**
     * Réplica exacta de la fórmula que vainilla usa en {@code selectTab} para reubicar cada slot
     * de {@code player.inventoryMenu} dentro de la pestaña "Inventory" del creativo (armadura y
     * mano secundaria se mueven arriba, a la zona donde normalmente iría la mesa de crafteo, que
     * el creativo no muestra). La necesitamos aparte para calcular el punto de anclaje "en
     * coordenadas creativas" de un slot_id vainilla (5-8 cabeza/pecho/piernas/pies, 45 mano
     * secundaria) tal como hace vainilla, sin depender de que ese índice exista todavía como slot
     * envuelto en este momento del loop.
     */
    @Unique
    private static Point madnesscore$vanillaSlotCreativePos(int k) {
        int x;
        int y;
        if (k >= 5 && k < 9) {
            int k1 = k - 5;
            x = 54 + (k1 / 2) * 54;
            y = 6 + (k1 % 2) * 27;
        } else if (k >= 0 && k < 5) {
            x = -2000;
            y = -2000;
        } else if (k == 45) {
            x = 35;
            y = 20;
        } else {
            int j1 = k - 9;
            int l1 = j1 % 9;
            int j2 = j1 / 9;
            x = 9 + l1 * 18;
            y = k >= 36 ? 112 : 54 + j2 * 18;
        }
        return new Point(x, y);
    }
}