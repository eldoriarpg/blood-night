package de.eldoria.bloodnight.mob.meta;

import org.bukkit.entity.EntityType;

public record Extension(ExtensionType type, EntityType entityType, Equipment equipment) {
}
