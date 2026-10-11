/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.rules;

import de.eldoria.bloodnight.announce.Messages;
import de.eldoria.bloodnight.night.BloodNights;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * Keeps players from using a world's blocked commands while a blood night runs in it.
 * <p>
 * A blocked entry is a command with optional leading arguments, e.g. {@code home} or {@code gamemode creative}. The
 * command is matched on what it resolves to, so aliases and namespaced forms ({@code minecraft:gamemode}) are blocked
 * too; the arguments are matched word by word. Players with {@value #BYPASS} are never blocked.
 */
public final class CommandBlocker implements Listener {
    public static final String BYPASS = "bloodnight.bypass.commands";
    private final BloodNights nights;
    private final CommandMap commands;
    private final Function<World, List<String>> blocked;

    /**
     * @param nights   the running blood nights
     * @param commands the server's commands
     * @param blocked  a world's blocked commands
     */
    public CommandBlocker(BloodNights nights, CommandMap commands, Function<World, List<String>> blocked) {
        this.nights = nights;
        this.commands = commands;
        this.blocked = blocked;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        if (nights.running(world).isEmpty() || player.hasPermission(BYPASS)) return;
        if (!isBlocked(event.getMessage(), blocked.apply(world))) return;
        event.setCancelled(true);
        player.sendMessage(Messages.message(Messages.COMMAND_BLOCKED));
    }

    /**
     * @param message the command as typed, with or without the leading slash
     * @param entries the blocked entries
     * @return whether the command is one of the entries
     */
    boolean isBlocked(String message, List<String> entries) {
        String[] input = words(message);
        if (input.length == 0) return false;
        return entries.stream().map(CommandBlocker::words).anyMatch(entry -> matches(input, entry));
    }

    private boolean matches(String[] input, String[] entry) {
        if (entry.length == 0 || entry.length > input.length) return false;
        if (!sameCommand(input[0], entry[0])) return false;
        for (int i = 1; i < entry.length; i++) {
            if (!entry[i].equals(input[i])) return false;
        }
        return true;
    }

    private boolean sameCommand(String typed, String blockedLabel) {
        if (withoutNamespace(typed).equals(withoutNamespace(blockedLabel))) return true;
        Command typedCommand = command(typed);
        Command blockedCommand = command(blockedLabel);
        if (typedCommand == null || blockedCommand == null) return false;
        return typedCommand == blockedCommand
                || withoutNamespace(typedCommand.getName()).equals(withoutNamespace(blockedCommand.getName()));
    }

    private @Nullable Command command(String label) {
        return commands.getCommand(label);
    }

    private static String withoutNamespace(String label) {
        return label.substring(label.indexOf(':') + 1).toLowerCase(Locale.ROOT);
    }

    private static String[] words(String command) {
        String trimmed = command.strip();
        if (trimmed.startsWith("/")) trimmed = trimmed.substring(1);
        if (trimmed.isEmpty()) return new String[0];
        return trimmed.toLowerCase(Locale.ROOT).split("\\s+");
    }
}
