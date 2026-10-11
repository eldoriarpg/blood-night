/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.defaults;

import de.eldoria.bloodnight.graph.BloodNightNodes;
import de.eldoria.bloodnight.graph.runtime.ProjectileKind;
import de.eldoria.graph.compiler.Graphs;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.platform.GraphPlatform;
import de.eldoria.graph.std.StdNodes;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Builds one mob behaviour from the fragments the default mobs have in common: effects kept on the mob, particles
 * around it, timed actions, targets and events.
 * <p>
 * Every method places nodes and returns the id of the node to chain, or of the node holding the value asked for. Nodes
 * that only read something, such as the mob itself or its location, are placed once and shared; they are evaluated
 * again in every chain that reads them.
 */
final class MobGraph implements BloodNightNodes {
    private static final String NEXT = "next";
    private static final String RESULT = "result";
    private static final String TARGET = "target";
    private static final String VALUE = "value";
    private static final double EFFECT_SECONDS = 60;
    private static final int EFFECT_REFRESH_MILLIS = 30_000;
    private static final int PARTICLE_MILLIS = 250;

    private final Graphs graphs;
    private final Map<String, String> shared = new HashMap<>();
    private final Map<String, Graphs.NodeBuilder> entries = new HashMap<>();
    private int next;

    MobGraph(GraphPlatform platform) {
        this.graphs = Graphs.with(platform);
    }

    Graph build() {
        return graphs.build();
    }

    /**
     * Places a node with a generated id.
     */
    Graphs.NodeBuilder node(String type) {
        return graphs.node("%s_%d".formatted(type.substring(type.lastIndexOf('.') + 1), next++), type);
    }

    /**
     * The entry or trigger node of a type; there is one per graph.
     */
    String entry(String type) {
        Graphs.NodeBuilder entry = entries.get(type);
        if (entry == null) {
            entry = node(type);
            entries.put(type, entry);
        }
        return entry.id();
    }

    /**
     * Appends nodes to what an entry or trigger runs.
     */
    void on(String type, String... chain) {
        entry(type);
        entries.get(type).chain(NEXT, chain);
    }

    String self() {
        return once("self", () -> node(MOB_SELF).id());
    }

    /**
     * The mob as a more specific type; output {@code result}.
     */
    String selfAs(String selectable) {
        return once("self_as_" + selectable, () -> as(selectable, self(), "entity"));
    }

    /**
     * The getters of the mob; outputs such as {@code location}, {@code eye_location}, {@code world}, {@code target}.
     */
    String selfMob() {
        return once(
                "self_mob",
                () -> node(MOB_GET).input(TARGET, selfAs("mob"), RESULT).id());
    }

    /**
     * The getters of any entity; outputs such as {@code location}, {@code world}.
     */
    String entity(String node, String port) {
        return node(ENTITY_GET).input(TARGET, node, port).id();
    }

    /**
     * A value as a more specific type; outputs {@code result} and {@code matches}.
     */
    String as(String selectable, String node, String port) {
        return node(StdNodes.cast(selectable)).input(VALUE, node, port).id();
    }

    /**
     * Keeps a potion effect on the mob, refreshed well before it runs out.
     */
    String keepEffect(String effect, int amplifier, boolean visible) {
        return every(
                EFFECT_REFRESH_MILLIS,
                node(LIVING_ENTITY_APPLY_EFFECT)
                        .input(TARGET, self(), "entity")
                        .literal("effect", effect)
                        .literal("seconds", EFFECT_SECONDS)
                        .literal("amplifier", amplifier)
                        .literal("visible", visible)
                        .id());
    }

    /**
     * Applies a visible potion effect to a living entity.
     */
    String effect(String node, String port, String effect, double seconds, int amplifier) {
        return node(LIVING_ENTITY_APPLY_EFFECT)
                .input(TARGET, node, port)
                .literal("effect", effect)
                .literal("seconds", seconds)
                .literal("amplifier", amplifier)
                .id();
    }

    /**
     * Spawns particles around the mob a few times a second.
     */
    String particles(String particle, int count) {
        return every(PARTICLE_MILLIS, particlesAround(particle, count));
    }

    /**
     * Spawns particles around the mob once.
     */
    String particlesAround(String particle, int count) {
        return particlesAt(particle, count, selfMob(), "eye_location");
    }

    /**
     * Spawns particles around a location once.
     */
    String particlesAt(String particle, int count, String node, String port) {
        return node(WORLD_SPAWN_PARTICLE)
                .input(TARGET, selfMob(), "world")
                .literal("particle", particle)
                .input("location", node, port)
                .literal("count", count)
                .literal("offset_x", 1.5)
                .literal("offset_y", 1.0)
                .literal("offset_z", 1.5)
                .id();
    }

    /**
     * Spawns coloured dust around the mob a few times a second.
     */
    String dust(int red, int green, int blue, double size, int count) {
        return every(PARTICLE_MILLIS, dustOnce(red, green, blue, size, count));
    }

    /**
     * Spawns coloured dust around the mob once.
     */
    String dustOnce(int red, int green, int blue, double size, int count) {
        String dust = node(DUST_CREATE)
                .input("color", color(red, green, blue), RESULT)
                .literal("size", size)
                .id();
        return node(WORLD_SPAWN_PARTICLE_WITH_DATA)
                .input(TARGET, selfMob(), "world")
                .literal("particle", "minecraft:dust")
                .input("location", selfMob(), "eye_location")
                .literal("count", count)
                .literal("offset_x", 1.5)
                .literal("offset_y", 1.0)
                .literal("offset_z", 1.5)
                .input("data", dust, RESULT)
                .id();
    }

    String color(int red, int green, int blue) {
        return node(COLOR_RGB)
                .literal("red", red)
                .literal("green", green)
                .literal("blue", blue)
                .id();
    }

    /**
     * Runs a chain at most once every so many milliseconds.
     */
    String every(int millis, String... chain) {
        String cooldown = node(FUNCTION_COOLDOWN).literal(VALUE, millis).id();
        return when(cooldown, RESULT, chain);
    }

    /**
     * Runs a chain when a condition holds.
     */
    String when(String node, String port, String... chain) {
        return node(FLOW_BRANCH)
                .input("condition", node, port)
                .chain("true", chain)
                .id();
    }

    /**
     * Runs a chain when a condition does not hold.
     */
    String unless(String node, String port, String... chain) {
        return node(FLOW_BRANCH)
                .input("condition", node, port)
                .chain("false", chain)
                .id();
    }

    /**
     * Runs a chain when a value is present.
     */
    String present(String node, String port, String... chain) {
        String missing = node(LOGIC_IS_NULL).input(VALUE, node, port).id();
        return unless(missing, RESULT, chain);
    }

    /**
     * Runs a chain with the given probability in percent.
     */
    String chance(double percent, String... chain) {
        String chance = node(FUNCTION_CHANCE).literal("percent", percent).id();
        return when(chance, RESULT, chain);
    }

    /**
     * Runs a chain once a named timer has run for at least so many seconds.
     */
    String after(String timer, double seconds, String... chain) {
        String elapsed = node(TIMER_ELAPSED).config("timer", timer).id();
        String passed = node(LOGIC_GREATER_OR_EQUAL)
                .input("first", elapsed, "seconds")
                .literal("second", seconds)
                .id();
        return when(passed, RESULT, chain);
    }

    String resetTimer(String timer) {
        return node(TIMER_RESET).config("timer", timer).id();
    }

    /**
     * Cancels the event of a trigger.
     */
    String cancel(String trigger) {
        return node(CANCELLABLE_SET_CANCELLED)
                .input(TARGET, trigger, "event")
                .literal(VALUE, true)
                .id();
    }

    /**
     * Puts an item into an equipment slot of the mob.
     *
     * @param setter one of the {@code equipment.set_*} node types
     * @param item   item type key, {@code minecraft:air} to empty the slot
     */
    String equip(String setter, String item) {
        return equip(selfMob(), setter, item);
    }

    /**
     * Puts an item into an equipment slot of a mob.
     */
    String equip(String mobGetter, String setter, String item) {
        String stack = node(ITEM_TYPE_CREATE_ITEM_STACK).literal(TARGET, item).id();
        return node(setter)
                .input(TARGET, mobGetter, "equipment")
                .input(VALUE, stack, RESULT)
                .id();
    }

    /**
     * Calls a setter of the creeper this mob is.
     */
    String creeper(String setter, Object value) {
        return node(setter)
                .input(TARGET, selfAs("creeper"), RESULT)
                .literal(VALUE, value)
                .id();
    }

    /**
     * Plays a sound to a player at their own location.
     */
    String sound(String player, String port, String sound, double volume, double pitch) {
        return node(PLAYER_PLAY_SOUND)
                .input(TARGET, player, port)
                .input("location", entity(player, port), "location")
                .literal("sound", sound)
                .literal("volume", volume)
                .literal("pitch", pitch)
                .id();
    }

    /**
     * Leaves a lingering cloud of a potion at a location.
     */
    String cloud(String node, String port, String potion, int seconds, double radius, int red, int green, int blue) {
        return node(WORLD_SPAWN_POTION_CLOUD)
                .input("location", node, port)
                .literal("potion", potion)
                .literal("seconds", seconds)
                .literal("radius", radius)
                .literal("radius_per_tick", 0.01)
                .input("color", color(red, green, blue), RESULT)
                .id();
    }

    /**
     * Fires a projectile from the mob at its current target, when it has one.
     */
    String shootAtTarget(ProjectileKind projectile, double speed) {
        return node(LIVING_ENTITY_LAUNCH_PROJECTILE)
                .config("projectile", projectile)
                .input("shooter", self(), "entity")
                .input(TARGET, selfMob(), TARGET)
                .literal("speed", speed)
                .id();
    }

    /**
     * Makes the mob target the nearest survival player within 16 blocks while it has no target.
     */
    String targetNearestPlayer() {
        String nearest = node(WORLD_NEAREST_PLAYER)
                .input("location", selfMob(), "location")
                .literal("radius", 16.0)
                .id();
        String target = node(MOB_SET_TARGET)
                .input(TARGET, selfAs("mob"), RESULT)
                .input(VALUE, nearest, "player")
                .id();
        String noTarget = node(LOGIC_IS_NULL).input(VALUE, selfMob(), TARGET).id();
        return when(noTarget, RESULT, present(nearest, "player", target));
    }

    /**
     * Gives the extension of the mob the target the mob just took.
     */
    void forwardTargetToExtension() {
        String trigger = entry(EVENT_TARGET_CHANGE);
        String extension = as("mob", node(MOB_EXTENSION).id(), "entity");
        String target = node(MOB_SET_TARGET)
                .input(TARGET, extension, RESULT)
                .input(VALUE, trigger, TARGET)
                .id();
        on(EVENT_TARGET_CHANGE, when(extension, "matches", target));
    }

    private String once(String key, Supplier<String> create) {
        String id = shared.get(key);
        if (id == null) {
            id = create.get();
            shared.put(key, id);
        }
        return id;
    }
}
