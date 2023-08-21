package de.eldoria.bloodnight.mobs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.meta.Attributes;
import de.eldoria.bloodnight.mobs.exceptions.MobIdAlreadyTakenException;
import de.eldoria.bloodnight.util.Checks;
import org.bukkit.entity.EntityType;

import javax.swing.text.html.Option;
import java.util.*;

public class MobRegistry {
    @JsonProperty
    private final Map<String, CustomMob> mobs;

    @JsonCreator
    public MobRegistry(Map<String, CustomMob> mobs) {
        this.mobs = mobs;
    }

    public MobRegistry() {
        this.mobs = new HashMap<>();
    }

    /**
     * Registers a new {@link CustomMob} at the registry.
     *
     * @param mob the mob to be registered.
     * @throws MobIdAlreadyTakenException when the id is already taken.
     */
    public void add(CustomMob mob) {
        Checks.notNull(mob, "Mob can not be null");

        var previousMob = mobs.putIfAbsent(mob.id(), mob);
        if (previousMob != null) {
            throw new MobIdAlreadyTakenException(mob, previousMob);
        }
    }

    /**
     * Gets all matching {@link CustomMob}s, depending on the result of calling {@link Attributes#isAssignable(EntityType)}.
     *
     * @param active a set containing the ids of active mobs that should be checked
     * @param type   the entityType of the entity
     * @return an immutable list containing all mobs that are matching. May be empty.
     */
    public List<CustomMob> getMatching(Set<String> active, EntityType type) {
        return active.stream().map(mobs::get).filter(mob -> mob.attributes().isAssignable(type)).toList();
    }
}
