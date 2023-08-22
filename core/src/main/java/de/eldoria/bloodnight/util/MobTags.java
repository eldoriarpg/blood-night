package de.eldoria.bloodnight.util;

import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.eldoutilities.utils.DataContainerUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Utility class for managing tags related to custom mobs.
 */
public final class MobTags {
    private static final String NAMESPACE = "bloodnight";

    private MobTags() {
        throw new UnsupportedOperationException("This is a utility class.");
    }


    private static NamespacedKey create(String key) {
        return new NamespacedKey(NAMESPACE, key);
    }

    /**
     * Marks that an {@link Entity} is a {@link CustomMob}.
     * This tag should use a {@link PersistentDataType#STRING} and save the {@link CustomMob#id()}.
     */
    public static final NamespacedKey CUSTOM_MOB = create("custom_mob");
    /**
     * Marks that a mob is an extension of a custom mob.
     * This tag should use a {@link PersistentDataType#STRING} containing the {@link Entity#getUniqueId()} of the entity that the mob has, that is extended.
     */
    public static final NamespacedKey EXTENDS = create("extends");
    /**
     * Marks that a mob is extended and has another mob attached to it.
     * This tag should use a {@link PersistentDataType#STRING} containing the {@link Entity#getUniqueId()} of the entity that extends this mob.
     */
    public static final NamespacedKey EXTENDED = create("extended");

    /**
     * Checks if the specified entity is a custom mob.
     *
     * @param entity the entity to check
     * @return true if the entity is a custom mob, false otherwise
     */
    public static boolean isCustomMob(Entity entity) {
        return DataContainerUtil.hasKey(entity, CUSTOM_MOB, PersistentDataType.STRING);
    }

    /**
     * Retrieves the {@link CustomMob#id()} associated with the given entity.
     *
     * @param entity the entity to retrieve the custom mob ID from
     * @return an optional containing the custom mob ID, or an empty optional if no custom mob ID is found
     */
    public static Optional<String> getCustomMobId(Entity entity) {
        return DataContainerUtil.get(entity, CUSTOM_MOB, PersistentDataType.STRING);
    }

    /**
     * Determines if the given entity is an extension.
     *
     * @param entity the entity to check for an extension
     * @return {@code true} if the entity has an extension, {@code false} otherwise
     */
    public static boolean isExtension(Entity entity) {
        return DataContainerUtil.hasKey(entity, EXTENDS, PersistentDataType.STRING);
    }

    /**
     * Checks if the given entity is extended.
     *
     * @param entity the entity to check
     * @return true if the entity is extended, false otherwise
     */
    public static boolean isExtended(Entity entity) {
        return DataContainerUtil.hasKey(entity, EXTENDED, PersistentDataType.STRING);
    }

    /**
     * Marks an Entity as a custom mob by adding a unique identifier to its data.
     *
     * @param entity The Entity to mark as a custom mob.
     */
    public static void markCustomMob(Entity entity, CustomMob mob) {
        DataContainerUtil.putValue(entity, CUSTOM_MOB, PersistentDataType.STRING, mob.id());
    }

    /**
     * Marks the given extension entity with the UUID of the base entity.
     *
     * @param extension The entity to mark as an extension.
     * @param base      The base entity that the extension is extending.
     */
    public static void markExtension(Entity extension, Entity base) {
        DataContainerUtil.putValue(extension, EXTENDS, PersistentDataType.STRING, base.getUniqueId().toString());
    }

    /**
     * Marks the base entity with the extension entity.
     *
     * @param base      The base entity to be marked.
     * @param extension The extension entity to mark the base entity with.
     */
    public static void markBase(Entity base, Entity extension) {
        DataContainerUtil.putValue(base, EXTENDED, PersistentDataType.STRING, extension.getUniqueId().toString());
    }

    /**
     * Marks an extended mob by establishing a connection between a base entity and an extension entity.
     *
     * @param base      The base entity to be marked.
     * @param extension The extension entity to be marked.
     */
    public static void markExtendedMob(Entity base, Entity extension) {
        markBase(base, extension);
        markExtension(extension, base);
    }

    /**
     * Sets up tags for a custom mob by establishing connections between the entities and tag the mob with its id.
     *
     * @param base      The base entity to be tagged.
     * @param extension The extension entity to be tagged, can be null.
     * @param mob       The custom mob that is represented by those entities.
     */
    public static void setupTags(Entity base, @Nullable Entity extension, CustomMob mob) {
        markCustomMob(base, mob);
        if (extension != null) {
            markCustomMob(extension, mob);
            markBase(base, extension);
            markExtension(extension, base);
        }
    }


    /**
     * Returns the extension of the given entity as an Optional UUID.
     *
     * @param entity The entity to retrieve the extension from
     * @return An Optional instance containing the extension UUID if found,
     * or an empty Optional if no extension is present
     */
    public static Optional<UUID> getExtension(Entity entity) {
        return DataContainerUtil.get(entity, EXTENDED, PersistentDataType.STRING).map(UUID::fromString);
    }

    /**
     * Retrieves the base UUID for the given entity.
     *
     * @param entity the entity to retrieve the base UUID for
     * @return an Optional containing the base UUID if it exists, otherwise an empty Optional
     */
    public static Optional<UUID> getBase(Entity entity) {
        return DataContainerUtil.get(entity, EXTENDS, PersistentDataType.STRING).map(UUID::fromString);
    }
}
