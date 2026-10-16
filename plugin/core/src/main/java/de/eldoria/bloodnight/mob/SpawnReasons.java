/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob;

import io.papermc.paper.entity.Bucketable;
import org.bukkit.entity.AbstractCubeMob;
import org.bukkit.entity.AbstractHorse;
import org.bukkit.entity.AbstractSkeleton;
import org.bukkit.entity.Allay;
import org.bukkit.entity.Bee;
import org.bukkit.entity.Breedable;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.CopperGolem;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Creaking;
import org.bukkit.entity.Drowned;
import org.bukkit.entity.Endermite;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Frog;
import org.bukkit.entity.HappyGhast;
import org.bukkit.entity.Hoglin;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.Ocelot;
import org.bukkit.entity.Parrot;
import org.bukkit.entity.PigZombie;
import org.bukkit.entity.Piglin;
import org.bukkit.entity.Pillager;
import org.bukkit.entity.Raider;
import org.bukkit.entity.Silverfish;
import org.bukkit.entity.SkeletonHorse;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Spider;
import org.bukkit.entity.Stray;
import org.bukkit.entity.Strider;
import org.bukkit.entity.Vex;
import org.bukkit.entity.Villager;
import org.bukkit.entity.Witch;
import org.bukkit.entity.Wither;
import org.bukkit.entity.Zombie;
import org.bukkit.entity.ZombieVillager;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The spawn reasons a mob type can spawn by, so spawn rules only offer those: a slime splits, an animal is bred, a
 * zombie calls reinforcements, but a creeper does neither. Reasons any mob can spawn by, such as natural spawning, a
 * spawner or a command, apply to every type; reasons that never bring a mob, such as an explosion, apply to none.
 */
public final class SpawnReasons {
    private SpawnReasons() {}

    /**
     * @param type an entity type
     * @return whether an entity of the type can spawn by the reason
     */
    public static boolean applies(SpawnReason reason, EntityType type) {
        Class<?> entity = type.getEntityClass();
        if (entity == null) return false;
        return switch (reason) {
            case NATURAL, CHUNK_GEN, SPAWNER, TRIAL_SPAWNER, SPAWNER_EGG, DISPENSE_EGG, COMMAND, CUSTOM, DEFAULT ->
                true;
            case EXPLOSION, ENCHANTMENT, OMINOUS_ITEM_SPAWNER -> false;
            case BREEDING -> is(entity, Breedable.class);
            case SLIME_SPLIT -> is(entity, AbstractCubeMob.class);
            case EGG -> is(entity, Chicken.class);
            case JOCKEY -> is(entity, AbstractSkeleton.class, Zombie.class, Spider.class, Chicken.class, Piglin.class);
            case MOUNT -> is(entity, Chicken.class, Spider.class, Strider.class, Hoglin.class, AbstractHorse.class);
            case TRAP -> is(entity, SkeletonHorse.class, AbstractSkeleton.class);
            case LIGHTNING -> is(entity, Witch.class, PigZombie.class);
            case BUILD_SNOWMAN -> is(entity, Snowman.class);
            case BUILD_IRONGOLEM, VILLAGE_DEFENSE -> is(entity, IronGolem.class);
            case BUILD_COPPERGOLEM -> is(entity, CopperGolem.class);
            case BUILD_WITHER -> is(entity, Wither.class);
            case VILLAGE_INVASION, REINFORCEMENTS -> is(entity, Zombie.class);
            case NETHER_PORTAL, PIGLIN_ZOMBIFIED -> is(entity, PigZombie.class);
            case INFECTION -> is(entity, ZombieVillager.class);
            case CURED -> is(entity, Villager.class);
            case OCELOT_BABY -> is(entity, Ocelot.class);
            case SILVERFISH_BLOCK -> is(entity, Silverfish.class);
            case ENDER_PEARL -> is(entity, Endermite.class);
            case SHOULDER_ENTITY -> is(entity, Parrot.class);
            case DROWNED -> is(entity, Drowned.class);
            case SHEARED -> is(entity, Cow.class);
            case RAID -> is(entity, Raider.class);
            case PATROL -> is(entity, Pillager.class);
            case BEEHIVE -> is(entity, Bee.class);
            case SPELL -> is(entity, Vex.class);
            case FROZEN -> is(entity, Stray.class);
            case METAMORPHOSIS -> is(entity, Frog.class);
            case DUPLICATION -> is(entity, Allay.class);
            case BUCKET -> is(entity, Bucketable.class);
            case POTION_EFFECT -> is(entity, Silverfish.class, Slime.class);
            case REANIMATE -> is(entity, Creaking.class);
            case REHYDRATION -> is(entity, HappyGhast.class);
        };
    }

    /**
     * @param types entity types by name
     * @return for each, the names of the spawn reasons it can spawn by, in the enum's order
     */
    public static Map<String, List<String>> byType(List<String> types) {
        Map<String, List<String>> reasons = new TreeMap<>();
        for (String name : types) {
            EntityType type = EntityType.valueOf(name);
            reasons.put(
                    name,
                    Arrays.stream(SpawnReason.values())
                            .filter(reason -> applies(reason, type))
                            .map(SpawnReason::name)
                            .toList());
        }
        return reasons;
    }

    private static boolean is(Class<?> entity, Class<?>... kinds) {
        return Arrays.stream(kinds).anyMatch(kind -> kind.isAssignableFrom(entity));
    }
}
