package de.eldoria.bloodnight.graph;

import de.eldoria.graph.model.Edge;
import de.eldoria.graph.model.Graph;
import de.eldoria.graph.model.NodeInstance;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.StringNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.UnaryOperator;

/**
 * Converts graphs saved by the old object runtime into the current model.
 * <p>
 * Old graphs name node classes ({@code nodeClass}), use integer ids, edges of {@code {node, name}} and executions under
 * {@code executions.nextNodes}; value nodes keep their value at the top level. Node types and ports are renamed per
 * the table below. Control flow changed shape: what the old batch and for-each nodes ran repeatedly under
 * {@code next} is now their {@code body}. Trigger outputs map onto the getters the new entries expose; outputs
 * without counterpart are dropped and reported, as are unknown node classes, which keep their old class name as type
 * so validation points at them.
 */
public final class LegacyGraphs {
    private static final Map<String, Mapping> TABLE = new HashMap<>();

    static {
        map("IntegerNode", "value.integer").value("value");
        map("NumberNode", "value.number").value("value");
        map("BooleanNode", "value.boolean").value("value");
        map("StringNode", "value.text").value("value");
        map("DamageCauseNode", "value.damage_cause").value("value");
        map("EntityTypeNode", "value.entity_type").value("value", LegacyGraphs::registryKey);
        map("PotionTypeNode", "value.potion_effect").value("value", LegacyGraphs::registryKey);
        map("ItemNode", "value.item").value("item");

        for (String math : List.of("Add", "Subtract", "Multiply", "Divide", "Abs", "Ceil", "Floor", "Round", "Clamp")) {
            map(math + "Node", "math." + math.toLowerCase());
        }
        map("AbsIntegerNode", "math.abs_integer");
        map("ClampIntegerNode", "math.clamp_integer");
        map("DistanceNode", "vector.distance");
        map("DistanceSqrtNode", "vector.distance_squared");
        map("NormalizeNode", "vector.normalize");
        map("DirectionVectorNode", "vector.direction");

        map("AndNode", "logic.and");
        map("OrNode", "logic.or");
        map("NotNode", "logic.not").input("first", "value");
        map("EqualNode", "logic.equal");
        map("GreaterNode", "logic.greater");
        map("GreaterOrEqualNode", "logic.greater_or_equal");
        map("LessNode", "logic.less");
        map("LessOrEqualNode", "logic.less_or_equal");
        map("IsNullNode", "logic.is_null");

        map("CooldownNode", "function.cooldown");
        map("RandomIntegerNode", "function.random_integer");
        map("RandomNumberNode", "function.random_number");
        map("NearbyEntitiesNode", "world.nearby_entities");

        map("CreateLocationNode", "location.create").drop("direction");
        map("CreateVectorNode", "vector.create");
        map("SplitLocationNode", "location.get").input("location", "target");
        map("SplitVectorNode", "vector.get").input("vector", "target");
        map("SplitEntityNode", "entity.get").input("value", "target").output("entity_type", "type");

        map("BranchNode", "flow.branch").input("value", "condition");
        map("BatchNode", "flow.batch").input("value", "count").execution("next", "body");
        map("ForEachNode", "flow.for_each").input("value", "list").output("current", "element").execution("next", "body");
        map("PrintNode", "debug.print");
        map("CurrentEntityNode", "mob.self").output("value", "entity");

        map("TickNode", "mob.tick");
        map("DebugNode", "debug.trigger");
        for (String damage : List.of("OnDamageNode:event.damage", "OnDamageByEntityNode:event.damage_by_entity",
                "OnDamageByPlayerNode:event.damage_by_player", "OnHitNode:event.hit")) {
            String[] parts = damage.split(":");
            map(parts[0], parts[1]).output("target", "entity").output("damage_cause", "cause").output("cancelable_event", "event");
        }
        for (String kill : List.of("OnKillNode:event.kill", "OnPlayerKillNode:event.player_kill", "OnEntityKillNode:event.entity_kill")) {
            String[] parts = kill.split(":");
            map(parts[0], parts[1]).output("killed_entity", "entity").output("cancelable_event", "event");
        }
        map("OnDeathNode", "event.death").drop("killer").drop("damage_cause");
        map("OnTeleportNode", "event.teleport").output("cancelable_event", "event");
        map("OnTargetChangeNode", "event.target_change").output("cancelable_event", "event");
        map("OnProjectileShoot", "event.projectile_shoot").output("projectile", "entity").output("cancelable_event", "event");
        map("OnProjectileHit", "event.projectile_hit").output("projectile", "entity").output("cancelable_event", "event");
        map("OnExplosionPrimeNode", "event.explosion_prime").output("cancelable_event", "event");
        map("OnExplosionNode", "event.explode").output("cancelable_event", "event");
    }

    private LegacyGraphs() {
    }

    /**
     * A converted graph.
     *
     * @param graph    the graph in the current model
     * @param warnings what could not be carried over
     */
    public record Migration(Graph graph, List<String> warnings) {
    }

    /**
     * @param legacy an old {@code NodeContainer}: an object with a {@code nodes} map
     * @return the converted graph
     */
    public static Migration migrate(JsonNode legacy) {
        List<String> warnings = new ArrayList<>();
        JsonNode nodes = legacy.path("nodes");
        Map<String, Mapping> mappings = new HashMap<>();
        for (Map.Entry<String, JsonNode> entry : nodes.properties()) {
            mappings.put(entry.getKey(), mapping(entry.getValue().path("nodeClass").asString("")));
        }

        Graph.Builder graph = Graph.builder();
        for (Map.Entry<String, JsonNode> entry : nodes.properties()) {
            String id = entry.getKey();
            JsonNode node = entry.getValue();
            String nodeClass = node.path("nodeClass").asString("");
            Mapping mapping = mappings.get(id);
            if (mapping == null) warnings.add("%s: unknown node class %s, kept for review".formatted(id, nodeClass));
            Mapping effective = mapping == null ? new Mapping(nodeClass) : mapping;

            NodeInstance.Builder instance = NodeInstance.of(effective.type);
            if (effective.valueField != null && node.has("value") && !node.get("value").isNull()) {
                instance.config(effective.valueField, effective.valueConversion.apply(node.get("value")));
            }
            for (Map.Entry<String, JsonNode> input : node.path("input").properties()) {
                String source = input.getValue().path("node").asString();
                String sourcePort = input.getValue().path("name").asString();
                if (effective.dropped.contains(input.getKey())) {
                    warnings.add("%s: input %s no longer exists, edge dropped".formatted(id, input.getKey()));
                    continue;
                }
                Mapping sourceMapping = mappings.get(source);
                if (sourceMapping != null && sourceMapping.dropped.contains(sourcePort)) {
                    warnings.add("%s: output %s of %s no longer exists, edge to %s dropped".formatted(source, sourcePort, source, id));
                    continue;
                }
                String renamed = sourceMapping == null ? sourcePort : sourceMapping.outputs.getOrDefault(sourcePort, sourcePort);
                Edge edge = Edge.to(source, renamed);
                instance.input(effective.inputs.getOrDefault(input.getKey(), input.getKey()), edge.node(), edge.port());
            }
            for (Map.Entry<String, JsonNode> execution : node.path("executions").path("nextNodes").properties()) {
                List<String> targets = new ArrayList<>();
                execution.getValue().forEach(target -> targets.add(target.asString()));
                instance.chain(effective.executions.getOrDefault(execution.getKey(), execution.getKey()), targets.toArray(String[]::new));
            }
            Optional.ofNullable(node.get("meta")).filter(meta -> !meta.isNull()).ifPresent(instance::editor);
            graph.node(id, instance);
        }
        return new Migration(graph.build(), List.copyOf(warnings));
    }

    private static Mapping mapping(String nodeClass) {
        return TABLE.get(nodeClass.substring(nodeClass.lastIndexOf('.') + 1));
    }

    private static Mapping map(String oldClass, String type) {
        Mapping mapping = new Mapping(type);
        TABLE.put(oldClass, mapping);
        return mapping;
    }

    /**
     * Old enum names like {@code ZOMBIE} become registry keys like {@code minecraft:zombie}.
     */
    private static JsonNode registryKey(JsonNode value) {
        if (!value.isString()) return value;
        String text = value.stringValue().toLowerCase();
        return StringNode.valueOf(text.contains(":") ? text : "minecraft:" + text);
    }

    private static final class Mapping {
        private final String type;
        private final Map<String, String> inputs = new LinkedHashMap<>();
        private final Map<String, String> outputs = new LinkedHashMap<>();
        private final Map<String, String> executions = new LinkedHashMap<>();
        private final List<String> dropped = new ArrayList<>();
        private String valueField;
        private UnaryOperator<JsonNode> valueConversion = UnaryOperator.identity();

        private Mapping(String type) {
            this.type = type;
        }

        Mapping input(String old, String renamed) {
            inputs.put(old, renamed);
            return this;
        }

        Mapping output(String old, String renamed) {
            outputs.put(old, renamed);
            return this;
        }

        Mapping execution(String old, String renamed) {
            executions.put(old, renamed);
            return this;
        }

        Mapping drop(String port) {
            dropped.add(port);
            return this;
        }

        Mapping value(String field) {
            this.valueField = field;
            return this;
        }

        Mapping value(String field, UnaryOperator<JsonNode> conversion) {
            this.valueField = field;
            this.valueConversion = conversion;
            return this;
        }
    }
}
