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

import static de.eldoria.bloodnight.command.Replies.arg;

/**
 * {@code /bloodnight item register}: keeps the item in the player's main hand, with all its data, under a generated id
 * that mob equipment and drops can name; the same item always gets the same id. With an empty hand it opens an
 * {@link ItemDropBox} to register many items at once. {@code /bloodnight item remove <id>}
 * forgets such an item. Both write {@code items.yml} at once, on the global region; items made in the editor are
 * changed there instead.
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
        Replies.send(sender, "usage", arg("usage", "/bloodnight item <register | remove <id>>"));
    }

    @Executes("register")
    void register(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            Replies.send(sender, "item.players");
            return;
        }
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held.isEmpty()) {
            ItemDropBox.open(player);
            return;
        }
        ItemStack item = held.clone();
        plugin.tasks().globalNow(() -> register(sender, item));
    }

    private void register(CommandSender sender, ItemStack held) {
        ItemRegistry items = copy(plugin.configuration().items());
        String id = items.register(held);
        plugin.configuration().replaceItems(items);
        Component copyable = Component.text(id, NamedTextColor.GOLD)
                .clickEvent(ClickEvent.copyToClipboard(id))
                .hoverEvent(Replies.text(sender, "item.copy"));
        Replies.send(sender, "item.registered", arg("id", copyable));
    }

    @Executes("remove")
    void remove(CommandSender sender, String id) {
        plugin.tasks().globalNow(() -> unregister(sender, id));
    }

    private void unregister(CommandSender sender, String id) {
        ItemRegistry items = copy(plugin.configuration().items());
        if (!items.unregister(id)) {
            Replies.send(sender, "item.unknown", arg("id", id));
            return;
        }
        plugin.configuration().replaceItems(items);
        Replies.send(sender, "item.removed", arg("id", id));
    }

    static ItemRegistry copy(ItemRegistry items) {
        return new ItemRegistry(Map.copyOf(items.registrations()), Map.copyOf(items.definitions()));
    }
}
