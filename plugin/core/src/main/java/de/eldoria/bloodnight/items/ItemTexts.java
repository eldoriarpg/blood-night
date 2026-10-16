/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.items;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Reads and writes the parts of an item the editor edits: its name and lore as MiniMessage, and its enchantments by
 * key. Names and lore written here are not italic unless the MiniMessage says so, as in the game's own item editors.
 */
public final class ItemTexts {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();

    private ItemTexts() {}

    /**
     * @param definition an item made in the editor
     * @return the item, or null when its material is no item
     */
    public static @Nullable ItemStack build(ItemDefinition definition) {
        Material material = Material.matchMaterial(definition.material());
        if (material == null || !material.isItem()) return null;
        ItemStack item = ItemStack.of(material);
        apply(item, definition);
        return item;
    }

    /**
     * Gives an item the name, lore and enchantments of a definition and shows them as it says, keeping everything else
     * the item carries, its material included.
     *
     * @param item the item, changed in place
     * @param look the name (none removes a custom name), lore, enchantments (unknown keys are left out), glint and
     *             whether the tooltip hides the enchantments
     */
    public static void apply(ItemStack item, ItemDefinition look) {
        item.editMeta(meta -> {
            String name = look.name();
            meta.displayName(name == null || name.isBlank() ? null : text(name));
            meta.lore(
                    look.lore().isEmpty()
                            ? null
                            : look.lore().stream().map(ItemTexts::text).toList());
            meta.getEnchants().keySet().forEach(meta::removeEnchant);
            look.enchantments().forEach((key, level) -> {
                Enchantment enchantment = enchantment(key);
                if (enchantment != null) meta.addEnchant(enchantment, Math.max(level, 1), true);
            });
            meta.setEnchantmentGlintOverride(look.glint());
            if (look.hideEnchantments()) meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            else meta.removeItemFlags(ItemFlag.HIDE_ENCHANTS);
        });
    }

    /**
     * @param item an item
     * @return whether it always or never glints, none when the game decides by its enchantments
     */
    public static @Nullable Boolean glint(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasEnchantmentGlintOverride() ? meta.getEnchantmentGlintOverride() : null;
    }

    /**
     * @param item an item
     * @return whether its tooltip leaves out the enchantments
     */
    public static boolean hidesEnchantments(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return meta != null && meta.hasItemFlag(ItemFlag.HIDE_ENCHANTS);
    }

    /**
     * @param item an item
     * @return its custom name as MiniMessage, or null when it has none
     */
    public static @Nullable String name(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        Component name = meta == null ? null : meta.displayName();
        return name == null ? null : MINI_MESSAGE.serialize(name);
    }

    /**
     * @param item an item
     * @return its lore lines as MiniMessage
     */
    public static List<String> lore(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        List<Component> lore = meta == null ? null : meta.lore();
        return lore == null
                ? List.of()
                : lore.stream().map(MINI_MESSAGE::serialize).toList();
    }

    /**
     * @param item an item
     * @return its enchantment levels by key, sorted
     */
    public static Map<String, Integer> enchantments(ItemStack item) {
        Map<String, Integer> levels = new TreeMap<>();
        item.getEnchantments()
                .forEach((enchantment, level) -> levels.put(enchantment.getKey().asString(), level));
        return levels;
    }

    private static Component text(String miniMessage) {
        return MINI_MESSAGE
                .deserialize(miniMessage)
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    private static @Nullable Enchantment enchantment(String key) {
        NamespacedKey namespaced = NamespacedKey.fromString(key);
        if (namespaced == null) return null;
        return RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.ENCHANTMENT)
                .get(namespaced);
    }
}
