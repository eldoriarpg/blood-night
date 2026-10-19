/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command;

import de.eldoria.bloodnight.announce.Texts;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.command.CommandSender;

/**
 * Command replies in the sender's language, with the plugin's prefix: texts under {@code bloodnight.command.} in the
 * message files.
 */
public final class Replies {
    private static final String PREFIX = "bloodnight.command.";

    private Replies() {}

    /**
     * Sends a reply.
     *
     * @param key       the text's key below {@code bloodnight.command.}, e.g. {@code reload.compiling}
     * @param arguments the text's placeholders, see {@link #arg}
     */
    public static void send(CommandSender sender, String key, TagResolver... arguments) {
        Texts.sender().sendMessage(sender, PREFIX + key, arguments);
    }

    /**
     * @return a reply text in the receiver's language, without the prefix, e.g. to put into another one
     */
    public static Component text(CommandSender receiver, String key, TagResolver... arguments) {
        return Texts.render(receiver, PREFIX + key, arguments);
    }

    /**
     * @return a placeholder filled in as plain text, never read as MiniMessage
     */
    public static TagResolver arg(String name, Object value) {
        return Placeholder.unparsed(name, String.valueOf(value));
    }

    /**
     * @return a placeholder filled in as a component
     */
    public static TagResolver arg(String name, ComponentLike value) {
        return Placeholder.component(name, value);
    }
}
