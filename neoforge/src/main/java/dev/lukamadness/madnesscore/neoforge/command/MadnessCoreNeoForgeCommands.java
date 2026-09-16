package dev.lukamadness.madnesscore.neoforge.command;

import dev.lukamadness.madnesscore.common.command.MadnessCoreCommands;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class MadnessCoreNeoForgeCommands {
    private MadnessCoreNeoForgeCommands() {}

    public static void register() {
        NeoForge.EVENT_BUS.addListener(MadnessCoreNeoForgeCommands::onRegisterCommands);
    }

    private static void onRegisterCommands(RegisterCommandsEvent event) {
        MadnessCoreCommands.register(event.getDispatcher());
    }
}
