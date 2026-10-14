/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.updates;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jspecify.annotations.Nullable;

import java.util.function.BooleanSupplier;
import java.util.logging.Logger;

/**
 * Tells the console once, and players with {@value #PERMISSION} when they join, that a newer BloodNight version is out.
 * Nothing is downloaded. {@link #check} blocks and runs off the server's threads, e.g. every twelve hours.
 */
public final class UpdateNotice implements Listener {
    /**
     * The permission to hear about new versions on join.
     */
    public static final String PERMISSION = "bloodnight.updates";

    static final String DOWNLOAD = "https://github.com/eldoriarpg/blood-night/releases/latest";
    private static final String MESSAGE =
            "<red>BloodNight <latest> is out.</red> <gray>You run <current>.</gray> <click:open_url:'"
                    + DOWNLOAD
                    + "'><underlined>Download it</underlined></click>";
    private final ReleaseSource source;
    private final String current;
    private final BooleanSupplier enabled;
    private final Logger logger;
    private volatile @Nullable String latest;

    /**
     * @param source  where the newest version is looked up
     * @param current the running version
     * @param enabled whether the server wants to hear about new versions
     * @param logger  the console
     */
    public UpdateNotice(ReleaseSource source, String current, BooleanSupplier enabled, Logger logger) {
        this.source = source;
        this.current = current;
        this.enabled = enabled;
        this.logger = logger;
    }

    /**
     * Asks the source for a newer version and tells the console when it found one it did not know yet.
     */
    public void check() {
        if (!enabled.getAsBoolean()) return;
        source.newer(current).ifPresent(found -> {
            if (found.equals(latest)) return;
            latest = found;
            logger.info("BloodNight %s is out. You run %s. Download it at %s".formatted(found, current, DOWNLOAD));
        });
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String found = latest;
        if (found == null || !enabled.getAsBoolean() || !player.hasPermission(PERMISSION)) return;
        player.sendMessage(message(found));
    }

    private Component message(String found) {
        return MiniMessage.miniMessage()
                .deserialize(MESSAGE, Placeholder.unparsed("latest", found), Placeholder.unparsed("current", current));
    }
}
