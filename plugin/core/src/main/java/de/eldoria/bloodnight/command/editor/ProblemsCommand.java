/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command.editor;

import de.eldoria.bloodnight.BloodNight;
import de.eldoria.bloodnight.command.Replies;
import de.eldoria.bloodnight.command.suggest.MobSuggestions;
import de.eldoria.graph.compiler.validate.GraphProblem;
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
public final class ProblemsCommand {
    private final BloodNight plugin;

    public ProblemsCommand(BloodNight plugin) {
        this.plugin = plugin;
    }

    @Executes
    public void list(CommandSender sender) {
        Set<String> mobs = new TreeSet<>(plugin.behaviours().problems().keySet());
        mobs.addAll(plugin.coordinator().failures().keySet());
        if (mobs.isEmpty()) {
            Replies.send(sender, "problems.none");
        } else {
            Replies.send(sender, "problems.list", Replies.arg("mobs", String.join(", ", mobs)));
        }
    }

    @Executes
    public void show(CommandSender sender, @MobSuggestions String mob) {
        List<GraphProblem> problems = plugin.behaviours().problems().getOrDefault(mob, List.of());
        List<String> failed = plugin.coordinator().failures().getOrDefault(mob, List.of());
        if (problems.isEmpty() && failed.isEmpty()) Replies.send(sender, "problems.mob_none", Replies.arg("mob", mob));
        problems.forEach(problem -> sender.sendMessage(problem.toString()));
        failed.forEach(failure -> Replies.send(sender, "problems.runtime", Replies.arg("failure", failure)));
    }
}
