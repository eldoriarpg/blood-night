/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.night;

import de.eldoria.bloodnight.api.event.night.BloodNightBeginEvent;
import de.eldoria.bloodnight.api.event.night.BloodNightEndEvent;
import de.eldoria.bloodnight.configuration.elements.world.NightSettings;
import de.eldoria.bloodnight.integration.placeholders.CommandText;
import de.eldoria.bloodnight.util.Commands;
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
 * world's name; a command with {@code {player}} runs once for every player in the world. The events come from the
 * global region, the thread console commands belong to.
 */
public final class NightCommands implements Listener {
    private final Server server;
    private final Function<World, NightSettings> settings;
    private final CommandText text;

    /**
     * @param server   runs the commands
     * @param settings a world's night settings
     * @param text     fills in other plugins' placeholders
     */
    public NightCommands(Server server, Function<World, NightSettings> settings, CommandText text) {
        this.server = server;
        this.settings = settings;
        this.text = text;
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
            String forWorld = Commands.withoutSlash(command).replace("{world}", world.getName());
            if (!forWorld.contains("{player}")) {
                server.dispatchCommand(server.getConsoleSender(), text.resolve(forWorld, null));
                continue;
            }
            for (Player player : world.getPlayers()) {
                server.dispatchCommand(
                        server.getConsoleSender(),
                        text.resolve(forWorld.replace("{player}", player.getName()), player));
            }
        }
    }
}
