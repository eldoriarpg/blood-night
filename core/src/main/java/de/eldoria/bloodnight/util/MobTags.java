package de.eldoria.bloodnight.util;

import de.eldoria.bloodnight.mob.CustomMob;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;

public class MobTags {
    /**
     * Marks that an {@link Entity} is a {@link CustomMob}.
     * This tag is solely for existence checks and should use a {@link PersistentDataType#BYTE}.
     * The value of the byte doesn't matter.
     * If this tag is set on the mob with the BYTE type, it is always considered a custom mob.
     */
    public static final NamespacedKey CUSTOM_MOB = new NamespacedKey("bloodnight", "custom_mob");
    /**
     * Marks that a mob is an extension of a custom mob.
     * This tag should use a {@link PersistentDataType#INTEGER} containing the {@link Entity#getEntityId()} of the entity that the mob has, that is extended.
     */
    public static final NamespacedKey EXTENDS = new NamespacedKey("bloodnight", "extends");
    /**
     * Marks that a mob is extended and has another mob attached to it.
     * This tag should use a {@link PersistentDataType#INTEGER} containing the {@link Entity#getEntityId()} of the entity that extends this mob.
     */
    public static final NamespacedKey EXTENDED = new NamespacedKey("bloodnight", "extended");
}
