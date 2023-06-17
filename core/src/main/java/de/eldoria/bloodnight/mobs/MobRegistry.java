package de.eldoria.bloodnight.mob;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashMap;
import java.util.Map;

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

    public void add(CustomMob mob) {
        mobs.put(mob.id(), mob);
    }
}
