package dev.lukamadness.madnesscore.common.content.technology.blocks;

import dev.lukamadness.madnesscore.common.registry.blockentity.ModBlockEntities;
import dev.lukamadness.madnesscore.common.content.technology.heat.HeatEnvironment;
import dev.lukamadness.madnesscore.common.content.technology.heat.HeatFuelRegistry;
import dev.lukamadness.madnesscore.common.content.technology.heat.HeatReceiver;
import dev.lukamadness.madnesscore.common.content.technology.heat.ModHeatStorage;
import dev.lukamadness.madnesscore.common.content.technology.screen.HeatGeneratorScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class HeatGeneratorBlockEntity extends BlockEntity implements Container, MenuProvider, HeatReceiver {
    private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);

    private static final double SELF_HEATING_RATE = 0.015;

    private final ModHeatStorage heatStorage = new ModHeatStorage(this::setChanged);

    private int burnTime;
    private int burnTimeTotal;

    private double currentFuelTemperature;

    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> (int) Math.round(heatStorage.getTemperature());
                case 1 -> (int) Math.round(HeatFuelRegistry.getMaxHeatTemperature(level));
                case 2 -> burnTime;
                case 3 -> burnTimeTotal;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> heatStorage.setTemperature(value);
                case 2 -> burnTime = value;
                case 3 -> burnTimeTotal = value;
            }
        }

        @Override
        public int getCount() {
            return 4;
        }
    };

    public HeatGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.HEAT_GENERATOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, HeatGeneratorBlockEntity entity) {
        if (level.isClientSide) return;

        boolean wasBurning = entity.isBurning();
        boolean dirty = false;

        if (entity.burnTime > 0) {
            entity.burnTime--;

            double gainMultiplier = HeatEnvironment.heatGainMultiplier(level, pos);
            if (entity.heatStorage.approachTemperature(entity.currentFuelTemperature, SELF_HEATING_RATE * gainMultiplier)) dirty = true;
        } else {
            ItemStack fuelStack = entity.items.get(0);
            if (!fuelStack.isEmpty()) {
                var match = HeatFuelRegistry.find(level, fuelStack);
                if (match.isPresent()) {
                    var recipe = match.get();

                    if (fuelStack.getItem().hasCraftingRemainingItem() && fuelStack.getCount() == 1) {
                        entity.items.set(0, new ItemStack(fuelStack.getItem().getCraftingRemainingItem()));
                    } else {
                        fuelStack.shrink(1);
                    }
                    entity.burnTime = recipe.getBurnTime();
                    entity.burnTimeTotal = recipe.getBurnTime();
                    entity.currentFuelTemperature = recipe.getTemperature();
                    dirty = true;
                }
            }
        }

        if (!entity.isBurning()) {
            double lossMultiplier = HeatEnvironment.lossMultiplier(level, pos);
            if (entity.heatStorage.approachTemperature(ModHeatStorage.AMBIENT_TEMPERATURE,
                    HeatEnvironment.BASE_TEMPERATURE_LOSS_RATE * lossMultiplier)) dirty = true;
        }

        if (wasBurning != entity.isBurning()) {
            level.setBlock(pos, state.setValue(HeatGeneratorBlock.LIT, entity.isBurning()), 3);
        }
        if (dirty) entity.setChanged();
    }

    public boolean isBurning() {
        return burnTime > 0;
    }

    @Override
    public ModHeatStorage getHeatStorage() {
        return heatStorage;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
        heatStorage.writeNbt(tag, "Heat");
        tag.putInt("BurnTime", burnTime);
        tag.putInt("BurnTimeTotal", burnTimeTotal);
        tag.putDouble("FuelTemperature", currentFuelTemperature);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(1, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        heatStorage.readNbt(tag, "Heat");
        burnTime = tag.getInt("BurnTime");
        burnTimeTotal = tag.getInt("BurnTimeTotal");
        currentFuelTemperature = tag.getDouble("FuelTemperature");
    }

    public BlockPos getScreenOpeningData(ServerPlayer player) {
        return getBlockPos();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory inv, Player player) {
        return new HeatGeneratorScreenHandler(syncId, inv, this, containerData);
    }

    @Override public int getContainerSize() { return items.size(); }
    @Override public boolean isEmpty() { return items.get(0).isEmpty(); }
    @Override public ItemStack getItem(int slot) { return items.get(slot); }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack r = ContainerHelper.removeItem(items, slot, amount);
        if (!r.isEmpty()) setChanged();
        return r;
    }

    @Override public ItemStack removeItemNoUpdate(int slot) { return ContainerHelper.takeItem(items, slot); }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) stack.setCount(getMaxStackSize());
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(getBlockPos()) == this
                && player.distanceToSqr(getBlockPos().getX() + 0.5, getBlockPos().getY() + 0.5, getBlockPos().getZ() + 0.5) <= 64.0;
    }

    @Override public void clearContent() { items.clear(); }
}
