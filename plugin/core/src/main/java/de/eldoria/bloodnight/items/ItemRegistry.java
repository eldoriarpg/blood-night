/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.items;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * The items mobs and graphs refer to by id: items registered from the game, kept with all their data under a hash id,
 * and items made in the editor, kept as {@link ItemDefinition}s under readable ids. An id that names neither is read as
 * a material key such as {@code minecraft:diamond_helmet}, giving a plain item of that material.
 */
public class ItemRegistry {
    @JsonProperty
    private final Map<String, ItemStack> registrations;

    @JsonProperty
    private final Map<String, ItemDefinition> definitions;

    @JsonCreator
    public ItemRegistry(
            @JsonProperty("registrations") @Nullable Map<String, ItemStack> registrations,
            @JsonProperty("definitions") @Nullable Map<String, ItemDefinition> definitions) {
        this.registrations = new HashMap<>(Objects.requireNonNullElse(registrations, Map.of()));
        this.definitions = new TreeMap<>(Objects.requireNonNullElse(definitions, Map.of()));
    }

    public ItemRegistry() {
        this(null, null);
    }

    /**
     * @param id an item's id or a material key
     * @return a copy of the item, or null when the id names nothing
     */
    public @Nullable ItemStack item(String id) {
        ItemStack registered = registrations.get(id);
        if (registered != null) return registered.clone();
        ItemDefinition definition = definitions.get(id);
        if (definition != null) return ItemTexts.build(definition);
        Material material = Material.matchMaterial(id);
        return material != null && material.isItem() ? ItemStack.of(material) : null;
    }

    /**
     * @param id an item's id or a material key
     * @return whether it names an item
     */
    public boolean knows(String id) {
        return registrations.containsKey(id) || definitions.containsKey(id) || item(id) != null;
    }

    public Map<String, ItemDefinition> definitions() {
        return Collections.unmodifiableMap(definitions);
    }

    public Map<String, ItemStack> registrations() {
        return Collections.unmodifiableMap(registrations);
    }

    /**
     * Keeps a copy of an item from the game, with all its data, as a single item.
     *
     * @param stack the item
     * @return its generated id; the same item always gets the same id
     */
    public String register(ItemStack stack) {
        ItemStack single = stack.clone();
        single.setAmount(1);
        String id = Integer.toHexString(single.hashCode());
        registrations.put(id, single);
        return id;
    }

    /**
     * @param id the id of an item registered from the game
     * @return whether there was such an item; definitions and material keys are left alone
     */
    public boolean unregister(String id) {
        return registrations.remove(id) != null;
    }
}
