/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.defaults;

import de.eldoria.bloodnight.graph.BloodNightNodes;
import de.eldoria.bloodnight.graph.runtime.ProjectileKind;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.meta.Attributes;
import de.eldoria.bloodnight.mob.meta.Despawn;
import de.eldoria.bloodnight.mob.meta.Equipment;
import de.eldoria.bloodnight.mob.meta.Extension;
import de.eldoria.bloodnight.mob.meta.ExtensionType;
import de.eldoria.bloodnight.mob.meta.MobDrops;
import de.eldoria.bloodnight.mob.meta.SpawnConditions;
import de.eldoria.bloodnight.mob.meta.SpawnRules;
import de.eldoria.bloodnight.mob.meta.ValueModifier;
import de.eldoria.graph.compiler.Graphs;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.platform.GraphPlatform;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * The special mobs shipped with the plugin, written to {@code mobs.yml} on first start.
 * <p>
 * Each mob is a node graph, so every one of them can be opened and changed in the editor. Effects are refreshed every
 * 30 seconds with a duration of 60, particles spawn four times a second, and anything a mob does on its own is spaced
 * by a cooldown or a named timer, since behaviours tick every server tick.
 */
public final class DefaultMobs implements BloodNightNodes {
    private static final String RESULT = "result";
    private static final String TARGET = "target";
    private static final String VALUE = "value";
    private static final String TELEPORT_NEAR = "teleport_near";

    /**
     * Id of the blood dragon, the ender dragon of a dragon fight during a blood night.
     */
    public static final String BLOOD_DRAGON = "blood_dragon";

    private final GraphPlatform platform;

    private DefaultMobs(GraphPlatform platform) {
        this.platform = platform;
    }

    /**
     * @param platform the node set the behaviours are built and checked against
     * @return every default mob
     * @throws IllegalArgumentException when a behaviour does not fit the platform
     */
    public static List<CustomMob> create(GraphPlatform platform) {
        DefaultMobs mobs = new DefaultMobs(platform);
        return List.of(
                mobs.armoredZombie(),
                mobs.knightZombie(),
                mobs.invisibleZombie(),
                mobs.speedZombie(),
                mobs.invisibleSkeleton(),
                mobs.magicSkeleton(),
                mobs.speedCreeper(),
                mobs.toxicCreeper(),
                mobs.unstableCreeper(),
                mobs.nervousPoweredCreeper(),
                mobs.enderCreeper(),
                mobs.ghostCreeper(),
                mobs.fearfulEnderman(),
                mobs.toxicEnderman(),
                mobs.fearfulPhantom(),
                mobs.firePhantom(),
                mobs.phantomSoul(),
                mobs.fireWizard(),
                mobs.thunderWizard(),
                mobs.witherWizard(),
                mobs.blazeRider(),
                mobs.speedSkeletonRider(),
                mobs.witherSkeletonRider(),
                mobs.toxicSlime(),
                mobs.bloodDragon());
    }

    private CustomMob armoredZombie() {
        return mob(
                "armored_zombie",
                "Armored Zombie",
                EntityType.ZOMBIE,
                new Equipment(
                        "minecraft:diamond_helmet",
                        "minecraft:diamond_chestplate",
                        "minecraft:diamond_leggings",
                        "minecraft:diamond_boots",
                        null,
                        null),
                graph -> {});
    }

    /**
     * Blocks some hits with a shield sound and sometimes knocks the player up and slows them instead of hitting.
     */
    private CustomMob knightZombie() {
        Equipment armor = new Equipment(
                "minecraft:chainmail_helmet",
                "minecraft:chainmail_chestplate",
                "minecraft:chainmail_leggings",
                "minecraft:chainmail_boots",
                "minecraft:golden_axe",
                null);
        return mob("knight_zombie", "Knight Zombie", EntityType.ZOMBIE, armor, graph -> {
            String hit = graph.entry(EVENT_HIT);
            String victim = graph.as("player", hit, "entity");
            String up = graph.node(VECTOR_CREATE)
                    .literal("x", 0.0)
                    .literal("y", 1.0)
                    .literal("z", 0.0)
                    .id();
            String launch = graph.node(ENTITY_MUTATE)
                    .input(TARGET, victim, RESULT)
                    .input("velocity", up, RESULT)
                    .id();
            graph.on(
                    EVENT_HIT,
                    graph.when(
                            victim,
                            "matches",
                            graph.chance(
                                    15,
                                    graph.cancel(hit),
                                    launch,
                                    graph.effect(victim, RESULT, "minecraft:slowness", 1, 2),
                                    graph.sound(victim, RESULT, "minecraft:block.bubble_column.bubble_pop", 15, 3))));

            String damaged = graph.entry(EVENT_DAMAGE_BY_PLAYER);
            String attacker = graph.as("player", damaged, "attacker");
            graph.on(
                    EVENT_DAMAGE_BY_PLAYER,
                    graph.when(
                            attacker,
                            "matches",
                            graph.chance(
                                    12,
                                    graph.cancel(damaged),
                                    graph.sound(attacker, RESULT, "minecraft:item.shield.block", 15, 1))));
        });
    }

    /**
     * Invisible and without equipment, until it is hit; turns invisible again ten seconds after the last hit.
     */
    private CustomMob invisibleZombie() {
        return mob("invisible_zombie", "Invisible Zombie", EntityType.ZOMBIE, graph -> {
            graph.on(
                    MOB_SPAWN,
                    graph.equip(EquipmentSlot.HAND, "minecraft:air"),
                    graph.equip(EquipmentSlot.HEAD, "minecraft:air"),
                    graph.equip(EquipmentSlot.CHEST, "minecraft:air"),
                    graph.equip(EquipmentSlot.LEGS, "minecraft:air"),
                    graph.equip(EquipmentSlot.FEET, "minecraft:air"));
            String hide = graph.node(LIVING_ENTITY_APPLY_EFFECT)
                    .input(TARGET, graph.self(), "entity")
                    .literal("effect", "minecraft:invisibility")
                    .literal("seconds", 2.0)
                    .literal("amplifier", 0)
                    .id();
            graph.on(MOB_TICK, graph.after("damaged", 10, graph.every(1000, hide)));
            for (String trigger : List.of(EVENT_DAMAGE_BY_ENTITY, EVENT_DAMAGE_BY_PLAYER)) {
                String reveal = graph.node(LIVING_ENTITY_REMOVE_POTION_EFFECT)
                        .input(TARGET, graph.self(), "entity")
                        .literal("type", "minecraft:invisibility")
                        .id();
                graph.on(trigger, graph.resetTimer("damaged"), reveal);
            }
        });
    }

    private CustomMob speedZombie() {
        return mob(
                "speed_zombie",
                "Speed Zombie",
                EntityType.ZOMBIE,
                graph -> graph.on(MOB_TICK, graph.keepEffect("minecraft:speed", 4, true)));
    }

    private CustomMob invisibleSkeleton() {
        return mob("invisible_skeleton", "Invisible Skeleton", EntityType.SKELETON, graph -> {
            graph.on(MOB_TICK, graph.keepEffect("minecraft:invisibility", 1, false));
            smokeAtNightEnd(graph);
        });
    }

    /**
     * Arrows that hit put one of eight random effects on the target.
     */
    private CustomMob magicSkeleton() {
        return mob("magic_skeleton", "Magic Skeleton", EntityType.SKELETON, graph -> {
            graph.on(MOB_TICK, graph.keepEffect("minecraft:night_vision", 1, true));
            String hit = graph.entry(EVENT_PROJECTILE_HIT);
            String victim = graph.as("living_entity", hit, "hit_entity");
            String pick = graph.node(FUNCTION_RANDOM_INTEGER)
                    .literal("lower", 0)
                    .literal("upper", 7)
                    .id();
            var choice = graph.node(FLOW_SWITCH).input(VALUE, pick, RESULT);
            for (int i = 0; i < 8; i++) choice.config("case_" + i, i);
            choice.chain("case_0", graph.effect(victim, RESULT, "minecraft:slowness", 8, 1));
            choice.chain("case_1", graph.effect(victim, RESULT, "minecraft:slowness", 6, 2));
            choice.chain("case_2", graph.effect(victim, RESULT, "minecraft:instant_damage", 2, 1));
            choice.chain("case_3", graph.effect(victim, RESULT, "minecraft:levitation", 5, 1));
            choice.chain("case_4", graph.effect(victim, RESULT, "minecraft:nausea", 5, 1));
            choice.chain("case_5", graph.effect(victim, RESULT, "minecraft:weakness", 5, 1));
            choice.chain("case_6", graph.effect(victim, RESULT, "minecraft:poison", 6, 1));
            choice.chain("case_7", graph.effect(victim, RESULT, "minecraft:poison", 4, 2));
            graph.on(EVENT_PROJECTILE_HIT, graph.when(victim, "matches", choice.id()));
            smokeAtNightEnd(graph);
        });
    }

    /**
     * Fast, with a short fuse and an explosion that sets fire.
     */
    private CustomMob speedCreeper() {
        return mob("speed_creeper", "Speed Creeper", EntityType.CREEPER, graph -> {
            graph.on(MOB_SPAWN, graph.creeper("max_fuse_ticks", 10));
            graph.on(MOB_TICK, graph.keepEffect("minecraft:speed", 4, true));
            String prime = graph.entry(EVENT_EXPLOSION_PRIME);
            graph.on(
                    EVENT_EXPLOSION_PRIME,
                    graph.node(EVENT_DATA_EXPLOSION_PRIME_MUTATE)
                            .input(TARGET, prime, "event")
                            .literal("fire", true)
                            .id());
            igniteAtNightEnd(graph);
        });
    }

    /**
     * Leaves a cloud of strong poison where it explodes.
     */
    private CustomMob toxicCreeper() {
        return mob("toxic_creeper", "Toxic Creeper", EntityType.CREEPER, graph -> {
            graph.on(MOB_SPAWN, graph.creeper("max_fuse_ticks", 20));
            graph.on(MOB_TICK, graph.dust(0, 128, 0, 2, 5));
            String explode = graph.entry(EVENT_EXPLODE);
            graph.on(EVENT_EXPLODE, graph.cloud(explode, "location", "minecraft:strong_poison", 10, 3, 0, 128, 0));
        });
    }

    /**
     * Powered with a large blast, and explodes at once when an entity hits it or shoots it.
     */
    private CustomMob unstableCreeper() {
        return mob("unstable_creeper", "Unstable Creeper", EntityType.CREEPER, graph -> {
            graph.on(
                    MOB_SPAWN,
                    graph.creeper("explosion_radius", 10),
                    graph.creeper("powered", true),
                    graph.creeper("max_fuse_ticks", 50));
            graph.on(MOB_TICK, graph.particles("minecraft:end_rod", 2));
            for (String trigger : List.of(EVENT_DAMAGE_BY_ENTITY, EVENT_DAMAGE_BY_PLAYER)) {
                String damage = graph.entry(trigger);
                String either = graph.node(LOGIC_OR)
                        .input("first", causeIs(graph, damage, "ENTITY_ATTACK"), RESULT)
                        .input("second", causeIs(graph, damage, "PROJECTILE"), RESULT)
                        .id();
                String explode = graph.node(CREEPER_EXPLODE)
                        .input(TARGET, graph.self(), "entity")
                        .id();
                graph.on(trigger, graph.when(either, RESULT, explode));
            }
        });
    }

    private CustomMob nervousPoweredCreeper() {
        return mob("nervous_powered_creeper", "Nervous Powered Creeper", EntityType.CREEPER, graph -> {
            graph.on(
                    MOB_SPAWN,
                    graph.creeper("powered", true),
                    graph.creeper("max_fuse_ticks", 1),
                    graph.dustOnce(255, 0, 0, 4, 10));
            graph.on(MOB_TICK, graph.keepEffect("minecraft:speed", 2, false));
            igniteAtNightEnd(graph);
        });
    }

    /**
     * Teleports next to its target when it is more than five blocks away, at most every five seconds. Takes whoever
     * hits it as its target.
     */
    private CustomMob enderCreeper() {
        return mob("ender_creeper", "Ender Creeper", EntityType.CREEPER, graph -> {
            defineTeleportNear(graph);
            graph.on(MOB_TICK, graph.dust(128, 0, 128, 2, 5), teleportNear(graph, graph.selfRead(), TARGET));
            graph.on(EVENT_TARGET_CHANGE, teleportNear(graph, graph.entry(EVENT_TARGET_CHANGE), TARGET));
            for (String trigger : List.of(EVENT_DAMAGE_BY_ENTITY, EVENT_DAMAGE_BY_PLAYER)) {
                String attacker = graph.as("living_entity", graph.entry(trigger), "attacker");
                String target = graph.selfMutate()
                        .input("target_value", attacker, RESULT)
                        .id();
                graph.on(trigger, graph.when(attacker, "matches", target), teleportNear(graph, attacker, RESULT));
            }
        });
    }

    /**
     * Rides an invisible vex; neither takes suffocation damage, and the creeper dies with its vex.
     */
    private CustomMob ghostCreeper() {
        Extension vex = new Extension(ExtensionType.RIDER, EntityType.VEX, null);
        return mob("ghost_creeper", "Ghost Creeper", EntityType.CREEPER, vex, graph -> {
            String extension = graph.extension();
            String hide = graph.extensionMutate().literal("invisible", true).id();
            graph.on(
                    MOB_SPAWN,
                    graph.present(
                            extension,
                            "entity",
                            hide,
                            graph.equip(graph.extensionRead(), EquipmentSlot.HAND, "minecraft:air"),
                            graph.equip(graph.extensionRead(), EquipmentSlot.OFF_HAND, "minecraft:air")));
            graph.on(MOB_TICK, graph.keepEffect("minecraft:speed", 4, false));
            String damage = graph.entry(EVENT_DAMAGE);
            graph.on(EVENT_DAMAGE, graph.when(causeIs(graph, damage, "SUFFOCATION"), RESULT, graph.cancel(damage)));
            bindToExtension(graph);
            String remove =
                    graph.node(ENTITY_REMOVE).input(TARGET, extension, "entity").id();
            graph.on(EVENT_EXPLODE, graph.present(extension, "entity", remove));
        });
    }

    private CustomMob fearfulEnderman() {
        return mob("fearful_enderman", "Fearful Enderman", EntityType.ENDERMAN, graph -> {
            graph.on(MOB_TICK, graph.targetNearestPlayer(), graph.particles("minecraft:witch", 10));
            blindPlayersOnHit(graph, 7, 1);
            graph.on(MOB_NIGHT_END, graph.particlesAroundWith("minecraft:dragon_breath", 10, graph.power(1), "power"));
        });
    }

    /**
     * Leaves a cloud of strong poison where it teleports away from.
     */
    private CustomMob toxicEnderman() {
        return mob("toxic_enderman", "Toxic Enderman", EntityType.ENDERMAN, graph -> {
            graph.on(MOB_TICK, graph.targetNearestPlayer(), graph.dust(0, 128, 0, 2, 5));
            String teleport = graph.entry(EVENT_TELEPORT);
            graph.on(EVENT_TELEPORT, graph.cloud(teleport, "from", "minecraft:strong_poison", 10, 4, 0, 128, 0));
            graph.on(MOB_NIGHT_END, graph.particlesAroundWith("minecraft:dragon_breath", 10, graph.power(1), "power"));
        });
    }

    private CustomMob fearfulPhantom() {
        return mob("fearful_phantom", "Fearful Phantom", EntityType.PHANTOM, graph -> {
            graph.on(MOB_TICK, graph.keepEffect("minecraft:glowing", 1, true));
            String hit = graph.entry(EVENT_HIT);
            String victim = graph.as("player", hit, "entity");
            graph.on(
                    EVENT_HIT, graph.when(victim, "matches", graph.effect(victim, RESULT, "minecraft:slowness", 7, 2)));
            smokeAtNightEnd(graph);
        });
    }

    /**
     * Carries a blaze that attacks whatever the phantom targets, and drops it when the night ends.
     */
    private CustomMob firePhantom() {
        Extension blaze = new Extension(ExtensionType.CARRIER, EntityType.BLAZE, null);
        return mob("fire_phantom", "Fire Phantom", EntityType.PHANTOM, blaze, graph -> {
            graph.forwardTargetToExtension();
            String extension = graph.extension();
            String remove =
                    graph.node(ENTITY_REMOVE).input(TARGET, extension, "entity").id();
            graph.on(MOB_NIGHT_END, graph.present(extension, "entity", remove));
        });
    }

    private CustomMob phantomSoul() {
        return mob("phantom_soul", "Phantom Soul", EntityType.PHANTOM, graph -> {
            graph.on(
                    MOB_TICK,
                    graph.keepEffect("minecraft:glowing", 1, true),
                    graph.keepEffect("minecraft:invisibility", 1, false));
            blindPlayersOnHit(graph, 0.5, 1);
            smokeAtNightEnd(graph);
        });
    }

    /**
     * Throws a large fireball at its target every five seconds instead of potions; fireballs hitting a player deal the
     * witch's attack damage on top.
     */
    private CustomMob fireWizard() {
        Equipment held = new Equipment(null, null, null, null, "minecraft:fire_charge", null);
        return mob("fire_wizard", "Fire Wizard", EntityType.WITCH, held, graph -> {
            graph.on(
                    MOB_TICK,
                    graph.particles("minecraft:dripping_lava", 5),
                    graph.present(
                            graph.selfRead(),
                            TARGET,
                            graph.every(5000, graph.shootAtTarget(ProjectileKind.LARGE_FIREBALL, 4))));
            onlyShoot(graph, "minecraft:fireball");

            String hit = graph.entry(EVENT_PROJECTILE_HIT);
            String victim = graph.as("player", hit, "hit_entity");
            String attribute = graph.node(LIVING_ENTITY_ATTRIBUTE)
                    .input(TARGET, graph.self(), "entity")
                    .literal("attribute", "minecraft:attack_damage")
                    .id();
            String strength =
                    graph.node(ATTRIBUTE_READ).input(TARGET, attribute, RESULT).id();
            String damage = graph.node(LIVING_ENTITY_DAMAGE)
                    .input(TARGET, victim, RESULT)
                    .input("amount", strength, VALUE)
                    .input("source", graph.self(), "entity")
                    .id();
            graph.on(EVENT_PROJECTILE_HIT, graph.when(victim, "matches", graph.present(attribute, RESULT, damage)));
            smokeAtNightEnd(graph);
        });
    }

    /**
     * Calls lightning down on its target every four seconds instead of throwing potions.
     */
    private CustomMob thunderWizard() {
        return mob("thunder_wizard", "Thunder Wizard", EntityType.WITCH, graph -> {
            String target = graph.living(graph.selfRead(), TARGET);
            String marked = graph.node(VARIABLE_GET)
                    .config("name", "mark")
                    .config("type", "location")
                    .id();
            String strike = graph.node(WORLD_STRIKE_LIGHTNING)
                    .input(TARGET, graph.selfRead(), "world")
                    .input("location", marked, VALUE)
                    .id();
            String wait = graph.node(FLOW_DELAY)
                    .literal("duration", 1.5)
                    .chain("next", strike)
                    .id();
            Graphs.NodeBuilder mark = graph.node(VARIABLE_SET)
                    .config("name", "mark")
                    .config("type", "location")
                    .input(VALUE, target, "location");
            String flash = graph.node(WORLD_STRIKE_LIGHTNING)
                    .input(TARGET, graph.selfRead(), "world")
                    .input("location", mark.id(), VALUE)
                    .literal("effect_only", true)
                    .id();
            mark.chain("next", flash, wait);
            String sees = graph.node(LIVING_ENTITY_HAS_LINE_OF_SIGHT)
                    .input(TARGET, graph.self(), "entity")
                    .input("other", graph.selfRead(), TARGET)
                    .id();
            graph.on(
                    MOB_TICK,
                    graph.every(
                            250,
                            graph.particlesAroundWith(
                                    "minecraft:instant_effect", 15, graph.spell(255, 255, 255, 1), RESULT)),
                    graph.present(graph.selfRead(), TARGET, graph.every(4000, graph.when(sees, RESULT, mark.id()))));
            String shoot = graph.entry(EVENT_PROJECTILE_SHOOT);
            graph.on(EVENT_PROJECTILE_SHOOT, graph.cancel(shoot));
            smokeAtNightEnd(graph);
        });
    }

    /**
     * Throws a wither skull at its target every five seconds instead of potions.
     */
    private CustomMob witherWizard() {
        Equipment held = new Equipment(null, null, null, null, "minecraft:wither_skeleton_skull", null);
        return mob("wither_wizard", "Wither Wizard", EntityType.WITCH, held, graph -> {
            graph.on(
                    MOB_TICK,
                    graph.every(
                            250,
                            graph.particlesAroundWith(
                                    "minecraft:instant_effect", 15, graph.spell(255, 255, 255, 1), RESULT)),
                    graph.present(
                            graph.selfRead(),
                            TARGET,
                            graph.every(5000, graph.shootAtTarget(ProjectileKind.WITHER_SKULL, 4))));
            onlyShoot(graph, "minecraft:wither_skull");
            smokeAtNightEnd(graph);
        });
    }

    private CustomMob blazeRider() {
        return rider("blaze_rider", "Blaze Rider", EntityType.BLAZE, false);
    }

    private CustomMob speedSkeletonRider() {
        return rider("speed_skeleton_rider", "Speed Skeleton Rider", EntityType.SKELETON, true);
    }

    private CustomMob witherSkeletonRider() {
        return rider("wither_skeleton_rider", "Wither Skeleton Rider", EntityType.WITHER_SKELETON, true);
    }

    /**
     * A spider carrying another mob that attacks whatever the spider targets; the spider dies with its rider.
     */
    private CustomMob rider(String id, String name, EntityType passenger, boolean fast) {
        Extension extension = new Extension(ExtensionType.CARRIER, passenger, null);
        return mob(id, name, EntityType.SPIDER, extension, graph -> {
            graph.forwardTargetToExtension();
            bindToExtension(graph);
            if (fast) graph.on(MOB_TICK, graph.keepEffect("minecraft:speed", 1, true));
            smokeAtNightEnd(graph);
        });
    }

    /**
     * Leaves a cloud of strong poison where it dies.
     */
    private CustomMob toxicSlime() {
        return mob("toxic_slime", "Toxic Slime", EntityType.SLIME, graph -> {
            String death = graph.entry(EVENT_DEATH);
            String body = graph.living(death, "entity");
            graph.on(EVENT_DEATH, graph.cloud(body, "location", "minecraft:strong_poison", 5, 3, 78, 147, 49));
        });
    }

    /**
     * Calls {@code teleport_near} for a target.
     */
    private static String teleportNear(MobGraph graph, String node, String port) {
        return graph.call(TELEPORT_NEAR).input(TARGET, node, port).id();
    }

    /**
     * Routine {@code teleport_near(target)}: teleports the mob next to the target when there is one more than five
     * blocks away, at most every five seconds, and only onto a spot with room for it.
     */
    private static void defineTeleportNear(MobGraph graph) {
        String parameter = graph.parameter(TELEPORT_NEAR, TARGET, "living_entity");
        String target = graph.living(parameter, VALUE);
        String distance = graph.node(LOCATION_DISTANCE)
                .input(TARGET, graph.selfRead(), "location")
                .input("other", target, "location")
                .id();
        String far = graph.node(LOGIC_GREATER)
                .input("first", distance, RESULT)
                .literal("second", 5.0)
                .id();
        String spot = nearby(graph, target, 0);
        String head = nearby(graph, target, 1);
        String feetFree = graph.node(BLOCK_READ)
                .input(
                        TARGET,
                        graph.node(WORLD_BLOCK_AT)
                                .input(TARGET, target, "world")
                                .input("location", spot, RESULT)
                                .id(),
                        RESULT)
                .id();
        String headFree = graph.node(BLOCK_READ)
                .input(
                        TARGET,
                        graph.node(WORLD_BLOCK_AT)
                                .input(TARGET, target, "world")
                                .input("location", head, RESULT)
                                .id(),
                        RESULT)
                .id();
        String free = graph.node(LOGIC_AND)
                .input("first", feetFree, "passable")
                .input("second", headFree, "passable")
                .id();
        String teleport = graph.node(ENTITY_TELEPORT)
                .input(TARGET, graph.self(), "entity")
                .input("location", spot, RESULT)
                .id();
        String puff = graph.particlesAt("minecraft:portal", 10, target, "location");
        graph.routine(
                TELEPORT_NEAR,
                graph.present(
                        parameter,
                        VALUE,
                        graph.after(
                                "teleport",
                                5,
                                graph.when(
                                        far,
                                        RESULT,
                                        graph.when(free, RESULT, teleport, graph.resetTimer("teleport"), puff)))));
    }

    /**
     * A random spot up to two blocks around an entity, some blocks above its feet.
     */
    private static String nearby(MobGraph graph, String entity, double above) {
        String dx = graph.node(FUNCTION_RANDOM_NUMBER)
                .literal("lower", -2.0)
                .literal("upper", 2.0)
                .id();
        String dz = graph.node(FUNCTION_RANDOM_NUMBER)
                .literal("lower", -2.0)
                .literal("upper", 2.0)
                .id();
        String x = graph.node(MATH_ADD)
                .input("first", entity, "x")
                .input("second", dx, RESULT)
                .id();
        String y = graph.node(MATH_ADD)
                .input("first", entity, "y")
                .literal("second", above)
                .id();
        String z = graph.node(MATH_ADD)
                .input("first", entity, "z")
                .input("second", dz, RESULT)
                .id();
        return graph.node(LOCATION_CREATE)
                .input("world", entity, "world")
                .input("x", x, RESULT)
                .input("y", y, RESULT)
                .input("z", z, RESULT)
                .id();
    }

    /**
     * Puffs smoke around the mob when the night ends.
     */
    private static void smokeAtNightEnd(MobGraph graph) {
        graph.on(MOB_NIGHT_END, graph.particlesAround("minecraft:campfire_cosy_smoke", 30));
    }

    /**
     * Lights the creeper's fuse when the night ends, so it explodes at once.
     */
    private static void igniteAtNightEnd(MobGraph graph) {
        String ignite =
                graph.node(CREEPER_IGNITE).input(TARGET, graph.self(), "entity").id();
        graph.on(MOB_NIGHT_END, graph.creeper("max_fuse_ticks", 0), ignite);
    }

    /**
     * Keeps the extension from suffocating in the blocks it is carried into, and lets the mob die with its extension.
     */
    private static void bindToExtension(MobGraph graph) {
        String damage = graph.entry(EVENT_EXTENSION_DAMAGE);
        graph.on(
                EVENT_EXTENSION_DAMAGE,
                graph.when(causeIs(graph, damage, "SUFFOCATION"), RESULT, graph.cancel(damage)));
        String die = graph.node(LIVING_ENTITY_DAMAGE)
                .input(TARGET, graph.self(), "entity")
                .input("amount", graph.selfRead(), "health")
                .id();
        graph.on(EVENT_EXTENSION_DEATH, graph.unless(graph.selfRead(), "dead", die));
    }

    private static String causeIs(MobGraph graph, String trigger, String cause) {
        String expected = graph.value("damage_cause", cause);
        return graph.node(LOGIC_EQUAL)
                .input("first", trigger, "cause")
                .input("second", expected, VALUE)
                .id();
    }

    private static void blindPlayersOnHit(MobGraph graph, double seconds, int amplifier) {
        String hit = graph.entry(EVENT_HIT);
        String victim = graph.as("player", hit, "entity");
        graph.on(
                EVENT_HIT,
                graph.when(victim, "matches", graph.effect(victim, RESULT, "minecraft:blindness", seconds, amplifier)));
    }

    /**
     * Cancels every projectile the mob launches except those of one entity type, which stops a witch throwing potions.
     */
    private static void onlyShoot(MobGraph graph, String entityType) {
        String shoot = graph.entry(EVENT_PROJECTILE_SHOOT);
        String allowed = graph.value("entity_type", entityType);
        String same = graph.node(LOGIC_EQUAL)
                .input("first", shoot, "entity_type")
                .input("second", allowed, VALUE)
                .id();
        graph.on(EVENT_PROJECTILE_SHOOT, graph.unless(same, RESULT, graph.cancel(shoot)));
    }

    /**
     * The ender dragon of a dragon fight during a blood night: twice the health, harder hits, a dragon fireball at the
     * nearest player every six seconds and lightning on them every ten, its hits wither. Never spawns by chance; the
     * dragon fight turns the dragon into it (see {@link #BLOOD_DRAGON}).
     */
    private CustomMob bloodDragon() {
        CustomMob dragon = mob(BLOOD_DRAGON, "<dark_red>Blood Dragon", EntityType.ENDER_DRAGON, graph -> {
            String nearest = graph.node(WORLD_NEAREST_PLAYER)
                    .input("location", graph.selfRead(), "location")
                    .literal("radius", 128.0)
                    .id();
            String fireball = graph.node(LIVING_ENTITY_LAUNCH_PROJECTILE)
                    .config("projectile", ProjectileKind.DRAGON_FIREBALL)
                    .input("shooter", graph.self(), "entity")
                    .input(TARGET, nearest, "player")
                    .literal("speed", 2.0)
                    .id();
            String victim =
                    graph.node(ENTITY_READ).input(TARGET, nearest, "player").id();
            String lightning = graph.node(WORLD_STRIKE_LIGHTNING)
                    .input(TARGET, graph.selfRead(), "world")
                    .input("location", victim, "location")
                    .id();
            graph.on(
                    MOB_TICK,
                    graph.dust(160, 0, 0, 4, 30),
                    graph.every(6000, graph.present(nearest, "player", fireball)),
                    graph.every(10000, graph.present(nearest, "player", lightning)));
            String hit = graph.entry(EVENT_HIT);
            String hitPlayer = graph.as("player", hit, "entity");
            graph.on(
                    EVENT_HIT,
                    graph.when(hitPlayer, "matches", graph.effect(hitPlayer, RESULT, "minecraft:wither", 5, 1)));
        });
        Attributes base = dragon.attributes();
        return new CustomMob(
                dragon.id(),
                dragon.equipment(),
                new Attributes(
                        base.displayName(),
                        base.entityType(),
                        true,
                        ValueModifier.VALUE,
                        400,
                        ValueModifier.MULTIPLY,
                        1.5,
                        List.of()),
                dragon.behaviour(),
                dragon.mobDrops(),
                dragon.extension(),
                new SpawnRules(0, false, Despawn.NEVER, Set.of(), SpawnConditions.any()));
    }

    private CustomMob mob(String id, String name, EntityType type, Consumer<MobGraph> behaviour) {
        return mob(id, name, type, null, new Extension(ExtensionType.NONE, null, null), behaviour);
    }

    private CustomMob mob(String id, String name, EntityType type, Equipment equipment, Consumer<MobGraph> behaviour) {
        return mob(id, name, type, equipment, new Extension(ExtensionType.NONE, null, null), behaviour);
    }

    private CustomMob mob(String id, String name, EntityType type, Extension extension, Consumer<MobGraph> behaviour) {
        return mob(id, name, type, null, extension, behaviour);
    }

    private CustomMob mob(
            String id,
            String name,
            EntityType type,
            @Nullable Equipment equipment,
            Extension extension,
            Consumer<MobGraph> behaviour) {
        MobGraph graph = new MobGraph(platform, type, extension.entityType());
        behaviour.accept(graph);
        return new CustomMob(
                id,
                equipment,
                new Attributes(name, type, true, ValueModifier.DEFAULT, 2, ValueModifier.DEFAULT, 2),
                built(id, graph),
                MobDrops.defaultDrops(),
                extension,
                SpawnRules.defaults());
    }

    private static Graph built(String id, MobGraph graph) {
        try {
            return graph.build();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Default mob %s: %s".formatted(id, e.getMessage()), e);
        }
    }
}
