package dev.lukamadness.madnesscore.common.client.api.config.option;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public enum OptionImpact implements NameProvider {
    LOW(ChatFormatting.GREEN, "madnesscore.config.option_impact.low"),

    MEDIUM(ChatFormatting.YELLOW, "madnesscore.config.option_impact.medium"),

    HIGH(ChatFormatting.GOLD, "madnesscore.config.option_impact.high"),

    VARIES(ChatFormatting.WHITE, "madnesscore.config.option_impact.varies");

    private final Component text;

    OptionImpact(ChatFormatting formatting, String text) {
        this.text = Component.translatable(text)
                .withStyle(formatting);
    }

    @Override
    public Component getName() {
        return this.text;
    }
}
