package dev.lukamadness.madnesscore.common.slots.network;

import dev.lukamadness.madnesscore.common.api.slots.*;
import dev.lukamadness.madnesscore.common.platform.Services;
import dev.lukamadness.madnesscore.common.slots.SlotsApi;
import dev.lukamadness.madnesscore.common.api.slots.SlotType;
import dev.lukamadness.madnesscore.common.slots.gui.PlayerSlotMenu;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Logica de red compartida (Fase 4) entre Fabric y NeoForge. Los payloads viven en este mismo
 * paquete y son vanilla puro; esta clase arma/aplica su contenido y queda 100% en common. Cada
 * loader solo aporta el registro del canal y el envio real (ver {@code ISlotNetwork}).
 */
public final class SlotNetworking {

    private SlotNetworking() {
    }

    /**
     * Construye y envia el estado completo de los slots de una entidad a todos los que la estan
     * trackeando (y a si misma si es un {@link ServerPlayer}). Se llama desde
     * {@link dev.lukamadness.madnesscore.common.slots.SlotTicker} cuando detecta un cambio de
     * equipo en un tick de servidor.
     */
    public static void syncToTrackers(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel)) {
            return;
        }
        SyncSlotComponentPayload payload = buildPayload(entity);
        if (payload != null) {
            Services.SLOT_NETWORK.sendToTrackingAndSelf(entity, payload);
        }
    }

    /**
     * Envia el estado completo de los slots de {@code entity} a un unico observador (ej: cuando
     * un jugador empieza a trackearla, o al loguearse para verse a si mismo/a otros ya visibles).
     */
    public static void sendFullSyncTo(LivingEntity entity, ServerPlayer viewer) {
        SyncSlotComponentPayload payload = buildPayload(entity);
        if (payload != null) {
            Services.SLOT_NETWORK.sendToPlayer(viewer, payload);
        }
    }

    private static SyncSlotComponentPayload buildPayload(LivingEntity entity) {
        SlotComponent component = SlotsApi.getSlotComponent(entity).orElse(null);
        if (component == null) {
            return null;
        }
        CompoundTag tag = new CompoundTag();
        component.writeToNbt(tag, entity.level().registryAccess());
        return new SyncSlotComponentPayload(entity.getId(), tag);
    }

    /**
     * Aplica en el cliente un paquete de sincronizacion recibido: busca la entidad por id en el
     * nivel actual y vuelca el NBT recibido en su {@code SlotComponent}. Debe llamarse ya en el
     * hilo principal del cliente (encolar con {@code execute}/{@code enqueueWork} segun el
     * loader antes de invocar esto).
     */
    public static void handleSyncOnClient(SyncSlotComponentPayload payload, Level clientLevel) {
        if (clientLevel == null) {
            return;
        }
        Entity entity = clientLevel.getEntity(payload.entityId());
        if (entity instanceof LivingEntity livingEntity) {
            SlotsApi.getSlotComponent(livingEntity).ifPresent(component -> {
                component.readFromNbt(payload.data(), clientLevel.registryAccess());

                // Sin esto, el InventoryMenu del cliente nunca se entera de que cambiaron los
                // grupos/slots: el UNICO rebuild que corre solo (sin este llamado) es el que
                // dispara SlotNetworking#handleDefinitionsSyncOnClient cuando llegan las
                // DEFINICIONES; este sync de aca es el de EQUIPO (que items tiene puestos la
                // entidad), que puede cambiar en cualquier momento sin que cambien las
                // definiciones. Sin este llamado madnesscore$groupPos queda desactualizado y
                // SlotHoverManager nunca encuentra nada bajo el mouse. Portado de
                // TrinketsClient#onInitializeClient (screenHandler.trinkets$updateTrinketSlots).
                //
                // OJO: el parametro va en FALSE, no true. El NBT recien aplicado por readFromNbt
                // (arriba) ya es la fuente de verdad para tamanos/contenido; pasar true haria que
                // madnesscore$updateSlots llame a component.update() de nuevo, que reconstruye el
                // inventario en base a los datos LOCALES de SlotsApi (tamanos por defecto, sin los
                // modificadores de atributo que trajo el sync) y pisaria lo que se acaba de leer.
                // Asi es exactamente como lo hace Trinkets (ver PlayerScreenHandlerMixin, llamado
                // con slotsChanged=false desde TrinketsClient tras aplicar el sync).
                if (entity instanceof Player player
                        && player.inventoryMenu instanceof PlayerSlotMenu menu) {
                    menu.madnesscore$updateSlots(false);
                }
            });
        }
    }

    // ---------------------------------------------------------------------------------------
    // Rotura de item equipado (Slottable#onBreak) - ver SlotBreakPayload
    // ---------------------------------------------------------------------------------------

    /**
     * Notifica a todos los que trackean la entidad (y a ella misma si es un jugador) que el item
     * equipado en {@code ref} acaba de romperse, para que reproduzcan el efecto en cliente.
     * Portado de {@code TrinketsApi#onTrinketBroken}. Llamar SOLO del lado servidor (ej. desde el
     * callback que le pasa un mod-item propio a {@code ItemStack#hurtAndBreak}, o desde cualquier
     * otro punto que detecte que un stack equipado llego a 0 de durabilidad).
     */
    public static void sendBreak(LivingEntity entity, SlotReference ref) {
        if (!(entity.level() instanceof ServerLevel)) {
            return;
        }
        SlotType slotType = ref.inventory().getSlotType();
        SlotBreakPayload payload = new SlotBreakPayload(entity.getId(), slotType.getGroup(), slotType.getName(), ref.index());
        Services.SLOT_NETWORK.sendToTrackingAndSelf(entity, payload);
    }

    /**
     * Aplica en el cliente un paquete de rotura recibido: busca la entidad, el slot referenciado
     * y el stack que tiene puesto ahora mismo, y le delega el efecto (sonido/particulas) al
     * {@link Slottable#onBreak} registrado para ese
     * item. Debe llamarse ya en el hilo principal del cliente.
     */
    public static void handleBreakOnClient(SlotBreakPayload payload, Level clientLevel) {
        if (clientLevel == null) {
            return;
        }
        Entity entity = clientLevel.getEntity(payload.entityId());
        if (!(entity instanceof LivingEntity livingEntity)) {
            return;
        }
        SlotsApi.getSlotComponent(livingEntity).ifPresent(component -> {
            Map<String, SlotInventory> group = component.getInventory().get(payload.group());
            if (group == null) {
                return;
            }
            SlotInventory inventory = group.get(payload.slot());
            if (inventory == null || payload.index() < 0 || payload.index() >= inventory.getContainerSize()) {
                return;
            }
            ItemStack stack = inventory.getItem(payload.index());
            if (stack.isEmpty()) {
                return;
            }
            SlotReference ref = new SlotReference(inventory, payload.index());
            SlotsApi.getSlottable(stack.getItem()).onBreak(stack, ref, livingEntity);
        });
    }

    // ---------------------------------------------------------------------------------------
    // Definiciones de slots (grupos/SlotType por tipo de entidad) - ver SyncSlotDefinitionsPayload
    // ---------------------------------------------------------------------------------------

    /**
     * Arma el paquete con TODAS las definiciones de slots resueltas actualmente en el servidor
     * ({@code SlotsApi.getServerEntityLoader()}). Se llama al loguearse un jugador y en cada
     * {@code /reload} (ver hooks en cada loader), nunca por-tick, asi que no hace falta filtrar
     * por lo que el jugador puede llegar a ver.
     */
    public static SyncSlotDefinitionsPayload buildDefinitionsPayload() {
        Map<EntityType<?>, Map<String, SlotGroup>> all = SlotsApi.getServerEntityLoader().getAllEntitySlots();
        CompoundTag entitiesTag = new CompoundTag();
        all.forEach((entityType, groups) -> {
            ResourceLocation entityId = BuiltInRegistries.ENTITY_TYPE.getKey(entityType);
            CompoundTag groupsTag = new CompoundTag();
            groups.forEach((groupName, group) -> groupsTag.put(groupName, writeGroup(group)));
            entitiesTag.put(entityId.toString(), groupsTag);
        });
        CompoundTag root = new CompoundTag();
        root.put("Entities", entitiesTag);
        return new SyncSlotDefinitionsPayload(root);
    }

    /**
     * Aplica en el cliente un paquete de definiciones recibido, reemplazando por completo el
     * contenido de {@code SlotsApi.getClientEntityLoader()}. Debe llamarse en el hilo principal
     * del cliente.
     * <p>
     * {@code localPlayer} es el jugador local (si ya existe en este momento), pasado por cada
     * loader desde su receiver S2C. Hace falta para forzar un rebuild del {@code InventoryMenu}
     * de ESE jugador ahora mismo: ver el bloque de abajo para el porque.
     */
    public static void handleDefinitionsSyncOnClient(SyncSlotDefinitionsPayload payload, @Nullable Player localPlayer) {
        Map<EntityType<?>, Map<String, SlotGroup>> parsed = new HashMap<>();
        CompoundTag entitiesTag = payload.data().getCompound("Entities");
        for (String entityKey : entitiesTag.getAllKeys()) {
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.getOptional(ResourceLocation.parse(entityKey)).orElse(null);
            if (entityType == null) {
                continue;
            }
            CompoundTag groupsTag = entitiesTag.getCompound(entityKey);
            Map<String, SlotGroup> groups = new HashMap<>();
            for (String groupName : groupsTag.getAllKeys()) {
                groups.put(groupName, readGroup(groupName, groupsTag.getCompound(groupName)));
            }
            parsed.put(entityType, groups);
        }
        SlotsApi.getClientEntityLoader().setEntitySlots(parsed);

        // Sin esto, el InventoryMenu local del jugador -ya construido, en el ctor de
        // InventoryMenu, ANTES de que estas definiciones llegaran por red- se queda para
        // siempre con cero slots dinamicos: MixinInventoryMenu#madnesscore$init corre en el
        // cliente con SlotsApi.getClientEntityLoader() todavia vacio, asi que
        // component.getGroups() no encuentra nada. El servidor, en cambio, ya tenia sus datos
        // cargados desde el arranque, asi que su copia de InventoryMenu SI tiene los slots
        // dinamicos desde el vamos (server vs cliente = distinta cantidad de slots para el MISMO
        // containerId 0). El proximo ClientboundContainerSetContentPacket que le llegue al
        // cliente (el inicial de login, o el reenvio explicito que hace cada loader justo
        // despues de este mismo paquete, ver SlotEventListener#onDatapackSync /
        // MadnessCore#registerSlotDataSync) va a traer mas indices de los que this.slots tiene
        // ahora mismo -> IndexOutOfBoundsException en AbstractContainerMenu#getSlot ->
        // "Network Protocol Error" -> el cliente se desconecta ("Too many suspicious packets").
        // Portado del mismo problema que Trinkets evita al no compartir InventoryMenu (ver
        // TrinketPlayerScreenHandler): al agregar los slots directamente sobre el menu de
        // toda la vida, hace falta este rebuild explicito apenas se sabe la forma real.
        if (localPlayer != null && localPlayer.inventoryMenu instanceof PlayerSlotMenu menu) {
            menu.madnesscore$updateSlots(true);
        }
    }

    private static CompoundTag writeGroup(SlotGroup group) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("SlotId", group.getSlotId());
        tag.putInt("Order", group.getOrder());
        CompoundTag slotsTag = new CompoundTag();
        group.getSlots().forEach((slotName, slotType) -> slotsTag.put(slotName, writeSlotType(slotType)));
        tag.put("Slots", slotsTag);
        return tag;
    }

    private static CompoundTag writeSlotType(SlotType type) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Order", type.getOrder());
        tag.putInt("Amount", type.getAmount());
        tag.putString("Icon", type.getIcon().toString());
        tag.put("QuickMove", writeIdSet(type.getQuickMovePredicates()));
        tag.put("Validator", writeIdSet(type.getValidatorPredicates()));
        tag.put("Tooltip", writeIdSet(type.getTooltipPredicates()));
        tag.putString("DropRule", type.getDropRule().name());
        return tag;
    }

    private static ListTag writeIdSet(Set<ResourceLocation> ids) {
        ListTag list = new ListTag();
        for (ResourceLocation id : ids) {
            list.add(StringTag.valueOf(id.toString()));
        }
        return list;
    }

    private static SlotGroup readGroup(String groupName, CompoundTag tag) {
        SlotGroup.Builder builder = new SlotGroup.Builder(groupName, tag.getInt("SlotId"), tag.getInt("Order"));
        CompoundTag slotsTag = tag.getCompound("Slots");
        for (String slotName : slotsTag.getAllKeys()) {
            builder.addSlot(slotName, readSlotType(groupName, slotName, slotsTag.getCompound(slotName)));
        }
        return builder.build();
    }

    private static SlotType readSlotType(String group, String name, CompoundTag tag) {
        int order = tag.getInt("Order");
        int amount = tag.getInt("Amount");
        ResourceLocation icon = ResourceLocation.parse(tag.getString("Icon"));
        Set<ResourceLocation> quickMove = readIdSet(tag.getList("QuickMove", Tag.TAG_STRING));
        Set<ResourceLocation> validator = readIdSet(tag.getList("Validator", Tag.TAG_STRING));
        Set<ResourceLocation> tooltip = readIdSet(tag.getList("Tooltip", Tag.TAG_STRING));
        DropRule dropRule = DropRule.valueOf(tag.getString("DropRule"));
        return new SlotType(group, name, order, amount, icon, quickMove, validator, tooltip, dropRule);
    }

    private static Set<ResourceLocation> readIdSet(ListTag list) {
        Set<ResourceLocation> set = new LinkedHashSet<>();
        for (Tag t : list) {
            set.add(ResourceLocation.parse(t.getAsString()));
        }
        return set;
    }
}