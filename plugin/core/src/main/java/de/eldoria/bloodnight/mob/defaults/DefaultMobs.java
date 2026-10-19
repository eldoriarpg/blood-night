/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.defaults;

import de.eldoria.bloodnight.api.internal.MovementPreset;
import de.eldoria.bloodnight.graph.BloodNightNodes;
import de.eldoria.bloodnight.graph.runtime.world.ProjectileKind;
import de.eldoria.bloodnight.loot.assign.MobLoot;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.MobParticles;
import de.eldoria.bloodnight.mob.attributes.Attributes;
import de.eldoria.bloodnight.mob.attributes.MobName;
import de.eldoria.bloodnight.mob.attributes.Movement;
import de.eldoria.bloodnight.mob.attributes.ValueModifier;
import de.eldoria.bloodnight.mob.equipment.Equipment;
import de.eldoria.bloodnight.mob.equipment.Extension;
import de.eldoria.bloodnight.mob.equipment.ExtensionType;
import de.eldoria.bloodnight.mob.spawn.NightEnd;
import de.eldoria.bloodnight.mob.spawn.SpawnConditions;
import de.eldoria.bloodnight.mob.spawn.SpawnRules;
import de.eldoria.bloodnight.mob.spawn.Surface;
import de.eldoria.graph.compiler.validate.Graphs;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.platform.GraphPlatform;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
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
     * The magic skeleton's effects in the order its arrows bring them, with the dust colour that announces each.
     */
    private static final List<MagicEffect> MAGIC_EFFECTS = List.of(
            new MagicEffect("minecraft:slowness", 6, 90, 108, 129),
            new MagicEffect("minecraft:weakness", 6, 72, 77, 72),
            new MagicEffect("minecraft:poison", 5, 135, 163, 99),
            new MagicEffect("minecraft:nausea", 6, 85, 29, 74));

    /**
     * Blocks a leap covers per unit of sideways speed: air drag takes 9 % of it every tick, so about ten.
     */
    private static final double LEAP_REACH = 10;

    /**
     * Id of the blood dragon, the ender dragon of a dragon fight during a blood night.
     */
    public static final String BLOOD_DRAGON = "blood_dragon";

    private static final Set<String> INVISIBLE = Set.of("invisible_zombie", "invisible_skeleton", "ghost_creeper");
    private static final Map<String, String> AMBIENT = Map.ofEntries(
            Map.entry("blood_dragon", "blood_aura"),
            Map.entry("knight_zombie", "blood_aura"),
            Map.entry("armored_zombie", "blood_aura"),
            Map.entry("toxic_creeper", "toxic_haze"),
            Map.entry("toxic_enderman", "toxic_haze"),
            Map.entry("toxic_slime", "toxic_haze"),
            Map.entry("ender_creeper", "void_shimmer"),
            Map.entry("launcher_enderman", "void_shimmer"),
            Map.entry("grabber_enderman", "void_shimmer"),
            Map.entry("blaze_rider", "ember_crown"),
            Map.entry("fire_wizard", "ember_crown"),
            Map.entry("fire_phantom", "ember_crown"),
            Map.entry("thunder_wizard", "storm_charge"),
            Map.entry("storm_breeze", "storm_charge"),
            Map.entry("nervous_powered_creeper", "storm_charge"),
            Map.entry("wither_wizard", "soul_mist"),
            Map.entry("wither_skeleton_rider", "soul_mist"),
            Map.entry("phantom_soul", "soul_mist"),
            Map.entry("carrier_phantom", "soul_mist"),
            Map.entry("tide_drowned", "tide_drip"),
            Map.entry("spore_bogged", "spore_cloud"),
            Map.entry("fuse_enderman", "fuse_glow"),
            Map.entry("unstable_creeper", "fuse_glow"),
            Map.entry("speed_zombie", "speed_trail"),
            Map.entry("speed_creeper", "speed_trail"),
            Map.entry("speed_skeleton_rider", "speed_trail"),
            Map.entry("swarm_silverfish", "speed_trail"),
            Map.entry("fearful_enderman", "dread_shadow"),
            Map.entry("fearful_phantom", "dread_shadow"),
            Map.entry("assassin_spider", "dread_shadow"),
            Map.entry("volley_pillager", "dread_shadow"),
            Map.entry("magic_skeleton", "arcane_glow"));

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

    /**
     * Full diamond armour. Every hit from a player has a chance to knock off a piece, helmet first, then boots,
     * leggings and chestplate; with nothing left it runs faster.
     */
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
                graph -> {
                    String lost = graph.node(VARIABLE_GET)
                            .config("name", "lost")
                            .config("type", "integer")
                            .id();
                    var knock = graph.node(FLOW_SWITCH).input(VALUE, lost, VALUE);
                    List<EquipmentSlot> order =
                            List.of(EquipmentSlot.HEAD, EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST);
                    for (int i = 0; i < order.size(); i++) {
                        knock.config("case_" + i, i).chain("case_" + i, graph.equip(order.get(i), "minecraft:air"));
                    }
                    String count = graph.node(VARIABLE_SET)
                            .config("name", "lost")
                            .config("type", "integer")
                            .input(
                                    VALUE,
                                    graph.node(MATH_ADD)
                                            .input("first", lost, VALUE)
                                            .literal("second", 1)
                                            .id(),
                                    RESULT)
                            .id();
                    String armoured = graph.node(LOGIC_LESS)
                            .input("first", lost, VALUE)
                            .literal("second", order.size())
                            .id();
                    String damaged = graph.entry(EVENT_DAMAGE_BY_PLAYER);
                    String attacker = graph.as("player", damaged, "attacker");
                    graph.on(
                            EVENT_DAMAGE_BY_PLAYER,
                            graph.when(
                                    armoured,
                                    RESULT,
                                    graph.chance(
                                            15,
                                            knock.id(),
                                            count,
                                            graph.when(
                                                    attacker,
                                                    "matches",
                                                    graph.sound(
                                                            attacker,
                                                            RESULT,
                                                            "minecraft:entity.item.break",
                                                            1,
                                                            0.8)))));
                    graph.on(MOB_TICK, graph.unless(armoured, RESULT, graph.keepEffect("minecraft:speed", 1, false)));
                });
    }
    /**
     * Every eight seconds it raises a shield for two seconds, slowed, and blocks every hit from the front meanwhile;
     * every ten seconds it growls and dashes at its target. Its own hits sometimes knock the player up and slow them.
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

            String raised = graph.node(VARIABLE_GET)
                    .config("name", "raised")
                    .config("type", "boolean")
                    .id();
            String lower = graph.node(FLOW_DELAY)
                    .literal("duration", 2.0)
                    .chain("next", graph.equip(EquipmentSlot.OFF_HAND, "minecraft:air"), raise(graph, false))
                    .id();
            String targetPlayer = graph.as("player", graph.selfRead(), TARGET);
            String stance = graph.every(
                    8000,
                    raise(graph, true),
                    graph.equip(EquipmentSlot.OFF_HAND, "minecraft:shield"),
                    graph.effect(graph.self(), "entity", "minecraft:slowness", 2, 2),
                    graph.when(
                            targetPlayer,
                            "matches",
                            graph.sound(targetPlayer, RESULT, "minecraft:item.armor.equip_chain", 1, 0.8)),
                    lower);
            String target = graph.living(graph.selfRead(), TARGET);
            String charge = graph.selfMutate()
                    .input(
                            "velocity",
                            graph.node(VECTOR_CREATE)
                                    .input("x", towards(graph, target, "x"), RESULT)
                                    .literal("y", 0.3)
                                    .input("z", towards(graph, target, "z"), RESULT)
                                    .id(),
                            RESULT)
                    .id();
            String dash = graph.every(
                    10000,
                    graph.when(
                            targetPlayer,
                            "matches",
                            graph.sound(targetPlayer, RESULT, "minecraft:entity.zombie.ambient", 1, 0.5)),
                    graph.node(FLOW_DELAY)
                            .literal("duration", 0.6)
                            .chain("next", graph.present(graph.selfRead(), TARGET, charge))
                            .id());
            graph.on(MOB_TICK, graph.present(graph.selfRead(), TARGET, stance, dash));

            String damaged = graph.entry(EVENT_DAMAGE_BY_PLAYER);
            String attacker = graph.as("player", damaged, "attacker");
            String attackerEntity =
                    graph.node(ENTITY_READ).input(TARGET, attacker, RESULT).id();
            String front = facing(graph, graph.selfRead(), attackerEntity, 0.3);
            String blocks = graph.node(LOGIC_AND)
                    .input("first", raised, VALUE)
                    .input("second", front, RESULT)
                    .id();
            graph.on(
                    EVENT_DAMAGE_BY_PLAYER,
                    graph.when(
                            attacker,
                            "matches",
                            graph.when(
                                    blocks,
                                    RESULT,
                                    graph.cancel(damaged),
                                    graph.sound(attacker, RESULT, "minecraft:item.shield.block", 1, 1))));
        });
    }

    private static String raise(MobGraph graph, boolean raised) {
        return graph.node(VARIABLE_SET)
                .config("name", "raised")
                .config("type", "boolean")
                .literal(VALUE, raised)
                .id();
    }

    /**
     * Whether an entity faces another, compared on the ground: the dot product of its looking direction and the
     * direction to the other, both flattened and of length 1, is above the threshold (1 is straight at it).
     *
     * @param looker the node with the looking entity's read ({@code location}, {@code x}, {@code z})
     * @param other  the node with the other entity's read
     */
    private static String facing(MobGraph graph, String looker, String other, double threshold) {
        String direction = graph.node(VECTOR_READ)
                .input(
                        TARGET,
                        graph.node(LOCATION_READ)
                                .input(TARGET, looker, "location")
                                .id(),
                        "direction")
                .id();
        String looking = graph.node(VECTOR_NORMALIZE)
                .input(
                        VALUE,
                        graph.node(VECTOR_CREATE)
                                .input("x", direction, "x")
                                .literal("y", 0.0)
                                .input("z", direction, "z")
                                .id(),
                        RESULT)
                .id();
        String towardsOther = graph.node(VECTOR_NORMALIZE)
                .input(
                        VALUE,
                        graph.node(VECTOR_DIRECTION)
                                .input("from", flat(graph, looker, 0.0), RESULT)
                                .input("to", flat(graph, other, 0.01), RESULT)
                                .id(),
                        RESULT)
                .id();
        String dot = graph.node(VECTOR_DOT)
                .input("first", looking, RESULT)
                .input("second", towardsOther, RESULT)
                .id();
        return graph.node(LOGIC_GREATER)
                .input("first", dot, RESULT)
                .literal("second", threshold)
                .id();
    }
    /**
     * Invisible and without equipment, until it is hit; turns invisible again ten seconds after the last hit. While
     * invisible, faint dust shows at its feet.
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
            graph.on(
                    MOB_TICK,
                    graph.after(
                            "damaged",
                            10,
                            graph.every(1000, hide),
                            graph.every(400, graph.dustOnce(150, 150, 150, 0.8, 2))));
            for (String trigger : List.of(EVENT_DAMAGE_BY_ENTITY, EVENT_DAMAGE_BY_PLAYER)) {
                String reveal = graph.node(LIVING_ENTITY_REMOVE_POTION_EFFECT)
                        .input(TARGET, graph.self(), "entity")
                        .literal("type", "minecraft:invisibility")
                        .id();
                graph.on(trigger, graph.resetTimer("damaged"), reveal);
            }
        });
    }

    /**
     * Lunges instead of running all the time: every six seconds near its target it growls, then gets Speed IV for two
     * seconds and is slowed for a second after.
     */
    private CustomMob speedZombie() {
        return mob("speed_zombie", EntityType.ZOMBIE, graph -> {
            String target = graph.living(graph.selfRead(), TARGET);
            String targetPlayer = graph.as("player", graph.selfRead(), TARGET);
            String distance = graph.node(LOCATION_DISTANCE)
                    .input(TARGET, graph.selfRead(), "location")
                    .input("other", target, "location")
                    .id();
            String near = graph.node(LOGIC_LESS)
                    .input("first", distance, RESULT)
                    .literal("second", 12.0)
                    .id();
            String tired = graph.node(FLOW_DELAY)
                    .literal("duration", 2.0)
                    .chain("next", graph.effect(graph.self(), "entity", "minecraft:slowness", 1, 1))
                    .id();
            String lunge = graph.node(FLOW_DELAY)
                    .literal("duration", 0.5)
                    .chain("next", graph.effect(graph.self(), "entity", "minecraft:speed", 2, 3), tired)
                    .id();
            graph.on(
                    MOB_TICK,
                    graph.present(
                            graph.selfRead(),
                            TARGET,
                            graph.when(
                                    near,
                                    RESULT,
                                    graph.every(
                                            6000,
                                            graph.when(
                                                    targetPlayer,
                                                    "matches",
                                                    graph.sound(
                                                            targetPlayer,
                                                            RESULT,
                                                            "minecraft:entity.zombie.ambient",
                                                            1.5,
                                                            0.6)),
                                            lunge))));
        });
    }
    /**
     * Invisible, except for a second after each shot; its arrows glow, so players see where they come from.
     */
    private CustomMob invisibleSkeleton() {
        return mob("invisible_skeleton", EntityType.SKELETON, graph -> {
            graph.on(MOB_TICK, graph.after("shot", 1, graph.keepEffect("minecraft:invisibility", 1, false)));
            String shoot = graph.entry(EVENT_PROJECTILE_SHOOT);
            String glow = graph.node(ENTITY_MUTATE)
                    .input(TARGET, shoot, "entity")
                    .literal("glowing", true)
                    .id();
            String reveal = graph.node(LIVING_ENTITY_REMOVE_POTION_EFFECT)
                    .input(TARGET, graph.self(), "entity")
                    .literal("type", "minecraft:invisibility")
                    .id();
            graph.on(EVENT_PROJECTILE_SHOOT, graph.resetTimer("shot"), reveal, glow);
            smokeAtNightEnd(graph);
        });
    }
    /**
     * Its arrows put effects on the target in a fixed order: slowness, weakness, poison, nausea. Coloured dust on the
     * skeleton shows which one comes next.
     */
    private CustomMob magicSkeleton() {
        return mob("magic_skeleton", EntityType.SKELETON, graph -> {
            graph.on(MOB_TICK, graph.keepEffect("minecraft:night_vision", 1, true));
            String next = graph.node(VARIABLE_GET)
                    .config("name", "next")
                    .config("type", "integer")
                    .id();
            var show = graph.node(FLOW_SWITCH).input(VALUE, next, VALUE);
            var apply = graph.node(FLOW_SWITCH).input(VALUE, next, VALUE);
            String hit = graph.entry(EVENT_PROJECTILE_HIT);
            String victim = graph.as("living_entity", hit, "hit_entity");
            for (int i = 0; i < MAGIC_EFFECTS.size(); i++) {
                MagicEffect effect = MAGIC_EFFECTS.get(i);
                show.config("case_" + i, i)
                        .chain("case_" + i, graph.dustOnce(effect.red(), effect.green(), effect.blue(), 1.5, 6));
                apply.config("case_" + i, i)
                        .chain("case_" + i, graph.effect(victim, RESULT, effect.effect(), effect.seconds(), 1));
            }
            String advance = graph.node(VARIABLE_SET)
                    .config("name", "next")
                    .config("type", "integer")
                    .input(
                            VALUE,
                            graph.node(MATH_MODULO_INTEGER)
                                    .input(
                                            "first",
                                            graph.node(MATH_ADD)
                                                    .input("first", next, VALUE)
                                                    .literal("second", 1)
                                                    .id(),
                                            RESULT)
                                    .literal("second", MAGIC_EFFECTS.size())
                                    .id(),
                            RESULT)
                    .id();
            graph.on(MOB_TICK, graph.every(500, show.id()));
            graph.on(EVENT_PROJECTILE_HIT, graph.when(victim, "matches", apply.id(), advance));
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
     * Leaves small poison clouds behind it as it walks, and a cloud of strong poison where it explodes.
     */
    private CustomMob toxicCreeper() {
        return mob("toxic_creeper", EntityType.CREEPER, graph -> {
            graph.on(MOB_SPAWN, graph.creeper("max_fuse_ticks", 20));
            graph.on(
                    MOB_TICK,
                    graph.every(
                            3000, graph.cloud(graph.selfRead(), "location", "minecraft:poison", 3, 1.5, 0, 128, 0)));
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
                    graph.creeper("explosion_radius", 5),
                    graph.creeper("powered", true),
                    graph.creeper("max_fuse_ticks", 50));
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
                    graph.node(FLOW_DO_ONCE)
                            .chain(
                                    "next",
                                    warn,
                                    graph.effect(graph.self(), "entity", "minecraft:glowing", 1, 0),
                                    ignite,
                                    fuse)
                            .id());
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
            graph.on(MOB_TICK, teleportNear(graph, graph.selfRead(), TARGET));
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
     * Rides an invisible vex; neither takes suffocation damage, and the creeper dies with its vex. Above its target it
     * hisses, and a second later the vex lets go: the creeper drops straight down and takes no fall damage.
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
            String noFall = graph.node(LOGIC_OR)
                    .input("first", causeIs(graph, damage, "SUFFOCATION"), RESULT)
                    .input("second", causeIs(graph, damage, "FALL"), RESULT)
                    .id();
            graph.on(EVENT_DAMAGE, graph.when(noFall, RESULT, graph.cancel(damage)));
            bindToExtension(graph);
            String remove =
                    graph.node(ENTITY_REMOVE).input(TARGET, extension, "entity").id();
            graph.on(EVENT_EXPLODE, graph.present(extension, "entity", remove));

            String target = graph.living(graph.selfRead(), TARGET);
            String targetPlayer = graph.as("player", graph.selfRead(), TARGET);
            String height = graph.node(MATH_SUBTRACT)
                    .input("first", graph.selfRead(), "y")
                    .input("second", target, "y")
                    .id();
            String high = graph.node(LOGIC_GREATER)
                    .input("first", height, RESULT)
                    .literal("second", 3.0)
                    .id();
            String across = graph.node(VECTOR_DISTANCE)
                    .input("first", flat(graph, graph.selfRead(), 0), RESULT)
                    .input("second", flat(graph, target, 0), RESULT)
                    .id();
            String over = graph.node(LOGIC_LESS)
                    .input("first", across, RESULT)
                    .literal("second", 3.0)
                    .id();
            String above = graph.node(LOGIC_AND)
                    .input("first", high, RESULT)
                    .input("second", over, RESULT)
                    .id();
            String letGo = graph.present(
                    extension,
                    "entity",
                    graph.node(ENTITY_DROP_PASSENGERS)
                            .input(TARGET, extension, "entity")
                            .id(),
                    graph.node(ENTITY_REMOVE).input(TARGET, extension, "entity").id());
            String dive = graph.node(FLOW_DELAY)
                    .literal("duration", 1.0)
                    .chain("next", letGo)
                    .id();
            graph.on(
                    MOB_TICK,
                    graph.present(
                            graph.selfRead(),
                            TARGET,
                            graph.when(
                                    above,
                                    RESULT,
                                    graph.node(FLOW_DO_ONCE)
                                            .chain(
                                                    "next",
                                                    graph.when(
                                                            targetPlayer,
                                                            "matches",
                                                            graph.sound(
                                                                    targetPlayer,
                                                                    RESULT,
                                                                    "minecraft:entity.creeper.primed",
                                                                    1.5,
                                                                    0.8)),
                                                    dive)
                                            .id())));
        });
    }
    /**
     * Every twelve seconds it screams; a second later every player within twenty blocks who still faces it is blinded
     * for four seconds, so looking away in time avoids it. Its hits blind as well.
     */
    private CustomMob fearfulEnderman() {
        return mob("fearful_enderman", EntityType.ENDERMAN, graph -> {
            String scream = playersAround(
                    graph, 20, player -> graph.sound(player, RESULT, "minecraft:entity.enderman.scream", 1.5, 0.7));
            String stare = playersAround(graph, 20, player -> {
                String playerEntity =
                        graph.node(ENTITY_READ).input(TARGET, player, RESULT).id();
                String looks = facing(graph, playerEntity, graph.selfRead(), 0.5);
                return graph.when(looks, RESULT, graph.effect(player, RESULT, "minecraft:blindness", 4, 0));
            });
            String gaze = graph.node(FLOW_DELAY)
                    .literal("duration", 1.0)
                    .chain("next", stare)
                    .id();
            graph.on(
                    MOB_TICK,
                    graph.targetNearestPlayer(),
                    graph.present(graph.selfRead(), TARGET, graph.every(12000, scream, gaze)));
            blindPlayersOnHit(graph, 7, 1);
            graph.on(MOB_NIGHT_END, graph.particlesAroundWith("minecraft:dragon_breath", 10, graph.power(1), "power"));
        });
    }

    /**
     * Runs a chain for every player within a radius of the mob.
     *
     * @param body the chain for one player, given the node whose {@code result} is the player
     */
    private static String playersAround(MobGraph graph, double radius, Function<String, String> body) {
        String nearby = graph.node(ENTITY_NEARBY_ENTITIES)
                .input(TARGET, graph.self(), "entity")
                .literal("x", radius)
                .literal("y", radius / 2)
                .literal("z", radius)
                .id();
        Graphs.NodeBuilder each = graph.node(FLOW_FOR_EACH).input("list", nearby, RESULT);
        String player = graph.as("player", each.id(), "element");
        each.chain("body", graph.when(player, "matches", body.apply(player)));
        return each.id();
    }
    /**
     * Leaves a cloud of strong poison where it teleports away from.
     */
    private CustomMob toxicEnderman() {
        return mob("toxic_enderman", EntityType.ENDERMAN, graph -> {
            graph.on(MOB_TICK, graph.targetNearestPlayer());
            String teleport = graph.entry(EVENT_TELEPORT);
            graph.on(EVENT_TELEPORT, graph.cloud(teleport, "from", "minecraft:strong_poison", 10, 4, 0, 128, 0));
            graph.on(MOB_NIGHT_END, graph.particlesAroundWith("minecraft:dragon_breath", 10, graph.power(1), "power"));
        });
    }

    /**
     * Glows; its hits slow the player, and a player gliding with an elytra is knocked out of the air.
     */
    private CustomMob fearfulPhantom() {
        return mob("fearful_phantom", EntityType.PHANTOM, graph -> {
            graph.on(MOB_TICK, graph.keepEffect("minecraft:glowing", 1, true));
            String hit = graph.entry(EVENT_HIT);
            String victim = graph.as("player", hit, "entity");
            String gliding =
                    graph.node(LIVING_ENTITY_READ).input(TARGET, victim, RESULT).id();
            String down = graph.node(VECTOR_CREATE)
                    .literal("x", 0.0)
                    .literal("y", -1.0)
                    .literal("z", 0.0)
                    .id();
            String ground = graph.node(LIVING_ENTITY_MUTATE)
                    .input(TARGET, victim, RESULT)
                    .literal("gliding", false)
                    .input("velocity", down, RESULT)
                    .id();
            graph.on(
                    EVENT_HIT,
                    graph.when(
                            victim,
                            "matches",
                            graph.effect(victim, RESULT, "minecraft:slowness", 7, 2),
                            graph.when(gliding, "gliding", ground)));
            smokeAtNightEnd(graph);
        });
    }
    /**
     * Carries a blaze that attacks whatever the phantom targets, and drops a small fireball on its target every six
     * seconds. The blaze goes with the phantom when it dies and when the night ends.
     */
    private CustomMob firePhantom() {
        Extension blaze = new Extension(ExtensionType.CARRIER, EntityType.BLAZE, null);
        return mob("fire_phantom", EntityType.PHANTOM, blaze, graph -> {
            graph.forwardTargetToExtension();
            String extension = graph.extension();
            for (String trigger : List.of(MOB_NIGHT_END, EVENT_DEATH)) {
                String remove = graph.node(ENTITY_REMOVE)
                        .input(TARGET, extension, "entity")
                        .id();
                graph.on(trigger, graph.present(extension, "entity", remove));
            }
            graph.on(
                    MOB_TICK,
                    graph.present(
                            graph.selfRead(),
                            TARGET,
                            graph.every(6000, graph.shootAtTarget(ProjectileKind.SMALL_FIREBALL, 1.5))));
        });
    }
    /**
     * Invisible while it circles; within twelve blocks of its target it shows itself and glows. Its hits blind for
     * three seconds.
     */
    private CustomMob phantomSoul() {
        return mob("phantom_soul", EntityType.PHANTOM, graph -> {
            String target = graph.living(graph.selfRead(), TARGET);
            String distance = graph.node(LOCATION_DISTANCE)
                    .input(TARGET, graph.selfRead(), "location")
                    .input("other", target, "location")
                    .id();
            String close = graph.node(LOGIC_LESS)
                    .input("first", distance, RESULT)
                    .literal("second", 12.0)
                    .id();
            String reveal = graph.node(LIVING_ENTITY_REMOVE_POTION_EFFECT)
                    .input(TARGET, graph.self(), "entity")
                    .literal("type", "minecraft:invisibility")
                    .id();
            String hidden = graph.keepEffect("minecraft:invisibility", 1, false);
            String diving = graph.node(FLOW_BRANCH)
                    .input("condition", close, RESULT)
                    .chain("true", reveal, graph.keepEffect("minecraft:glowing", 1, true))
                    .chain("false", hidden)
                    .id();
            graph.on(
                    MOB_TICK,
                    graph.node(FLOW_BRANCH)
                            .input(
                                    "condition",
                                    graph.node(LOGIC_IS_NULL)
                                            .input(VALUE, graph.selfRead(), TARGET)
                                            .id(),
                                    RESULT)
                            .chain("true", graph.keepEffect("minecraft:invisibility", 1, false))
                            .chain("false", diving)
                            .id());
            blindPlayersOnHit(graph, 3, 0);
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
                            graph.effectAt("bloodnight:shadow_wings", victim, RESULT),
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
     * Throws a large fireball at its target every five seconds instead of potions, a second after lava starts dripping
     * around it; fireballs hitting a player deal the witch's attack damage on top. A fireball hit back at it deals
     * triple damage.
     */
    private CustomMob fireWizard() {
        Equipment held = new Equipment(null, null, null, null, "minecraft:fire_charge", null);
        return mob("fire_wizard", EntityType.WITCH, held, graph -> {
            String throwIt = graph.node(FLOW_DELAY)
                    .literal("duration", 1.0)
                    .chain(
                            "next",
                            graph.present(
                                    graph.selfRead(), TARGET, graph.shootAtTarget(ProjectileKind.LARGE_FIREBALL, 4)))
                    .id();
            graph.on(
                    MOB_TICK,
                    graph.present(
                            graph.selfRead(),
                            TARGET,
                            graph.every(
                                    5000,
                                    graph.particlesAround("minecraft:dripping_lava", 40),
                                    graph.effectOnSelf("bloodnight:rune_circle"),
                                    graph.effectAt("bloodnight:arcane_lines", graph.selfRead(), TARGET),
                                    throwIt)));
            onlyShoot(graph, "minecraft:fireball");
            String struck = graph.entry(EVENT_DAMAGE_BY_ENTITY);
            String damager =
                    graph.node(ENTITY_READ).input(TARGET, struck, "damager").id();
            String fireball = graph.node(LOGIC_EQUAL)
                    .input("first", damager, "type")
                    .input("second", graph.value("entity_type", "minecraft:fireball"), VALUE)
                    .id();
            String tripled = graph.node(EVENT_DATA_ENTITY_DAMAGE_MUTATE)
                    .input(TARGET, struck, "event")
                    .input(
                            "damage",
                            graph.node(MATH_MULTIPLY)
                                    .input("first", struck, "damage")
                                    .literal("second", 3.0)
                                    .id(),
                            RESULT)
                    .id();
            graph.on(EVENT_DAMAGE_BY_ENTITY, graph.when(fireball, RESULT, tripled));

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
     * Every four seconds it calls three strikes after its target, half a second apart, each on the spot the target
     * stood on then. A red ring grows on each spot for the 1.5 seconds before its strike, so the player keeps moving.
     */
    private CustomMob thunderWizard() {
        return mob("thunder_wizard", EntityType.WITCH, graph -> {
            String third = strikeFollowing(graph, 2, null);
            String second = strikeFollowing(graph, 1, third);
            String first = strikeFollowing(graph, 0, second);
            String sees = graph.node(LIVING_ENTITY_HAS_LINE_OF_SIGHT)
                    .input(TARGET, graph.self(), "entity")
                    .input("other", graph.selfRead(), TARGET)
                    .id();
            graph.on(
                    MOB_TICK,
                    graph.present(
                            graph.selfRead(),
                            TARGET,
                            graph.every(
                                    4000,
                                    graph.when(sees, RESULT, graph.effectOnSelf("bloodnight:rune_circle"), first))));
            String shoot = graph.entry(EVENT_PROJECTILE_SHOOT);
            graph.on(EVENT_PROJECTILE_SHOOT, graph.cancel(shoot));
            smokeAtNightEnd(graph);
        });
    }

    /**
     * One strike of the thunder wizard: marks where its target stands, rings the spot, strikes it 1.5 seconds later,
     * and half a second after the mark starts the next strike, if any.
     *
     * @return the node that marks the spot
     */
    private static String strikeFollowing(MobGraph graph, int number, @Nullable String next) {
        String name = "mark_" + number;
        String target = graph.living(graph.selfRead(), TARGET);
        String marked = graph.node(VARIABLE_GET)
                .config("name", name)
                .config("type", "location")
                .id();
        String strike = graph.node(WORLD_STRIKE_LIGHTNING)
                .input(TARGET, graph.selfRead(), "world")
                .input("location", marked, VALUE)
                .id();
        String bolt =
                graph.playEffect("bloodnight:lightning_bolt", marked, VALUE).id();
        String wait = graph.node(FLOW_DELAY)
                .literal("duration", 1.5)
                .chain("next", strike, bolt)
                .id();
        Graphs.NodeBuilder mark = graph.node(VARIABLE_SET)
                .config("name", name)
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
        if (next == null) {
            mark.chain("next", flash, warn, wait);
        } else {
            String then = graph.node(FLOW_DELAY)
                    .literal("duration", 0.5)
                    .chain("next", graph.present(graph.selfRead(), TARGET, next))
                    .id();
            mark.chain("next", flash, warn, wait, then);
        }
        return mark.id();
    }
    /**
     * Throws three wither skulls at its target, a third of a second apart, then rests for eight seconds. Once its health
     * drops below half, it heals itself a single time.
     */
    private CustomMob witherWizard() {
        Equipment held = new Equipment(null, null, null, null, "minecraft:wither_skeleton_skull", null);
        return mob("wither_wizard", EntityType.WITCH, held, graph -> {
            String volley = null;
            for (int i = 0; i < 2; i++) {
                String skull =
                        graph.present(graph.selfRead(), TARGET, graph.shootAtTarget(ProjectileKind.WITHER_SKULL, 4));
                Graphs.NodeBuilder pause = graph.node(FLOW_DELAY).literal("duration", 0.35);
                volley = (volley == null ? pause.chain("next", skull) : pause.chain("next", skull, volley)).id();
            }
            graph.on(
                    MOB_TICK,
                    graph.present(
                            graph.selfRead(),
                            TARGET,
                            graph.every(
                                    8000,
                                    graph.effectOnSelf("bloodnight:rune_circle"),
                                    graph.effectAt("bloodnight:arcane_lines", graph.selfRead(), TARGET),
                                    graph.shootAtTarget(ProjectileKind.WITHER_SKULL, 4),
                                    volley)));
            onlyShoot(graph, "minecraft:wither_skull");
            String damage = graph.entry(EVENT_DAMAGE);
            String attribute = graph.node(LIVING_ENTITY_ATTRIBUTE)
                    .input(TARGET, graph.self(), "entity")
                    .literal("attribute", "minecraft:max_health")
                    .id();
            String max =
                    graph.node(ATTRIBUTE_READ).input(TARGET, attribute, RESULT).id();
            String half = graph.node(MATH_MULTIPLY)
                    .input("first", max, VALUE)
                    .literal("second", 0.5)
                    .id();
            String left = graph.node(MATH_SUBTRACT)
                    .input("first", graph.selfRead(), "health")
                    .input("second", damage, "final_damage")
                    .id();
            String low = graph.node(LOGIC_LESS)
                    .input("first", left, RESULT)
                    .input("second", half, RESULT)
                    .id();
            String heal = graph.node(FLOW_DO_ONCE)
                    .chain(
                            "next",
                            graph.effect(graph.self(), "entity", "minecraft:regeneration", 6, 2),
                            graph.effectOnSelf("bloodnight:heal_burst"))
                    .id();
            graph.on(EVENT_DAMAGE, graph.present(attribute, RESULT, graph.when(low, RESULT, heal)));
            smokeAtNightEnd(graph);
        });
    }
    /**
     * A spider carrying a blaze; players right next to it catch fire.
     */
    private CustomMob blazeRider() {
        return rider(
                "blaze_rider",
                EntityType.BLAZE,
                false,
                graph -> graph.on(
                        MOB_TICK, graph.every(1000, playersAround(graph, 2.5, player -> graph.node(ENTITY_MUTATE)
                                .input(TARGET, player, RESULT)
                                .literal("fire_ticks", 60)
                                .id()))));
    }
    /**
     * A fast spider carrying a skeleton; when its target comes close, the spider leaps away so the skeleton can shoot.
     */
    private CustomMob speedSkeletonRider() {
        return rider("speed_skeleton_rider", EntityType.SKELETON, true, graph -> {
            String target = graph.living(graph.selfRead(), TARGET);
            String distance = graph.node(LOCATION_DISTANCE)
                    .input(TARGET, graph.selfRead(), "location")
                    .input("other", target, "location")
                    .id();
            String close = graph.node(LOGIC_LESS)
                    .input("first", distance, RESULT)
                    .literal("second", 5.0)
                    .id();
            String away = graph.node(VECTOR_NORMALIZE)
                    .input(
                            VALUE,
                            graph.node(VECTOR_DIRECTION)
                                    .input("from", flat(graph, target, 0), RESULT)
                                    .input("to", flat(graph, graph.selfRead(), 0.01), RESULT)
                                    .id(),
                            RESULT)
                    .id();
            String leap = graph.selfMutate()
                    .input(
                            "velocity",
                            graph.node(VECTOR_CREATE)
                                    .input("x", scaled(graph, away, "x", 1.1), RESULT)
                                    .literal("y", 0.5)
                                    .input("z", scaled(graph, away, "z", 1.1), RESULT)
                                    .id(),
                            RESULT)
                    .id();
            graph.on(
                    MOB_TICK,
                    graph.present(
                            graph.selfRead(),
                            TARGET,
                            graph.when(
                                    close,
                                    RESULT,
                                    graph.when(graph.selfRead(), "on_ground", graph.every(5000, leap)))));
        });
    }
    /**
     * A fast spider carrying a wither skeleton; the spider's bite webs the player in place for a moment.
     */
    private CustomMob witherSkeletonRider() {
        return rider("wither_skeleton_rider", EntityType.WITHER_SKELETON, true, graph -> {
            String hit = graph.entry(EVENT_HIT);
            String victim = graph.as("player", hit, "entity");
            graph.on(
                    EVENT_HIT,
                    graph.when(
                            victim,
                            "matches",
                            graph.effect(victim, RESULT, "minecraft:slowness", 1.5, 4),
                            graph.sound(victim, RESULT, "minecraft:block.cobweb.place", 1, 0.8)));
        });
    }
    /**
     * A spider carrying another mob that attacks whatever the spider targets; the spider dies with its rider.
     *
     * @param role what makes this rider different from the others
     */
    private CustomMob rider(String id, EntityType passenger, boolean fast, Consumer<MobGraph> role) {
        Extension extension = new Extension(ExtensionType.CARRIER, passenger, null);
        return mob(id, EntityType.SPIDER, extension, graph -> {
            graph.forwardTargetToExtension();
            bindToExtension(graph);
            if (fast) graph.on(MOB_TICK, graph.keepEffect("minecraft:speed", 1, true));
            role.accept(graph);
            smokeAtNightEnd(graph);
        });
    }
    /**
     * Its hits poison for three seconds, and it leaves a cloud of strong poison where it dies.
     */
    private CustomMob toxicSlime() {
        return mob("toxic_slime", EntityType.SLIME, graph -> {
            String death = graph.entry(EVENT_DEATH);
            String body = graph.living(death, "entity");
            graph.on(EVENT_DEATH, graph.cloud(body, "location", "minecraft:strong_poison", 5, 3, 78, 147, 49));
            String hit = graph.entry(EVENT_HIT);
            String victim = graph.as("living_entity", hit, "entity");
            graph.on(EVENT_HIT, graph.when(victim, "matches", graph.effect(victim, RESULT, "minecraft:poison", 3, 0)));
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
                                            graph.effectAt("bloodnight:blood_spear", graph.selfRead(), TARGET),
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
                                    graph.every(4000, leap, graph.effectOnSelf("bloodnight:shock_wave"))))
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
                            graph.playEffect("bloodnight:teleport_puff", sky, RESULT)
                                    .id(),
                            graph.effectAt("bloodnight:shadow_wings", held, RESULT),
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
            graph.on(MOB_TICK, graph.targetNearestPlayer());
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
                            graph.every(
                                    10000,
                                    each.id(),
                                    graph.playEffect("bloodnight:teleport_puff", front, RESULT)
                                            .id())));
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
                mob.loot(),
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
                        base.name(),
                        base.entityType(),
                        false,
                        base.healthModifier(),
                        base.health(),
                        base.damageModifier(),
                        base.damage(),
                        base.changes()),
                mob.behaviour(),
                mob.loot(),
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
        String puff =
                graph.playEffect("bloodnight:teleport_puff", target, "location").id();
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
        graph.on(MOB_NIGHT_END, graph.effectOnSelf("bloodnight:night_end_smoke"));
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
            String bolt = graph.playEffect("bloodnight:lightning_bolt", victim, "location")
                    .id();
            String pierce = graph.node(LIVING_ENTITY_DAMAGE)
                    .input(TARGET, nearest, "player")
                    .literal("amount", 6.0)
                    .input("source", graph.self(), "entity")
                    .id();
            String spear = graph.playEffect("bloodnight:blood_spear", graph.self(), "entity")
                    .input("target", nearest, "player")
                    .chain("arrived", graph.present(nearest, "player", pierce))
                    .id();
            graph.on(
                    MOB_TICK,
                    graph.every(6000, graph.present(nearest, "player", fireball)),
                    graph.every(10000, graph.present(nearest, "player", lightning, bolt)),
                    graph.every(8000, graph.present(nearest, "player", spear)));
            String hit = graph.entry(EVENT_HIT);
            String hitPlayer = graph.as("player", hit, "entity");
            graph.on(
                    EVENT_HIT,
                    graph.when(
                            hitPlayer,
                            "matches",
                            graph.effect(hitPlayer, RESULT, "minecraft:wither", 5, 1),
                            graph.effectAt("bloodnight:drain_beam", hitPlayer, RESULT)));
        });
        Attributes base = dragon.attributes();
        return new CustomMob(
                dragon.id(),
                dragon.equipment(),
                new Attributes(
                        new MobName("<dark_red>", base.name().texts()),
                        base.entityType(),
                        true,
                        ValueModifier.VALUE,
                        400,
                        ValueModifier.MULTIPLY,
                        1.5,
                        List.of()),
                dragon.behaviour(),
                dragon.loot(),
                dragon.extension(),
                new SpawnRules(0, false, NightEnd.KEEP, Set.of(), SpawnConditions.any()),
                null,
                dragon.movement(),
                dragon.particles());
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
                        MobLoot.inherited(),
                        extension,
                        SpawnRules.defaults())
                .withParticles(looks(id));
    }

    /**
     * The particle effects a default mob wears: an effect of its own while it lives, souls rising when it spawns and a
     * burst where it dies. Invisible mobs wear none, so nothing gives them away.
     */
    private static @Nullable MobParticles looks(String id) {
        if (INVISIBLE.contains(id)) return null;
        String ambient = AMBIENT.get(id);
        return new MobParticles(
                ambient == null ? null : "bloodnight:" + ambient, "bloodnight:spawn_rise", "bloodnight:death_burst");
    }

    /**
     * The name of a default mob in English; the store generator adds the other languages from its package.
     */
    private MobName translated(String id) {
        return MobName.english(names.apply(id));
    }

    private static Graph built(String id, MobGraph graph) {
        try {
            return graph.build();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Default mob %s: %s".formatted(id, e.getMessage()), e);
        }
    }

    /**
     * One effect of the magic skeleton's arrows and the dust colour that announces it.
     */
    private record MagicEffect(String effect, double seconds, int red, int green, int blue) {}
}
