package dev.lukamadness.madnesscore.common.mixin;

import com.google.common.collect.ImmutableList;
import dev.lukamadness.madnesscore.common.mixin.accessor.ContainerMenuAccessor;
import dev.lukamadness.madnesscore.common.api.slots.SlotsApi;
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

        if (!isOnServer && !SlotsApi.getClientEntityLoader().getAllEntitySlots().isEmpty()) {
            madnesscore$updateSlots(true);
        }
    }

    @Override
    public void madnesscore$updateSlots(boolean slotsChanged) {
        SlotsApi.getSlotComponent(this.madnesscore$owner).ifPresent(component -> {
            if (slotsChanged) {
                component.update();
            }
            Map<String, SlotGroup> groups = component.getGroups();
            madnesscore$groupPos.clear();

            List<ItemStack> lastSlots = ((ContainerMenuAccessor) (Object) this).madnesscore$getLastSlots();
            while (madnesscore$slotRangeStart < madnesscore$slotRangeEnd) {
                this.slots.remove(madnesscore$slotRangeStart);
                if (lastSlots.size() > madnesscore$slotRangeStart) {
                    lastSlots.remove(madnesscore$slotRangeStart);
                }
                madnesscore$slotRangeEnd--;
            }

            int groupNum = 1;

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
            if (!this.moveItemStackTo(stack, 9, 45, false)) {
                info.setReturnValue(ItemStack.EMPTY);
            } else {
                info.setReturnValue(stack);
            }
        } else if (index >= 9 && index < 45) {
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
