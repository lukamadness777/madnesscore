package dev.lukamadness.madnesscore.common.slots.data;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.api.slots.SlotGroup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reload listener (datapack, carpeta "data/&lt;namespace&gt;/slot_assignments/*.json") que asigna
 * grupos/slots definidos por {@link SlotGroupReloadListener} a tipos de entidad concretos, o a
 * tags de tipo de entidad (prefijo "#").
 * <p>
 * Formato del JSON:
 * <pre>
 * {
 *   "replace": false,
 *   "entities": ["minecraft:player", "#minecraft:raiders"],
 *   "slots": ["hand/ring", "chest/necklace"]
 * }
 * </pre>
 * Portado de dev.emi.trinkets.data.EntitySlotLoader.
 */
public class EntitySlotReloadListener extends SimplePreparableReloadListener<Map<String, Map<String, Set<String>>>> {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "slot_assignments");
    private static final String DATA_TYPE = "slot_assignments";
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();

    private final SlotGroupReloadListener groupLoader;
    private final Map<EntityType<?>, Map<String, SlotGroup>> entitySlots = new HashMap<>();

    public EntitySlotReloadListener(SlotGroupReloadListener groupLoader) {
        this.groupLoader = groupLoader;
    }

    @Override
    protected Map<String, Map<String, Set<String>>> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        // entityId (o "#tagId") -> groupName -> nombres de slot asignados
        Map<String, Map<String, Set<String>>> map = new HashMap<>();

        Map<ResourceLocation, Resource> topResources = resourceManager.listResources(DATA_TYPE, id -> id.getPath().endsWith(".json"));

        for (ResourceLocation identifier : topResources.keySet()) {
            List<Resource> stack;
            try {
                stack = resourceManager.getResourceStack(identifier);
            } catch (Exception e) {
                MadnessCoreCommon.LOG.error("[madnesscore] No se pudo obtener el stack de recursos para {}", identifier, e);
                continue;
            }

            try {
                for (Resource resource : stack) {
                    JsonObject jsonObject;
                    try (InputStreamReader reader = new InputStreamReader(resource.open())) {
                        jsonObject = GsonHelper.fromJson(GSON, reader, JsonObject.class);
                    }

                    if (jsonObject == null) {
                        continue;
                    }

                    try {
                        boolean replace = GsonHelper.getAsBoolean(jsonObject, "replace", false);
                        JsonArray assignedSlots = GsonHelper.getAsJsonArray(jsonObject, "slots", new JsonArray());
                        Map<String, Set<String>> groups = new HashMap<>();

                        for (JsonElement assignedSlot : assignedSlots) {
                            String slot = assignedSlot.getAsString();
                            String[] parsedSlot = slot.split("/");

                            if (parsedSlot.length != 2) {
                                MadnessCoreCommon.LOG.error("[madnesscore] Asignacion de slot malformada '{}', debe tener el formato 'grupo/slot'", slot);
                                continue;
                            }
                            groups.computeIfAbsent(parsedSlot[0], k -> new HashSet<>()).add(parsedSlot[1]);
                        }

                        JsonArray entities = GsonHelper.getAsJsonArray(jsonObject, "entities", new JsonArray());

                        if (!groups.isEmpty()) {
                            for (JsonElement entityElement : entities) {
                                String name = entityElement.getAsString();
                                String key = name.startsWith("#")
                                        ? "#" + ResourceLocation.parse(name.substring(1))
                                        : ResourceLocation.parse(name).toString();

                                Map<String, Set<String>> slots = map.computeIfAbsent(key, k -> new HashMap<>());
                                if (replace) {
                                    slots.clear();
                                }
                                groups.forEach((groupName, slotNames) -> slots.computeIfAbsent(groupName, k -> new HashSet<>()).addAll(slotNames));
                            }
                        }
                    } catch (JsonSyntaxException e) {
                        MadnessCoreCommon.LOG.error("[madnesscore] Error de sintaxis leyendo {}", identifier.getPath(), e);
                    }
                }
            } catch (IOException e) {
                MadnessCoreCommon.LOG.error("[madnesscore] Error de IO leyendo asignaciones de slots para {}", identifier, e);
            }
        }
        return map;
    }

    @Override
    protected void apply(Map<String, Map<String, Set<String>>> loaded, ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<String, SlotGroupReloadListener.GroupData> groupData = groupLoader.getGroups();
        Map<EntityType<?>, Map<String, SlotGroup.Builder>> groupBuilders = new HashMap<>();

        loaded.forEach((entityKey, groups) -> {
            Set<EntityType<?>> types = resolveEntityTypes(entityKey);

            for (EntityType<?> type : types) {
                Map<String, SlotGroup.Builder> builders = groupBuilders.computeIfAbsent(type, k -> new HashMap<>());
                groups.forEach((groupName, slotNames) -> {
                    SlotGroupReloadListener.GroupData group = groupData.get(groupName);

                    if (group == null) {
                        MadnessCoreCommon.LOG.error("[madnesscore] Se intento asignar slots del grupo desconocido '{}'", groupName);
                        return;
                    }

                    SlotGroup.Builder builder = builders.computeIfAbsent(groupName,
                            k -> new SlotGroup.Builder(groupName, group.getSlotId(), group.getOrder()));

                    slotNames.forEach(slotName -> {
                        SlotGroupReloadListener.SlotData slotData = group.getSlot(slotName);
                        if (slotData != null) {
                            builder.addSlot(slotName, slotData.create(groupName, slotName));
                        } else {
                            MadnessCoreCommon.LOG.error("[madnesscore] Se intento asignar el slot desconocido '{}/{}'", groupName, slotName);
                        }
                    });
                });
            }
        });

        this.entitySlots.clear();
        groupBuilders.forEach((entityType, groups) -> {
            Map<String, SlotGroup> resolved = this.entitySlots.computeIfAbsent(entityType, k -> new HashMap<>());
            groups.forEach((groupName, builder) -> resolved.putIfAbsent(groupName, builder.build()));
        });
    }

    private Set<EntityType<?>> resolveEntityTypes(String entityKey) {
        Set<EntityType<?>> types = new HashSet<>();
        try {
            if (entityKey.startsWith("#")) {
                ResourceLocation tagId = ResourceLocation.parse(entityKey.substring(1));
                TagKey<EntityType<?>> tag = TagKey.create(Registries.ENTITY_TYPE, tagId);
                BuiltInRegistries.ENTITY_TYPE.getTag(tag).ifPresentOrElse(
                        holderSet -> holderSet.forEach(holder -> types.add(holder.value())),
                        () -> MadnessCoreCommon.LOG.error("[madnesscore] Tag de entidad desconocido '{}'", entityKey)
                );
            } else {
                ResourceLocation id = ResourceLocation.parse(entityKey);
                BuiltInRegistries.ENTITY_TYPE.getOptional(id).ifPresentOrElse(
                        types::add,
                        () -> MadnessCoreCommon.LOG.error("[madnesscore] Entidad desconocida '{}'", entityKey)
                );
            }
        } catch (Exception e) {
            MadnessCoreCommon.LOG.error("[madnesscore] Se intento asignar una entrada de entidad invalida '{}'", entityKey, e);
        }
        return types;
    }

    public Map<String, SlotGroup> getEntitySlots(EntityType<?> entityType) {
        Map<String, SlotGroup> found = this.entitySlots.get(entityType);
        return found != null ? ImmutableMap.copyOf(found) : ImmutableMap.of();
    }

    public Map<EntityType<?>, Map<String, SlotGroup>> getAllEntitySlots() {
        return ImmutableMap.copyOf(this.entitySlots);
    }

    /**
     * Usado por el sync de red (Fase 4) para reemplazar por completo los datos, por ejemplo al
     * recibir el paquete del servidor en el cliente.
     */
    public void setEntitySlots(Map<EntityType<?>, Map<String, SlotGroup>> slots) {
        this.entitySlots.clear();
        this.entitySlots.putAll(slots);
    }

    public Collection<ResourceLocation> getDependencies() {
        return Lists.newArrayList(SlotGroupReloadListener.ID);
    }
}