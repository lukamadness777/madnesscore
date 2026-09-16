package dev.lukamadness.madnesscore.fabric.command;

import dev.lukamadness.madnesscore.common.command.MadnessCoreCommands;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public final class MadnessCoreFabricCommands {
    private MadnessCoreFabricCommands() {}

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                MadnessCoreCommands.register(dispatcher));
    }
}
