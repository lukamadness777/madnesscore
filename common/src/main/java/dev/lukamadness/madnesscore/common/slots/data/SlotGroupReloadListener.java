package dev.lukamadness.madnesscore.common.slots.data;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.api.slots.DropRule;
import dev.lukamadness.madnesscore.common.api.slots.SlotType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SlotGroupReloadListener extends SimplePreparableReloadListener<Map<String, SlotGroupReloadListener.GroupData>> {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "slots");
    private static final String DATA_TYPE = "slots";
    private static final Gson GSON = new GsonBuilder().disableHtmlEscaping().create();
    private static final int FILE_SUFFIX_LENGTH = ".json".length();

    private Map<String, GroupData> groups = new HashMap<>();

    @Override
    protected Map<String, GroupData> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        Map<String, GroupData> map = new HashMap<>();

        Map<ResourceLocation, Resource> topResources = resourceManager.listResources(DATA_TYPE, id -> id.getPath().endsWith(".json"));

        for (ResourceLocation identifier : topResources.keySet()) {
            List<Resource> stack;
            try {
                stack = resourceManager.getResourceStack(identifier);
            } catch (Exception e) {
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

                    String path = identifier.getPath();
                    String[] parsed = path.substring(DATA_TYPE.length() + 1, path.length() - FILE_SUFFIX_LENGTH).split("/");
                    String groupName = parsed[0];
                    String fileName = parsed[parsed.length - 1];
                    GroupData group = map.computeIfAbsent(groupName, k -> new GroupData());

                    try {
                        if (fileName.equals("group")) {
                            group.read(jsonObject);
                        } else {
                            SlotData slot = group.slots.computeIfAbsent(fileName, k -> new SlotData());
                            slot.read(jsonObject);
                        }
                    } catch (JsonSyntaxException ignored) {
                    }
                }
            } catch (IOException ignored) {
            }
        }
        return map;
    }

    @Override
    protected void apply(Map<String, GroupData> loaded, ResourceManager resourceManager, ProfilerFiller profiler) {
        this.groups = loaded;
    }

    public Map<String, GroupData> getGroups() {
        return ImmutableMap.copyOf(this.groups);
    }

    public static class GroupData {
        private int slotId = -1;
        private int order = 0;
        final Map<String, SlotData> slots = new HashMap<>();

        void read(JsonObject json) {
            slotId = GsonHelper.getAsInt(json, "slot_id", slotId);
            order = GsonHelper.getAsInt(json, "order", order);
        }

        public int getSlotId() {
            return slotId;
        }

        public int getOrder() {
            return order;
        }

        public SlotData getSlot(String name) {
            return slots.get(name);
        }
    }

    public static class SlotData {
        private static final Set<ResourceLocation> DEFAULT_QUICK_MOVE_PREDICATES =
                ImmutableSet.of(ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "all"));
        private static final Set<ResourceLocation> DEFAULT_VALIDATOR_PREDICATES =
                ImmutableSet.of(ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "tag"));
        private static final Set<ResourceLocation> DEFAULT_TOOLTIP_PREDICATES =
                ImmutableSet.of(ResourceLocation.fromNamespaceAndPath(MadnessCoreCommon.MOD_ID, "all"));

        private int order = 0;
        private int amount = -1;
        private String icon = "";
        private final Set<String> quickMovePredicates = new HashSet<>();
        private final Set<String> validatorPredicates = new HashSet<>();
        private final Set<String> tooltipPredicates = new HashSet<>();
        private String dropRule = DropRule.DEFAULT.name();
        private boolean mirrorsVanillaEquipment = false;

        void read(JsonObject json) {
            boolean replace = GsonHelper.getAsBoolean(json, "replace", false);

            order = GsonHelper.getAsInt(json, "order", order);

            int jsonAmount = GsonHelper.getAsInt(json, "amount", amount);
            amount = replace ? jsonAmount : Math.max(jsonAmount, amount);

            icon = GsonHelper.getAsString(json, "icon", icon);

            readPredicateArray(json, "quick_move_predicates", quickMovePredicates, replace);
            readPredicateArray(json, "validator_predicates", validatorPredicates, replace);
            readPredicateArray(json, "tooltip_predicates", tooltipPredicates, replace);

            String jsonDropRule = GsonHelper.getAsString(json, "drop_rule", dropRule).toUpperCase();
            if (DropRule.has(jsonDropRule)) {
                dropRule = jsonDropRule;
            }

            mirrorsVanillaEquipment = GsonHelper.getAsBoolean(json, "mirror_vanilla_equipment", mirrorsVanillaEquipment);
        }

        private void readPredicateArray(JsonObject json, String key, Set<String> target, boolean replace) {
            JsonArray array = GsonHelper.getAsJsonArray(json, key, new JsonArray());
            if (array.size() > 0 && replace) {
                target.clear();
            }
            for (JsonElement element : array) {
                target.add(element.getAsString());
            }
        }

        public SlotType create(String group, String name) {
            ResourceLocation rawIcon = ResourceLocation.parse(icon.isEmpty() ? MadnessCoreCommon.MOD_ID + ":missing" : icon);
            ResourceLocation finalIcon = ResourceLocation.fromNamespaceAndPath(rawIcon.getNamespace(), "textures/" + rawIcon.getPath() + ".png");

            Set<ResourceLocation> finalQuickMove = quickMovePredicates.stream().map(ResourceLocation::parse).collect(Collectors.toSet());
            Set<ResourceLocation> finalValidator = validatorPredicates.stream().map(ResourceLocation::parse).collect(Collectors.toSet());
            Set<ResourceLocation> finalTooltip = tooltipPredicates.stream().map(ResourceLocation::parse).collect(Collectors.toSet());

            if (finalQuickMove.isEmpty()) {
                finalQuickMove = DEFAULT_QUICK_MOVE_PREDICATES;
            }
            if (finalValidator.isEmpty()) {
                finalValidator = DEFAULT_VALIDATOR_PREDICATES;
            }
            if (finalTooltip.isEmpty()) {
                finalTooltip = DEFAULT_TOOLTIP_PREDICATES;
            }

            int finalAmount = amount == -1 ? 1 : amount;

            return new SlotType(group, name, order, finalAmount, finalIcon, finalQuickMove, finalValidator, finalTooltip,
                    DropRule.valueOf(dropRule), mirrorsVanillaEquipment);
        }
    }
}
