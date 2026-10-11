/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command;

import de.eldoria.bloodnight.BloodNight;
import de.eldoria.graph.compiler.GraphProblem;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Subcommand;
import net.strokkur.commands.permission.Permission;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * {@code /bloodnight problems [mob]}: the mobs whose behaviour does not compile or failed while running, or one mob's
 * problems and failures.
 */
@Subcommand("problems")
@Permission("bloodnight.command.problems")
final class ProblemsCommand {
    private final BloodNight plugin;

    ProblemsCommand(BloodNight plugin) {
        this.plugin = plugin;
    }

    @Executes
    void list(CommandSender sender) {
        Set<String> mobs = new TreeSet<>(plugin.behaviours().problems().keySet());
        mobs.addAll(plugin.coordinator().failures().keySet());
        sender.sendMessage(mobs.isEmpty() ? "No mob has problems" : "Mobs with problems: " + String.join(", ", mobs));
    }

    @Executes
    void show(CommandSender sender, @MobSuggestions String mob) {
        List<GraphProblem> problems = plugin.behaviours().problems().getOrDefault(mob, List.of());
        List<String> failed = plugin.coordinator().failures().getOrDefault(mob, List.of());
        if (problems.isEmpty() && failed.isEmpty()) sender.sendMessage("Mob %s has no problems".formatted(mob));
        problems.forEach(problem -> sender.sendMessage(problem.toString()));
        failed.forEach(failure -> sender.sendMessage("Failed at runtime: " + failure));
    }
}
