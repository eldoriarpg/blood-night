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

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Provides {@link MobSuggestions}. StrokkCommands calls suggestion providers statically, so the plugin hands in where
 * the mob ids come from when it enables and takes it back when it disables.
 */
public final class MobIds {
    private static volatile Supplier<Collection<String>> source = List::of;

    private MobIds() {}

    /**
     * @param ids the ids of the configured mobs, read on every suggestion
     */
    public static void use(Supplier<Collection<String>> ids) {
        source = ids;
    }

    /**
     * Stops suggesting mob ids.
     */
    public static void clear() {
        source = List::of;
    }

    /**
     * @param context the command being typed
     * @param builder where suggestions go
     * @return the ids starting with what is typed, sorted
     */
    @MobSuggestions
    public static CompletableFuture<Suggestions> suggest(
            CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String typed = builder.getRemainingLowerCase();
        source.get().stream()
                .filter(id -> id.toLowerCase(Locale.ROOT).startsWith(typed))
                .sorted()
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
