/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command.setup;

import de.eldoria.bloodnight.command.editor.EditorCommand;
import net.strokkur.commands.DefaultExecutes;
import net.strokkur.commands.Subcommand;
import net.strokkur.commands.permission.Permission;
import org.bukkit.command.CommandSender;

/**
 * {@code /bloodnight setup}: opens a fresh editor session that starts with the setup wizard: worlds, how often and how
 * hard blood nights are, atmosphere, mobs from the store, and the old BloodNight's settings when they are in
 * {@code legacy/}. It is applied like any session; the wizard marks the setup as done.
 */
@Subcommand("setup")
@Permission("bloodnight.command.setup")
public final class SetupCommand {
    private final EditorCommand editor;

    public SetupCommand(EditorCommand editor) {
        this.editor = editor;
    }

    @DefaultExecutes
    public void setup(CommandSender sender) {
        editor.openSetup(sender);
    }
}
