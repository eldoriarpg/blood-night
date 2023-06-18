package de.eldoria.bloodnight.mobs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.eldoria.bloodnight.mob.CustomMob;
import org.bukkit.entity.EntityType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    public List<CustomMob> getMatching(Set<String> active, EntityType type) {
        return active.stream().map(mobs::get).filter(mob -> mob.attributes().isAssignable(type)).toList();
    }
}
