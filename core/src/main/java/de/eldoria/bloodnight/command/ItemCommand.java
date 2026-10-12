/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command;

import de.eldoria.bloodnight.BloodNight;
import de.eldoria.bloodnight.items.ItemRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.strokkur.commands.DefaultExecutes;
import net.strokkur.commands.Executes;
import net.strokkur.commands.Subcommand;
import net.strokkur.commands.permission.Permission;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;

/**
 * {@code /bloodnight item register}: keeps the item in the player's main hand, with all its data, under a generated id
 * that mob equipment and drops can name; the same item always gets the same id. {@code /bloodnight item remove <id>}
 * forgets such an item. Both write {@code items.yml} at once; items made in the editor are changed there instead.
 */
@Subcommand("item")
@Permission("bloodnight.command.item")
final class ItemCommand {
    private final BloodNight plugin;

    ItemCommand(BloodNight plugin) {
        this.plugin = plugin;
    }

    @DefaultExecutes
    void usage(CommandSender sender) {
        sender.sendMessage("Usage: /bloodnight item <register | remove <id>>");
    }

    @Executes("register")
    void register(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can register the item in their hand");
            return;
        }
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held.isEmpty()) {
            sender.sendMessage(Component.text("Hold the item to register in your main hand", NamedTextColor.RED));
            return;
        }
        ItemRegistry items = copy(plugin.configuration().items());
        String id = items.register(held);
        plugin.configuration().replaceItems(items);
        sender.sendMessage(Component.text("Registered as ")
                .append(Component.text(id, NamedTextColor.GOLD)
                        .clickEvent(ClickEvent.copyToClipboard(id))
                        .hoverEvent(Component.text("Click to copy")))
                .append(Component.text("; mobs can wear and drop it under this id")));
    }

    @Executes("remove")
    void remove(CommandSender sender, String id) {
        ItemRegistry items = copy(plugin.configuration().items());
        if (!items.unregister(id)) {
            sender.sendMessage(Component.text("No registered item " + id, NamedTextColor.RED));
            return;
        }
        plugin.configuration().replaceItems(items);
        sender.sendMessage("Removed " + id);
    }

    private static ItemRegistry copy(ItemRegistry items) {
        return new ItemRegistry(Map.copyOf(items.registrations()), Map.copyOf(items.definitions()));
    }
}
