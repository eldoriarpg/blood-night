/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command.suggest;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

/**
 * Provides {@link WorldSuggestions}: world names as admins know them from the server's folders, rather than the
 * dimension keys of Paper's world argument.
 */
public final class WorldNames {
    private WorldNames() {}

    /**
     * @param context the command being typed
     * @param builder where suggestions go
     * @return the world names starting with what is typed
     */
    @WorldSuggestions
    public static CompletableFuture<Suggestions> suggest(
            CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String typed = builder.getRemainingLowerCase();
        Bukkit.getWorlds().stream()
                .map(World::getName)
                .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(typed))
                .sorted()
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
