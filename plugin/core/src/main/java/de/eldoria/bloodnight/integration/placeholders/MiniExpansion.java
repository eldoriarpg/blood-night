/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.integration.placeholders;

import io.github.miniplaceholders.api.Expansion;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * BloodNight's placeholders for MiniPlaceholders: {@code <bloodnight_<name>[:<arguments>]>}, e.g.
 * {@code <bloodnight_probability:2:world_nether>}, see {@link Placeholders}. Only loaded when MiniPlaceholders runs.
 */
public final class MiniExpansion {
    private MiniExpansion() {}

    /**
     * Registers the placeholders.
     *
     * @return the expansion, to unregister it later
     */
    public static Expansion register(Placeholders placeholders, String version) {
        Expansion.Builder builder =
                Expansion.builder("bloodnight").author("RainbowDashLabs").version(version);
        for (String name : Placeholders.NAMES) {
            builder.audiencePlaceholder(
                    name, (audience, arguments, context) -> tag(placeholders, name, audience, arguments));
        }
        Expansion expansion = builder.build();
        expansion.register();
        return expansion;
    }

    private static Tag tag(Placeholders placeholders, String name, Audience audience, ArgumentQueue queue) {
        List<String> arguments = new ArrayList<>();
        while (queue.hasNext()) arguments.add(queue.pop().value());
        @Nullable World world = audience instanceof Player player ? player.getWorld() : null;
        String value = placeholders.value(new Placeholders.Request(name, arguments), world);
        return Tag.selfClosingInserting(Component.text(value == null ? "" : value));
    }

    /**
     * Removes the placeholders again.
     *
     * @param expansion what {@link #register} returned
     */
    public static void unregister(Object expansion) {
        ((Expansion) expansion).unregister();
    }
}
