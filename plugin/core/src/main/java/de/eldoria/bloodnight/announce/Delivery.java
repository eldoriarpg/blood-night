/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.announce;

import de.eldoria.bloodnight.configuration.server.Announcements.Method;
import de.eldoria.eldoutilities.messages.MessageSender;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import net.kyori.adventure.title.Title;
import org.bukkit.entity.Player;

/**
 * Shows a text the way an {@link Method} says; only chat messages carry the prefix.
 */
final class Delivery {
    private Delivery() {}

    /**
     * @param player    who receives the text
     * @param method    how it is shown
     * @param key       the text's key
     * @param arguments its placeholders
     */
    static void send(Player player, Method method, String key, TagResolver... arguments) {
        MessageSender sender = Texts.sender();
        switch (method) {
            case CHAT -> sender.sendMessage(player, key, arguments);
            case TITLE -> sender.sendTitle(player, key, "", Title.DEFAULT_TIMES, arguments);
            case SUBTITLE -> sender.sendTitle(player, "", key, Title.DEFAULT_TIMES, arguments);
            case ACTION_BAR -> sender.sendActionBar(player, key, arguments);
        }
    }
}
