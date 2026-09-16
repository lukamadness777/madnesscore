package dev.lukamadness.madnesscore.common.client.slots.render;

import net.minecraft.world.item.Items;

public final class VanillaArmorSlotRendererInit {
    private VanillaArmorSlotRendererInit() {
    }

    public static void register() {
        VanillaArmorSlotRenderer renderer = new VanillaArmorSlotRenderer();

        SlotRendererRegistry.registerRenderer(Items.LEATHER_HELMET, renderer);
        SlotRendererRegistry.registerRenderer(Items.CHAINMAIL_HELMET, renderer);
        SlotRendererRegistry.registerRenderer(Items.IRON_HELMET, renderer);
        SlotRendererRegistry.registerRenderer(Items.GOLDEN_HELMET, renderer);
        SlotRendererRegistry.registerRenderer(Items.DIAMOND_HELMET, renderer);
        SlotRendererRegistry.registerRenderer(Items.NETHERITE_HELMET, renderer);
        SlotRendererRegistry.registerRenderer(Items.TURTLE_HELMET, renderer);

        SlotRendererRegistry.registerRenderer(Items.LEATHER_CHESTPLATE, renderer);
        SlotRendererRegistry.registerRenderer(Items.CHAINMAIL_CHESTPLATE, renderer);
        SlotRendererRegistry.registerRenderer(Items.IRON_CHESTPLATE, renderer);
        SlotRendererRegistry.registerRenderer(Items.GOLDEN_CHESTPLATE, renderer);
        SlotRendererRegistry.registerRenderer(Items.DIAMOND_CHESTPLATE, renderer);
        SlotRendererRegistry.registerRenderer(Items.NETHERITE_CHESTPLATE, renderer);

        SlotRendererRegistry.registerRenderer(Items.LEATHER_LEGGINGS, renderer);
        SlotRendererRegistry.registerRenderer(Items.CHAINMAIL_LEGGINGS, renderer);
        SlotRendererRegistry.registerRenderer(Items.IRON_LEGGINGS, renderer);
        SlotRendererRegistry.registerRenderer(Items.GOLDEN_LEGGINGS, renderer);
        SlotRendererRegistry.registerRenderer(Items.DIAMOND_LEGGINGS, renderer);
        SlotRendererRegistry.registerRenderer(Items.NETHERITE_LEGGINGS, renderer);

        SlotRendererRegistry.registerRenderer(Items.LEATHER_BOOTS, renderer);
        SlotRendererRegistry.registerRenderer(Items.CHAINMAIL_BOOTS, renderer);
        SlotRendererRegistry.registerRenderer(Items.IRON_BOOTS, renderer);
        SlotRendererRegistry.registerRenderer(Items.GOLDEN_BOOTS, renderer);
        SlotRendererRegistry.registerRenderer(Items.DIAMOND_BOOTS, renderer);
        SlotRendererRegistry.registerRenderer(Items.NETHERITE_BOOTS, renderer);
    }
}
