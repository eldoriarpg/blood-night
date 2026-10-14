/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.TypeName;
import de.eldoria.bloodnight.graph.runtime.BukkitRuntime;
import de.eldoria.bloodnight.graph.runtime.ProjectileKind;
import de.eldoria.bloodnight.graph.runtime.Reach;
import de.eldoria.bloodnight.graph.runtime.Trace;
import de.eldoria.bloodnight.graph.runtime.TraceHits;
import de.eldoria.graph.definition.EmitContext;
import de.eldoria.graph.definition.ExpressionDefinition;
import de.eldoria.graph.definition.NodeDefinition;
import de.eldoria.graph.definition.NodeDescriptor;
import de.eldoria.graph.definition.NodeKind;
import de.eldoria.graph.definition.PortType;
import de.eldoria.graph.definition.StatementContext;
import de.eldoria.graph.definition.StatementDefinition;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Vibration;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;
import tools.jackson.databind.node.StringNode;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Bukkit nodes that are not property access: constructing locations and vectors, vector math, area queries, the mob
 * itself, and literal values of Bukkit types.
 * <p>
 * Vector operations work on copies; the old runtime normalised its input in place.
 */
public final class BukkitNodes {
    private static final String RESULT = "result";
    private static final String VALUE = "value";
    private static final String NEXT = "next";
    private static final String FIRST = "first";
    private static final String SECOND = "second";

    public static final ExpressionDefinition CREATE_LOCATION = new ExpressionDefinition(
            NodeDescriptor.builder(BloodNightNodes.LOCATION_CREATE, NodeKind.PURE)
                    .name("Create Location")
                    .description("A location in a world; yaw and pitch default to 0")
                    .input("world", World.class)
                    .input("x", double.class)
                    .input("y", double.class)
                    .input("z", double.class)
                    .optionalInput("yaw", double.class)
                    .optionalInput("pitch", double.class)
                    .output(RESULT, Location.class)
                    .describe("world", "The world it lies in")
                    .describe("x", "East-west position")
                    .describe("y", "Height")
                    .describe("z", "North-south position")
                    .describe("yaw", "Facing around the vertical axis, in degrees; 0 when not given")
                    .describe("pitch", "Facing up or down, in degrees; 0 when not given")
                    .describe("result", "The location")
                    .build(),
            (port, ctx) -> CodeBlock.of(
                    "new $T($L, $L, $L, $L, (float) $L, (float) $L)",
                    Location.class,
                    ctx.input("world"),
                    ctx.input("x"),
                    ctx.input("y"),
                    ctx.input("z"),
                    ctx.connected("yaw") ? ctx.input("yaw") : CodeBlock.of("0"),
                    ctx.connected("pitch") ? ctx.input("pitch") : CodeBlock.of("0")));

    public static final ExpressionDefinition NORMALIZE = new ExpressionDefinition(
            NodeDescriptor.builder(BloodNightNodes.VECTOR_NORMALIZE, NodeKind.PURE)
                    .name("Normalize Vector")
                    .description("A vector of length 1 pointing the same way")
                    .input(VALUE, Vector.class)
                    .output(RESULT, Vector.class)
                    .describe("value", "A vector")
                    .describe("result", "The same direction with length 1")
                    .build(),
            (port, ctx) -> CodeBlock.of("$L.clone().normalize()", ctx.input(VALUE)));

    public static final ExpressionDefinition DIRECTION = new ExpressionDefinition(
            NodeDescriptor.builder(BloodNightNodes.VECTOR_DIRECTION, NodeKind.PURE)
                    .name("Direction Between Points")
                    .description("The vector pointing from one point to another")
                    .input("from", Vector.class)
                    .input("to", Vector.class)
                    .output(RESULT, Vector.class)
                    .describe("from", "Where it starts")
                    .describe("to", "Where it points")
                    .describe("result", "The vector from start to end")
                    .build(),
            (port, ctx) -> CodeBlock.of("$L.clone().subtract($L)", ctx.input("to"), ctx.input("from")));

    public static final ExpressionDefinition DISTANCE = new ExpressionDefinition(
            NodeDescriptor.builder(BloodNightNodes.VECTOR_DISTANCE, NodeKind.PURE)
                    .name("Distance Between Points")
                    .description(
                            "Distance between two points. Uses a square root; prefer Distance Squared for comparisons")
                    .input(FIRST, Vector.class)
                    .input(SECOND, Vector.class)
                    .output(RESULT, double.class)
                    .describe("first", "A point")
                    .describe("second", "Another point")
                    .describe("result", "The distance between both")
                    .build(),
            (port, ctx) -> CodeBlock.of("$L.distance($L)", ctx.input(FIRST), ctx.input(SECOND)));

    public static final ExpressionDefinition DISTANCE_SQUARED = new ExpressionDefinition(
            NodeDescriptor.builder(BloodNightNodes.VECTOR_DISTANCE_SQUARED, NodeKind.PURE)
                    .name("Squared Distance Between Points")
                    .description("Squared distance between two points")
                    .input(FIRST, Vector.class)
                    .input(SECOND, Vector.class)
                    .output(RESULT, double.class)
                    .describe("first", "A point")
                    .describe("second", "Another point")
                    .describe("result", "The distance between both, squared; cheaper when only comparing")
                    .build(),
            (port, ctx) -> CodeBlock.of("$L.distanceSquared($L)", ctx.input(FIRST), ctx.input(SECOND)));

    public static final ExpressionDefinition NEARBY_ENTITIES = new ExpressionDefinition(
            NodeDescriptor.builder(BloodNightNodes.WORLD_NEARBY_ENTITIES, NodeKind.PURE)
                    .name("Entities Near Location")
                    .description("Entities within a box around a location; x, y and z are half the box size")
                    .input("location", Location.class)
                    .input("x", double.class)
                    .input("y", double.class)
                    .input("z", double.class)
                    .output(RESULT, PortType.parameterized(Collection.class, PortType.of(Entity.class)))
                    .describe("location", "The centre of the search")
                    .describe("x", "How far to search east and west")
                    .describe("y", "How far to search up and down")
                    .describe("z", "How far to search north and south")
                    .describe(
                            "result",
                            "The entities in that box; on Folia empty when the box reaches into another region")
                    .build(),
            (port, ctx) -> CodeBlock.of(
                    "$T.nearbyEntities($L, $L, $L, $L)",
                    BukkitRuntime.class,
                    ctx.input("location"),
                    ctx.input("x"),
                    ctx.input("y"),
                    ctx.input("z")));

    public static final ExpressionDefinition SELF = new ExpressionDefinition(
            NodeDescriptor.builder(BloodNightNodes.MOB_SELF, NodeKind.PURE)
                    .description("The mob this behaviour belongs to, as its own entity type, e.g. a Creeper")
                    .output("entity", PortType.variable(MobBindings.SELF))
                    .describe("entity", "The mob this behaviour belongs to")
                    .build(),
            (port, ctx) -> narrowed(ctx, "entity", LivingEntity.class, CodeBlock.of("$L.entity()", ctx.context())));

    public static final ExpressionDefinition EXTENSION = new ExpressionDefinition(
            NodeDescriptor.builder(BloodNightNodes.MOB_EXTENSION, NodeKind.PURE)
                    .description("The entity riding or carrying the mob; missing when it has none")
                    .output("entity", PortType.variable(MobBindings.EXTENSION))
                    .describe(
                            "entity",
                            "The entity riding or carrying the mob, as its own entity type; missing when it has none")
                    .build(),
            (port, ctx) -> narrowed(ctx, "entity", Entity.class, CodeBlock.of("$L.extension()", ctx.context())));

    public static final ExpressionDefinition ITEM = new ExpressionDefinition(
            NodeDescriptor.builder(BloodNightNodes.VALUE_ITEM, NodeKind.VALUE)
                    .name("Item By Id")
                    .description("A copy of an item from the item registry; missing when the id is unknown")
                    .config("item", String.class, null)
                    .output(VALUE, ItemStack.class)
                    .describe("value", "A copy of the item; missing when the id is unknown")
                    .build(),
            (port, ctx) -> CodeBlock.of("$L.item($L)", ctx.context(), ctx.config("item")));

    public static final StatementDefinition APPLY_EFFECT = new StatementDefinition(
            NodeDescriptor.builder(BloodNightNodes.LIVING_ENTITY_APPLY_EFFECT, NodeKind.ACTION)
                    .category("living_entity")
                    .description(
                            "Gives an entity a potion effect for some seconds; visible effects show particles and an icon")
                    .input("target", "Living Entity", PortType.of(LivingEntity.class), false)
                    .input("effect", "Effect", PortType.of(PotionEffectType.class), false)
                    .input("seconds", double.class)
                    .input("amplifier", int.class)
                    .optionalInput("visible", boolean.class)
                    .execution(NEXT)
                    .describe("target", "Who gets the effect")
                    .describe("effect", "The kind of effect")
                    .describe("seconds", "How long it lasts")
                    .describe("amplifier", "Its strength, 0 for level I")
                    .describe("visible", "Whether it shows particles and an icon; true when not given")
                    .build(),
            ctx -> CodeBlock.builder()
                    .addStatement(
                            "$L.addPotionEffect(new $T($L, (int) ($L * 20), $L, $L, $L))",
                            ctx.input("target"),
                            PotionEffect.class,
                            ctx.input("effect"),
                            ctx.input("seconds"),
                            ctx.input("amplifier"),
                            visible(ctx),
                            visible(ctx))
                    .add(ctx.chain(NEXT))
                    .build());

    public static final StatementDefinition DAMAGE = new StatementDefinition(
            NodeDescriptor.builder(BloodNightNodes.LIVING_ENTITY_DAMAGE, NodeKind.ACTION)
                    .category("living_entity")
                    .description("Deals damage to an entity; with a source, the damage counts as dealt by it")
                    .input("target", "Living Entity", PortType.of(LivingEntity.class), false)
                    .input("amount", double.class)
                    .input("source", "Source", PortType.of(Entity.class), true)
                    .execution(NEXT)
                    .describe("target", "Who takes the damage")
                    .describe("amount", "The damage, in half hearts")
                    .describe("source", "Who deals it; without, the damage has no source")
                    .build(),
            ctx -> CodeBlock.builder()
                    .addStatement("$L.damage($L)", ctx.input("target"), arguments(ctx, "amount", "source"))
                    .add(ctx.chain(NEXT))
                    .build());

    public static final StatementDefinition SPAWN_PARTICLE = new StatementDefinition(
            NodeDescriptor.builder(BloodNightNodes.WORLD_SPAWN_PARTICLE, NodeKind.ACTION)
                    .category("world")
                    .description("Spawns particles around a location; particles such as dust need data, connected"
                            + " from a node like Dust")
                    .input("target", "World", PortType.of(World.class), false)
                    .input("particle", Particle.class)
                    .input("location", Location.class)
                    .input("count", int.class)
                    .input("offset_x", double.class)
                    .input("offset_y", double.class)
                    .input("offset_z", double.class)
                    .input("data", "Data", PortType.of(Object.class), true)
                    .execution(NEXT)
                    .describe("target", "The world to spawn them in")
                    .describe("particle", "The kind of particle")
                    .describe("location", "Where they appear")
                    .describe("count", "How many")
                    .describe("offset_x", "Spread east and west")
                    .describe("offset_y", "Spread up and down")
                    .describe("offset_z", "Spread north and south")
                    .describe("data", "What particles such as dust need, from a node like Dust; leave empty for others")
                    .build(),
            ctx -> CodeBlock.builder()
                    .addStatement(
                            "$T.particles($L, $L, $L, $L, $L, $L, $L, $L, $L)",
                            BukkitRuntime.class,
                            ctx.context(),
                            ctx.input("target"),
                            ctx.input("particle"),
                            ctx.input("location"),
                            ctx.input("count"),
                            ctx.input("offset_x"),
                            ctx.input("offset_y"),
                            ctx.input("offset_z"),
                            ctx.connected("data") ? ctx.input("data") : CodeBlock.of("null"))
                    .add(ctx.chain(NEXT))
                    .build());

    public static final StatementDefinition STRIKE_LIGHTNING = new StatementDefinition(
            NodeDescriptor.builder(BloodNightNodes.WORLD_STRIKE_LIGHTNING, NodeKind.ACTION)
                    .category("world")
                    .description("Strikes lightning at a location; an effect only strike looks the same but deals no"
                            + " damage and sets nothing on fire")
                    .input("target", "World", PortType.of(World.class), false)
                    .input("location", Location.class)
                    .optionalInput("effect_only", boolean.class)
                    .execution(NEXT)
                    .describe("target", "The world to strike in")
                    .describe("location", "Where it strikes")
                    .describe("effect_only", "Only the look and sound, no damage and no fire; false when not given")
                    .build(),
            ctx -> CodeBlock.builder()
                    .addStatement(
                            "$T.strikeLightning($L, $L, $L, $L)",
                            BukkitRuntime.class,
                            ctx.context(),
                            ctx.input("target"),
                            ctx.input("location"),
                            ctx.connected("effect_only") ? ctx.input("effect_only") : CodeBlock.of("false"))
                    .add(ctx.chain(NEXT))
                    .build());

    public static final StatementDefinition RUN_COMMAND = new StatementDefinition(
            NodeDescriptor.builder(BloodNightNodes.SERVER_RUN_COMMAND, NodeKind.ACTION)
                    .category("server")
                    .name("Run Command")
                    .description("Runs a command as the console, e.g. to use another plugin. {player} is the player's"
                            + " name, {world}, {x}, {y} and {z} where the mob stands")
                    .input("command", String.class)
                    .optionalInput("player", Player.class)
                    .execution(NEXT)
                    .describe("command", "The command, without the slash")
                    .describe("player", "The player {player} stands for; leave it empty when there is none")
                    .build(),
            ctx -> CodeBlock.builder()
                    .addStatement(
                            "$T.runCommand($L, $L, $L)",
                            BukkitRuntime.class,
                            ctx.context(),
                            ctx.input("command"),
                            ctx.connected("player") ? ctx.input("player") : CodeBlock.of("null"))
                    .add(ctx.chain(NEXT))
                    .build());

    public static final StatementDefinition PLAYER_RUN_COMMAND = new StatementDefinition(
            NodeDescriptor.builder(BloodNightNodes.PLAYER_RUN_COMMAND, NodeKind.ACTION)
                    .category("player")
                    .name("Run Command As Player")
                    .description("Runs a command as a player, with their permissions. {player}, {world}, {x}, {y}"
                            + " and {z} are filled in like for Run Command")
                    .input("target", "Player", PortType.of(Player.class), false)
                    .input("command", String.class)
                    .execution(NEXT)
                    .describe("target", "The player who runs it")
                    .describe("command", "The command, without the slash")
                    .build(),
            ctx -> CodeBlock.builder()
                    .addStatement(
                            "$T.playerCommand($L, $L, $L)",
                            BukkitRuntime.class,
                            ctx.context(),
                            ctx.input("target"),
                            ctx.input("command"))
                    .add(ctx.chain(NEXT))
                    .build());

    public static final StatementDefinition BROADCAST = new StatementDefinition(
            NodeDescriptor.builder(BloodNightNodes.SERVER_BROADCAST, NodeKind.ACTION)
                    .category("server")
                    .name("Broadcast")
                    .description("Sends a message to the players in the mob's world or on the whole server")
                    .config("reach", "Who hears it", PortType.of(Reach.class), StringNode.valueOf(Reach.WORLD.name()))
                    .input("text", String.class)
                    .execution(NEXT)
                    .describe("text", "The message, MiniMessage such as <red>Run!")
                    .build(),
            ctx -> CodeBlock.builder()
                    .addStatement(
                            "$T.broadcast($L, $L, $L)",
                            BukkitRuntime.class,
                            ctx.context(),
                            ctx.input("text"),
                            ctx.config("reach"))
                    .add(ctx.chain(NEXT))
                    .build());

    public static final StatementDefinition LINE_TRACE = new StatementDefinition(
            NodeDescriptor.builder(BloodNightNodes.WORLD_LINE_TRACE, NodeKind.ACTION)
                    .category("world")
                    .name("Line Trace")
                    .description("Follows a line from a location and tells what it meets first, ignoring fluids and"
                            + " passable blocks such as grass")
                    .config("hits", "Stops at", PortType.of(TraceHits.class), StringNode.valueOf(TraceHits.BOTH.name()))
                    .input("start", Location.class)
                    .input("direction", Vector.class)
                    .input("distance", double.class)
                    .optionalInput("ignore", Entity.class)
                    .output("hit", boolean.class)
                    .output("location", Location.class)
                    .output("entity", Entity.class)
                    .output("block", Block.class)
                    .output("hit_distance", "Distance", PortType.of(double.class))
                    .execution(NEXT)
                    .describe("start", "Where the line starts, e.g. the eyes of the mob")
                    .describe("direction", "Where the line goes, e.g. the direction the mob looks in")
                    .describe("distance", "How far the line goes")
                    .describe("ignore", "An entity the line passes through, e.g. the mob itself")
                    .describe("hit", "Whether the line met a block or entity")
                    .describe("location", "Where it met it; missing without a hit")
                    .describe("entity", "The entity it met; missing when it met a block or nothing")
                    .describe("block", "The block it met; missing when it met an entity or nothing")
                    .describe(
                            "hit_distance", "How far from the start it met something, the full distance without a hit")
                    .build(),
            ctx -> {
                CodeBlock trace = ctx.field("trace", ClassName.get(Trace.class), CodeBlock.of("null"));
                CodeBlock.Builder code = CodeBlock.builder()
                        .addStatement(
                                "$L = $T.of($L, $L, $L, $L, $L)",
                                trace,
                                Trace.class,
                                ctx.input("start"),
                                ctx.input("direction"),
                                ctx.input("distance"),
                                ctx.connected("ignore") ? ctx.input("ignore") : CodeBlock.of("null"),
                                ctx.config("hits"));
                Map.of(
                                "hit", "hit",
                                "location", "location",
                                "entity", "entity",
                                "block", "block",
                                "hit_distance", "distance")
                        .forEach((port, accessor) -> {
                            if (ctx.isRead(port)) {
                                code.addStatement(
                                        "final $T $L = $L.$N()", ctx.type(port), ctx.bind(port, NEXT), trace, accessor);
                            }
                        });
                return code.add(ctx.chain(NEXT)).build();
            });

    public static final StatementDefinition LAUNCH_PROJECTILE = new StatementDefinition(
            NodeDescriptor.builder(BloodNightNodes.LIVING_ENTITY_LAUNCH_PROJECTILE, NodeKind.ACTION)
                    .category("living_entity")
                    .description("Launches a projectile from the shooter straight at a target")
                    .config("projectile", ProjectileKind.class, StringNode.valueOf("ARROW"))
                    .input("shooter", "Shooter", PortType.of(LivingEntity.class), false)
                    .input("target", "Target", PortType.of(Entity.class), false)
                    .input("speed", double.class)
                    .output("projectile", Projectile.class)
                    .execution(NEXT)
                    .describe("shooter", "Who launches it")
                    .describe("target", "What it flies at")
                    .describe("speed", "How fast it flies")
                    .describe(
                            "projectile",
                            "The launched projectile; on Folia missing when the target is in another region and nothing"
                                    + " was launched")
                    .build(),
            ctx -> {
                CodeBlock projectile = ctx.bind("projectile", NEXT);
                return CodeBlock.builder()
                        .addStatement(
                                "final $T $L = $T.launchAt($L, $L, $L, $L)",
                                Projectile.class,
                                projectile,
                                BukkitRuntime.class,
                                ctx.input("shooter"),
                                ctx.input("target"),
                                ctx.config("projectile"),
                                ctx.input("speed"))
                        .add(ctx.chain(NEXT))
                        .build();
            });

    public static final StatementDefinition SPAWN_POTION_CLOUD = new StatementDefinition(
            NodeDescriptor.builder(BloodNightNodes.WORLD_SPAWN_POTION_CLOUD, NodeKind.ACTION)
                    .category("world")
                    .description("Spawns a lingering cloud that applies a potion to everything inside")
                    .input("location", Location.class)
                    .input("potion", PotionType.class)
                    .input("seconds", int.class)
                    .input("radius", double.class)
                    .optionalInput("radius_per_tick", double.class)
                    .optionalInput("color", Color.class)
                    .execution(NEXT)
                    .describe("location", "Where the cloud appears")
                    .describe("potion", "The effect it gives")
                    .describe("seconds", "How long it lasts")
                    .describe("radius", "Its size when it appears")
                    .describe("radius_per_tick", "How much it grows per tick; negative to shrink")
                    .describe("color", "Its colour")
                    .build(),
            ctx -> CodeBlock.builder()
                    .addStatement(
                            "$T.potionCloud($L, $L, $L, $L, $L, $L, $L)",
                            BukkitRuntime.class,
                            ctx.context(),
                            ctx.input("location"),
                            ctx.input("potion"),
                            ctx.input("seconds"),
                            ctx.input("radius"),
                            ctx.connected("radius_per_tick") ? ctx.input("radius_per_tick") : CodeBlock.of("0"),
                            ctx.connected("color") ? ctx.input("color") : CodeBlock.of("null"))
                    .add(ctx.chain(NEXT))
                    .build());

    public static final ExpressionDefinition NEAREST_PLAYER = new ExpressionDefinition(
            NodeDescriptor.builder(BloodNightNodes.WORLD_NEAREST_PLAYER, NodeKind.PURE)
                    .description("The closest player within a radius, missing when there is none")
                    .input("location", Location.class)
                    .input("radius", double.class)
                    .optionalInput("survival_only", boolean.class)
                    .output("player", Player.class)
                    .describe("location", "Where to search from")
                    .describe("radius", "How far to search")
                    .describe("survival_only", "Whether only players in survival or adventure count")
                    .describe(
                            "player",
                            "The closest player; missing when none is in range, and on Folia when the range reaches"
                                    + " into another region")
                    .build(),
            (port, ctx) -> CodeBlock.of(
                    "$T.nearestPlayer($L, $L, $L)",
                    BukkitRuntime.class,
                    ctx.input("location"),
                    ctx.input("radius"),
                    ctx.connected("survival_only") ? ctx.input("survival_only") : CodeBlock.of("true")));

    public static final ExpressionDefinition PARTICLE_POWER = new ExpressionDefinition(
            NodeDescriptor.builder(BloodNightNodes.PARTICLE_POWER, NodeKind.PURE)
                    .name("Particle Power")
                    .category("world")
                    .description("Data for particles that take a strength, such as dragon breath and sculk charge")
                    .input("power", double.class)
                    .output("power", PortType.of(Float.class))
                    .describe("power", "The strength the particle shows, e.g. of dragon breath")
                    .build(),
            (port, ctx) -> CodeBlock.of("$T.valueOf((float) $L)", Float.class, ctx.input("power")));

    public static final ExpressionDefinition PARTICLE_DELAY = new ExpressionDefinition(
            NodeDescriptor.builder(BloodNightNodes.PARTICLE_DELAY, NodeKind.PURE)
                    .name("Particle Delay")
                    .category("world")
                    .description("Data for particles that appear after a delay in ticks, such as shriek")
                    .input("ticks", int.class)
                    .output("delay", PortType.of(Integer.class))
                    .describe("ticks", "How long the particle waits before it shows")
                    .describe("delay", "The delay, as data for particles such as shriek")
                    .build(),
            (port, ctx) -> CodeBlock.of("$T.valueOf($L)", Integer.class, ctx.input("ticks")));

    public static final ExpressionDefinition VIBRATION = new ExpressionDefinition(
            NodeDescriptor.builder(BloodNightNodes.VIBRATION_CREATE, NodeKind.PURE)
                    .name("Create Vibration")
                    .category("world")
                    .description("Data for the vibration particle: travels to a location within some ticks")
                    .input("destination", Location.class)
                    .input("ticks", int.class)
                    .output("vibration", Vibration.class)
                    .describe("destination", "Where the vibration travels to")
                    .describe("ticks", "How long it travels")
                    .describe("vibration", "The vibration, as data for the vibration particle")
                    .build(),
            (port, ctx) -> CodeBlock.of(
                    "new $T(new $T($L), $L)",
                    Vibration.class,
                    Vibration.Destination.BlockDestination.class,
                    ctx.input("destination"),
                    ctx.input("ticks")));

    private BukkitNodes() {}

    /**
     * @return every hand-written Bukkit node
     */
    public static final StatementDefinition TELEPORT = new StatementDefinition(
            NodeDescriptor.builder(BloodNightNodes.ENTITY_TELEPORT, NodeKind.ACTION)
                    .category("entity")
                    .name("Teleport")
                    .description("Moves an entity to a location; the move finishes once the place is loaded")
                    .input("target", "Entity", PortType.of(Entity.class), false)
                    .input("location", Location.class)
                    .output(RESULT, boolean.class)
                    .execution(NEXT)
                    .describe("target", "Who moves")
                    .describe("location", "Where to")
                    .describe("result", "True: the move has started")
                    .build(),
            ctx -> CodeBlock.builder()
                    .addStatement(
                            "final boolean $L = $T.teleport($L, $L)",
                            ctx.bind(RESULT, NEXT),
                            BukkitRuntime.class,
                            ctx.input("target"),
                            ctx.input("location"))
                    .add(ctx.chain(NEXT))
                    .build());

    public static final ExpressionDefinition ENTITY_NEARBY_ENTITIES = new ExpressionDefinition(
            NodeDescriptor.builder(BloodNightNodes.ENTITY_NEARBY_ENTITIES, NodeKind.PURE)
                    .category("entity")
                    .name("Entities Near Entity")
                    .description("Entities within a box around an entity, without the entity itself")
                    .input("target", "Entity", PortType.of(Entity.class), false)
                    .input("x", double.class)
                    .input("y", double.class)
                    .input("z", double.class)
                    .output(RESULT, PortType.parameterized(List.class, PortType.of(Entity.class)))
                    .describe("target", "The centre of the search")
                    .describe("x", "How far to search east and west")
                    .describe("y", "How far to search up and down")
                    .describe("z", "How far to search north and south")
                    .describe(
                            "result",
                            "The entities in that box; on Folia empty when the box reaches into another region")
                    .build(),
            (port, ctx) -> CodeBlock.of(
                    "$T.nearbyEntities($L, $L, $L, $L)",
                    BukkitRuntime.class,
                    ctx.input("target"),
                    ctx.input("x"),
                    ctx.input("y"),
                    ctx.input("z")));

    public static final ExpressionDefinition HAS_LINE_OF_SIGHT = new ExpressionDefinition(
            NodeDescriptor.builder(BloodNightNodes.LIVING_ENTITY_HAS_LINE_OF_SIGHT, NodeKind.PURE)
                    .category("living_entity")
                    .name("Has Line Of Sight")
                    .description("Whether a living entity can see another one, with no block in between")
                    .input("target", "Living Entity", PortType.of(LivingEntity.class), false)
                    .input("other", Entity.class)
                    .output(RESULT, boolean.class)
                    .describe("target", "Who looks")
                    .describe("other", "What it looks at")
                    .describe("result", "Whether it sees it; on Folia false when the other is in another region")
                    .build(),
            (port, ctx) -> CodeBlock.of(
                    "$T.hasLineOfSight($L, $L)", BukkitRuntime.class, ctx.input("target"), ctx.input("other")));

    public static final ExpressionDefinition BLOCK_AT = new ExpressionDefinition(
            NodeDescriptor.builder(BloodNightNodes.WORLD_BLOCK_AT, NodeKind.PURE)
                    .category("world")
                    .name("Block At")
                    .description("The block at a location")
                    .input("target", "World", PortType.of(World.class), false)
                    .input("location", Location.class)
                    .output(RESULT, Block.class)
                    .describe("target", "The world to look in")
                    .describe("location", "Where the block is")
                    .describe("result", "The block; on Folia missing when the location is in another region")
                    .build(),
            (port, ctx) -> CodeBlock.of(
                    "$T.blockAt($L, $L)", BukkitRuntime.class, ctx.input("target"), ctx.input("location")));

    public static List<NodeDefinition> all() {
        return List.of(
                TELEPORT,
                ENTITY_NEARBY_ENTITIES,
                HAS_LINE_OF_SIGHT,
                BLOCK_AT,
                CREATE_LOCATION,
                APPLY_EFFECT,
                DAMAGE,
                SPAWN_PARTICLE,
                STRIKE_LIGHTNING,
                RUN_COMMAND,
                PLAYER_RUN_COMMAND,
                BROADCAST,
                LINE_TRACE,
                LAUNCH_PROJECTILE,
                SPAWN_POTION_CLOUD,
                NEAREST_PLAYER,
                PARTICLE_POWER,
                PARTICLE_DELAY,
                VIBRATION,
                NORMALIZE,
                DIRECTION,
                DISTANCE,
                DISTANCE_SQUARED,
                NEARBY_ENTITIES,
                SELF,
                EXTENSION,
                ITEM);
    }

    /**
     * The value as the type its port resolved to, cast when that is narrower than what the context returns.
     */
    private static CodeBlock narrowed(EmitContext ctx, String port, Class<?> returned, CodeBlock value) {
        TypeName type = ctx.type(port);
        return type.equals(TypeName.get(returned)) ? value : CodeBlock.of("(($T) $L)", type, value);
    }

    private static CodeBlock visible(StatementContext ctx) {
        return ctx.connected("visible") ? ctx.input("visible") : CodeBlock.of("true");
    }

    /**
     * The given inputs as a comma separated argument list, leaving out optional inputs without a value, so one node can
     * call the overload that matches what is wired.
     */
    private static CodeBlock arguments(EmitContext ctx, String... ports) {
        return Arrays.stream(ports).filter(ctx::connected).map(ctx::input).collect(CodeBlock.joining(", "));
    }
}
