package dev.lukamadness.madnesscore.common.client.config;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import dev.lukamadness.madnesscore.common.client.api.config.ConfigEntryPoint;
import dev.lukamadness.madnesscore.common.client.api.config.structure.ConfigBuilder;
import dev.lukamadness.madnesscore.common.client.config.builder.ConfigBuilderImpl;
import dev.lukamadness.madnesscore.common.client.config.structure.Config;
import dev.lukamadness.madnesscore.common.client.config.structure.ModOptions;
import net.minecraft.CrashReport;
import net.minecraft.client.Minecraft;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ConfigManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigManager.class);

    public static final String CONFIG_ENTRY_POINT_KEY = "madnesscore:config_api_user";

    private record ConfigUser(
            Supplier<ConfigEntryPoint> configEntrypoint,
            String modId) {
    }
    public record ModMetadata(String modName, String modVersion) {
    }

    private static final Collection<ConfigUser> configUsers = new ArrayList<>();

    public static Config CONFIG;
    private static Function<String, ModMetadata> modInfoFunction;

    public static void setModInfoFunction(Function<String, ModMetadata> modInfoFunction) {
        ConfigManager.modInfoFunction = modInfoFunction;
    }

    public static void registerConfigEntryPoint(String className, String modId) {
        Class<?> entryPointClass;
        try {
            entryPointClass = Class.forName(className);
        } catch (ClassNotFoundException e) {
            LOGGER.warn("Mod '{}' provided a custom config integration but the class is missing: {}", modId, className);
            return;
        }
        if (!ConfigEntryPoint.class.isAssignableFrom(entryPointClass)) {
            LOGGER.warn("Mod '{}' provided a custom config integration but the class is of the wrong type: {}", modId, entryPointClass);
            return;
        }

        registerConfigEntryPoint(() -> {
            try {
                Constructor<?> constructor = entryPointClass.getDeclaredConstructor();
                constructor.setAccessible(true);
                return (ConfigEntryPoint) constructor.newInstance();
            } catch (ReflectiveOperationException e) {
                LOGGER.warn("Mod '{}' provided a custom config integration but the class could not be constructed: {}", modId, entryPointClass);
            }
            return null;
        }, modId);
    }

    public static void registerConfigEntryPoint(Supplier<ConfigEntryPoint> entryPoint, String modId) {
        configUsers.add(new ConfigUser(entryPoint, modId));
    }

    public static void registerConfigsEarly() {
        for (ConfigUser configUser : configUsers) {
            var entryPoint = configUser.configEntrypoint.get();
            if (entryPoint == null) {
                continue;
            }

            var builder = new ConfigBuilderImpl(modInfoFunction, configUser.modId);
            try {
                entryPoint.registerConfigEarly(builder);
            } catch (Exception e) {
                crashWithMessage("Mod '" + configUser.modId + "' failed while registering config options early.", e);
                return;
            }
        }
    }

    public static void registerConfigsLate() {
        registerConfigs(ConfigEntryPoint::registerConfigLate);
    }

    private static final String ANCHOR_CONFIG_ID = "madnesscore";

    private static void registerConfigs(BiConsumer<ConfigEntryPoint, ConfigBuilder> registerMethod) {
        var configIds = new ObjectOpenHashSet<>();
        ModOptions anchorModOptions = null;
        var modConfigs = new ObjectArrayList<ModOptions>();

        for (ConfigUser configUser : configUsers) {
            var entryPoint = configUser.configEntrypoint.get();
            if (entryPoint == null) {
                continue;
            }

            var builder = new ConfigBuilderImpl(modInfoFunction, configUser.modId);
            Collection<ModOptions> builtConfigs;
            try {
                registerMethod.accept(entryPoint, builder);
                builtConfigs = builder.build();

                for (var modConfig : builtConfigs) {
                    var configId = modConfig.configId();
                    if (configIds.contains(configId)) {
                        throw new IllegalArgumentException("Mod '" + configUser.modId + "' provided a duplicate mod id: " + configId);
                    }

                    configIds.add(configId);

                    if (configId.equals(ANCHOR_CONFIG_ID)) {
                        anchorModOptions = modConfig;
                    } else {
                        modConfigs.add(modConfig);
                    }
                }
            } catch (Exception e) {
                crashWithMessage("Mod '" + configUser.modId + "' failed while registering config options.", e);
                return;
            }
        }

        modConfigs.sort(Comparator.comparing(ModOptions::name));

        if (anchorModOptions == null) {
            throw new RuntimeException("Madness Core's own config could not be found; the game is in a broken state most likely caused by an earlier error from another mod. Please check the game log (latest.log) for any errors first instead of reporting this as a Madness Core-related crash.");
        }
        modConfigs.add(0, anchorModOptions);

        try {
            CONFIG = new Config(modConfigs);
        } catch (Exception e) {
            crashWithMessage("Failed to build config options", e);
        }
    }

    private static void crashWithMessage(String message, Exception e) {
        Minecraft.crash(null, Minecraft.getInstance().gameDirectory, new CrashReport(message, e));
    }
}
