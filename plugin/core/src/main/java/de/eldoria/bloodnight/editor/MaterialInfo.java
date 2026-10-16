/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor;

import com.google.common.collect.Multimap;
import de.eldoria.bloodnight.items.ItemAttribute;
import de.eldoria.bloodnight.items.ItemTexts;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Function;

/**
 * What the editor needs to know about a material to offer only the settings that fit it.
 *
 * @param stackSize  how many fit in one stack
 * @param durability how many uses it lasts; 0 for one that does not wear out
 * @param dyeable    whether it takes a dye colour, like leather armour
 * @param trimmable  whether it takes an armour trim
 * @param attributes the attribute modifiers it gives by itself, e.g. a sword's attack damage
 */
public record MaterialInfo(
        int stackSize, int durability, boolean dyeable, boolean trimmable, List<ItemAttribute> attributes) {
    private static final int STACK = 64;

    public MaterialInfo {
        attributes = List.copyOf(attributes);
    }

    /**
     * @param material a material that is an item
     * @return what the editor needs to know about it, none when it is a plain item: a full stack, no durability, no
     *     dye, no trim and no attributes of its own
     */
    public static @Nullable MaterialInfo of(Material material) {
        return of(material, Material::getDefaultAttributeModifiers);
    }

    /**
     * @param material a material that is an item
     * @param defaults the attribute modifiers a material gives by itself
     * @return what the editor needs to know about it, none when it is a plain item
     */
    static @Nullable MaterialInfo of(
            Material material, Function<Material, Multimap<Attribute, AttributeModifier>> defaults) {
        MaterialInfo info = new MaterialInfo(
                material.getMaxStackSize(),
                material.getMaxDurability(),
                ItemStack.of(material).getItemMeta() instanceof LeatherArmorMeta,
                Tag.ITEMS_TRIMMABLE_ARMOR.isTagged(material),
                ItemTexts.rows(defaults.apply(material)));
        boolean plain = info.stackSize == STACK
                && info.durability == 0
                && !info.dyeable
                && !info.trimmable
                && info.attributes.isEmpty();
        return plain ? null : info;
    }
}
