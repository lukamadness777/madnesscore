package dev.lukamadness.madnesscore.common.content.technology.blocks;

import dev.lukamadness.madnesscore.common.registry.blockentity.ModBlockEntities;
import dev.lukamadness.madnesscore.common.content.technology.heat.HeatEnvironment;
import dev.lukamadness.madnesscore.common.content.technology.heat.HeatReceiver;
import dev.lukamadness.madnesscore.common.content.technology.heat.ModHeatStorage;
import dev.lukamadness.madnesscore.common.content.technology.fuel.FuelValues;
import dev.lukamadness.madnesscore.common.content.technology.fuel.HeatFuel;
import dev.lukamadness.madnesscore.common.content.technology.screen.HeatGeneratorScreenHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
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


/**
 * Recreación de CoalGeneratorBlockEntity. Quema combustible (carbón y derivados) igual
 * que antes, pero en vez de generar Energy genera HEAT: un recurso propio, separado del
 * de Energy, que solo pueden recibir los bloques que implementen HeatReceiver
 * (NO EnergyReceiver — son recursos distintos y no se mezclan).
 * <p>
 * El Energy Converter es el puente: implementa HeatReceiver para tomar este Heat, y por
 * dentro lo convierte en Energy para bloques como el futuro Compressor.
 * <p>
 * NOTA: la apertura de pantalla con datos extra (BlockPos) no puede usar
 * ExtendedScreenHandlerFactory acá porque esa interfaz es de Fabric API, no vanilla.
 * Este BE implementa el {@link MenuProvider} vanilla; cada plataforma debe envolverlo
 * (Fabric: su propio ExtendedScreenHandlerFactory llamando a getScreenOpeningData();
 * NeoForge: IContainerFactory leyendo el BlockPos del buffer) al registrar el menú.
 */
public class HeatGeneratorBlockEntity extends BlockEntity implements Container, MenuProvider, HeatReceiver {

    private NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);

    public static final int HEAT_CAPACITY = 10_000;

    /** Cuánto Heat puede empujar por tick a un vecino — suficiente para cubrir Lava (4/tick). */
    private static final int MAX_HEAT_TRANSFER = 8;

    /** Pérdida ambiental base (ritmo Plains) antes de aplicar el multiplicador de bioma. */
    private static final double BASE_AMOUNT_LOSS_PER_TICK = 0.2; // 4 Heat/seg a ritmo Plains

    /** Qué tan rápido (°C/tick) conduce su temperatura a un vecino mientras le empuja Heat. */
    private static final double TEMPERATURE_CONDUCTION_RATE = 5.0;

    private final ModHeatStorage heatStorage = new ModHeatStorage(HEAT_CAPACITY, 0, MAX_HEAT_TRANSFER, this::setChanged);

    private int burnTime;
    private int burnTimeTotal;

    /** Qué combustible está quemando ahora mismo (o el último que quemó), null si nunca quemó nada. */
    @Nullable
    private HeatFuel currentFuel;

    /** Acumula la pérdida ambiental fraccionaria de amount entre ticks (amount es int). */
    private double amountLossAccumulator;

    private final ContainerData containerData = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> heatStorage.getHeat();
                case 1 -> heatStorage.getCapacity();
                case 2 -> burnTime;
                case 3 -> burnTimeTotal;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> heatStorage.setHeat(value);
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
            HeatFuel fuel = entity.currentFuel != null ? entity.currentFuel : HeatFuel.COAL;
            entity.heatStorage.generate(fuel.getHeatPerTick());
            entity.heatStorage.heatUp(fuel.getMaxTemperature(), fuel.getTemperatureRisePerTick());
            dirty = true;
        } else {
            ItemStack fuel = entity.items.get(0);
            if (!fuel.isEmpty() && entity.heatStorage.getHeat() < entity.heatStorage.getCapacity()) {
                if (fuel.is(ItemTags.COALS)) {
                    // Carbón y derivados: genera Heat lentamente, sube la temperatura de a poco.
                    int fuelTime = FuelValues.INSTANCE.get(fuel.getItem());
                    if (fuelTime > 0) {
                        fuel.shrink(1);
                        entity.burnTime = fuelTime;
                        entity.burnTimeTotal = fuelTime;
                        entity.currentFuel = HeatFuel.COAL;
                        dirty = true;
                    }
                } else if (fuel.is(Items.LAVA_BUCKET) && fuel.getCount() == 1) {
                    // Lava: genera mucho Heat y sube la temperatura rápido. Solo acepta baldes
                    // sueltos (count == 1) para poder devolver el balde vacío al mismo slot sin
                    // necesitar un slot de salida separado.
                    entity.items.set(0, new ItemStack(Items.BUCKET));
                    entity.burnTime = HeatFuel.LAVA_BURN_TIME;
                    entity.burnTimeTotal = HeatFuel.LAVA_BURN_TIME;
                    entity.currentFuel = HeatFuel.LAVA;
                    dirty = true;
                }
            }
        }

        // Pérdida ambiental: SOLO cuando el generador se quedó sin combustible (burnTime
        // llegó a 0 y no había otro combustible para arrancar de nuevo). Mientras está
        // quemando algo, el Heat y la temperatura se mantienen constantes (suben hasta el
        // máximo del combustible actual y ahí se quedan) — igual que un horno vanilla no
        // "gasta" su progreso mientras tiene combustible. Cuánto se drena cuando SÍ se
        // apaga depende del bioma: Taiga pierde más, Plains es la base, Desierto pierde
        // menos (ver HeatEnvironment).
        if (!entity.isBurning()) {
            double lossMultiplier = HeatEnvironment.lossMultiplier(level, pos);
            entity.amountLossAccumulator += BASE_AMOUNT_LOSS_PER_TICK * lossMultiplier;
            int amountLoss = (int) entity.amountLossAccumulator;
            if (amountLoss > 0) {
                entity.amountLossAccumulator -= amountLoss;
                entity.heatStorage.drain(amountLoss);
                dirty = true;
            }
            entity.heatStorage.coolTowards(ModHeatStorage.AMBIENT_TEMPERATURE, HeatEnvironment.BASE_TEMPERATURE_LOSS_PER_TICK * lossMultiplier);
        } else {
            // Sigue quemando (o acaba de consumir el siguiente combustible este mismo
            // tick): resetea el acumulador para que no arrastre pérdida pendiente de la
            // última vez que se apagó.
            entity.amountLossAccumulator = 0;
        }

        // Empuja Heat SOLO a los vecinos que implementen HeatReceiver (no a EnergyReceiver,
        // que es un recurso distinto). El Energy Converter es el puente: implementa
        // HeatReceiver acá y por dentro empuja Energy a mesas como el futuro Compressor.
        // Mesas que consumen Heat directo (ej. Alloy Smeltery a 800°C) también implementan
        // HeatReceiver y reciben acá mismo.
        if (entity.heatStorage.getHeat() > 0) {
            for (Direction direction : Direction.values()) {
                if (entity.heatStorage.getHeat() <= 0) break;
                BlockEntity neighbor = level.getBlockEntity(pos.relative(direction));
                if (neighbor instanceof HeatReceiver receiver) {
                    ModHeatStorage neighborStorage = receiver.getHeatStorage();
                    int toSend = Math.min(entity.heatStorage.getHeat(), entity.heatStorage.getMaxExtract());
                    int accepted = neighborStorage.insert(toSend, true);
                    if (accepted > 0) {
                        neighborStorage.insert(accepted, false);
                        // Solo se drena a sí mismo si el receptor "compite" por el recurso
                        // (drainsSource() == true, el default). El Energy Converter pide
                        // drainsSource() == false: llena su propia barra sin vaciar al
                        // generador — este se queda con su Heat intacto.
                        if (receiver.drainsSource()) {
                            entity.heatStorage.extract(accepted, false);
                        }
                        // El vecino conduce hacia la temperatura del generador mientras recibe
                        // Heat de él — así "el sistema" (generador + mesa) converge a una sola
                        // temperatura en vez de quedarse cada bloque a 20°C ambiente para siempre.
                        if (neighborStorage.getTemperature() < entity.heatStorage.getTemperature()) {
                            neighborStorage.heatUp(entity.heatStorage.getTemperature(), TEMPERATURE_CONDUCTION_RATE);
                        }
                        dirty = true;
                    }
                }
            }
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
        if (currentFuel != null) tag.putString("Fuel", currentFuel.name());
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        items = NonNullList.withSize(1, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, items, registries);
        heatStorage.readNbt(tag, "Heat");
        burnTime = tag.getInt("BurnTime");
        burnTimeTotal = tag.getInt("BurnTimeTotal");
        currentFuel = tag.contains("Fuel") ? HeatFuel.valueOf(tag.getString("Fuel")) : null;
    }

    // Llamado por el wrapper de cada plataforma para saber qué BlockPos sincronizar
    // al abrir la pantalla (ver nota de clase arriba).
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

    // --- Container ---
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