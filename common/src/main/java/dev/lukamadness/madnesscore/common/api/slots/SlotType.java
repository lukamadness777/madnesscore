package dev.lukamadness.madnesscore.common.api.slots;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.Objects;
import java.util.Set;

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
