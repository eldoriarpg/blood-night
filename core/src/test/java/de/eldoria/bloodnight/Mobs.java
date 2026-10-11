package de.eldoria.bloodnight;

import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.meta.Attributes;
import de.eldoria.bloodnight.mob.meta.Equipment;
import de.eldoria.bloodnight.mob.meta.Extension;
import de.eldoria.bloodnight.mob.meta.ExtensionType;
import de.eldoria.bloodnight.mob.meta.ValueModifier;
import de.eldoria.graph.model.Graph;
import org.bukkit.entity.EntityType;

/**
 * Mob definitions for tests.
 */
public final class Mobs {
    private Mobs() {
    }

    public static CustomMob zombie(String id, Graph behaviour) {
        return new CustomMob(id,
                new Equipment(null, null, null, null, null, null),
                new Attributes(id, EntityType.ZOMBIE, false, ValueModifier.DEFAULT, 20, ValueModifier.DEFAULT, 1),
                behaviour, null, new Extension(ExtensionType.NONE, null, null));
    }
}
