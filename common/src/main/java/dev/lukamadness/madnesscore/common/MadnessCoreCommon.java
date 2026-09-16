package dev.lukamadness.madnesscore.common;

import dev.lukamadness.madnesscore.common.api.appearance.compat.mca.McaAppearanceCompat;
import dev.lukamadness.madnesscore.common.event.bloodline.BloodlineSpeciesLink;
import dev.lukamadness.madnesscore.common.event.family.FamilyRequirementLink;
import dev.lukamadness.madnesscore.common.impact.material.ImpactMaterials;
import dev.lukamadness.madnesscore.common.registry.block.ModBlocks;
import dev.lukamadness.madnesscore.common.registry.blockentity.ModBlockEntities;
import dev.lukamadness.madnesscore.common.registry.component.ModDataComponents;
import dev.lukamadness.madnesscore.common.registry.creativetab.ModCreativeTabs;
import dev.lukamadness.madnesscore.common.registry.entity.ModEntities;
import dev.lukamadness.madnesscore.common.registry.fluid.ModFluids;
import dev.lukamadness.madnesscore.common.registry.gamerule.ModGameRules;
import dev.lukamadness.madnesscore.common.registry.item.ModArmorMaterials;
import dev.lukamadness.madnesscore.common.registry.item.ModItems;
import dev.lukamadness.madnesscore.common.registry.item.compat.ModDyeDepotItems;
import dev.lukamadness.madnesscore.common.registry.menu.ModMenus;
import dev.lukamadness.madnesscore.common.registry.recipe.ModRecipes;
import dev.lukamadness.madnesscore.common.registry.sound.ModSounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MadnessCoreCommon {
    public static final String MOD_ID = "madnesscore";
    public static final String MOD_NAME = "Madness Core";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    public static void init() {
        ModFluids.init();
        ModDataComponents.init();
        ModBlocks.init();

        ModArmorMaterials.init();
        ModItems.init();
        ModBlockEntities.init();
        ModEntities.init();
        ModMenus.init();
        ModSounds.init();
        ModCreativeTabs.init();
        ModRecipes.init();
        ModGameRules.init();
        ImpactMaterials.init();
        BloodlineSpeciesLink.init();
        FamilyRequirementLink.init();

        McaAppearanceCompat.init();
    }
}