/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor;

import de.eldoria.bloodnight.items.ItemDefinition;
import de.eldoria.bloodnight.items.ItemRegistry;
import de.eldoria.bloodnight.items.ItemTexts;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * Applies the items of an editor result to the item registry: items made in the editor replace the definitions;
 * registered items take the name, lore and enchantments the editor gave them and keep everything else. Registered
 * items are never removed or created by the editor.
 */
public final class EditorItems {
    private EditorItems() {}

    /**
     * @param current the registry now
     * @param edited the items of the result by id
     * @return the registry to write
     */
    public static ItemRegistry apply(ItemRegistry current, Map<String, EditorItem> edited) {
        Map<String, ItemStack> registrations = new HashMap<>();
        current.registrations().forEach((id, stored) -> {
            ItemStack item = stored.clone();
            EditorItem edit = edited.get(id);
            if (edit != null && edit.kind() == ItemKind.REGISTERED)
                ItemTexts.apply(item, edit.name(), edit.lore(), edit.enchantments());
            registrations.put(id, item);
        });
        Map<String, ItemDefinition> definitions = new TreeMap<>();
        edited.forEach((id, item) -> {
            if (item.kind() == ItemKind.DEFINED && !registrations.containsKey(id))
                definitions.put(id, item.definition());
        });
        return new ItemRegistry(registrations, definitions);
    }
}
