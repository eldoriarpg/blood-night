/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.night;

import de.eldoria.bloodnight.api.event.BloodNightBeginEvent;
import de.eldoria.bloodnight.api.event.BloodNightEndEvent;
import de.eldoria.bloodnight.configuration.elements.world.NightSettings;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.List;
import java.util.function.Function;

/**
 * Runs a world's begin and end commands from the console when a blood night begins and ends. {@code {world}} is the
 * world's name; a command with {@code {player}} runs once for every player in the world.
 */
public final class NightCommands implements Listener {
    private final Server server;
    private final Function<World, NightSettings> settings;

    /**
     * @param server   runs the commands
     * @param settings a world's night settings
     */
    public NightCommands(Server server, Function<World, NightSettings> settings) {
        this.server = server;
        this.settings = settings;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBegin(BloodNightBeginEvent event) {
        run(event.getWorld(), settings.apply(event.getWorld()).beginCommands());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnd(BloodNightEndEvent event) {
        run(event.getWorld(), settings.apply(event.getWorld()).endCommands());
    }

    private void run(World world, List<String> commands) {
        for (String command : commands) {
            String forWorld = command.replace("{world}", world.getName());
            if (!forWorld.contains("{player}")) {
                server.dispatchCommand(server.getConsoleSender(), forWorld);
                continue;
            }
            for (Player player : world.getPlayers()) {
                server.dispatchCommand(server.getConsoleSender(), forWorld.replace("{player}", player.getName()));
            }
        }
    }
}
