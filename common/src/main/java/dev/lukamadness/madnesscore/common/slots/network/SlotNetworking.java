package dev.lukamadness.madnesscore.common.slots.network;

import dev.lukamadness.madnesscore.common.api.slots.*;
import dev.lukamadness.madnesscore.common.platform.Services;
import dev.lukamadness.madnesscore.common.api.slots.SlotsApi;
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

public final class SlotNetworking {
    private SlotNetworking() {
    }

    public static void syncToTrackers(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel)) {
            return;
        }
        SyncSlotComponentPayload payload = buildPayload(entity);
        if (payload != null) {
            Services.SLOT_NETWORK.sendToTrackingAndSelf(entity, payload);
        }
    }

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
        component.getInventory().forEach((groupName, types) -> types.forEach((typeName, inv) -> {
            for (int i = 0; i < inv.getContainerSize(); i++) {
                SlotType.class.getSimpleName();
            }
        }));
        CompoundTag tag = new CompoundTag();
        component.writeToNbt(tag, entity.level().registryAccess());
        return new SyncSlotComponentPayload(entity.getId(), tag);
    }

    public static void handleSyncOnClient(SyncSlotComponentPayload payload, Level clientLevel) {
        if (clientLevel == null) {
            return;
        }
        Entity entity = clientLevel.getEntity(payload.entityId());
        if (entity instanceof LivingEntity livingEntity) {
            SlotsApi.getSlotComponent(livingEntity).ifPresentOrElse(component -> {
                component.readFromNbt(payload.data(), clientLevel.registryAccess());

                if (entity instanceof Player player
                        && player.inventoryMenu instanceof PlayerSlotMenu menu) {
                    menu.madnesscore$updateSlots(false);
                }
            }, () -> dev.lukamadness.madnesscore.common.MadnessCoreCommon.LOG.info(
                    "[slots-debug] handleSyncOnClient WITHOUT COMPONENT in client for entityId={}",
                    payload.entityId()));
        }
    }

    public static void sendBreak(LivingEntity entity, SlotReference ref) {
        if (!(entity.level() instanceof ServerLevel)) {
            return;
        }
        SlotType slotType = ref.inventory().getSlotType();
        SlotBreakPayload payload = new SlotBreakPayload(entity.getId(), slotType.getGroup(), slotType.getName(), ref.index());
        Services.SLOT_NETWORK.sendToTrackingAndSelf(entity, payload);
    }

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
