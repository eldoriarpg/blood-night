/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command;

import de.eldoria.bloodnight.BloodNight;
import de.eldoria.bloodnight.night.NightTimes;
import de.eldoria.bloodnight.night.Prediction;
import net.kyori.adventure.text.Component;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Subcommand;
import net.strokkur.commands.permission.Permission;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

import static de.eldoria.bloodnight.command.Replies.arg;

/**
 * {@code /bloodnight night [force|cancel] [world]}: shows a world's night selection and next blood night, or forces or
 * cancels its coming night. Without a world, a player's own world or the first world for the console.
 * <p>
 * The night is read and changed on the global region, which owns world time and world data.
 */
@Subcommand("night")
@Permission("bloodnight.command.night")
final class NightCommand {
    private static final int HORIZON = 1000;

    private final BloodNight plugin;

    NightCommand(BloodNight plugin) {
        this.plugin = plugin;
    }

    @Executes
    void show(CommandSender sender) {
        show(sender, worldOf(sender));
    }

    @Executes
    void show(CommandSender sender, @WorldSuggestions String world) {
        world(sender, world).ifPresent(found -> show(sender, found));
    }

    private void show(CommandSender sender, World world) {
        plugin.tasks().globalNow(() -> report(sender, world));
    }

    private void report(CommandSender sender, World world) {
        long coming = plugin.selector().times(world).upcoming(NightTimes.now(world));
        Replies.send(
                sender,
                plugin.bloodNights().running(world).isPresent() ? "night.running" : "night.not_running",
                arg("world", plugin.worldNames().name(world)),
                arg("selection", selection(sender, world)));
        Replies.send(
                sender,
                "night.coming",
                arg("night", coming),
                arg("probability", plugin.selector().probability(world, coming)));
        plugin.selector()
                .next(world, HORIZON)
                .ifPresentOrElse(
                        prediction -> tellNext(sender, coming, prediction),
                        () -> Replies.send(sender, "night.none", arg("nights", HORIZON)));
    }

    @Executes("force")
    @Permission("bloodnight.command.night.manage")
    void force(CommandSender sender) {
        force(sender, worldOf(sender));
    }

    @Executes("force")
    @Permission("bloodnight.command.night.manage")
    void force(CommandSender sender, @WorldSuggestions String world) {
        world(sender, world).ifPresent(found -> force(sender, found));
    }

    private void force(CommandSender sender, World world) {
        plugin.tasks().globalNow(() -> {
            long night = plugin.bloodNights().force(world);
            Replies.send(
                    sender,
                    "night.forced",
                    arg("night", night),
                    arg("world", plugin.worldNames().name(world)));
        });
    }

    @Executes("cancel")
    @Permission("bloodnight.command.night.manage")
    void cancel(CommandSender sender) {
        cancel(sender, worldOf(sender));
    }

    @Executes("cancel")
    @Permission("bloodnight.command.night.manage")
    void cancel(CommandSender sender, @WorldSuggestions String world) {
        world(sender, world).ifPresent(found -> cancel(sender, found));
    }

    private void cancel(CommandSender sender, World world) {
        plugin.tasks().globalNow(() -> {
            long night = plugin.bloodNights().cancel(world);
            Replies.send(
                    sender,
                    "night.cancelled",
                    arg("night", night),
                    arg("world", plugin.worldNames().name(world)));
        });
    }

    private Component selection(CommandSender sender, World world) {
        var settings = plugin.configuration().worldConfig(world).nightSettings();
        if (!world.isFixedTime()) {
            return Component.text(plugin.configuration()
                    .worldConfig(world)
                    .nightSelection()
                    .type()
                    .name());
        }
        return Replies.text(
                sender,
                "night.timed",
                arg("every", settings.timed().everyMinutes()),
                arg("length", settings.timed().lengthMinutes()));
    }

    private static void tellNext(CommandSender sender, long coming, Prediction prediction) {
        if (prediction.ticksUntil() == 0) {
            Replies.send(sender, "night.now");
            return;
        }
        long nights = prediction.night() - coming;
        Replies.send(
                sender,
                prediction.forced() ? "night.next_forced" : "night.next",
                arg("night", prediction.night()),
                arg("after", nights),
                arg("minutes", prediction.ticksUntil() / 20 / 60));
    }

    private Optional<World> world(CommandSender sender, String name) {
        World world = plugin.getServer().getWorld(name);
        if (world == null) Replies.send(sender, "night.unknown_world", arg("world", name));
        return Optional.ofNullable(world);
    }

    private World worldOf(CommandSender sender) {
        return sender instanceof Player player
                ? player.getWorld()
                : plugin.getServer().getWorlds().getFirst();
    }
}
