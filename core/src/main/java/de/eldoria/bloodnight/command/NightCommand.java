/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command;

import de.eldoria.bloodnight.BloodNight;
import de.eldoria.bloodnight.night.Nights;
import de.eldoria.bloodnight.night.Prediction;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Subcommand;
import net.strokkur.commands.permission.Permission;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * {@code /bloodnight night [force|cancel] [world]}: shows a world's night selection and next blood night, or forces or
 * cancels its coming night. Without a world, a player's own world or the first world for the console.
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
    void show(CommandSender sender, World world) {
        long coming = Nights.upcoming(world.getFullTime());
        sender.sendMessage("%s: %s, blood night %s"
                .formatted(
                        world.getName(),
                        plugin.configuration()
                                .worldConfig(world)
                                .nightSelection()
                                .type(),
                        plugin.bloodNights().running(world).isPresent() ? "running" : "not running"));
        sender.sendMessage("Coming night %d: %d%% probability"
                .formatted(coming, plugin.selector().probability(world, coming)));
        sender.sendMessage(plugin.selector()
                .next(world, HORIZON)
                .map(prediction -> describe(coming, prediction))
                .orElse("No blood night in the next %d nights".formatted(HORIZON)));
    }

    @Executes("force")
    @Permission("bloodnight.command.night.manage")
    void force(CommandSender sender) {
        force(sender, worldOf(sender));
    }

    @Executes("force")
    @Permission("bloodnight.command.night.manage")
    void force(CommandSender sender, World world) {
        long night = plugin.bloodNights().force(world);
        sender.sendMessage("Night %d in %s is a blood night".formatted(night, world.getName()));
    }

    @Executes("cancel")
    @Permission("bloodnight.command.night.manage")
    void cancel(CommandSender sender) {
        cancel(sender, worldOf(sender));
    }

    @Executes("cancel")
    @Permission("bloodnight.command.night.manage")
    void cancel(CommandSender sender, World world) {
        long night = plugin.bloodNights().cancel(world);
        sender.sendMessage("Night %d in %s is an ordinary night".formatted(night, world.getName()));
    }

    private static String describe(long coming, Prediction prediction) {
        if (prediction.ticksUntil() == 0) return "The blood night is now";
        long nights = prediction.night() - coming;
        long minutes = prediction.ticksUntil() / 20 / 60;
        return "Next blood night: night %d, %s, in about %d minutes%s"
                .formatted(
                        prediction.night(),
                        nights == 0 ? "the coming one" : "%d nights after the coming one".formatted(nights),
                        minutes,
                        prediction.forced() ? " (forced)" : "");
    }

    private World worldOf(CommandSender sender) {
        return sender instanceof Player player
                ? player.getWorld()
                : plugin.getServer().getWorlds().getFirst();
    }
}
