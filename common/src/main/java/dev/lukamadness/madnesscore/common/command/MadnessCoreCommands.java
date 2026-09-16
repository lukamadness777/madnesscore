package dev.lukamadness.madnesscore.common.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.lukamadness.madnesscore.common.api.bloodline.Bloodline;
import dev.lukamadness.madnesscore.common.api.bloodline.BloodlineApi;
import dev.lukamadness.madnesscore.common.api.bloodline.BloodlineInstance;
import dev.lukamadness.madnesscore.common.api.family.Family;
import dev.lukamadness.madnesscore.common.api.family.FamilyApi;
import dev.lukamadness.madnesscore.common.api.family.FamilyInstance;
import dev.lukamadness.madnesscore.common.api.species.Species;
import dev.lukamadness.madnesscore.common.api.species.SpeciesApi;
import dev.lukamadness.madnesscore.common.data.bloodline.PlayerBloodlineData;
import dev.lukamadness.madnesscore.common.data.family.PlayerFamilyData;
import dev.lukamadness.madnesscore.common.data.species.PlayerSpeciesData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class MadnessCoreCommands {
    private MadnessCoreCommands() {}

    private static final String ARG_PLAYER = "player";
    private static final String ARG_SPECIES = "speciesId";
    private static final String ARG_BLOODLINE = "bloodlineId";
    private static final String ARG_FAMILY = "familyId";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("madnesscore")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("list")
                        .executes(MadnessCoreCommands::listCounts))
                .then(Commands.literal("check")
                        .then(Commands.argument(ARG_PLAYER, EntityArgument.player())
                                .executes(MadnessCoreCommands::showProfile)))
                .then(buildSpeciesNode())
                .then(buildBloodlineNode())
                .then(buildFamilyNode()));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildSpeciesNode() {
        return Commands.literal("species")
                .then(Commands.literal("get")
                        .then(Commands.argument(ARG_PLAYER, EntityArgument.player())
                                .executes(MadnessCoreCommands::showProfile)))
                .then(Commands.literal("set")
                        .then(Commands.argument(ARG_PLAYER, EntityArgument.player())
                                .then(Commands.argument(ARG_SPECIES, ResourceLocationArgument.id())
                                        .suggests(MadnessCoreCommands::suggestAllSpecies)
                                        .executes(MadnessCoreCommands::setSpeciesOnly)
                                        .then(Commands.argument(ARG_BLOODLINE, ResourceLocationArgument.id())
                                                .suggests(MadnessCoreCommands::suggestBloodlinesForSpeciesArg)
                                                .executes(MadnessCoreCommands::setSpeciesWithBloodline)
                                                .then(Commands.argument(ARG_FAMILY, ResourceLocationArgument.id())
                                                        .suggests(MadnessCoreCommands::suggestFamiliesForSpeciesAndBloodlineArgs)
                                                        .executes(MadnessCoreCommands::setSpeciesWithBloodlineAndFamily))))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildBloodlineNode() {
        return Commands.literal("bloodline")
                .then(Commands.literal("get")
                        .then(Commands.argument(ARG_PLAYER, EntityArgument.player())
                                .executes(MadnessCoreCommands::showProfile)))
                .then(Commands.literal("set")
                        .then(Commands.argument(ARG_PLAYER, EntityArgument.player())
                                .then(Commands.argument(ARG_BLOODLINE, ResourceLocationArgument.id())
                                        .suggests(MadnessCoreCommands::suggestBloodlinesForTargetSpecies)
                                        .executes(MadnessCoreCommands::setBloodlineOnly)
                                        .then(Commands.argument(ARG_FAMILY, ResourceLocationArgument.id())
                                                .suggests(MadnessCoreCommands::suggestFamiliesForTargetAndBloodlineArg)
                                                .executes(MadnessCoreCommands::setBloodlineWithFamily)))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildFamilyNode() {
        return Commands.literal("family")
                .then(Commands.literal("get")
                        .then(Commands.argument(ARG_PLAYER, EntityArgument.player())
                                .executes(MadnessCoreCommands::showProfile)))
                .then(Commands.literal("set")
                        .then(Commands.argument(ARG_PLAYER, EntityArgument.player())
                                .then(Commands.argument(ARG_FAMILY, ResourceLocationArgument.id())
                                        .suggests(MadnessCoreCommands::suggestFamiliesForTarget)
                                        .executes(MadnessCoreCommands::setFamilyOnly))));
    }

    private static int listCounts(CommandContext<CommandSourceStack> ctx) {
        CommandSourceStack source = ctx.getSource();
        MinecraftServer server = source.getServer();

        send(source, header(Component.translatable("madnesscore.command.section.species")));
        Map<UUID, ResourceLocation> speciesByPlayer = PlayerSpeciesData.get(server).getAll();
        for (Species species : sortById(SpeciesApi.getAllSpecies().values(), Species::id)) {
            long count = speciesByPlayer.values().stream().filter(id -> id.equals(species.id())).count();
            send(source, listLine(species.displayName(), species.id(), count));
        }

        send(source, header(Component.translatable("madnesscore.command.section.bloodlines")));
        Map<UUID, Map<ResourceLocation, Double>> bloodlinesByPlayer = PlayerBloodlineData.get(server).getAllPlayers();
        for (Bloodline bloodline : sortById(BloodlineApi.getAllBloodlines().values(), Bloodline::id)) {
            long count = bloodlinesByPlayer.values().stream().filter(m -> m.containsKey(bloodline.id())).count();
            send(source, listLine(bloodline.displayName(), bloodline.id(), count));
        }

        send(source, header(Component.translatable("madnesscore.command.section.families")));
        Map<UUID, Map<ResourceLocation, Boolean>> familiesByPlayer = PlayerFamilyData.get(server).getAllPlayers();
        for (Family family : sortById(FamilyApi.getAllFamilies().values(), Family::id)) {
            long count = familiesByPlayer.values().stream().filter(m -> m.containsKey(family.id())).count();
            send(source, listLine(family.displayName(), family.id(), count));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int showProfile(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer target = EntityArgument.getPlayer(ctx, ARG_PLAYER);
        MinecraftServer server = source.getServer();
        UUID uuid = target.getUUID();

        send(source, header(Component.translatable("madnesscore.command.section.profile", target.getGameProfile().getName())));

        Species species = SpeciesApi.getSpecies(server, uuid);
        send(source, Component.translatable("madnesscore.command.profile.species_label").withStyle(ChatFormatting.GRAY)
                .append(species.displayName().copy().withStyle(ChatFormatting.AQUA))
                .append(idSuffix(species.id())));

        List<BloodlineInstance> bloodlines = BloodlineApi.getBloodlines(server, uuid);
        if (bloodlines.isEmpty()) {
            send(source, Component.translatable("madnesscore.command.profile.bloodlines_none").withStyle(ChatFormatting.GRAY));
        } else {
            send(source, Component.translatable("madnesscore.command.profile.bloodlines_header").withStyle(ChatFormatting.GRAY));
            for (BloodlineInstance instance : bloodlines) {
                send(source, Component.translatable("madnesscore.command.profile.entry_prefix")
                        .append(instance.bloodline().displayName().copy().withStyle(ChatFormatting.LIGHT_PURPLE))
                        .append(idSuffix(instance.bloodline().id()))
                        .append(Component.translatable("madnesscore.command.profile.percentage", formatPercentage(instance.percentage()))
                                .withStyle(ChatFormatting.DARK_GRAY)));
            }
        }

        List<FamilyInstance> families = FamilyApi.getFamilies(server, uuid);
        if (families.isEmpty()) {
            send(source, Component.translatable("madnesscore.command.profile.families_none").withStyle(ChatFormatting.GRAY));
        } else {
            send(source, Component.translatable("madnesscore.command.profile.families_header").withStyle(ChatFormatting.GRAY));
            for (FamilyInstance instance : families) {
                send(source, Component.translatable("madnesscore.command.profile.entry_prefix")
                        .append(instance.family().displayName().copy().withStyle(ChatFormatting.GOLD))
                        .append(idSuffix(instance.family().id()))
                        .append(instance.disabled()
                                ? Component.translatable("madnesscore.command.profile.disabled").withStyle(ChatFormatting.RED)
                                : Component.empty()));
            }
        }

        return Command.SINGLE_SUCCESS;
    }

    private static int setSpeciesOnly(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer target = EntityArgument.getPlayer(ctx, ARG_PLAYER);
        ResourceLocation speciesId = ctx.getArgument(ARG_SPECIES, ResourceLocation.class);
        if (speciesInvalid(source, speciesId)) return 0;

        applySpecies(source, target, speciesId);
        return Command.SINGLE_SUCCESS;
    }

    private static int setSpeciesWithBloodline(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer target = EntityArgument.getPlayer(ctx, ARG_PLAYER);
        ResourceLocation speciesId = ctx.getArgument(ARG_SPECIES, ResourceLocation.class);
        ResourceLocation bloodlineId = ctx.getArgument(ARG_BLOODLINE, ResourceLocation.class);
        if (speciesInvalid(source, speciesId)) return 0;
        if (bloodlineInvalid(source, bloodlineId)) return 0;

        applySpecies(source, target, speciesId);
        applyBloodline(source, target, bloodlineId);
        return Command.SINGLE_SUCCESS;
    }

    private static int setSpeciesWithBloodlineAndFamily(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer target = EntityArgument.getPlayer(ctx, ARG_PLAYER);
        ResourceLocation speciesId = ctx.getArgument(ARG_SPECIES, ResourceLocation.class);
        ResourceLocation bloodlineId = ctx.getArgument(ARG_BLOODLINE, ResourceLocation.class);
        ResourceLocation familyId = ctx.getArgument(ARG_FAMILY, ResourceLocation.class);
        if (speciesInvalid(source, speciesId)) return 0;
        if (bloodlineInvalid(source, bloodlineId)) return 0;
        if (familyInvalid(source, familyId)) return 0;

        applySpecies(source, target, speciesId);
        if (!applyBloodline(source, target, bloodlineId)) {
            source.sendFailure(Component.translatable("madnesscore.command.error.family_skipped_bloodline_failed")
                    .withStyle(ChatFormatting.RED));
            return Command.SINGLE_SUCCESS;
        }
        applyFamily(source, target, familyId);
        return Command.SINGLE_SUCCESS;
    }

    private static int setBloodlineOnly(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer target = EntityArgument.getPlayer(ctx, ARG_PLAYER);
        ResourceLocation bloodlineId = ctx.getArgument(ARG_BLOODLINE, ResourceLocation.class);
        if (bloodlineInvalid(source, bloodlineId)) return 0;

        applyBloodline(source, target, bloodlineId);
        return Command.SINGLE_SUCCESS;
    }

    private static int setBloodlineWithFamily(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer target = EntityArgument.getPlayer(ctx, ARG_PLAYER);
        ResourceLocation bloodlineId = ctx.getArgument(ARG_BLOODLINE, ResourceLocation.class);
        ResourceLocation familyId = ctx.getArgument(ARG_FAMILY, ResourceLocation.class);
        if (bloodlineInvalid(source, bloodlineId)) return 0;
        if (familyInvalid(source, familyId)) return 0;

        if (!applyBloodline(source, target, bloodlineId)) {
            source.sendFailure(Component.translatable("madnesscore.command.error.family_skipped_bloodline_failed")
                    .withStyle(ChatFormatting.RED));
            return Command.SINGLE_SUCCESS;
        }
        applyFamily(source, target, familyId);
        return Command.SINGLE_SUCCESS;
    }

    private static int setFamilyOnly(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        CommandSourceStack source = ctx.getSource();
        ServerPlayer target = EntityArgument.getPlayer(ctx, ARG_PLAYER);
        ResourceLocation familyId = ctx.getArgument(ARG_FAMILY, ResourceLocation.class);
        if (familyInvalid(source, familyId)) return 0;

        applyFamily(source, target, familyId);
        return Command.SINGLE_SUCCESS;
    }

    private static void applySpecies(CommandSourceStack source, ServerPlayer target, ResourceLocation speciesId) {
        SpeciesApi.setSpecies(target, speciesId);
        Species species = SpeciesApi.getSpeciesDefinition(speciesId);
        source.sendSuccess(() -> successLine(Component.translatable("madnesscore.command.label.species"), species.displayName(), speciesId, target), true);
    }

    private static boolean applyBloodline(CommandSourceStack source, ServerPlayer target, ResourceLocation bloodlineId) {
        BloodlineApi.resetBloodlines(target);
        boolean ok = BloodlineApi.addBloodline(target, bloodlineId, 100.0);
        if (!ok) {
            source.sendFailure(Component.translatable("madnesscore.command.error.bloodline_incompatible",
                            bloodlineId.toString(), target.getGameProfile().getName())
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        Bloodline bloodline = BloodlineApi.getBloodlineDefinition(bloodlineId).orElseThrow();
        source.sendSuccess(() -> successLine(Component.translatable("madnesscore.command.label.bloodline"), bloodline.displayName(), bloodlineId, target), true);
        return true;
    }

    private static boolean applyFamily(CommandSourceStack source, ServerPlayer target, ResourceLocation familyId) {
        FamilyApi.resetFamilies(target);
        boolean ok = FamilyApi.addFamily(target, familyId);
        if (!ok) {
            source.sendFailure(Component.translatable("madnesscore.command.error.family_incompatible",
                            familyId.toString(), target.getGameProfile().getName())
                    .withStyle(ChatFormatting.RED));
            return false;
        }
        Family family = FamilyApi.getFamilyDefinition(familyId).orElseThrow();
        source.sendSuccess(() -> successLine(Component.translatable("madnesscore.command.label.family"), family.displayName(), familyId, target), true);
        return true;
    }

    private static boolean speciesInvalid(CommandSourceStack source, ResourceLocation id) {
        if (!SpeciesApi.speciesExists(id)) {
            source.sendFailure(Component.translatable("madnesscore.command.error.unknown_species", id.toString()).withStyle(ChatFormatting.RED));
            return true;
        }
        return false;
    }

    private static boolean bloodlineInvalid(CommandSourceStack source, ResourceLocation id) {
        if (!BloodlineApi.bloodlineExists(id)) {
            source.sendFailure(Component.translatable("madnesscore.command.error.unknown_bloodline", id.toString()).withStyle(ChatFormatting.RED));
            return true;
        }
        return false;
    }

    private static boolean familyInvalid(CommandSourceStack source, ResourceLocation id) {
        if (!FamilyApi.familyExists(id)) {
            source.sendFailure(Component.translatable("madnesscore.command.error.unknown_family", id.toString()).withStyle(ChatFormatting.RED));
            return true;
        }
        return false;
    }

    private static CompletableFuture<Suggestions> suggestAllSpecies(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggestResource(SpeciesApi.getAllSpecies().keySet().stream(), builder);
    }

    private static CompletableFuture<Suggestions> suggestBloodlinesForSpeciesArg(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        ResourceLocation speciesId = ctx.getArgument(ARG_SPECIES, ResourceLocation.class);
        Species species = SpeciesApi.getSpeciesDefinition(speciesId);
        return SharedSuggestionProvider.suggestResource(
                BloodlineApi.getAllBloodlines().values().stream()
                        .filter(b -> b.isCompatibleWith(species))
                        .map(Bloodline::id),
                builder);
    }

    private static CompletableFuture<Suggestions> suggestBloodlinesForTargetSpecies(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        ServerPlayer target = tryGetPlayer(ctx);
        if (target == null) return builder.buildFuture();
        Species species = SpeciesApi.getSpecies(target);
        return SharedSuggestionProvider.suggestResource(
                BloodlineApi.getAllBloodlines().values().stream()
                        .filter(b -> b.isCompatibleWith(species))
                        .map(Bloodline::id),
                builder);
    }

    private static CompletableFuture<Suggestions> suggestFamiliesForSpeciesAndBloodlineArgs(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        ResourceLocation speciesId = ctx.getArgument(ARG_SPECIES, ResourceLocation.class);
        ResourceLocation bloodlineId = ctx.getArgument(ARG_BLOODLINE, ResourceLocation.class);
        Species species = SpeciesApi.getSpeciesDefinition(speciesId);
        return SharedSuggestionProvider.suggestResource(
                FamilyApi.getAllFamilies().values().stream()
                        .filter(f -> f.isCompatibleWith(species))
                        .filter(f -> f.requiredBloodline().isEmpty() || f.requiredBloodline().get().equals(bloodlineId))
                        .map(Family::id),
                builder);
    }

    private static CompletableFuture<Suggestions> suggestFamiliesForTargetAndBloodlineArg(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        ServerPlayer target = tryGetPlayer(ctx);
        if (target == null) return builder.buildFuture();
        ResourceLocation bloodlineId = ctx.getArgument(ARG_BLOODLINE, ResourceLocation.class);
        Species species = SpeciesApi.getSpecies(target);
        return SharedSuggestionProvider.suggestResource(
                FamilyApi.getAllFamilies().values().stream()
                        .filter(f -> f.isCompatibleWith(species))
                        .filter(f -> f.requiredBloodline().isEmpty() || f.requiredBloodline().get().equals(bloodlineId))
                        .map(Family::id),
                builder);
    }

    private static CompletableFuture<Suggestions> suggestFamiliesForTarget(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        ServerPlayer target = tryGetPlayer(ctx);
        if (target == null) return builder.buildFuture();
        Species species = SpeciesApi.getSpecies(target);
        return SharedSuggestionProvider.suggestResource(
                FamilyApi.getAllFamilies().values().stream()
                        .filter(f -> f.isCompatibleWith(species))
                        .filter(f -> f.requiredBloodline().isEmpty()
                                || BloodlineApi.isBloodline(target, f.requiredBloodline().get()))
                        .map(Family::id),
                builder);
    }

    private static ServerPlayer tryGetPlayer(CommandContext<CommandSourceStack> ctx) {
        try {
            return EntityArgument.getPlayer(ctx, ARG_PLAYER);
        } catch (CommandSyntaxException e) {
            return null;
        }
    }

    private static void send(CommandSourceStack source, Component message) {
        source.sendSuccess(() -> message, false);
    }

    private static Component header(Component title) {
        return Component.translatable("madnesscore.command.header", title).withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD);
    }

    private static Component listLine(Component displayName, ResourceLocation id, long count) {
        String countKey = count == 1 ? "madnesscore.command.count.singular" : "madnesscore.command.count.plural";
        Component countPart = Component.translatable(countKey, count).withStyle(ChatFormatting.GREEN);
        return Component.translatable("madnesscore.command.list_line",
                displayName.copy().withStyle(ChatFormatting.WHITE), idSuffix(id), countPart);
    }

    private static Component successLine(Component label, Component displayName, ResourceLocation id, ServerPlayer target) {
        return Component.translatable("madnesscore.command.success_prefix", label, target.getGameProfile().getName())
                .withStyle(ChatFormatting.GREEN)
                .append(displayName.copy().withStyle(ChatFormatting.AQUA))
                .append(idSuffix(id));
    }

    private static Component idSuffix(ResourceLocation id) {
        return Component.translatable("madnesscore.command.id_suffix", id.toString()).withStyle(ChatFormatting.DARK_GRAY);
    }

    private static String formatPercentage(double percentage) {
        if (percentage == Math.rint(percentage)) {
            return String.valueOf((long) percentage);
        }
        return String.valueOf(percentage);
    }

    private static <T> List<T> sortById(java.util.Collection<T> values, java.util.function.Function<T, ResourceLocation> idFn) {
        return values.stream().sorted(Comparator.comparing(v -> idFn.apply(v).toString())).toList();
    }
}
