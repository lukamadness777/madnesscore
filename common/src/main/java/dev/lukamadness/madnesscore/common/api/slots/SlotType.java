package dev.lukamadness.madnesscore.common.api.slots;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Set;

/**
 * Definicion inmutable de un tipo de slot dentro de un {@link SlotGroup} (ej: "hand/ring").
 * Portado de dev.emi.trinkets.api.SlotType, adaptado a Madness Core (multiloader, sin dependencias
 * de Fabric API en el codigo common).
 */
public final class SlotType {

    private final String group;
    private final String name;
    private final int order;
    private final int amount;
    private final ResourceLocation icon;
    private final Set<ResourceLocation> quickMovePredicates;
    private final Set<ResourceLocation> validatorPredicates;
    private final Set<ResourceLocation> tooltipPredicates;
    private final DropRule dropRule;
    private final boolean mirrorsVanillaEquipment;

    public SlotType(String group, String name, int order, int amount, ResourceLocation icon,
                    Set<ResourceLocation> quickMovePredicates, Set<ResourceLocation> validatorPredicates,
                    Set<ResourceLocation> tooltipPredicates, DropRule dropRule) {
        this(group, name, order, amount, icon, quickMovePredicates, validatorPredicates, tooltipPredicates,
                dropRule, false);
    }

    /**
     * @param mirrorsVanillaEquipment si {@code true}, mientras haya equipada aca una pieza de
     * armadura real (compatible con el {@link net.minecraft.world.entity.EquipmentSlot} al que
     * este slot esta anclado via {@code group.json#slot_id}) y el {@code EquipmentSlot} vainilla
     * real de la entidad este vacio, vainilla la "ve" ahi para efectos de stats y comportamiento
     * (respiracion de agua del casco de tortuga, encantamientos de armadura como Proteccion,
     * Espinas, Paso Helado, etc.) - ver {@code VanillaEquipmentMirror} y
     * {@code MixinLivingEntityEquipmentMirror#getItemBySlot}.
     * <p>
     * Es un espejo de SOLO LECTURA: nunca se copia ni se mueve el stack a ningun lado, sigue
     * viviendo unicamente en este {@code SlotInventory}. Si el slot vainilla real ya tiene algo
     * puesto, ese algo siempre gana y no se compara ni se pisa.
     * <p>
     * FIX (bug de duplicacion en "hat"): por defecto es {@code false}. Antes existio una version
     * de este mecanismo que copiaba el stack via {@code entity.setItemSlot(...)}
     * ({@code SlotTicker#mirrorToVanillaEquipment}, eliminado) - eso hacia que el item quedara
     * existiendo a la vez como dos referencias independientes (una en el inventario de trinkets,
     * otra en el array de equipo real), duplicandolo. Ademas, disparar el espejado para
     * CUALQUIER slot type de un grupo anclado a un slot_id de armadura (ej: "head/hat" junto con
     * "head/face", que comparten grupo) tambien estaba mal: "face" es un slot cosmetico/accesorio
     * que NO deberia tocar el slot de armadura real. Por eso hace falta optar explicitamente por
     * este comportamiento poniendo {@code "mirror_vanilla_equipment": true} en el JSON del slot
     * type que si deba ser funcional (ej: "head/hat", "chest/back", "feet/shoes").
     */
    public SlotType(String group, String name, int order, int amount, ResourceLocation icon,
                    Set<ResourceLocation> quickMovePredicates, Set<ResourceLocation> validatorPredicates,
                    Set<ResourceLocation> tooltipPredicates, DropRule dropRule, boolean mirrorsVanillaEquipment) {
        this.group = group;
        this.name = name;
        this.order = order;
        this.amount = amount;
        this.icon = icon;
        this.quickMovePredicates = quickMovePredicates;
        this.validatorPredicates = validatorPredicates;
        this.tooltipPredicates = tooltipPredicates;
        this.dropRule = dropRule;
        this.mirrorsVanillaEquipment = mirrorsVanillaEquipment;
    }

    public String getGroup() {
        return group;
    }

    public String getName() {
        return name;
    }

    public int getOrder() {
        return order;
    }

    public int getAmount() {
        return amount;
    }

    public ResourceLocation getIcon() {
        return icon;
    }

    public Set<ResourceLocation> getQuickMovePredicates() {
        return quickMovePredicates;
    }

    public Set<ResourceLocation> getValidatorPredicates() {
        return validatorPredicates;
    }

    public Set<ResourceLocation> getTooltipPredicates() {
        return tooltipPredicates;
    }

    public DropRule getDropRule() {
        return dropRule;
    }

    public boolean mirrorsVanillaEquipment() {
        return mirrorsVanillaEquipment;
    }

    /**
     * @return el id compuesto "group/name" que identifica este slot de forma unica.
     */
    public String getId() {
        return this.group + "/" + this.name;
    }

    public MutableComponent getTranslation() {
        return Component.translatable("madnesscore.slot." + this.group + "." + this.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SlotType slotType = (SlotType) o;
        return group.equals(slotType.group) && name.equals(slotType.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, name);
    }

    @Override
    public String toString() {
        return "SlotType[" + getId() + "]";
    }
}