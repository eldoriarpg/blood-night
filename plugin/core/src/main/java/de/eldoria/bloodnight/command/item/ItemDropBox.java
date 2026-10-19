/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command.item;

import de.eldoria.bloodnight.BloodNight;
import de.eldoria.bloodnight.command.Replies;
import de.eldoria.bloodnight.items.ItemRegistry;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A chest a player drops items into to register them all at once. When it closes, the player gets every item back
 * (what no longer fits drops at their feet) and each one is registered; an item that is already registered keeps its
 * id, so dropping the same item twice registers it once.
 */
public final class ItemDropBox implements InventoryHolder {
    private static final int SIZE = 54;
    private final Inventory inventory;

    private ItemDropBox(Component title) {
        inventory = Bukkit.createInventory(this, SIZE, title);
    }

    /**
     * Opens an empty box for the player.
     */
    static void open(Player player) {
        player.openInventory(new ItemDropBox(Replies.text(player, "item.box.title")).getInventory());
        Replies.send(player, "item.box.opened");
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    /**
     * @param plugin the plugin whose registry the items go into
     * @return the listener registering a box's items when it closes
     */
    public static Listener closing(BloodNight plugin) {
        return new Listener() {
            @EventHandler
            public void onClose(InventoryCloseEvent event) {
                if (!(event.getInventory().getHolder(false) instanceof ItemDropBox)) return;
                if (!(event.getPlayer() instanceof Player player)) return;
                List<ItemStack> items = Arrays.stream(event.getInventory().getContents())
                        .filter(Objects::nonNull)
                        .filter(item -> !item.isEmpty())
                        .map(ItemStack::clone)
                        .toList();
                event.getInventory().clear();
                giveBack(player, items);
                if (items.isEmpty()) {
                    Replies.send(player, "item.box.empty");
                    return;
                }
                plugin.tasks().globalNow(() -> register(plugin, player, items));
            }
        };
    }

    static void giveBack(Player player, List<ItemStack> items) {
        player.getInventory()
                .addItem(items.stream().map(ItemStack::clone).toArray(ItemStack[]::new))
                .values()
                .forEach(left -> player.getWorld().dropItemNaturally(player.getLocation(), left));
    }

    private static void register(BloodNight plugin, Player player, List<ItemStack> items) {
        ItemRegistry registry = ItemCommand.copy(plugin.configuration().items());
        Map<String, Boolean> ids = registerAll(registry, items);
        plugin.configuration().replaceItems(registry);
        List<String> fresh = new ArrayList<>();
        ids.forEach((id, known) -> {
            if (!known) fresh.add(id);
        });
        Replies.send(
                player,
                "item.box.registered",
                Replies.arg("count", fresh.size()),
                Replies.arg("known", ids.size() - fresh.size()));
        ids.forEach((id, known) -> Replies.send(
                player, known ? "item.box.known" : "item.box.new", Replies.arg("id", copyable(player, id))));
    }

    /**
     * @param registry the registry the items go into
     * @param items    the items, possibly the same one more than once
     * @return each item's id once, in the items' order, with whether it was registered before
     */
    static Map<String, Boolean> registerAll(ItemRegistry registry, List<ItemStack> items) {
        Map<String, Boolean> ids = new LinkedHashMap<>();
        for (ItemStack item : items) {
            boolean known = registry.find(item) != null;
            ids.putIfAbsent(registry.register(item), known);
        }
        return ids;
    }

    static Component copyable(Player player, String id) {
        return Component.text(id, NamedTextColor.GOLD)
                .clickEvent(ClickEvent.copyToClipboard(id))
                .hoverEvent(Replies.text(player, "item.copy"));
    }
}
