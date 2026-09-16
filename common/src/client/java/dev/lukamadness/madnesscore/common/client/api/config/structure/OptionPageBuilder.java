package dev.lukamadness.madnesscore.common.client.api.config.structure;

import net.minecraft.network.chat.Component;

public interface OptionPageBuilder extends PageBuilder {
    OptionPageBuilder setName(Component name);

    OptionPageBuilder addOptionGroup(OptionGroupBuilder group);

    OptionPageBuilder addOption(OptionBuilder option);
}
