/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command.night;

import de.eldoria.bloodnight.BloodNight;
import de.eldoria.bloodnight.command.Replies;
import de.eldoria.bloodnight.command.suggest.WorldSuggestions;
import de.eldoria.bloodnight.night.selection.NightTimes;
import de.eldoria.bloodnight.night.selection.Prediction;
import net.kyori.adventure.text.Component;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Subcommand;
import net.strokkur.commands.permission.Permission;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * {@code /bloodnight night [force|cancel] [world]}: shows a world's night selection and next blood night, or forces or
 * cancels its coming night. Without a world, a player's own world or the first world for the console.
 * <p>
 * The night is read and changed on the global region, which owns world time and world data.
 */
@Subcommand("night")
@Permission("bloodnight.command.night")
public final class NightCommand {
    private static final int HORIZON = 1000;

    private final BloodNight plugin;

    public NightCommand(BloodNight plugin) {
        this.plugin = plugin;
    }

    @Executes
    public void show(CommandSender sender) {
        show(sender, worldOf(sender));
    }

    @Executes
    public void show(CommandSender sender, @WorldSuggestions String world) {
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
                Replies.arg("world", plugin.worldNames().name(world)),
                Replies.arg("selection", selection(sender, world)));
        Replies.send(
                sender,
                "night.coming",
                Replies.arg("night", coming),
                Replies.arg("probability", plugin.selector().probability(world, coming)));
        plugin.selector()
                .next(world, HORIZON)
                .ifPresentOrElse(
                        prediction -> tellNext(sender, coming, prediction),
                        () -> Replies.send(sender, "night.none", Replies.arg("nights", HORIZON)));
    }

    @Executes("force")
    @Permission("bloodnight.command.night.manage")
    public void force(CommandSender sender) {
        force(sender, worldOf(sender));
    }

    @Executes("force")
    @Permission("bloodnight.command.night.manage")
    public void force(CommandSender sender, @WorldSuggestions String world) {
        world(sender, world).ifPresent(found -> force(sender, found));
    }

    private void force(CommandSender sender, World world) {
        plugin.tasks().globalNow(() -> {
            long night = plugin.bloodNights().force(world);
            if (plugin.bloodNights().running(world).isPresent()) {
                Replies.send(
                        sender,
                        "night.forced_now",
                        Replies.arg("world", plugin.worldNames().name(world)));
                return;
            }
            long ticks = plugin.selector().times(world).ticksUntil(NightTimes.now(world), night);
            Replies.send(
                    sender,
                    "night.forced",
                    Replies.arg("night", night),
                    Replies.arg("world", plugin.worldNames().name(world)),
                    Replies.arg("minutes", Math.max(1, ticks / 20 / 60)));
        });
    }

    @Executes("cancel")
    @Permission("bloodnight.command.night.manage")
    public void cancel(CommandSender sender) {
        cancel(sender, worldOf(sender));
    }

    @Executes("cancel")
    @Permission("bloodnight.command.night.manage")
    public void cancel(CommandSender sender, @WorldSuggestions String world) {
        world(sender, world).ifPresent(found -> cancel(sender, found));
    }

    private void cancel(CommandSender sender, World world) {
        plugin.tasks().globalNow(() -> {
            long night = plugin.bloodNights().cancel(world);
            Replies.send(
                    sender,
                    "night.cancelled",
                    Replies.arg("night", night),
                    Replies.arg("world", plugin.worldNames().name(world)));
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
                Replies.arg("every", settings.timed().everyMinutes()),
                Replies.arg("length", settings.timed().lengthMinutes()));
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
                Replies.arg("night", prediction.night()),
                Replies.arg("after", nights),
                Replies.arg("minutes", prediction.ticksUntil() / 20 / 60));
    }

    private Optional<World> world(CommandSender sender, String name) {
        World world = plugin.getServer().getWorld(name);
        if (world == null) Replies.send(sender, "night.unknown_world", Replies.arg("world", name));
        return Optional.ofNullable(world);
    }

    private World worldOf(CommandSender sender) {
        return sender instanceof Player player
                ? player.getWorld()
                : plugin.getServer().getWorlds().getFirst();
    }
}
