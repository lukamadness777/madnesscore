package dev.lukamadness.madnesscore.common.datagen;

import dev.lukamadness.madnesscore.common.MadnessCoreCommon;
import dev.lukamadness.madnesscore.common.registry.helper.RegistryHelper;
import dev.lukamadness.madnesscore.common.registry.item.ModItems;
import dev.lukamadness.madnesscore.common.registry.item.compat.ModDyeDepotItems;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.model.ModelLocationUtils;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TextureSlot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class ModItemModelProvider implements DataProvider {
    private final PackOutput.PathProvider modelPathProvider;

    public ModItemModelProvider(PackOutput output) {
        this.modelPathProvider = output.createPathProvider(PackOutput.Target.RESOURCE_PACK, "models");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        List<CompletableFuture<?>> futures = new ArrayList<>();

        for (Entry entry : itemsNeedingModel()) {
            Item item = entry.item().get();
            ResourceLocation modelLocation = ModelLocationUtils.getModelLocation(item);
            ResourceLocation textureLocation = RegistryHelper.id("item/" + entry.texturePath());
            Map<TextureSlot, ResourceLocation> textures = Map.of(TextureSlot.LAYER0, textureLocation);

            futures.add(
                    DataProvider.saveStable(
                            cachedOutput,
                            ModelTemplates.FLAT_ITEM.createBaseTemplate(modelLocation, textures),
                            this.modelPathProvider.json(modelLocation)
                    )
            );
        }

        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    private record Entry(Supplier<Item> item, String texturePath) {
    }

    private static List<Entry> itemsNeedingModel() {
        List<Entry> items = new ArrayList<>();

        items.add(new Entry(ModItems.WHITE_FABRIC, "fabric/white_fabric"));
        items.add(new Entry(ModItems.ORANGE_FABRIC, "fabric/orange_fabric"));
        items.add(new Entry(ModItems.MAGENTA_FABRIC, "fabric/magenta_fabric"));
        items.add(new Entry(ModItems.LIGHT_BLUE_FABRIC, "fabric/light_blue_fabric"));
        items.add(new Entry(ModItems.YELLOW_FABRIC, "fabric/yellow_fabric"));
        items.add(new Entry(ModItems.LIME_FABRIC, "fabric/lime_fabric"));
        items.add(new Entry(ModItems.PINK_FABRIC, "fabric/pink_fabric"));
        items.add(new Entry(ModItems.GRAY_FABRIC, "fabric/gray_fabric"));
        items.add(new Entry(ModItems.LIGHT_GRAY_FABRIC, "fabric/light_gray_fabric"));
        items.add(new Entry(ModItems.CYAN_FABRIC, "fabric/cyan_fabric"));
        items.add(new Entry(ModItems.PURPLE_FABRIC, "fabric/purple_fabric"));
        items.add(new Entry(ModItems.BLUE_FABRIC, "fabric/blue_fabric"));
        items.add(new Entry(ModItems.BROWN_FABRIC, "fabric/brown_fabric"));
        items.add(new Entry(ModItems.GREEN_FABRIC, "fabric/green_fabric"));
        items.add(new Entry(ModItems.RED_FABRIC, "fabric/red_fabric"));
        items.add(new Entry(ModItems.BLACK_FABRIC, "fabric/black_fabric"));

        items.add(new Entry(ModDyeDepotItems.MAROON_FABRIC, "fabric/dye_depot/maroon_fabric"));
        items.add(new Entry(ModDyeDepotItems.ROSE_FABRIC, "fabric/dye_depot/rose_fabric"));
        items.add(new Entry(ModDyeDepotItems.CORAL_FABRIC, "fabric/dye_depot/coral_fabric"));
        items.add(new Entry(ModDyeDepotItems.GINGER_FABRIC, "fabric/dye_depot/ginger_fabric"));
        items.add(new Entry(ModDyeDepotItems.TAN_FABRIC, "fabric/dye_depot/tan_fabric"));
        items.add(new Entry(ModDyeDepotItems.BEIGE_FABRIC, "fabric/dye_depot/beige_fabric"));
        items.add(new Entry(ModDyeDepotItems.AMBER_FABRIC, "fabric/dye_depot/amber_fabric"));
        items.add(new Entry(ModDyeDepotItems.OLIVE_FABRIC, "fabric/dye_depot/olive_fabric"));
        items.add(new Entry(ModDyeDepotItems.FOREST_FABRIC, "fabric/dye_depot/forest_fabric"));
        items.add(new Entry(ModDyeDepotItems.VERDANT_FABRIC, "fabric/dye_depot/verdant_fabric"));
        items.add(new Entry(ModDyeDepotItems.TEAL_FABRIC, "fabric/dye_depot/teal_fabric"));
        items.add(new Entry(ModDyeDepotItems.MINT_FABRIC, "fabric/dye_depot/mint_fabric"));
        items.add(new Entry(ModDyeDepotItems.AQUA_FABRIC, "fabric/dye_depot/aqua_fabric"));
        items.add(new Entry(ModDyeDepotItems.SLATE_FABRIC, "fabric/dye_depot/slate_fabric"));
        items.add(new Entry(ModDyeDepotItems.NAVY_FABRIC, "fabric/dye_depot/navy_fabric"));
        items.add(new Entry(ModDyeDepotItems.INDIGO_FABRIC, "fabric/dye_depot/indigo_fabric"));

        items.add(new Entry(ModItems.GOLD_PLATE, "material/plate/gold_plate"));

        items.add(new Entry(ModItems.BRONZE_INGOT, "material/bronze_ingot"));
        items.add(new Entry(ModItems.BRONZE_NUGGET, "material/nugget/bronze_nugget"));
        items.add(new Entry(ModItems.BRONZE_PLATE, "material/plate/bronze_plate"));

        items.add(new Entry(ModItems.IRON_PLATE, "material/plate/iron_plate"));

        items.add(new Entry(ModItems.STEEL_INGOT, "material/steel_ingot"));
        items.add(new Entry(ModItems.STEEL_NUGGET, "material/nugget/steel_nugget"));
        items.add(new Entry(ModItems.STEEL_PLATE, "material/plate/steel_plate"));

        items.add(new Entry(ModItems.COPPER_PLATE, "material/plate/copper_plate"));

        return items;
    }

    @Override
    public String getName() {
        return "Item Models (" + MadnessCoreCommon.MOD_ID + ")";
    }
}
