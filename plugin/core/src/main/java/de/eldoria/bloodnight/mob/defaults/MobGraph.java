/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mob.defaults;

import de.eldoria.bloodnight.graph.BloodNightNodes;
import de.eldoria.bloodnight.graph.MobBindings;
import de.eldoria.bloodnight.graph.runtime.ProjectileKind;
import de.eldoria.graph.compiler.emit.Routines;
import de.eldoria.graph.compiler.validate.Graphs;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.platform.GraphPlatform;
import de.eldoria.graph.std.StdNodes;
import org.bukkit.entity.Creeper;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.EquipmentSlot;
import org.jspecify.annotations.Nullable;

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
    private final String selfType;
    private final String extensionType;
    private final Map<String, String> shared = new HashMap<>();
    private final Map<String, Graphs.NodeBuilder> entries = new HashMap<>();
    private int next;

    /**
     * @param platform  the platform
     * @param entity    the mob's entity type; Self has this type in the graph
     * @param extension the extension's entity type, if the mob has one
     */
    MobGraph(GraphPlatform platform, EntityType entity, @Nullable EntityType extension) {
        this.graphs = Graphs.with(platform).bindings(MobBindings.of(entity, extension));
        this.selfType = reflected(entity);
        this.extensionType = reflected(extension);
    }

    /**
     * The most specific reflected entity type an entity type is, whose read and mutate nodes take it.
     */
    private static String reflected(@Nullable EntityType type) {
        Class<?> entity = type == null ? null : type.getEntityClass();
        if (entity == null) return "entity";
        if (Creeper.class.isAssignableFrom(entity)) return "creeper";
        if (Player.class.isAssignableFrom(entity)) return "player";
        if (Mob.class.isAssignableFrom(entity)) return "mob";
        if (LivingEntity.class.isAssignableFrom(entity)) return "living_entity";
        if (Projectile.class.isAssignableFrom(entity)) return "projectile";
        return "entity";
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

    /**
     * Defines a routine: a chain the graph's other chains call by name.
     */
    void routine(String name, String... chain) {
        node(Routines.DEFINE).config("name", name).chain(NEXT, chain);
    }

    /**
     * A parameter of a routine; output {@code value}.
     *
     * @param selectable the parameter's type, e.g. {@code living_entity}
     */
    String parameter(String routine, String name, String selectable) {
        return node(Routines.PARAMETER)
                .config("type", selectable)
                .config("routine", routine)
                .config("name", name)
                .id();
    }

    /**
     * Calls a routine; its definition and parameters must be placed already.
     */
    Graphs.NodeBuilder call(String routine) {
        return node(Routines.CALL_PREFIX + routine);
    }

    String self() {
        return once("self", () -> node(MOB_SELF).id());
    }

    /**
     * Every property of the mob, read through the read node of its own type; outputs such as {@code location},
     * {@code eye_location}, {@code equipment}, {@code target}.
     */
    String selfRead() {
        return once(
                "self_read",
                () -> node(selfType + ".read").input(TARGET, self(), "entity").id());
    }

    /**
     * A new mutate node of the mob's own type, changing the properties given to it.
     */
    Graphs.NodeBuilder selfMutate() {
        return node(selfType + ".mutate").input(TARGET, self(), "entity");
    }

    /**
     * Every property of the extension, read through the read node of its own type; its outputs are missing when the
     * mob has no extension.
     */
    String extensionRead() {
        return once("extension_read", () -> node(extensionType + ".read")
                .input(TARGET, extension(), "entity")
                .id());
    }

    /**
     * The entity riding or carrying the mob, as its own type; output {@code entity}, missing when there is none.
     */
    String extension() {
        return once("extension", () -> node(MOB_EXTENSION).id());
    }

    /**
     * A new mutate node of the extension's own type.
     */
    Graphs.NodeBuilder extensionMutate() {
        return node(extensionType + ".mutate").input(TARGET, extension(), "entity");
    }

    /**
     * Every property of a living entity, such as the mob's target; outputs such as {@code location}, {@code health}.
     */
    String living(String node, String port) {
        return node(LIVING_ENTITY_READ).input(TARGET, node, port).id();
    }

    /**
     * A fixed value of a selectable type.
     *
     * @param type  the selectable type, e.g. {@code damage_cause}
     * @param value the value as the editor stores it
     */
    String value(String type, Object value) {
        return node(StdNodes.VALUE).config("type", type).config(VALUE, value).id();
    }

    /**
     * A value as a more specific type; outputs {@code result} and {@code matches}.
     */
    String as(String selectable, String node, String port) {
        return node(StdNodes.CAST)
                .config("type", selectable)
                .input(VALUE, node, port)
                .id();
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
        return particlesAt(particle, count, selfRead(), "eye_location");
    }

    /**
     * Spawns particles around a location once.
     */
    String particlesAt(String particle, int count, String node, String port) {
        return node(WORLD_SPAWN_PARTICLE)
                .input(TARGET, selfRead(), "world")
                .literal("particle", particle)
                .input("location", node, port)
                .literal("count", count)
                .literal("offset_x", 1.5)
                .literal("offset_y", 1.0)
                .literal("offset_z", 1.5)
                .id();
    }

    /**
     * Spawns particles that take data around the mob once.
     *
     * @param node the node providing the data
     * @param port its output
     */
    String particlesAroundWith(String particle, int count, String node, String port) {
        return node(WORLD_SPAWN_PARTICLE)
                .input(TARGET, selfRead(), "world")
                .literal("particle", particle)
                .input("location", selfRead(), "eye_location")
                .literal("count", count)
                .literal("offset_x", 1.5)
                .literal("offset_y", 1.0)
                .literal("offset_z", 1.5)
                .input("data", node, port)
                .id();
    }

    /**
     * Data for particles that take a strength.
     */
    String power(double power) {
        return node(PARTICLE_POWER).literal("power", power).id();
    }

    /**
     * Data for spell particles: a colour and a strength.
     */
    String spell(int red, int green, int blue, double power) {
        return node(SPELL_CREATE)
                .input("color", color(red, green, blue), RESULT)
                .literal("power", power)
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
        return node(WORLD_SPAWN_PARTICLE)
                .input(TARGET, selfRead(), "world")
                .literal("particle", "minecraft:dust")
                .input("location", selfRead(), "eye_location")
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
        String cooldown = node(FUNCTION_COOLDOWN)
                .config("unit", "MILLISECONDS")
                .literal("duration", millis)
                .id();
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
                .input("first", elapsed, "elapsed")
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
        return node(CANCELLABLE_MUTATE)
                .input(TARGET, trigger, "event")
                .literal("cancelled", true)
                .id();
    }

    /**
     * Puts an item into an equipment slot of the mob.
     *
     * @param slot the slot
     * @param item item type key, {@code minecraft:air} to empty the slot
     */
    String equip(EquipmentSlot slot, String item) {
        return equip(selfRead(), slot, item);
    }

    /**
     * Puts an item into an equipment slot of a living entity, read from the {@code equipment} output of its getter.
     */
    String equip(String livingGetter, EquipmentSlot slot, String item) {
        String stack = node(ITEM_TYPE_CREATE_ITEM_STACK).literal(TARGET, item).id();
        return node(EQUIPMENT_EQUIP)
                .input(TARGET, livingGetter, "equipment")
                .literal("slot", slot.name())
                .input("item", stack, RESULT)
                .id();
    }

    /**
     * Sets a property of the creeper this mob is.
     *
     * @param property the property's input of the creeper's set node, e.g. {@code max_fuse_ticks}
     */
    String creeper(String property, Object value) {
        return selfMutate().literal(property, value).id();
    }

    /**
     * Plays a sound to a player at their own location.
     */
    String sound(String player, String port, String sound, double volume, double pitch) {
        return node(PLAYER_PLAY_SOUND)
                .input(TARGET, player, port)
                .input("location", node(PLAYER_READ).input(TARGET, player, port).id(), "location")
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
                .input(TARGET, selfRead(), TARGET)
                .literal("speed", speed)
                .id();
    }

    /**
     * Makes the mob target the nearest survival player within 16 blocks while it has no target.
     */
    String targetNearestPlayer() {
        String nearest = node(WORLD_NEAREST_PLAYER)
                .input("location", selfRead(), "location")
                .literal("radius", 16.0)
                .id();
        String target = selfMutate().input("target_value", nearest, "player").id();
        String noTarget = node(LOGIC_IS_NULL).input(VALUE, selfRead(), TARGET).id();
        return when(noTarget, RESULT, present(nearest, "player", target));
    }

    /**
     * Gives the extension of the mob the target the mob just took.
     */
    void forwardTargetToExtension() {
        String trigger = entry(EVENT_TARGET_CHANGE);
        String target = extensionMutate().input("target_value", trigger, TARGET).id();
        on(EVENT_TARGET_CHANGE, present(extension(), "entity", target));
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
