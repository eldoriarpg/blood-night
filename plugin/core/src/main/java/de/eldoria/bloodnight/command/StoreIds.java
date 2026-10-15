/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.eldoria.bloodnight.store.Store;
import de.eldoria.bloodnight.store.StoreEntry;
import de.eldoria.bloodnight.store.StorePack;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import org.jspecify.annotations.Nullable;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

/**
 * Provides {@link StoreSuggestions}: pack ids first, then mob ids. StrokkCommands calls suggestion providers
 * statically, so the plugin hands in its store when it enables and takes it back when it disables.
 */
public final class StoreIds {
    private static volatile @Nullable Store store;

    private StoreIds() {}

    /**
     * @param current the store to suggest from
     */
    public static void use(Store current) {
        store = current;
    }

    /**
     * Stops suggesting.
     */
    public static void clear() {
        store = null;
    }

    /**
     * @return the packs and mobs starting with what is typed
     */
    @StoreSuggestions
    public static CompletableFuture<Suggestions> suggest(
            CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        Store current = store;
        if (current == null) return builder.buildFuture();
        String typed = builder.getRemainingLowerCase();
        Stream.concat(
                        current.packs().stream().map(StorePack::id),
                        current.entries().stream().map(StoreEntry::id).sorted())
                .filter(id -> id.toLowerCase(Locale.ROOT).startsWith(typed))
                .forEach(builder::suggest);
        return builder.buildFuture();
    }
}
