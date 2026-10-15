/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mobs.exceptions.MobIdAlreadyTakenException;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MobRegistry {
    @JsonProperty
    private final Map<String, CustomMob> mobs;

    @JsonCreator
    public MobRegistry(@JsonProperty("mobs") Map<String, CustomMob> mobs) {
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
        Objects.requireNonNull(mob, "Mob can not be null");

        if (mobs.containsKey(mob.id())) throw new MobIdAlreadyTakenException(mob, get(mob.id()));
        mobs.put(mob.id(), mob);
    }

    /**
     * @return every registered mob
     */
    public Collection<CustomMob> all() {
        return Collections.unmodifiableCollection(mobs.values());
    }

    /**
     * Returns the {@link CustomMob} associated with the given id.
     *
     * @param id the id of the mob to be retrieved.
     * @return the {@link CustomMob} associated with the given id, or null if no mob is found.
     */
    public CustomMob get(String id) {
        return mobs.get(id);
    }
}
