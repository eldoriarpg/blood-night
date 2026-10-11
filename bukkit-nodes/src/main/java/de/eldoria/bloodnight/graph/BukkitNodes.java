/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph;

import com.palantir.javapoet.CodeBlock;
import de.eldoria.graph.definition.ExpressionDefinition;
import de.eldoria.graph.definition.NodeDefinition;
import de.eldoria.graph.definition.NodeDescriptor;
import de.eldoria.graph.definition.NodeKind;
import de.eldoria.graph.definition.PortType;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ItemType;
import org.bukkit.potion.PotionEffectType;
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
    private static final String FIRST = "first";
    private static final String SECOND = "second";

    public static final ExpressionDefinition CREATE_LOCATION = new ExpressionDefinition(
            NodeDescriptor.builder("location.create", NodeKind.PURE)
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
            NodeDescriptor.builder("vector.normalize", NodeKind.PURE)
                    .description("A vector of length 1 pointing the same way")
                    .input(VALUE, Vector.class)
                    .output(RESULT, Vector.class)
                    .build(),
            (port, ctx) -> CodeBlock.of("$L.clone().normalize()", ctx.input(VALUE)));

    public static final ExpressionDefinition DIRECTION = new ExpressionDefinition(
            NodeDescriptor.builder("vector.direction", NodeKind.PURE)
                    .description("The vector pointing from one point to another")
                    .input("from", Vector.class)
                    .input("to", Vector.class)
                    .output(RESULT, Vector.class)
                    .build(),
            (port, ctx) -> CodeBlock.of("$L.clone().subtract($L)", ctx.input("to"), ctx.input("from")));

    public static final ExpressionDefinition DISTANCE = new ExpressionDefinition(
            NodeDescriptor.builder("vector.distance", NodeKind.PURE)
                    .description(
                            "Distance between two points. Uses a square root; prefer Distance Squared for comparisons")
                    .input(FIRST, Vector.class)
                    .input(SECOND, Vector.class)
                    .output(RESULT, double.class)
                    .build(),
            (port, ctx) -> CodeBlock.of("$L.distance($L)", ctx.input(FIRST), ctx.input(SECOND)));

    public static final ExpressionDefinition DISTANCE_SQUARED = new ExpressionDefinition(
            NodeDescriptor.builder("vector.distance_squared", NodeKind.PURE)
                    .description("Squared distance between two points")
                    .input(FIRST, Vector.class)
                    .input(SECOND, Vector.class)
                    .output(RESULT, double.class)
                    .build(),
            (port, ctx) -> CodeBlock.of("$L.distanceSquared($L)", ctx.input(FIRST), ctx.input(SECOND)));

    public static final ExpressionDefinition NEARBY_ENTITIES = new ExpressionDefinition(
            NodeDescriptor.builder("world.nearby_entities", NodeKind.PURE)
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
            NodeDescriptor.builder("mob.self", NodeKind.PURE)
                    .description("The mob this behaviour belongs to")
                    .output("entity", LivingEntity.class)
                    .build(),
            (port, ctx) -> CodeBlock.of("$L.entity()", ctx.context()));

    public static final ExpressionDefinition EXTENSION = new ExpressionDefinition(
            NodeDescriptor.builder("mob.extension", NodeKind.PURE)
                    .description("The entity riding or carrying the mob; missing when it has none")
                    .output("entity", Entity.class)
                    .build(),
            (port, ctx) -> CodeBlock.of("$L.extension()", ctx.context()));

    public static final ExpressionDefinition ITEM = new ExpressionDefinition(
            NodeDescriptor.builder("value.item", NodeKind.VALUE)
                    .description("A copy of an item from the item registry; missing when the id is unknown")
                    .config("item", String.class, null)
                    .output(VALUE, ItemStack.class)
                    .build(),
            (port, ctx) -> CodeBlock.of("$L.item($L)", ctx.context(), ctx.config("item")));

    public static final ExpressionDefinition DAMAGE_CAUSE =
            value("value.damage_cause", EntityDamageEvent.DamageCause.class, StringNode.valueOf("ENTITY_ATTACK"));
    public static final ExpressionDefinition ENTITY_TYPE =
            value("value.entity_type", EntityType.class, StringNode.valueOf("minecraft:zombie"));
    public static final ExpressionDefinition ITEM_TYPE =
            value("value.item_type", ItemType.class, StringNode.valueOf("minecraft:stone"));
    public static final ExpressionDefinition BLOCK_TYPE =
            value("value.block_type", BlockType.class, StringNode.valueOf("minecraft:stone"));
    public static final ExpressionDefinition POTION_EFFECT_TYPE =
            value("value.potion_effect_type", PotionEffectType.class, StringNode.valueOf("minecraft:speed"));

    private BukkitNodes() {}

    /**
     * @return every hand-written Bukkit node
     */
    public static List<NodeDefinition> all() {
        return List.of(
                CREATE_LOCATION,
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

    private static ExpressionDefinition value(String id, Class<?> type, JsonNode defaultValue) {
        return new ExpressionDefinition(
                NodeDescriptor.builder(id, NodeKind.VALUE)
                        .config(VALUE, type, defaultValue)
                        .output(VALUE, type)
                        .build(),
                (port, ctx) -> ctx.config(VALUE));
    }
}
