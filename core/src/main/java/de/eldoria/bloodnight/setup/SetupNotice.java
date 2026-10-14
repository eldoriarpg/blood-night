/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.setup;

import de.eldoria.bloodnight.announce.Messages;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.function.BooleanSupplier;

/**
 * Reminds admins with {@value #PERMISSION} on join to run {@code /bloodnight setup} while the setup is not done.
 */
public final class SetupNotice implements Listener {
    /**
     * The permission to run the setup and hear that it is missing.
     */
    public static final String PERMISSION = "bloodnight.command.setup";

    static final String PENDING = "bloodnight.command.setup.pending";
    private final BooleanSupplier done;

    /**
     * @param done whether the setup is done, read on every join
     */
    public SetupNotice(BooleanSupplier done) {
        this.done = done;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (done.getAsBoolean() || !player.hasPermission(PERMISSION)) return;
        player.sendMessage(Messages.message(PENDING));
    }
}
