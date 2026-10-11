/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph;

import com.palantir.javapoet.CodeBlock;
import de.eldoria.bloodnight.graph.runtime.BukkitRuntime;
import de.eldoria.bloodnight.graph.runtime.ProjectileKind;
import de.eldoria.graph.definition.ExpressionDefinition;
import de.eldoria.graph.definition.NodeDefinition;
import de.eldoria.graph.definition.NodeDescriptor;
import de.eldoria.graph.definition.NodeKind;
import de.eldoria.graph.definition.PortType;
import de.eldoria.graph.definition.StatementContext;
import de.eldoria.graph.definition.StatementDefinition;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.StringNode;

import java.util.Collection;
import java.util.List;

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
                    .description("A location in a world; yaw and pitch default to 0")
                    .input("world", World.class)
                    .input("x", double.class)
                    .input("y", double.class)
                    .input("z", double.class)
                    .optionalInput("yaw", double.class)
                    .optionalInput("pitch", double.class)
                    .output(RESULT, Location.class)
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
                    .description("A vector of length 1 pointing the same way")
                    .input(VALUE, Vector.class)
                    .output(RESULT, Vector.class)
                    .build(),
            (port, ctx) -> CodeBlock.of("$L.clone().normalize()", ctx.input(VALUE)));

    public static final ExpressionDefinition DIRECTION = new ExpressionDefinition(
            NodeDescriptor.builder(BloodNightNodes.VECTOR_DIRECTION, NodeKind.PURE)
                    .description("The vector pointing from one point to another")
                    .input("from", Vector.class)
                    .input("to", Vector.class)
                    .output(RESULT, Vector.class)
                    .build(),
            (port, ctx) -> CodeBlock.of("$L.clone().subtract($L)", ctx.input("to"), ctx.input("from")));

    public static final ExpressionDefinition DISTANCE = new ExpressionDefinition(
            NodeDescriptor.builder(BloodNightNodes.VECTOR_DISTANCE, NodeKind.PURE)
                    .description(
                            "Distance between two points. Uses a square root; prefer Distance Squared for comparisons")
                    .input(FIRST, Vector.class)
                    .input(SECOND, Vector.class)
                    .output(RESULT, double.class)
                    .build(),
            (port, ctx) -> CodeBlock.of("$L.distance($L)", ctx.input(FIRST), ctx.input(SECOND)));

    public static final ExpressionDefinition DISTANCE_SQUARED = new ExpressionDefinition(
            NodeDescriptor.builder(BloodNightNodes.VECTOR_DISTANCE_SQUARED, NodeKind.PURE)
                    .description("Squared distance between two points")
                    .input(FIRST, Vector.class)
                    .input(SECOND, Vector.class)
                    .output(RESULT, double.class)
                    .build(),
            (port, ctx) -> CodeBlock.of("$L.distanceSquared($L)", ctx.input(FIRST), ctx.input(SECOND)));

    public static final ExpressionDefinition NEARBY_ENTITIES = new ExpressionDefinition(
            NodeDescriptor.builder(BloodNightNodes.WORLD_NEARBY_ENTITIES, NodeKind.PURE)
                    .description("Entities within a box around a location; x, y and z are half the box size")
                    .input("location", Location.class)
                    .input("x", double.class)
                    .input("y", double.class)
                    .input("z", double.class)
                    .output(RESULT, PortType.parameterized(Collection.class, PortType.of(Entity.class)))
                    .build(),
            (port, ctx) -> CodeBlock.of(
                    "$L.getWorld().getNearbyEntities($L, $L, $L, $L)",
                    ctx.input("location"),
                    ctx.input("location"),
                    ctx.input("x"),
                    ctx.input("y"),
                    ctx.input("z")));

    public static final ExpressionDefinition SELF = new ExpressionDefinition(
            NodeDescriptor.builder(BloodNightNodes.MOB_SELF, NodeKind.PURE)
                    .description("The mob this behaviour belongs to")
                    .output("entity", LivingEntity.class)
                    .build(),
            (port, ctx) -> CodeBlock.of("$L.entity()", ctx.context()));

    public static final ExpressionDefinition EXTENSION = new ExpressionDefinition(
            NodeDescriptor.builder(BloodNightNodes.MOB_EXTENSION, NodeKind.PURE)
                    .description("The entity riding or carrying the mob; missing when it has none")
                    .output("entity", Entity.class)
                    .build(),
            (port, ctx) -> CodeBlock.of("$L.extension()", ctx.context()));

    public static final ExpressionDefinition ITEM = new ExpressionDefinition(
            NodeDescriptor.builder(BloodNightNodes.VALUE_ITEM, NodeKind.VALUE)
                    .description("A copy of an item from the item registry; missing when the id is unknown")
                    .config("item", String.class, null)
                    .output(VALUE, ItemStack.class)
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
                    .build(),
            ctx -> CodeBlock.builder()
                    .addStatement(
                            "$T.potionCloud($L, $L, $L, $L, $L, $L)",
                            BukkitRuntime.class,
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
                    .build(),
            (port, ctx) -> CodeBlock.of(
                    "$T.nearestPlayer($L, $L, $L)",
                    BukkitRuntime.class,
                    ctx.input("location"),
                    ctx.input("radius"),
                    ctx.connected("survival_only") ? ctx.input("survival_only") : CodeBlock.of("true")));

    public static final ExpressionDefinition DAMAGE_CAUSE = value(
            BloodNightNodes.VALUE_DAMAGE_CAUSE,
            EntityDamageEvent.DamageCause.class,
            StringNode.valueOf("ENTITY_ATTACK"));
    public static final ExpressionDefinition ENTITY_TYPE =
            value(BloodNightNodes.VALUE_ENTITY_TYPE, EntityType.class, StringNode.valueOf("minecraft:zombie"));
    public static final ExpressionDefinition ITEM_TYPE =
            value(BloodNightNodes.VALUE_ITEM_TYPE, ItemType.class, StringNode.valueOf("minecraft:stone"));
    public static final ExpressionDefinition BLOCK_TYPE =
            value(BloodNightNodes.VALUE_BLOCK_TYPE, BlockType.class, StringNode.valueOf("minecraft:stone"));
    public static final ExpressionDefinition POTION_EFFECT_TYPE = value(
            BloodNightNodes.VALUE_POTION_EFFECT_TYPE, PotionEffectType.class, StringNode.valueOf("minecraft:speed"));

    private BukkitNodes() {}

    /**
     * @return every hand-written Bukkit node
     */
    public static List<NodeDefinition> all() {
        return List.of(
                CREATE_LOCATION,
                APPLY_EFFECT,
                LAUNCH_PROJECTILE,
                SPAWN_POTION_CLOUD,
                NEAREST_PLAYER,
                NORMALIZE,
                DIRECTION,
                DISTANCE,
                DISTANCE_SQUARED,
                NEARBY_ENTITIES,
                SELF,
                EXTENSION,
                ITEM,
                DAMAGE_CAUSE,
                ENTITY_TYPE,
                ITEM_TYPE,
                BLOCK_TYPE,
                POTION_EFFECT_TYPE);
    }

    private static CodeBlock visible(StatementContext ctx) {
        return ctx.connected("visible") ? ctx.input("visible") : CodeBlock.of("true");
    }

    private static ExpressionDefinition value(String id, Class<?> type, JsonNode defaultValue) {
        return new ExpressionDefinition(
                NodeDescriptor.builder(id, NodeKind.VALUE)
                        .config(VALUE, type, defaultValue)
                        .output(VALUE, type)
                        .build(),
                (port, ctx) -> ctx.config(VALUE));
    }
}
