package dev.lukamadness.madnesscore.common.client.api.config.structure;

import net.minecraft.network.chat.Component;

public interface OptionGroupBuilder {
    OptionGroupBuilder setName(Component name);

    OptionGroupBuilder addOption(OptionBuilder option);
}
