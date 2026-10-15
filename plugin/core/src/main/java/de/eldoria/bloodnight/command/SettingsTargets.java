/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.eldoria.bloodnight.configuration.ConfigFile;
import de.eldoria.bloodnight.configuration.SettingsPaths;
import de.eldoria.bloodnight.configuration.elements.WorldSettings;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Provides {@link SettingsTargetSuggestions} and {@link SettingsPathSuggestions}. {@value #SERVER} stands for
 * {@code config.yml}, any other target for the world of that name. Paths come from fresh settings, so they are the
 * same for every world.
 */
public final class SettingsTargets {
    /**
     * The target naming the server-wide settings.
     */
    public static final String SERVER = "server";

    private static final List<String> SERVER_PATHS = SettingsPaths.paths(new ConfigFile());
    private static final List<String> WORLD_PATHS = SettingsPaths.paths(new WorldSettings());

    private SettingsTargets() {}

    /**
     * @return {@value #SERVER} and the world names starting with what is typed
     */
    @SettingsTargetSuggestions
    public static CompletableFuture<Suggestions> targets(
            CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String typed = builder.getRemainingLowerCase();
        Stream.concat(Stream.of(SERVER), Bukkit.getWorlds().stream().map(World::getName))
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(typed))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    /**
     * @return the setting paths of the typed target starting with what is typed
     */
    @SettingsPathSuggestions
    public static CompletableFuture<Suggestions> paths(
            CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String typed = builder.getRemainingLowerCase();
        List<String> paths = SERVER.equals(target(context)) ? SERVER_PATHS : WORLD_PATHS;
        paths.stream()
                .filter(path -> path.toLowerCase(Locale.ROOT).startsWith(typed))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }

    private static String target(CommandContext<CommandSourceStack> context) {
        try {
            return context.getArgument("target", String.class);
        } catch (IllegalArgumentException e) {
            return "";
        }
    }
}
