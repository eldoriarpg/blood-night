/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command;

import de.eldoria.bloodnight.announce.Messages;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.bukkit.command.CommandSender;

/**
 * Command replies, translated for each sender: texts under {@code bloodnight.command.} in the message bundles.
 */
final class Replies {
    private static final String PREFIX = "bloodnight.command.";

    private Replies() {}

    /**
     * Sends a reply.
     *
     * @param key       the text's key below {@code bloodnight.command.}, e.g. {@code reload.compiling}
     * @param arguments the text's arguments, see {@link #arg}
     */
    static void send(CommandSender sender, String key, ComponentLike... arguments) {
        sender.sendMessage(text(key, arguments));
    }

    /**
     * @return a reply text, e.g. to put into another one
     */
    static TranslatableComponent text(String key, ComponentLike... arguments) {
        return Messages.message(PREFIX + key, arguments);
    }

    /**
     * @return an argument filled in as plain text
     */
    static ComponentLike arg(String name, Object value) {
        return Argument.string(name, String.valueOf(value));
    }

    /**
     * @return an argument filled in as a component
     */
    static ComponentLike arg(String name, ComponentLike value) {
        return Argument.component(name, value);
    }
}
