/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.items;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import de.eldoria.bloodnight.items.look.ItemAttribute;
import de.eldoria.bloodnight.items.look.ItemSlot;
import de.eldoria.bloodnight.items.look.ItemTrim;
import de.eldoria.bloodnight.items.look.TooltipPart;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Color;
import org.bukkit.Keyed;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import org.bukkit.inventory.meta.trim.TrimMaterial;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Writes what the editor sets on an item onto an item stack, and reads it back: name and lore as MiniMessage,
 * enchantments, glint, model, durability, dye colour, armour trim, attribute modifiers and the hidden tooltip parts.
 * What does not fit the item, such as a trim on a sword, is left out, and so are unknown keys. An enchanted book stores
 * its enchantments, so it works in an anvil like a book from an enchanting table.
 */
public final class ItemTexts {
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final String MODIFIER_NAMESPACE = "bloodnight";

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
     * Gives an item everything a definition sets, keeping what else the item carries, its material included.
     *
     * @param item the item, changed in place
     * @param look what to set; an unset model, model data, wear, colour or trim takes the item's away, no attribute
     *             modifiers give it back the material's own
     */
    public static void apply(ItemStack item, ItemDefinition look) {
        int maxDurability = maxDurability(item);
        item.editMeta(meta -> {
            String name = look.name();
            meta.displayName(name == null || name.isBlank() ? null : text(name));
            meta.lore(
                    look.lore().isEmpty()
                            ? null
                            : look.lore().stream().map(ItemTexts::text).toList());
            enchant(meta, look.enchantments());
            meta.setEnchantmentGlintOverride(look.glint());
            String model = look.model();
            meta.setItemModel(model == null || model.isBlank() ? null : NamespacedKey.fromString(model));
            meta.setCustomModelDataComponent(modelData(meta, look.modelData()));
            meta.setUnbreakable(look.unbreakable());
            Double wear = look.wear();
            if (meta instanceof Damageable damageable && maxDurability > 0) {
                damageable.setDamage(wear == null ? 0 : damage(wear, maxDurability));
            }
            if (meta instanceof LeatherArmorMeta leather) leather.setColor(color(look.color()));
            if (meta instanceof ArmorMeta armor) armor.setTrim(trim(look.trim()));
            meta.setAttributeModifiers(look.attributes().isEmpty() ? null : modifiers(look.attributes()));
            for (TooltipPart part : TooltipPart.values()) {
                if (look.hidden().contains(part)) meta.addItemFlags(part.flag());
                else meta.removeItemFlags(part.flag());
            }
        });
    }

    /**
     * Sets an item's enchantments, replacing the ones it had. An enchanted book stores them, as books from an enchanting
     * table do, so an anvil can put them onto gear; any other item carries them itself.
     *
     * @param enchantments levels by enchantment key; unknown keys are left out
     */
    private static void enchant(ItemMeta meta, Map<String, Integer> enchantments) {
        if (meta instanceof EnchantmentStorageMeta book) {
            book.getStoredEnchants().keySet().forEach(book::removeStoredEnchant);
        }
        meta.getEnchants().keySet().forEach(meta::removeEnchant);
        enchantments.forEach((key, level) -> {
            Enchantment enchantment = registered(RegistryKey.ENCHANTMENT, key);
            if (enchantment == null) return;
            if (meta instanceof EnchantmentStorageMeta book)
                book.addStoredEnchant(enchantment, Math.max(level, 1), true);
            else meta.addEnchant(enchantment, Math.max(level, 1), true);
        });
    }

    /**
     * @param item an item
     * @return everything the editor shows of it
     */
    public static ItemDefinition read(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        String material = item.getType().getKey().asString();
        if (meta == null) return ItemDefinition.of(material);
        Component name = meta.displayName();
        List<Component> lore = meta.lore();
        Map<String, Integer> enchantments = new TreeMap<>();
        Map<Enchantment, Integer> held =
                meta instanceof EnchantmentStorageMeta book ? book.getStoredEnchants() : item.getEnchantments();
        held.forEach(
                (enchantment, level) -> enchantments.put(enchantment.getKey().asString(), level));
        NamespacedKey model = meta.hasItemModel() ? meta.getItemModel() : null;
        int maxDurability = maxDurability(item);
        Double wear = meta instanceof Damageable damageable && damageable.hasDamage() && maxDurability > 0
                ? (double) damageable.getDamage() / maxDurability
                : null;
        String color = meta instanceof LeatherArmorMeta leather && leather.isDyed()
                ? "#" + HexFormat.of().toHexDigits(leather.getColor().asRGB()).substring(2)
                : null;
        ArmorTrim armorTrim = meta instanceof ArmorMeta armor && armor.hasTrim() ? armor.getTrim() : null;
        Set<TooltipPart> hidden = EnumSet.noneOf(TooltipPart.class);
        for (TooltipPart part : TooltipPart.values()) {
            if (meta.hasItemFlag(part.flag())) hidden.add(part);
        }
        return new ItemDefinition(
                material,
                name == null ? null : MINI_MESSAGE.serialize(name),
                lore == null
                        ? List.of()
                        : lore.stream().map(MINI_MESSAGE::serialize).toList(),
                enchantments,
                meta.hasEnchantmentGlintOverride() ? meta.getEnchantmentGlintOverride() : null,
                model == null ? null : model.asString(),
                modelNumber(meta),
                meta.isUnbreakable(),
                wear,
                color,
                armorTrim == null ? null : trim(armorTrim),
                meta.hasAttributeModifiers() ? rows(meta.getAttributeModifiers()) : List.of(),
                hidden);
    }

    /**
     * @param modifiers attribute modifiers by attribute
     * @return them as rows, sorted by attribute and slot
     */
    public static List<ItemAttribute> rows(@Nullable Multimap<Attribute, AttributeModifier> modifiers) {
        List<ItemAttribute> rows = new ArrayList<>();
        if (modifiers == null) return rows;
        modifiers.forEach((attribute, modifier) -> rows.add(new ItemAttribute(
                attribute.getKey().asString(),
                modifier.getAmount(),
                modifier.getOperation(),
                ItemSlot.of(modifier.getSlotGroup()))));
        rows.sort((a, b) -> a.attribute().equals(b.attribute())
                ? a.slot().compareTo(b.slot())
                : a.attribute().compareTo(b.attribute()));
        return rows;
    }

    private static Multimap<Attribute, AttributeModifier> modifiers(List<ItemAttribute> rows) {
        Multimap<Attribute, AttributeModifier> modifiers = ArrayListMultimap.create();
        for (int index = 0; index < rows.size(); index++) {
            ItemAttribute row = rows.get(index);
            Attribute attribute = registered(RegistryKey.ATTRIBUTE, row.attribute());
            if (attribute == null) continue;
            NamespacedKey key = new NamespacedKey(MODIFIER_NAMESPACE, "attribute_" + index);
            modifiers.put(
                    attribute,
                    new AttributeModifier(
                            key, row.amount(), row.operation(), row.slot().group()));
        }
        return modifiers;
    }

    /**
     * @param item an item, changed in place
     * @param wear how worn it is, a share of its durability from 0 (new) to 1; nothing changes for an item that does
     *             not wear out
     */
    public static void wear(ItemStack item, double wear) {
        int maxDurability = maxDurability(item);
        if (maxDurability <= 0) return;
        item.editMeta(Damageable.class, damageable -> damageable.setDamage(damage(wear, maxDurability)));
    }

    /**
     * The custom model data with the number as its first float, the way a number was stored before the component
     * existed; the other values the item carries stay. No number removes the custom model data.
     */
    private static @Nullable CustomModelDataComponent modelData(ItemMeta meta, @Nullable Integer number) {
        if (number == null) return null;
        CustomModelDataComponent data = meta.getCustomModelDataComponent();
        List<Float> floats = new ArrayList<>(data.getFloats());
        if (floats.isEmpty()) floats.add(number.floatValue());
        else floats.set(0, number.floatValue());
        data.setFloats(floats);
        return data;
    }

    private static @Nullable Integer modelNumber(ItemMeta meta) {
        if (!meta.hasCustomModelDataComponent()) return null;
        List<Float> floats = meta.getCustomModelDataComponent().getFloats();
        return floats.isEmpty() ? null : floats.getFirst().intValue();
    }

    private static int maxDurability(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta instanceof Damageable damageable && damageable.hasMaxDamage()) return damageable.getMaxDamage();
        return item.getType().getMaxDurability();
    }

    private static int damage(double wear, int maxDurability) {
        double share = Math.clamp(wear, 0, 1);
        return Math.min(maxDurability - 1, (int) Math.round(share * maxDurability));
    }

    private static @Nullable Color color(@Nullable String hex) {
        if (hex == null || !hex.matches("#[0-9a-fA-F]{6}")) return null;
        return Color.fromRGB(HexFormat.fromHexDigits(hex.substring(1)));
    }

    private static @Nullable ArmorTrim trim(@Nullable ItemTrim trim) {
        if (trim == null) return null;
        TrimMaterial material = registered(RegistryKey.TRIM_MATERIAL, trim.material());
        TrimPattern pattern = registered(RegistryKey.TRIM_PATTERN, trim.pattern());
        return material == null || pattern == null ? null : new ArmorTrim(material, pattern);
    }

    private static @Nullable ItemTrim trim(ArmorTrim trim) {
        NamespacedKey pattern = registryKey(RegistryKey.TRIM_PATTERN, trim.getPattern());
        NamespacedKey material = registryKey(RegistryKey.TRIM_MATERIAL, trim.getMaterial());
        return pattern == null || material == null ? null : new ItemTrim(pattern.asString(), material.asString());
    }

    private static <T extends Keyed> @Nullable T registered(RegistryKey<T> registry, String key) {
        NamespacedKey namespaced = NamespacedKey.fromString(key.toLowerCase(Locale.ROOT));
        if (namespaced == null) return null;
        return RegistryAccess.registryAccess().getRegistry(registry).get(namespaced);
    }

    private static <T extends Keyed> @Nullable NamespacedKey registryKey(RegistryKey<T> registry, T value) {
        Registry<T> entries = RegistryAccess.registryAccess().getRegistry(registry);
        return entries.getKey(value);
    }

    private static Component text(String miniMessage) {
        return MINI_MESSAGE
                .deserialize(miniMessage)
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }
}
