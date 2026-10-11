/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.announce;

import de.eldoria.bloodnight.configuration.elements.Announcements.Method;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;

/**
 * Shows a message the way an {@link Method} says; chat messages carry the configured prefix.
 */
final class Delivery {
    private Delivery() {}

    /**
     * @param audience who receives the message
     * @param method   how it is shown
     * @param prefix   MiniMessage put in front of chat messages
     * @param message  the message
     */
    static void send(Audience audience, Method method, String prefix, Component message) {
        switch (method) {
            case CHAT ->
                audience.sendMessage(
                        MiniMessage.miniMessage().deserialize(prefix).append(message));
            case TITLE -> audience.showTitle(Title.title(message, Component.empty()));
            case SUBTITLE -> audience.showTitle(Title.title(Component.empty(), message));
            case ACTION_BAR -> audience.sendActionBar(message);
        }
    }
}
