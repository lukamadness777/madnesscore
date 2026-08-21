package dev.lukamadness.madnesscore.common.mixin;

import com.google.common.collect.ImmutableList;
import dev.lukamadness.madnesscore.common.mixin.accessor.ContainerMenuAccessor;
import dev.lukamadness.madnesscore.common.slots.SlotsApi;
import dev.lukamadness.madnesscore.common.api.slots.SlotComponent;
import dev.lukamadness.madnesscore.common.api.slots.SlotGroup;
import dev.lukamadness.madnesscore.common.api.slots.SlotInventory;
import dev.lukamadness.madnesscore.common.api.slots.SlotReference;
import dev.lukamadness.madnesscore.common.api.slots.SlotType;
import dev.lukamadness.madnesscore.common.slots.gui.Point;
import dev.lukamadness.madnesscore.common.slots.gui.PlayerDynamicSlot;
import dev.lukamadness.madnesscore.common.slots.gui.PlayerSlotMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Agrega los slots dinámicos del sistema de Madness Core directamente al contenedor del
 * inventario normal del jugador (tanto supervivencia como creativo comparten {@code InventoryMenu}
 * del lado servidor), anclados por {@code slot_id} igual que los grupos ya definidos en
 * {@code group.json} (5=cabeza, 6=pecho, 7=piernas, 8=pies, 45=mano secundaria) o, si el grupo no
 * tiene slot_id, acomodados en columnas a la izquierda del ícono del jugador.
 * <p>
 * Corre en AMBOS lados (main, no client): al igual que los slots vainilla, la validación de
 * inserción/extracción y el quick-move tienen que ser consistentes en servidor y en la copia
 * predictiva del cliente. Todo lo puramente visual (qué grupo está "abierto" bajo el mouse, qué
 * slots dibujar expandidos) vive aparte, en el sourceSet client (ver
 * {@code SlotHoverManager}/{@code MixinAbstractContainerScreen}), así este mixin no depende nunca
 * de clases client-only y el módulo common/main sigue siendo válido en un servidor dedicado.
 * <p>
 * Portado de dev.emi.trinkets.mixin.PlayerScreenHandlerMixin.
 * <p>
 * NOTA (no se puede compilar en este entorno): confirmar en el IDE que el constructor de
 * InventoryMenu es exactamente {@code (Inventory, boolean, Player)} y que
 * {@code AbstractContainerMenu#quickMoveStack(Player, int)} es el nombre correcto para esta
 * versión (1.21.1); ambos son nombres muy estables en mappings oficiales pero no se pudieron
 * verificar contra el jar real desde este entorno sandboxed.
 */
@Mixin(InventoryMenu.class)
public abstract class MixinInventoryMenu extends AbstractContainerMenu implements PlayerSlotMenu {

    @Unique
    private final Map<SlotGroup, Integer> madnesscore$groupNums = new HashMap<>();
    @Unique
    private final Map<SlotGroup, Point> madnesscore$groupPos = new HashMap<>();
    @Unique
    private final Map<SlotGroup, List<Point>> madnesscore$slotHeights = new HashMap<>();
    @Unique
    private final Map<SlotGroup, List<SlotType>> madnesscore$slotTypes = new HashMap<>();
    @Unique
    private final Map<SlotGroup, Integer> madnesscore$slotWidths = new HashMap<>();
    @Unique
    private int madnesscore$slotRangeStart = 0;
    @Unique
    private int madnesscore$slotRangeEnd = 0;
    @Unique
    private int madnesscore$groupCount = 0;
    @Unique
    private Player madnesscore$owner;

    private MixinInventoryMenu() {
        super(null, 0);
    }

    @Inject(at = @At("RETURN"), method = "<init>")
    private void madnesscore$init(Inventory inventory, boolean isOnServer, Player owner, CallbackInfo info) {
        this.madnesscore$owner = owner;

        // A PROPOSITO no llamamos madnesscore$updateSlots(true) aca (aunque antes lo haciamos).
        // Este constructor corre en AMBOS lados exactamente cuando se crea el Player: en el
        // servidor, SlotsApi.getServerEntityLoader() YA tiene los datos (cargados al arrancar el
        // server), asi que un updateSlots(true) inmediato agregaba los slots dinamicos de una,
        // dejando el InventoryMenu del servidor con, por ej., 47 slots. En el cliente, en cambio,
        // SlotsApi.getClientEntityLoader() todavia esta vacio en este punto (el cliente no puede
        // leer "data/" de los data packs; recien se entera por red via SyncSlotDefinitionsPayload,
        // que es asincronico y NO hay garantia de que llegue antes de que el motor mande el
        // ClientboundContainerSetContentPacket inicial del login) -> el InventoryMenu del cliente
        // se quedaba en 46. Server (47) != cliente (46) para el MISMO containerId 0 -> el primer
        // container_set_content que llega trae mas indices de los que this.slots tiene ->
        // IndexOutOfBoundsException en AbstractContainerMenu#getSlot -> "Network Protocol Error"
        // -> desconexion ("Too many suspicious packets"). Esto reprodujo identico en Fabric y en
        // NeoForge (el evento equivalente a OnDatapackSyncEvent no tiene el mismo orden garantizado
        // en ambos loaders respecto al packet inicial de login, asi que confiar en esa carrera no
        // alcanza).
        //
        // La solucion real es no depender de NINGUN orden: los dos lados arrancan siempre en la
        // misma base (0 slots dinamicos, el InventoryMenu vainilla de 46) y el rebuild a la
        // cantidad real se dispara recien cuando el servidor manda SyncSlotDefinitionsPayload
        // (ver SlotEventListener#onDatapackSync en NeoForge / MadnessCore#registerSlotDataSync en
        // Fabric, que ahora hacen su PROPIO madnesscore$updateSlots(true) del lado servidor antes
        // de mandar el paquete y de reenviar el contenido) y cuando el cliente lo recibe y aplica
        // (ver SlotNetworking#handleDefinitionsSyncOnClient). Como ambos ocurren recien a partir
        // del mismo paquete (y el reenvio de contenido se manda DESPUES de ese paquete, en la
        // misma conexion, que preserva el orden), server y cliente terminan siempre de acuerdo.
    }

    @Override
    public void madnesscore$updateSlots(boolean slotsChanged) {
        SlotsApi.getSlotComponent(this.madnesscore$owner).ifPresent(component -> {
            if (slotsChanged) {
                component.update();
            }
            Map<String, SlotGroup> groups = component.getGroups();
            madnesscore$groupPos.clear();

            //noinspection unchecked
            List<ItemStack> lastSlots = ((ContainerMenuAccessor) (Object) this).madnesscore$getLastSlots();
            while (madnesscore$slotRangeStart < madnesscore$slotRangeEnd) {
                this.slots.remove(madnesscore$slotRangeStart);
                if (lastSlots.size() > madnesscore$slotRangeStart) {
                    lastSlots.remove(madnesscore$slotRangeStart);
                }
                madnesscore$slotRangeEnd--;
            }

            int groupNum = 1; // Arranca en 1 porque el slot de mano secundaria ya existe (id 45)

            for (SlotGroup group : groups.values().stream().sorted(Comparator.comparingInt(SlotGroup::getOrder)).toList()) {
                if (!madnesscore$hasSlots(component, group)) {
                    continue;
                }
                int id = group.getSlotId();
                if (id != -1) {
                    if (this.slots.size() > id) {
                        Slot slot = this.slots.get(id);
                        if (!(slot instanceof PlayerDynamicSlot)) {
                            madnesscore$groupPos.put(group, new Point(slot.x, slot.y));
                            madnesscore$groupNums.put(group, -id);
                        }
                    }
                } else {
                    int x = 77;
                    int y;
                    if (groupNum >= 4) {
                        x = 4 - (groupNum / 4) * 18;
                        y = 8 + (groupNum % 4) * 18;
                    } else {
                        y = 62 - groupNum * 18;
                    }
                    madnesscore$groupPos.put(group, new Point(x, y));
                    madnesscore$groupNums.put(group, groupNum);
                    groupNum++;
                }
            }
            madnesscore$groupCount = Math.max(0, groupNum - 4);
            madnesscore$slotRangeStart = this.slots.size();
            madnesscore$slotWidths.clear();
            madnesscore$slotHeights.clear();
            madnesscore$slotTypes.clear();

            for (Map.Entry<String, Map<String, SlotInventory>> entry : component.getInventory().entrySet()) {
                String groupId = entry.getKey();
                SlotGroup group = groups.get(groupId);
                if (group == null) {
                    continue;
                }
                int groupOffset = 1;
                if (group.getSlotId() != -1) {
                    groupOffset++;
                }
                int width = 0;
                Point pos = madnesscore$getGroupPos(group);
                if (pos == null) {
                    continue;
                }
                // Los tipos con items listos (hasReadyItems) van primero - asi el "ancla" del
                // grupo (groupOffset == 1, ver mas abajo) termina siendo siempre uno usable si hay
                // alguno, sin importar el "order" data-driven que traiga cada slot type. Ver
                // MixinPlayerDynamicSlotActive para la otra mitad de este fix (por que hace falta
                // ademas de esto).
                List<Map.Entry<String, SlotInventory>> orderedSlots = entry.getValue().entrySet().stream()
                        .sorted(Comparator
                                .comparing((Map.Entry<String, SlotInventory> e) -> !SlotsApi.hasReadyItems(e.getValue().getSlotType()))
                                .thenComparingInt(e -> e.getValue().getSlotType().getOrder()))
                        .toList();
                for (Map.Entry<String, SlotInventory> slotEntry : orderedSlots) {
                    SlotInventory stacks = slotEntry.getValue();
                    if (stacks.getContainerSize() == 0) {
                        continue;
                    }
                    int slotOffset = 1;
                    int x = (int) ((groupOffset / 2) * 18 * Math.pow(-1, groupOffset));
                    // FIX ("moreslots dibuja espacio de mas"): SlotHoverManager usa
                    // madnesscore$getSlotWidth/getSlotHeights/getSlotTypes UNICAMENTE para medir y
                    // dibujar el panel more_slots.png (ver SlotHoverManager#update/#drawGroup) - no
                    // tiene nada que ver con si el PlayerDynamicSlot en si se crea o se puede usar.
                    // Antes se agregaba ACA todo tipo con getContainerSize() > 0, sin importar si
                    // tenia algun item posible - un grupo como "head" (hat listo + face sin tag)
                    // terminaba reservando 2 columnas de ancho en el panel aunque "face" nunca
                    // fuera a mostrar nada, dejando un hueco vacio dibujado al lado de "hat". Con
                    // este chequeo, esos tipos siguen recibiendo su PlayerDynamicSlot real mas
                    // abajo (siguen "creandose", persisten su item si el datapack cambia despues)
                    // pero el panel deja de contarlos/dibujarlos.
                    if (SlotsApi.hasReadyItems(stacks.getSlotType())) {
                        madnesscore$slotHeights.computeIfAbsent(group, k -> new ArrayList<>()).add(new Point(x, stacks.getContainerSize()));
                        madnesscore$slotTypes.computeIfAbsent(group, k -> new ArrayList<>()).add(stacks.getSlotType());
                        width++;
                    }
                    for (int i = 0; i < stacks.getContainerSize(); i++) {
                        int y = (int) (pos.y() + (slotOffset / 2) * 18 * Math.pow(-1, slotOffset));
                        this.addSlot(new PlayerDynamicSlot(stacks, i, x + pos.x(), y, group, stacks.getSlotType(), i,
                                groupOffset == 1 && i == 0));
                        slotOffset++;
                    }
                    groupOffset++;
                }
                madnesscore$slotWidths.put(group, width);
            }

            madnesscore$slotRangeEnd = this.slots.size();
        });
    }

    /**
     * FIX ("moreslots dibuja el espacio faltante y a si mismo" en grupos sin ningun item
     * posible): antes alcanzaba con {@code getContainerSize() > 0} (el grupo tiene inventario
     * reservado) para que el grupo recibiera posicion via {@code madnesscore$groupPos} - un grupo
     * como "legs" (solo "belt", sin tag todavia) seguia registrandose en la posicion vainilla real
     * (encima del slot de piernas de verdad), y aunque su ancla ya queda inactiva gracias a
     * {@code MixinPlayerDynamicSlotActive} (no se dibuja, no es clickeable), esa posicion seguia
     * siendo un {@code Rect2i} valido para {@link dev.lukamadness.madnesscore.common.client.slots.SlotHoverManager}
     * - hovereando ahi (que es exactamente donde esta el slot de armadura vainilla real de las
     * piernas) igual disparaba {@code activeGroup = legs} y dibujaba el panel {@code more_slots.png}
     * alrededor de un grupo sin nada adentro.
     * <p>
     * Ahora se exige ademas que AL MENOS UN tipo del grupo tenga items posibles
     * ({@link SlotsApi#hasReadyItems}). Si ninguno lo tiene, el grupo no recibe posicion
     * ({@code madnesscore$getGroupPos} devuelve null) y {@code madnesscore$getGroupRect} cae en
     * su rama de {@code Rect2i} vacio (0,0,0,0) - {@code SlotHoverManager} nunca lo puede activar.
     * <p>
     * En ese caso (grupo COMPLETO sin ningun tipo listo, ej. "legs" hoy) el {@code pos == null}
     * mas abajo hace que sus {@link PlayerDynamicSlot} tampoco se creen en ESTA apertura del menu
     * - no representa perdida de datos: el {@code SlotInventory} (la storage real, persistida en
     * el {@link SlotComponent} de la entidad) no depende de que existan objetos {@code Slot} en un
     * menu en particular. Si mas adelante el datapack le agrega un tag a "legs/belt", el proximo
     * {@code madnesscore$updateSlots} (dispara con cada resync/reload) vuelve a crear esos slots
     * con lo que sea que ya tuvieran guardado. Para un grupo MIXTO (ej. "head": "hat" listo +
     * "face" sin tag), en cambio, {@code hasSlots} sigue dando true (alcanza con que UN tipo este
     * listo) y ahi si se siguen creando los {@link PlayerDynamicSlot} de TODOS sus tipos, listos o
     * no - "face" sigue existiendo como slot, solo que inactivo/invisible.
     */
    @Unique
    private boolean madnesscore$hasSlots(SlotComponent component, SlotGroup group) {
        Map<String, SlotInventory> groupInv = component.getInventory().get(group.getName());
        if (groupInv == null) {
            return false;
        }
        for (SlotInventory inv : groupInv.values()) {
            if (inv.getContainerSize() > 0 && SlotsApi.hasReadyItems(inv.getSlotType())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int madnesscore$getGroupNum(SlotGroup group) {
        return madnesscore$groupNums.getOrDefault(group, 0);
    }

    @Nullable
    @Override
    public Point madnesscore$getGroupPos(SlotGroup group) {
        return madnesscore$groupPos.get(group);
    }

    @NotNull
    @Override
    public List<Point> madnesscore$getSlotHeights(SlotGroup group) {
        return madnesscore$slotHeights.getOrDefault(group, ImmutableList.of());
    }

    @Nullable
    @Override
    public Point madnesscore$getSlotHeight(SlotGroup group, int i) {
        List<Point> points = this.madnesscore$getSlotHeights(group);
        return i < points.size() ? points.get(i) : null;
    }

    @NotNull
    @Override
    public List<SlotType> madnesscore$getSlotTypes(SlotGroup group) {
        return madnesscore$slotTypes.getOrDefault(group, ImmutableList.of());
    }

    @Override
    public int madnesscore$getSlotWidth(SlotGroup group) {
        return madnesscore$slotWidths.getOrDefault(group, 0);
    }

    @Override
    public int madnesscore$getGroupCount() {
        return madnesscore$groupCount;
    }

    @Override
    public int madnesscore$getSlotRangeStart() {
        return madnesscore$slotRangeStart;
    }

    @Override
    public int madnesscore$getSlotRangeEnd() {
        return madnesscore$slotRangeEnd;
    }

    @Inject(at = @At("HEAD"), method = "quickMoveStack", cancellable = true)
    private void madnesscore$quickMoveStack(Player player, int index, CallbackInfoReturnable<ItemStack> info) {
        Slot slot = this.slots.get(index);

        if (!slot.hasItem()) {
            return;
        }
        ItemStack stack = slot.getItem();

        if (index >= madnesscore$slotRangeStart && index < madnesscore$slotRangeEnd) {
            // De un slot dinámico hacia el inventario normal (igual que sacarse la armadura).
            if (!this.moveItemStackTo(stack, 9, 45, false)) {
                info.setReturnValue(ItemStack.EMPTY);
            } else {
                info.setReturnValue(stack);
            }
        } else if (index >= 9 && index < 45) {
            // Del inventario/hotbar hacia el primer slot dinámico compatible.
            SlotsApi.getSlotComponent(player).ifPresent(component -> {
                for (int i = madnesscore$slotRangeStart; i < madnesscore$slotRangeEnd; i++) {
                    Slot s = this.slots.get(i);
                    if (!(s instanceof PlayerDynamicSlot ds) || !s.mayPlace(stack)) {
                        continue;
                    }
                    SlotType type = ds.madnesscore$getType();
                    SlotReference ref = ds.madnesscore$getReference();
                    boolean allowed = SlotsApi.evaluatePredicateSet(type.getQuickMovePredicates(), stack, ref, player);
                    if (allowed && this.moveItemStackTo(stack, i, i + 1, false)) {
                        break;
                    }
                }
            });
        }
    }
}