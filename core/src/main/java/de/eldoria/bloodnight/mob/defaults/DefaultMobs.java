/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.defaults;

import de.eldoria.bloodnight.api.MovementPreset;
import de.eldoria.bloodnight.graph.BloodNightNodes;
import de.eldoria.bloodnight.graph.runtime.ProjectileKind;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.meta.Attributes;
import de.eldoria.bloodnight.mob.meta.Equipment;
import de.eldoria.bloodnight.mob.meta.Extension;
import de.eldoria.bloodnight.mob.meta.ExtensionType;
import de.eldoria.bloodnight.mob.meta.MobDrops;
import de.eldoria.bloodnight.mob.meta.Movement;
import de.eldoria.bloodnight.mob.meta.NightEnd;
import de.eldoria.bloodnight.mob.meta.SpawnConditions;
import de.eldoria.bloodnight.mob.meta.SpawnRules;
import de.eldoria.bloodnight.mob.meta.Surface;
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
import java.util.function.Function;

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
     * Upward speed of the assassin spider's leap at a target on its own height, about three blocks high.
     */
    private static final double LEAP_UP = 0.75;

    /**
     * Upward speed the assassin spider's leap gains per block its target stands higher, so it clears trees.
     */
    private static final double LEAP_UP_PER_BLOCK = 0.1;

    /**
     * Upward speed of the assassin spider's highest leap, about thirteen blocks high.
     */
    private static final double LEAP_UP_MAX = 1.6;

    /**
     * How far sideways from a target above it the assassin spider wants to be before it leaps; closer, it hops back
     * first, so it does not leap straight up into the leaves under its target.
     */
    private static final double LEAP_STAND_OFF = 4;

    /**
     * Sideways speed of the assassin spider's hop away from a target above it, about five blocks.
     */
    private static final double BACK_OFF_SPEED = 0.6;

    /**
     * Upward speed of the assassin spider's hop away from a target above it, about one block high.
     */
    private static final double BACK_OFF_UP = 0.42;

    /**
     * Blocks a leap covers per unit of sideways speed: air drag takes 9 % of it every tick, so about ten.
     */
    private static final double LEAP_REACH = 10;

    /**
     * Id of the blood dragon, the ender dragon of a dragon fight during a blood night.
     */
    public static final String BLOOD_DRAGON = "blood_dragon";

    private final GraphPlatform platform;
    private final Function<String, String> names;

    private DefaultMobs(GraphPlatform platform, Function<String, String> names) {
        this.platform = platform;
        this.names = names;
    }

    /**
     * Every default mob, named by its id; for checks that do not show names.
     *
     * @param platform the node set the behaviours are built and checked against
     * @return every default mob
     * @throws IllegalArgumentException when a behaviour does not fit the platform
     */
    public static List<CustomMob> create(GraphPlatform platform) {
        return create(platform, Function.identity());
    }

    /**
     * @param platform the node set the behaviours are built and checked against
     * @param names    the English name of a mob by its id, from its store package
     * @return every default mob
     * @throws IllegalArgumentException when a behaviour does not fit the platform
     */
    public static List<CustomMob> create(GraphPlatform platform, Function<String, String> names) {
        DefaultMobs mobs = new DefaultMobs(platform, names);
        return List.of(
                subtypesToo(mobs.armoredZombie()),
                subtypesToo(moving(mobs.knightZombie(), MovementPreset.HIT_AND_RUN)),
                subtypesToo(moving(mobs.invisibleZombie(), MovementPreset.STALKER)),
                subtypesToo(moving(mobs.speedZombie(), MovementPreset.PACK)),
                moving(mobs.invisibleSkeleton(), MovementPreset.KEEP_DISTANCE),
                moving(mobs.magicSkeleton(), MovementPreset.KEEP_DISTANCE),
                moving(mobs.speedCreeper(), MovementPreset.AGGRESSIVE),
                mobs.toxicCreeper(),
                mobs.unstableCreeper(),
                mobs.nervousPoweredCreeper(),
                moving(mobs.enderCreeper(), MovementPreset.STALKER),
                mobs.ghostCreeper(),
                moving(mobs.fearfulEnderman(), MovementPreset.STALKER),
                mobs.toxicEnderman(),
                mobs.fearfulPhantom(),
                mobs.firePhantom(),
                mobs.phantomSoul(),
                mobs.carrierPhantom(),
                moving(mobs.fireWizard(), MovementPreset.KEEP_DISTANCE),
                moving(mobs.thunderWizard(), MovementPreset.KEEP_DISTANCE),
                moving(mobs.witherWizard(), MovementPreset.KEEP_DISTANCE),
                subtypesToo(mobs.blazeRider()),
                subtypesToo(mobs.speedSkeletonRider()),
                subtypesToo(mobs.witherSkeletonRider()),
                mobs.toxicSlime(),
                mobs.stormBreeze(),
                mobs.sporeBogged(),
                mobs.volleyPillager(),
                mobs.tideDrowned(),
                mobs.swarmSilverfish(),
                subtypesToo(mobs.assassinSpider()),
                inTheEnd(mobs.launcherEnderman()),
                inTheEnd(mobs.grabberEnderman()),
                inTheEnd(mobs.fuseEnderman()),
                mobs.bloodDragon());
    }

    private CustomMob armoredZombie() {
        return mob(
                "armored_zombie",
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
        return mob("knight_zombie", EntityType.ZOMBIE, armor, graph -> {
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
        return mob("invisible_zombie", EntityType.ZOMBIE, graph -> {
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
                EntityType.ZOMBIE,
                graph -> graph.on(MOB_TICK, graph.keepEffect("minecraft:speed", 4, true)));
    }

    private CustomMob invisibleSkeleton() {
        return mob("invisible_skeleton", EntityType.SKELETON, graph -> {
            graph.on(MOB_TICK, graph.keepEffect("minecraft:invisibility", 1, false));
            smokeAtNightEnd(graph);
        });
    }

    /**
     * Arrows that hit put one of eight random effects on the target.
     */
    private CustomMob magicSkeleton() {
        return mob("magic_skeleton", EntityType.SKELETON, graph -> {
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
        return mob("speed_creeper", EntityType.CREEPER, graph -> {
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
        return mob("toxic_creeper", EntityType.CREEPER, graph -> {
            graph.on(MOB_SPAWN, graph.creeper("max_fuse_ticks", 20));
            graph.on(MOB_TICK, graph.dust(0, 128, 0, 2, 5));
            String explode = graph.entry(EVENT_EXPLODE);
            graph.on(EVENT_EXPLODE, graph.cloud(explode, "location", "minecraft:strong_poison", 10, 3, 0, 128, 0));
        });
    }

    /**
     * Powered with a large blast. When an entity hits it or shoots it, it hisses, a red area grows around it and it
     * explodes a second later.
     */
    private CustomMob unstableCreeper() {
        return mob("unstable_creeper", EntityType.CREEPER, graph -> {
            graph.on(
                    MOB_SPAWN,
                    graph.creeper("explosion_radius", 10),
                    graph.creeper("powered", true),
                    graph.creeper("max_fuse_ticks", 50));
            graph.on(MOB_TICK, graph.particles("minecraft:end_rod", 2));
            String warn = graph.node(WORLD_SHOW_MARKER)
                    .config("shape", "CIRCLE")
                    .input("location", graph.selfRead(), "location")
                    .input("color", graph.color(255, 40, 40), RESULT)
                    .literal("size", 5.0)
                    .literal("seconds", 1.0)
                    .id();
            String ignite = graph.node(CREEPER_IGNITE)
                    .input(TARGET, graph.self(), "entity")
                    .id();
            String explode = graph.node(CREEPER_EXPLODE)
                    .input(TARGET, graph.self(), "entity")
                    .id();
            String fuse = graph.node(FLOW_DELAY)
                    .literal("duration", 1.0)
                    .chain("next", explode)
                    .id();
            graph.routine(
                    "light_fuse",
                    graph.node(FLOW_DO_ONCE).chain("next", warn, ignite, fuse).id());
            for (String trigger : List.of(EVENT_DAMAGE_BY_ENTITY, EVENT_DAMAGE_BY_PLAYER)) {
                String damage = graph.entry(trigger);
                String either = graph.node(LOGIC_OR)
                        .input("first", causeIs(graph, damage, "ENTITY_ATTACK"), RESULT)
                        .input("second", causeIs(graph, damage, "PROJECTILE"), RESULT)
                        .id();
                graph.on(
                        trigger,
                        graph.when(either, RESULT, graph.call("light_fuse").id()));
            }
        });
    }

    private CustomMob nervousPoweredCreeper() {
        return mob("nervous_powered_creeper", EntityType.CREEPER, graph -> {
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
        return mob("ender_creeper", EntityType.CREEPER, graph -> {
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
        return mob("ghost_creeper", EntityType.CREEPER, vex, graph -> {
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
        return mob("fearful_enderman", EntityType.ENDERMAN, graph -> {
            graph.on(MOB_TICK, graph.targetNearestPlayer(), graph.particles("minecraft:witch", 10));
            blindPlayersOnHit(graph, 7, 1);
            graph.on(MOB_NIGHT_END, graph.particlesAroundWith("minecraft:dragon_breath", 10, graph.power(1), "power"));
        });
    }

    /**
     * Leaves a cloud of strong poison where it teleports away from.
     */
    private CustomMob toxicEnderman() {
        return mob("toxic_enderman", EntityType.ENDERMAN, graph -> {
            graph.on(MOB_TICK, graph.targetNearestPlayer(), graph.dust(0, 128, 0, 2, 5));
            String teleport = graph.entry(EVENT_TELEPORT);
            graph.on(EVENT_TELEPORT, graph.cloud(teleport, "from", "minecraft:strong_poison", 10, 4, 0, 128, 0));
            graph.on(MOB_NIGHT_END, graph.particlesAroundWith("minecraft:dragon_breath", 10, graph.power(1), "power"));
        });
    }

    private CustomMob fearfulPhantom() {
        return mob("fearful_phantom", EntityType.PHANTOM, graph -> {
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
        return mob("fire_phantom", EntityType.PHANTOM, blaze, graph -> {
            graph.forwardTargetToExtension();
            String extension = graph.extension();
            String remove =
                    graph.node(ENTITY_REMOVE).input(TARGET, extension, "entity").id();
            graph.on(MOB_NIGHT_END, graph.present(extension, "entity", remove));
        });
    }

    private CustomMob phantomSoul() {
        return mob("phantom_soul", EntityType.PHANTOM, graph -> {
            graph.on(
                    MOB_TICK,
                    graph.keepEffect("minecraft:glowing", 1, true),
                    graph.keepEffect("minecraft:invisibility", 1, false));
            blindPlayersOnHit(graph, 0.5, 1);
            smokeAtNightEnd(graph);
        });
    }

    /**
     * Grabs a player it hits, at most every 15 seconds, carries them 20 blocks up within three seconds and drops them.
     * A hit on its way in, up to three seconds before it reaches the player, calls the grab off.
     */
    private CustomMob carrierPhantom() {
        return mob("carrier_phantom", EntityType.PHANTOM, graph -> {
            String hit = graph.entry(EVENT_HIT);
            String victim = graph.as("player", hit, "entity");
            Graphs.NodeBuilder carry = graph.node(ENTITY_CARRY)
                    .input(TARGET, graph.self(), "entity")
                    .input("passenger", victim, RESULT);
            String lift = graph.node(ENTITY_LIFT)
                    .input(TARGET, graph.self(), "entity")
                    .literal("height", 20.0)
                    .literal("seconds", 3.0)
                    .id();
            String drop = graph.node(ENTITY_DROP_PASSENGERS)
                    .input(TARGET, graph.self(), "entity")
                    .id();
            String release = graph.node(FLOW_DELAY)
                    .literal("duration", 3.5)
                    .chain("next", drop)
                    .id();
            carry.chain(
                    "next",
                    graph.when(
                            carry.id(),
                            RESULT,
                            graph.sound(victim, RESULT, "minecraft:entity.phantom.swoop", 1, 0.6),
                            lift,
                            release));
            graph.on(EVENT_HIT, graph.when(victim, "matches", graph.after("hurt", 3, graph.every(15000, carry.id()))));
            for (String trigger : List.of(EVENT_DAMAGE_BY_ENTITY, EVENT_DAMAGE_BY_PLAYER)) {
                graph.on(trigger, graph.resetTimer("hurt"));
            }
            smokeAtNightEnd(graph);
        });
    }

    /**
     * Throws a large fireball at its target every five seconds instead of potions; fireballs hitting a player deal the
     * witch's attack damage on top.
     */
    private CustomMob fireWizard() {
        Equipment held = new Equipment(null, null, null, null, "minecraft:fire_charge", null);
        return mob("fire_wizard", EntityType.WITCH, held, graph -> {
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
     * Calls lightning down on its target every four seconds instead of throwing potions. A red area grows on the
     * marked spot for the 1.5 seconds before the strike.
     */
    private CustomMob thunderWizard() {
        return mob("thunder_wizard", EntityType.WITCH, graph -> {
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
            String warn = graph.node(WORLD_SHOW_MARKER)
                    .config("shape", "CIRCLE")
                    .input("location", mark.id(), VALUE)
                    .input("color", graph.color(255, 40, 40), RESULT)
                    .literal("size", 1.5)
                    .literal("seconds", 1.5)
                    .id();
            mark.chain("next", flash, warn, wait);
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
        return mob("wither_wizard", EntityType.WITCH, held, graph -> {
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
        return rider("blaze_rider", EntityType.BLAZE, false);
    }

    private CustomMob speedSkeletonRider() {
        return rider("speed_skeleton_rider", EntityType.SKELETON, true);
    }

    private CustomMob witherSkeletonRider() {
        return rider("wither_skeleton_rider", EntityType.WITHER_SKELETON, true);
    }

    /**
     * A spider carrying another mob that attacks whatever the spider targets; the spider dies with its rider.
     */
    private CustomMob rider(String id, EntityType passenger, boolean fast) {
        Extension extension = new Extension(ExtensionType.CARRIER, passenger, null);
        return mob(id, EntityType.SPIDER, extension, graph -> {
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
        return mob("toxic_slime", EntityType.SLIME, graph -> {
            String death = graph.entry(EVENT_DEATH);
            String body = graph.living(death, "entity");
            graph.on(EVENT_DEATH, graph.cloud(body, "location", "minecraft:strong_poison", 5, 3, 78, 147, 49));
        });
    }

    /**
     * Fires three wind charges at its target every six seconds.
     */
    private CustomMob stormBreeze() {
        return mob(
                "storm_breeze",
                EntityType.BREEZE,
                graph -> graph.on(
                        MOB_TICK,
                        graph.particles("minecraft:cloud", 1),
                        graph.present(
                                graph.selfRead(),
                                TARGET,
                                graph.every(
                                        6000,
                                        graph.shootAtTarget(ProjectileKind.WIND_CHARGE, 1.5),
                                        graph.shootAtTarget(ProjectileKind.WIND_CHARGE, 1.2),
                                        graph.shootAtTarget(ProjectileKind.WIND_CHARGE, 0.9)))));
    }

    /**
     * Arrows that hit poison and confuse the target; leaves a poison cloud where it dies.
     */
    private CustomMob sporeBogged() {
        return mob("spore_bogged", EntityType.BOGGED, graph -> {
            graph.on(MOB_TICK, graph.particles("minecraft:crimson_spore", 3));
            String hit = graph.entry(EVENT_PROJECTILE_HIT);
            String victim = graph.as("living_entity", hit, "hit_entity");
            graph.on(
                    EVENT_PROJECTILE_HIT,
                    graph.when(
                            victim,
                            "matches",
                            graph.effect(victim, RESULT, "minecraft:poison", 6, 1),
                            graph.effect(victim, RESULT, "minecraft:nausea", 4, 0)));
            String death = graph.entry(EVENT_DEATH);
            String body = graph.living(death, "entity");
            graph.on(EVENT_DEATH, graph.cloud(body, "location", "minecraft:poison", 6, 3, 110, 90, 60));
        });
    }

    /**
     * Fires two extra arrows at its target every four seconds while it can see it.
     */
    private CustomMob volleyPillager() {
        return mob("volley_pillager", EntityType.PILLAGER, graph -> {
            String sees = graph.node(LIVING_ENTITY_HAS_LINE_OF_SIGHT)
                    .input(TARGET, graph.self(), "entity")
                    .input("other", graph.selfRead(), TARGET)
                    .id();
            graph.on(
                    MOB_TICK,
                    graph.selfMutate().literal("fall_distance", 0.0).id(),
                    graph.present(
                            graph.selfRead(),
                            TARGET,
                            graph.every(
                                    4000,
                                    graph.when(
                                            sees,
                                            RESULT,
                                            graph.shootAtTarget(ProjectileKind.ARROW, 2.5),
                                            graph.shootAtTarget(ProjectileKind.ARROW, 2.2)))));
            smokeAtNightEnd(graph);
        });
    }

    /**
     * Throws a trident at its target every three seconds while the target is in water.
     */
    private CustomMob tideDrowned() {
        return mob("tide_drowned", EntityType.DROWNED, graph -> {
            String target = graph.living(graph.selfRead(), TARGET);
            graph.on(
                    MOB_TICK,
                    graph.particles("minecraft:dripping_water", 2),
                    graph.present(
                            graph.selfRead(),
                            TARGET,
                            graph.when(
                                    target,
                                    "in_water",
                                    graph.every(3000, graph.shootAtTarget(ProjectileKind.TRIDENT, 2.0)))));
        });
    }

    /**
     * When a player hurts it, every silverfish within twelve blocks goes for that player.
     */
    private CustomMob swarmSilverfish() {
        return mob("swarm_silverfish", EntityType.SILVERFISH, graph -> {
            graph.on(MOB_TICK, graph.keepEffect("minecraft:speed", 1, false));
            String damaged = graph.entry(EVENT_DAMAGE_BY_PLAYER);
            String attacker = graph.as("player", damaged, "attacker");
            String nearby = graph.node(ENTITY_NEARBY_ENTITIES)
                    .input(TARGET, graph.self(), "entity")
                    .literal("x", 12.0)
                    .literal("y", 6.0)
                    .literal("z", 12.0)
                    .id();
            Graphs.NodeBuilder each = graph.node(FLOW_FOR_EACH).input("list", nearby, RESULT);
            String other =
                    graph.node(ENTITY_READ).input(TARGET, each.id(), "element").id();
            String silverfish = graph.value("entity_type", "minecraft:silverfish");
            String same = graph.node(LOGIC_EQUAL)
                    .input("first", other, "type")
                    .input("second", silverfish, VALUE)
                    .id();
            String mob = graph.as("mob", each.id(), "element");
            String hunt = graph.node(MOB_MUTATE)
                    .input(TARGET, mob, RESULT)
                    .input("target_value", attacker, RESULT)
                    .id();
            each.chain("body", graph.when(same, RESULT, graph.when(mob, "matches", hunt)));
            graph.on(EVENT_DAMAGE_BY_PLAYER, graph.when(attacker, "matches", graph.every(2000, each.id())));
        });
    }

    /**
     * Leaps at its target every four seconds when it stands more than 3 blocks to the side and less than 16 away: high
     * enough to reach a target up in a tree and far enough to land on it. Right below a target that is higher up, it
     * hops back first, since a leap straight up ends in the leaves. Takes no fall damage at all.
     */
    private CustomMob assassinSpider() {
        return mob("assassin_spider", EntityType.SPIDER, graph -> {
            String target = graph.living(graph.selfRead(), TARGET);
            String distance = graph.node(LOCATION_DISTANCE)
                    .input(TARGET, graph.selfRead(), "location")
                    .input("other", target, "location")
                    .id();
            String flatSelf = flat(graph, graph.selfRead(), 0.01);
            String flatTarget = flat(graph, target, 0);
            String across = graph.node(VECTOR_DISTANCE)
                    .input("first", flatSelf, RESULT)
                    .input("second", flatTarget, RESULT)
                    .id();
            String rise = graph.node(MATH_SUBTRACT)
                    .input("first", target, "y")
                    .input("second", graph.selfRead(), "y")
                    .id();
            String high = graph.node(LOGIC_GREATER)
                    .input("first", rise, RESULT)
                    .literal("second", 2.0)
                    .id();
            String close = graph.node(LOGIC_LESS)
                    .input("first", across, RESULT)
                    .literal("second", LEAP_STAND_OFF)
                    .id();
            String beneath = graph.node(LOGIC_AND)
                    .input("first", high, RESULT)
                    .input("second", close, RESULT)
                    .id();
            String far = graph.node(LOGIC_GREATER)
                    .input("first", across, RESULT)
                    .literal("second", 3.0)
                    .id();
            String near = graph.node(LOGIC_LESS)
                    .input("first", distance, RESULT)
                    .literal("second", 16.0)
                    .id();
            String inReach = graph.node(LOGIC_AND)
                    .input("first", far, RESULT)
                    .input("second", near, RESULT)
                    .id();
            String leap = graph.selfMutate()
                    .input(
                            "velocity",
                            graph.node(VECTOR_CREATE)
                                    .input("x", towards(graph, target, "x"), RESULT)
                                    .input("y", leapUp(graph, target), RESULT)
                                    .input("z", towards(graph, target, "z"), RESULT)
                                    .id(),
                            RESULT)
                    .id();
            String away = graph.node(VECTOR_NORMALIZE)
                    .input(
                            VALUE,
                            graph.node(VECTOR_DIRECTION)
                                    .input("from", flatTarget, RESULT)
                                    .input("to", flatSelf, RESULT)
                                    .id(),
                            RESULT)
                    .id();
            String backOff = graph.selfMutate()
                    .input(
                            "velocity",
                            graph.node(VECTOR_CREATE)
                                    .input("x", scaled(graph, away, "x", BACK_OFF_SPEED), RESULT)
                                    .literal("y", BACK_OFF_UP)
                                    .input("z", scaled(graph, away, "z", BACK_OFF_SPEED), RESULT)
                                    .id(),
                            RESULT)
                    .id();
            String move = graph.node(FLOW_BRANCH)
                    .input("condition", beneath, RESULT)
                    .chain("true", graph.every(1500, backOff))
                    .chain(
                            "false",
                            graph.when(
                                    inReach,
                                    RESULT,
                                    graph.every(4000, leap, graph.particlesAround("minecraft:crit", 15))))
                    .id();
            graph.on(
                    MOB_TICK, graph.present(graph.selfRead(), TARGET, graph.when(graph.selfRead(), "on_ground", move)));
            String damage = graph.entry(EVENT_DAMAGE);
            graph.on(EVENT_DAMAGE, graph.when(causeIs(graph, damage, "FALL"), RESULT, graph.cancel(damage)));
            smokeAtNightEnd(graph);
        });
    }

    /**
     * A point at an entity's x and z on height 0, shifted along x by a little so two such points never coincide.
     */
    private static String flat(MobGraph graph, String entity, double shift) {
        return graph.node(VECTOR_CREATE)
                .input(
                        "x",
                        graph.node(MATH_ADD)
                                .input("first", entity, "x")
                                .literal("second", shift)
                                .id(),
                        RESULT)
                .literal("y", 0.0)
                .input("z", entity, "z")
                .id();
    }

    private static String scaled(MobGraph graph, String vector, String axis, double factor) {
        String component = graph.node(VECTOR_READ).input(TARGET, vector, RESULT).id();
        return graph.node(MATH_MULTIPLY)
                .input("first", component, axis)
                .literal("second", factor)
                .id();
    }

    /**
     * @return the mob moving by the preset, with its default numbers
     */
    private static CustomMob moving(CustomMob mob, MovementPreset preset) {
        return mob.withMovement(Movement.of(preset));
    }

    /**
     * The upward speed of a leap: {@value #LEAP_UP}, plus {@value #LEAP_UP_PER_BLOCK} per block the target stands
     * higher, at most {@value #LEAP_UP_MAX}.
     */
    private static String leapUp(MobGraph graph, String target) {
        String rise = graph.node(MATH_SUBTRACT)
                .input("first", target, "y")
                .input("second", graph.selfRead(), "y")
                .id();
        String extra = graph.node(MATH_MULTIPLY)
                .input("first", rise, RESULT)
                .literal("second", LEAP_UP_PER_BLOCK)
                .id();
        String speed = graph.node(MATH_ADD)
                .input("first", extra, RESULT)
                .literal("second", LEAP_UP)
                .id();
        return graph.node(MATH_CLAMP)
                .input(VALUE, speed, RESULT)
                .literal("lower", LEAP_UP)
                .literal("upper", LEAP_UP_MAX)
                .id();
    }

    /**
     * The speed along one axis that carries the mob to the target during a leap, air drag counted in.
     */
    private static String towards(MobGraph graph, String target, String axis) {
        String gap = graph.node(MATH_SUBTRACT)
                .input("first", target, axis)
                .input("second", graph.selfRead(), axis)
                .id();
        return graph.node(MATH_DIVIDE)
                .input("first", gap, RESULT)
                .literal("second", LEAP_REACH)
                .id();
    }

    /**
     * Grabs a player it hits. Unless they sneak within a second, it carries them up to twenty blocks into the air
     * somewhere within thirty blocks and lets go, then returns to where it stood. At most every eight seconds.
     */
    private CustomMob launcherEnderman() {
        return mob("launcher_enderman", EntityType.ENDERMAN, graph -> {
            String hit = graph.entry(EVENT_HIT);
            String victim = graph.as("player", hit, "entity");
            String remember = graph.node(VARIABLE_SET)
                    .config("name", "origin")
                    .config("type", "location")
                    .input(VALUE, graph.selfRead(), "location")
                    .id();
            String held = graph.as("player", graph.selfRead(), TARGET);
            String sneaking =
                    graph.node(PLAYER_READ).input(TARGET, held, RESULT).id();
            String player = graph.node(ENTITY_READ).input(TARGET, held, RESULT).id();
            String sky = graph.node(LOCATION_CREATE)
                    .input("world", player, "world")
                    .input("x", offset(graph, player, "x", -30, 30), RESULT)
                    .input("y", offset(graph, player, "y", 8, 20), RESULT)
                    .input("z", offset(graph, player, "z", -30, 30), RESULT)
                    .id();
            String origin = graph.node(VARIABLE_GET)
                    .config("name", "origin")
                    .config("type", "location")
                    .id();
            String back = graph.node(FLOW_DELAY)
                    .literal("duration", 0.5)
                    .chain("next", teleport(graph, graph.self(), "entity", origin, VALUE))
                    .id();
            String launch = graph.when(
                    held,
                    "matches",
                    graph.unless(
                            sneaking,
                            "sneaking",
                            teleport(graph, held, RESULT, sky, RESULT),
                            teleport(graph, graph.self(), "entity", sky, RESULT),
                            graph.particlesAt("minecraft:portal", 40, sky, RESULT),
                            back));
            String wait = graph.node(FLOW_DELAY)
                    .literal("duration", 1.0)
                    .chain("next", launch)
                    .id();
            graph.on(
                    EVENT_HIT,
                    graph.when(
                            victim,
                            "matches",
                            graph.every(
                                    8000,
                                    remember,
                                    graph.effect(victim, RESULT, "minecraft:slowness", 1, 4),
                                    graph.sound(victim, RESULT, "minecraft:entity.enderman.scream", 1, 0.7),
                                    wait)));
            graph.on(MOB_TICK, graph.targetNearestPlayer(), graph.particles("minecraft:reverse_portal", 3));
        });
    }

    /**
     * Every ten seconds, picks some of the mobs within sixteen blocks and puts them down in front of its target.
     */
    private CustomMob grabberEnderman() {
        return mob("grabber_enderman", EntityType.ENDERMAN, graph -> {
            String target = graph.living(graph.selfRead(), TARGET);
            String facing =
                    graph.node(LOCATION_READ).input(TARGET, target, "location").id();
            String direction =
                    graph.node(VECTOR_READ).input(TARGET, facing, "direction").id();
            String front = graph.node(LOCATION_CREATE)
                    .input("world", target, "world")
                    .input("x", ahead(graph, target, direction, "x"), RESULT)
                    .input("y", target, "y")
                    .input("z", ahead(graph, target, direction, "z"), RESULT)
                    .id();
            String nearby = graph.node(ENTITY_NEARBY_ENTITIES)
                    .input(TARGET, graph.self(), "entity")
                    .literal("x", 16.0)
                    .literal("y", 8.0)
                    .literal("z", 16.0)
                    .id();
            Graphs.NodeBuilder each = graph.node(FLOW_FOR_EACH).input("list", nearby, RESULT);
            String mob = graph.as("mob", each.id(), "element");
            String other = graph.node(ENTITY_READ).input(TARGET, mob, RESULT).id();
            String dragon = graph.value("entity_type", "minecraft:ender_dragon");
            String isDragon = graph.node(LOGIC_EQUAL)
                    .input("first", other, "type")
                    .input("second", dragon, VALUE)
                    .id();
            each.chain(
                    "body",
                    graph.when(
                            mob,
                            "matches",
                            graph.unless(
                                    isDragon, RESULT, graph.chance(35, teleport(graph, mob, RESULT, front, RESULT)))));
            graph.on(
                    MOB_TICK,
                    graph.targetNearestPlayer(),
                    graph.present(
                            graph.selfRead(),
                            TARGET,
                            graph.every(10000, each.id(), graph.particlesAt("minecraft:portal", 30, front, RESULT))));
        });
    }

    /**
     * Every six seconds, lays lit TNT next to its target that hurts but breaks no blocks.
     */
    private CustomMob fuseEnderman() {
        return mob("fuse_enderman", EntityType.ENDERMAN, graph -> {
            String target = graph.living(graph.selfRead(), TARGET);
            String tnt = graph.node(WORLD_SPAWN_TNT)
                    .input("location", nearby(graph, target, 0), RESULT)
                    .literal("seconds", 2.0)
                    .literal("power", 3.0)
                    .id();
            graph.on(
                    MOB_TICK,
                    graph.targetNearestPlayer(),
                    graph.dust(200, 40, 0, 1, 3),
                    graph.present(graph.selfRead(), TARGET, graph.every(6000, tnt)));
        });
    }

    private static String teleport(MobGraph graph, String entity, String entityPort, String location, String port) {
        return graph.node(ENTITY_TELEPORT)
                .input(TARGET, entity, entityPort)
                .input("location", location, port)
                .id();
    }

    /**
     * A coordinate of an entity plus a random amount between two bounds.
     */
    private static String offset(MobGraph graph, String entity, String axis, double lower, double upper) {
        String random = graph.node(FUNCTION_RANDOM_NUMBER)
                .literal("lower", lower)
                .literal("upper", upper)
                .id();
        return graph.node(MATH_ADD)
                .input("first", entity, axis)
                .input("second", random, RESULT)
                .id();
    }

    /**
     * A coordinate three blocks ahead of where an entity looks.
     */
    private static String ahead(MobGraph graph, String entity, String direction, String axis) {
        String step = graph.node(MATH_MULTIPLY)
                .input("first", direction, axis)
                .literal("second", 3.0)
                .id();
        return graph.node(MATH_ADD)
                .input("first", entity, axis)
                .input("second", step, RESULT)
                .id();
    }

    /**
     * The same mob, spawning only in End biomes.
     */
    private static CustomMob inTheEnd(CustomMob mob) {
        SpawnRules rules = mob.spawn();
        return new CustomMob(
                mob.id(),
                mob.equipment(),
                mob.attributes(),
                mob.behaviour(),
                mob.mobDrops(),
                mob.extension(),
                new SpawnRules(
                        rules.weight(),
                        rules.nightOnly(),
                        rules.nightEnd(),
                        rules.reasons(),
                        new SpawnConditions(List.of(), List.of("#minecraft:is_end"), null, null, null, Surface.ANY)));
    }

    /**
     * The same mob, also taken by subtypes of its type when no mob of their own fits, e.g. a zombie mob for husks and
     * drowned, or a spider mob for cave spiders.
     */
    private static CustomMob subtypesToo(CustomMob mob) {
        Attributes base = mob.attributes();
        return new CustomMob(
                mob.id(),
                mob.equipment(),
                new Attributes(
                        base.displayName(),
                        base.entityType(),
                        false,
                        base.healthModifier(),
                        base.health(),
                        base.damageModifier(),
                        base.damage(),
                        base.changes()),
                mob.behaviour(),
                mob.mobDrops(),
                mob.extension(),
                mob.spawn());
    }

    /**
     * Calls {@code teleport_near} for a target.
     */
    private static String teleportNear(MobGraph graph, String node, String port) {
        return graph.call(TELEPORT_NEAR).input(TARGET, node, port).id();
    }

    /**
     * Routine {@code teleport_near(target)}: teleports the mob next to the target when there is one more than five
     * blocks away, at most every five seconds, and only onto a spot with room for it. A purple pillar shows the spot
     * for a second before the mob arrives.
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
        Graphs.NodeBuilder landing = graph.node(VARIABLE_SET)
                .config("name", "landing")
                .config("type", "location")
                .input(VALUE, spot, RESULT);
        String landed = graph.node(VARIABLE_GET)
                .config("name", "landing")
                .config("type", "location")
                .id();
        String warn = graph.node(WORLD_SHOW_MARKER)
                .config("shape", "PILLAR")
                .input("location", landing.id(), VALUE)
                .input("color", graph.color(170, 40, 255), RESULT)
                .literal("size", 3.0)
                .literal("seconds", 1.0)
                .id();
        String teleport = graph.node(ENTITY_TELEPORT)
                .input(TARGET, graph.self(), "entity")
                .input("location", landed, VALUE)
                .id();
        String puff = graph.particlesAt("minecraft:portal", 10, target, "location");
        String arrive = graph.node(FLOW_DELAY)
                .literal("duration", 1.0)
                .chain("next", teleport, puff)
                .id();
        landing.chain("next", warn, arrive);
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
                                        graph.when(free, RESULT, graph.resetTimer("teleport"), landing.id())))));
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
        CustomMob dragon = mob(BLOOD_DRAGON, EntityType.ENDER_DRAGON, graph -> {
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
                        "<dark_red>" + base.displayName(),
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
                new SpawnRules(0, false, NightEnd.KEEP, Set.of(), SpawnConditions.any()));
    }

    private CustomMob mob(String id, EntityType type, Consumer<MobGraph> behaviour) {
        return mob(id, type, null, new Extension(ExtensionType.NONE, null, null), behaviour);
    }

    private CustomMob mob(String id, EntityType type, Equipment equipment, Consumer<MobGraph> behaviour) {
        return mob(id, type, equipment, new Extension(ExtensionType.NONE, null, null), behaviour);
    }

    private CustomMob mob(String id, EntityType type, Extension extension, Consumer<MobGraph> behaviour) {
        return mob(id, type, null, extension, behaviour);
    }

    private CustomMob mob(
            String id,
            EntityType type,
            @Nullable Equipment equipment,
            Extension extension,
            Consumer<MobGraph> behaviour) {
        MobGraph graph = new MobGraph(platform, type, extension.entityType());
        behaviour.accept(graph);
        return new CustomMob(
                id,
                equipment,
                new Attributes(translated(id), type, true, ValueModifier.DEFAULT, 2, ValueModifier.DEFAULT, 2),
                built(id, graph),
                MobDrops.defaultDrops(),
                extension,
                SpawnRules.defaults());
    }

    /**
     * The name of a default mob as MiniMessage: the text {@code bloodnight.mob.<id>} in the player's language, from the
     * names the store registers or the resource pack, else the English name.
     */
    private String translated(String id) {
        return "<lang_or:bloodnight.mob.%s:'%s'>".formatted(id, names.apply(id).replace("'", "\\'"));
    }

    private static Graph built(String id, MobGraph graph) {
        try {
            return graph.build();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Default mob %s: %s".formatted(id, e.getMessage()), e);
        }
    }
}
