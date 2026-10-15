/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.rules;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CommandBlockerTest extends RulesTest {
    private boolean denied;
    private static final List<String> BLOCKED = List.of("warp", "gamemode creative");
    private CommandBlocker blocker;

    @BeforeEach
    void setUp() {
        server.getCommandMap().register("test", new Command("warp", "", "/warp", List.of("w")) {
            @Override
            public boolean execute(CommandSender sender, String label, String[] args) {
                return true;
            }
        });
        blocker = new CommandBlocker(nights, server.getCommandMap(), ignored -> BLOCKED, (location, flag) -> !denied);
        server.getPluginManager().registerEvents(blocker, plugin);
    }

    @Test
    void commandsAreMatchedOnWhatTheyResolveTo() {
        assertTrue(blocker.isBlocked("/warp", BLOCKED));
        assertTrue(blocker.isBlocked("/WARP spawn", BLOCKED));
        assertTrue(blocker.isBlocked("/w spawn", BLOCKED));
        assertTrue(blocker.isBlocked("/test:warp", BLOCKED));
        assertFalse(blocker.isBlocked("/warps", BLOCKED));
    }

    @Test
    void argumentsAreMatchedWordByWord() {
        assertTrue(blocker.isBlocked("/gamemode creative", BLOCKED));
        assertTrue(blocker.isBlocked("/minecraft:gamemode creative Steve", BLOCKED));
        assertFalse(blocker.isBlocked("/gamemode survival", BLOCKED));
        assertFalse(blocker.isBlocked("/gamemode", BLOCKED));
    }

    private boolean cancelled(PlayerMock player, String command) {
        var event = new PlayerCommandPreprocessEvent(player, command);
        server.getPluginManager().callEvent(event);
        return event.isCancelled();
    }

    @Test
    void onlyBlockedDuringABloodNight() {
        PlayerMock player = server.addPlayer();
        player.teleport(world.getSpawnLocation());
        assertFalse(cancelled(player, "/warp"));
        bloodNight();
        assertTrue(cancelled(player, "/warp"));
        player.addAttachment(plugin, CommandBlocker.BYPASS, true);
        assertFalse(cancelled(player, "/warp"));
    }

    @Test
    void regionsCanLetBlockedCommandsThrough() {
        PlayerMock player = server.addPlayer();
        player.teleport(world.getSpawnLocation());
        bloodNight();
        denied = true;
        assertFalse(cancelled(player, "/warp"));
    }
}
