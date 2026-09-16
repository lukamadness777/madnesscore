package dev.lukamadness.madnesscore.common.impact.material;

import net.minecraft.world.entity.EntityType;

/**
 * This is where "what color/material does entity X expel on impact" gets registered.
 * <p>
 * {@link ImpactMaterialRegistry#resolve(net.minecraft.world.entity.Entity)} already falls back to
 * {@link ImpactMaterials#BLOOD} (dark red) for any {@link net.minecraft.world.entity.LivingEntity}
 * with no explicit binding, so ordinary flesh-and-blood mobs don't need an entry here at all - dark
 * red is simply what they get by default. See the big comment block at the bottom of {@link #init()}
 * for the full, explicit list of vanilla mobs that intentionally have NO binding here.
 * <p>
 * Everything that should look different only needs one line via
 * {@link ImpactMaterialRegistry#bindEntityType(EntityType, ImpactMaterial)}. Other mods can do the
 * exact same thing for their own entities/materials - this class only wires up MadnessCore's own
 * built-in materials and is not a required extension point.
 */
final class ImpactMobBindings {
    private ImpactMobBindings() {
    }

    static void init() {

        // --- Undead: verde/marrón oscuro (ImpactMaterials.UNDEAD_BLOOD ya alterna entre los dos
        // tonos por partícula). Zoglin y Zombified Piglin SON undead; Hoglin y Piglin NO lo son y
        // se dejan caer al BLOOD rojo default (ver bloque de "sin binding" más abajo). ---
        ImpactMaterialRegistry.bindEntityType(EntityType.ZOMBIE, ImpactMaterials.UNDEAD_BLOOD);
        ImpactMaterialRegistry.bindEntityType(EntityType.ZOMBIE_VILLAGER, ImpactMaterials.UNDEAD_BLOOD);
        ImpactMaterialRegistry.bindEntityType(EntityType.HUSK, ImpactMaterials.UNDEAD_BLOOD);
        ImpactMaterialRegistry.bindEntityType(EntityType.DROWNED, ImpactMaterials.UNDEAD_BLOOD);
        ImpactMaterialRegistry.bindEntityType(EntityType.ZOMBIFIED_PIGLIN, ImpactMaterials.UNDEAD_BLOOD);
        ImpactMaterialRegistry.bindEntityType(EntityType.ZOGLIN, ImpactMaterials.UNDEAD_BLOOD);
        ImpactMaterialRegistry.bindEntityType(EntityType.ZOMBIE_HORSE, ImpactMaterials.UNDEAD_BLOOD);

        // --- Hueso blanco/gris genérico. Bogged y Silverfish no estaban en la clasificación
        // original pedida por el usuario; se decidió mantenerlos acá (Bogged ya estaba antes,
        // Silverfish encaja por analogía de "bicho/exoesqueleto"). ---
        ImpactMaterialRegistry.bindEntityType(EntityType.SKELETON, ImpactMaterials.BONE);
        ImpactMaterialRegistry.bindEntityType(EntityType.STRAY, ImpactMaterials.BONE);
        ImpactMaterialRegistry.bindEntityType(EntityType.SKELETON_HORSE, ImpactMaterials.BONE);
        ImpactMaterialRegistry.bindEntityType(EntityType.BOGGED, ImpactMaterials.BONE);
        ImpactMaterialRegistry.bindEntityType(EntityType.SILVERFISH, ImpactMaterials.BONE);

        // Wither Skeleton: hueso NEGRO, no el blanco genérico.
        ImpactMaterialRegistry.bindEntityType(EntityType.WITHER_SKELETON, ImpactMaterials.WITHER_BONE);

        // --- Sangre de arácnido (un solo material, alterna dos tonos de morado). ---
        ImpactMaterialRegistry.bindEntityType(EntityType.SPIDER, ImpactMaterials.ARACHNID_BLOOD);
        ImpactMaterialRegistry.bindEntityType(EntityType.CAVE_SPIDER, ImpactMaterials.ARACHNID_BLOOD);

        // Phantom: sangre roja pero desaturada, gris-azulada.
        ImpactMaterialRegistry.bindEntityType(EntityType.PHANTOM, ImpactMaterials.PHANTOM_BLOOD);

        // --- Fluido violeta del End. Enderman/Endermite/Shulker comparten material; el Ender
        // Dragon tiene el suyo propio (ENERGY) para diferenciarse del resto. ---
        ImpactMaterialRegistry.bindEntityType(EntityType.ENDERMAN, ImpactMaterials.END_FLUID);
        ImpactMaterialRegistry.bindEntityType(EntityType.ENDERMITE, ImpactMaterials.END_FLUID);
        ImpactMaterialRegistry.bindEntityType(EntityType.SHULKER, ImpactMaterials.END_FLUID);
        ImpactMaterialRegistry.bindEntityType(EntityType.ENDER_DRAGON, ImpactMaterials.ENDER_DRAGON_ENERGY);

        // Warden: fluido azul-verdoso oscuro.
        ImpactMaterialRegistry.bindEntityType(EntityType.WARDEN, ImpactMaterials.WARDEN_FLUID);

        // Guardian / Elder Guardian: un solo material, alterna dos tonos de teal.
        ImpactMaterialRegistry.bindEntityType(EntityType.GUARDIAN, ImpactMaterials.GUARDIAN_FLUID);
        ImpactMaterialRegistry.bindEntityType(EntityType.ELDER_GUARDIAN, ImpactMaterials.GUARDIAN_FLUID);

        // --- Fluidos temáticos 1:1 por mob. ---
        ImpactMaterialRegistry.bindEntityType(EntityType.SLIME, ImpactMaterials.SLIME_FLUID);
        ImpactMaterialRegistry.bindEntityType(EntityType.MAGMA_CUBE, ImpactMaterials.MAGMA_FLUID);
        ImpactMaterialRegistry.bindEntityType(EntityType.CREEPER, ImpactMaterials.CREEPER_FLUID);
        ImpactMaterialRegistry.bindEntityType(EntityType.GHAST, ImpactMaterials.GHAST_FLUID);

        // --- Energía: mobs "mágicos"/elementales y los dos jefes que no son ni sangre ni fluido
        // biológico. Piglin Brute NO está acá a propósito - ver bloque de "sin binding" abajo. ---
        ImpactMaterialRegistry.bindEntityType(EntityType.BLAZE, ImpactMaterials.BLAZE_ENERGY);
        ImpactMaterialRegistry.bindEntityType(EntityType.BREEZE, ImpactMaterials.BREEZE_ENERGY);
        ImpactMaterialRegistry.bindEntityType(EntityType.VEX, ImpactMaterials.VEX_ENERGY);
        ImpactMaterialRegistry.bindEntityType(EntityType.WITHER, ImpactMaterials.WITHER_ENERGY);

        // --- Golems: no biológicos, fragmentos del material del que están hechos (no sangre). ---
        ImpactMaterialRegistry.bindEntityType(EntityType.IRON_GOLEM, ImpactMaterials.METAL_FRAGMENTS);
        ImpactMaterialRegistry.bindEntityType(EntityType.SNOW_GOLEM, ImpactMaterials.SNOW_FRAGMENTS);

        // ------------------------------------------------------------------------------------
        // SIN BINDING A PROPÓSITO - caen al fallback de ImpactMaterialRegistry#resolve(), que da
        // ImpactMaterials.BLOOD (rojo) a cualquier LivingEntity sin binding explícito. Se listan
        // acá para dejar constancia de que no quedaron sin clasificar por accidente:
        //
        // Animales/pasivos comunes: Allay, Armadillo, Axolotl, Bat, Camel, Cat, Chicken, Cod, Cow,
        // Donkey, Fox, Frog, Glow Squid, Horse, Mooshroom, Mule, Ocelot, Parrot, Pig, Pufferfish,
        // Rabbit, Salmon, Sheep, Sniffer, Squid, Strider, Tadpole, Tropical Fish, Turtle, Villager,
        // Wandering Trader.
        //
        // Neutrales comunes: Bee, Dolphin, Goat, Llama, Trader Llama, Panda, Polar Bear, Wolf.
        //
        // Piglin y Hoglin: sangre roja normal (NO son undead - a diferencia de Zombified Piglin
        // y Zoglin, que sí están bindeados arriba a UNDEAD_BLOOD). Piglin Brute los acompaña acá
        // por la misma razón, aunque no estaba en la lista original del usuario.
        //
        // Villagers/Illagers y Ravager: Villager, Wandering Trader, Pillager, Vindicator, Evoker,
        // Witch, Ravager - todos sangre roja normal, ninguno necesita binding propio.
        // ------------------------------------------------------------------------------------
    }
}