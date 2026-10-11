/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.editor.service;

import de.eldoria.bloodnight.editor.api.PayloadProblem;
import de.eldoria.graph.payload.Catalog;
import de.eldoria.graph.payload.ConfigInfo;
import de.eldoria.graph.payload.ExecutionInfo;
import de.eldoria.graph.payload.NodeType;
import de.eldoria.graph.payload.PortInfo;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Checks an edited payload as far as the service can without a server: its shape, the graphs of mobs and macros
 * against the server's catalog (known node types, ports, connections to existing nodes and outputs) and that mobs
 * only use items the server registered. Anything that needs the server, such as entity types or effect keys, is left
 * to the server when it pulls the result. Catalogs leave empty port lists out, so a missing list counts as empty.
 * <p>
 * Uses of macros ({@code macro.<id>}) need a macro of that id in the payload; uses of routines
 * ({@code routine.<name>}) need a {@code routine.define} of that name in the same graph, and their inputs are the
 * names of that routine's {@code routine.parameter.*} nodes.
 */
final class PayloadValidator {
    private static final String ROUTINE = "routine.";
    private static final List<String> SLOTS = List.of("head", "chest", "legs", "feet", "mainHand", "offHand");
    private final Map<String, NodeType> nodes;

    /**
     * @param catalog the catalog the payload's server runs
     */
    PayloadValidator(Catalog catalog) {
        nodes = catalog.nodes().stream().collect(Collectors.toMap(NodeType::id, Function.identity(), (a, b) -> a));
    }

    /**
     * @param payload an editor payload
     * @return what is wrong with it; empty when nothing was found
     */
    List<PayloadProblem> validate(JsonNode payload) {
        List<PayloadProblem> problems = new ArrayList<>();
        if (!payload.isObject()) {
            problems.add(new PayloadProblem("", "The payload is no object"));
            return problems;
        }
        Set<String> macros = macroIds(payload.path("macros"));
        Set<String> items = new HashSet<>(payload.path("items").propertyNames());
        JsonNode mobs = payload.path("mobs");
        if (!mobs.isMissingNode() && !mobs.isObject()) problems.add(new PayloadProblem("/mobs", "Mobs are no object"));
        for (var entry : mobs.properties()) {
            String path = "/mobs/" + escape(entry.getKey());
            validateMob(path, entry.getKey(), entry.getValue(), macros, items, problems);
        }
        JsonNode macroList = payload.path("macros");
        if (!macroList.isMissingNode() && !macroList.isArray()) {
            problems.add(new PayloadProblem("/macros", "Macros are no list"));
        }
        for (int i = 0; i < macroList.size(); i++) {
            JsonNode macro = macroList.get(i);
            if (!macro.path("id").isString()) problems.add(new PayloadProblem("/macros/" + i + "/id", "Missing id"));
            validateGraph("/macros/" + i + "/body", macro.path("body"), macros, problems);
        }
        JsonNode settings = payload.path("settings");
        if (!settings.isMissingNode() && !settings.isObject()) {
            problems.add(new PayloadProblem("/settings", "Settings are no object"));
        }
        return problems;
    }

    private void validateMob(
            String path,
            String id,
            JsonNode mob,
            Set<String> macros,
            Set<String> items,
            List<PayloadProblem> problems) {
        if (!mob.isObject()) {
            problems.add(new PayloadProblem(path, "The mob is no object"));
            return;
        }
        if (!id.equals(mob.path("id").asString("")))
            problems.add(new PayloadProblem(path + "/id", "Id differs from key " + id));
        if (!mob.path("attributes").path("entityType").isString()) {
            problems.add(new PayloadProblem(path + "/attributes/entityType", "Missing entity type"));
        }
        validateItems(path + "/equipment", mob.path("equipment"), items, problems);
        validateItems(path + "/extension/equipment", mob.path("extension").path("equipment"), items, problems);
        JsonNode drops = mob.path("mobDrops").path("dropList");
        for (int i = 0; i < drops.size(); i++) {
            JsonNode item = drops.get(i).path("itemId");
            if (item.isString() && !items.contains(item.asString())) {
                problems.add(new PayloadProblem(
                        path + "/mobDrops/dropList/" + i + "/itemId", "Unknown item " + item.asString()));
            }
        }
        if (!mob.path("behaviour").isMissingNode())
            validateGraph(path + "/behaviour", mob.path("behaviour"), macros, problems);
    }

    private static void validateItems(
            String path, JsonNode equipment, Set<String> items, List<PayloadProblem> problems) {
        for (String slot : SLOTS) {
            JsonNode item = equipment.path(slot);
            if (item.isString() && !items.contains(item.asString())) {
                problems.add(new PayloadProblem(path + "/" + slot, "Unknown item " + item.asString()));
            }
        }
    }

    private void validateGraph(String path, JsonNode graph, Set<String> macros, List<PayloadProblem> problems) {
        if (!graph.isObject()) {
            problems.add(new PayloadProblem(path, "The graph is no object"));
            return;
        }
        JsonNode instances = graph.path("nodes");
        Set<String> ids = new HashSet<>(instances.propertyNames());
        Map<String, Set<String>> routines = routines(instances);
        for (var entry : instances.properties()) {
            String nodePath = path + "/nodes/" + escape(entry.getKey());
            JsonNode node = entry.getValue();
            String type = node.path("type").asString("");
            if (type.startsWith(ROUTINE) && !nodes.containsKey(type)) {
                validateRoutineUse(
                        nodePath, node, type.substring(ROUTINE.length()), routines, instances, macros, problems);
                continue;
            }
            validateNode(nodePath, node, instances, ids, macros, problems);
        }
    }

    private void validateRoutineUse(
            String path,
            JsonNode node,
            String routine,
            Map<String, Set<String>> routines,
            JsonNode instances,
            Set<String> macros,
            List<PayloadProblem> problems) {
        Set<String> parameters = routines.get(routine);
        if (parameters == null) {
            problems.add(new PayloadProblem(path + "/type", "Unknown routine " + routine));
            return;
        }
        for (var input : node.path("inputs").properties()) {
            String port = path + "/inputs/" + escape(input.getKey());
            if (!parameters.contains(input.getKey()))
                problems.add(new PayloadProblem(port, "Unknown input " + input.getKey()));
            validateEdge(port, input.getValue(), instances, macros, problems);
        }
    }

    private static Map<String, Set<String>> routines(JsonNode instances) {
        Map<String, Set<String>> routines = new HashMap<>();
        for (JsonNode node : instances) {
            if (node.path("type").asString("").equals("routine.define")) {
                routines.putIfAbsent(node.path("config").path("name").asString(""), new HashSet<>());
            }
        }
        for (JsonNode node : instances) {
            if (!node.path("type").asString("").startsWith("routine.parameter.")) continue;
            Set<String> parameters =
                    routines.get(node.path("config").path("routine").asString(""));
            if (parameters != null)
                parameters.add(node.path("config").path("name").asString(""));
        }
        return routines;
    }

    private void validateNode(
            String path,
            JsonNode node,
            JsonNode instances,
            Set<String> ids,
            Set<String> macros,
            List<PayloadProblem> problems) {
        String type = node.path("type").asString("");
        NodeType definition = nodes.get(type);
        if (definition == null) {
            if (!isMacroUse(type, macros))
                problems.add(new PayloadProblem(path + "/type", "Unknown node type " + type));
            return;
        }
        Set<String> inputs = ids(definition.inputs(), PortInfo::id);
        for (var input : node.path("inputs").properties()) {
            String port = path + "/inputs/" + escape(input.getKey());
            if (!inputs.contains(input.getKey()))
                problems.add(new PayloadProblem(port, "Unknown input " + input.getKey()));
            validateEdge(port, input.getValue(), instances, macros, problems);
        }
        for (String literal : node.path("literals").propertyNames()) {
            if (!inputs.contains(literal)) {
                problems.add(new PayloadProblem(path + "/literals/" + escape(literal), "Unknown input " + literal));
            }
        }
        Set<String> config = ids(definition.config(), ConfigInfo::id);
        for (String field : node.path("config").propertyNames()) {
            if (!config.contains(field))
                problems.add(new PayloadProblem(path + "/config/" + escape(field), "Unknown config " + field));
        }
        Set<String> executions = ids(definition.executions(), ExecutionInfo::id);
        for (var execution : node.path("executions").properties()) {
            String port = path + "/executions/" + escape(execution.getKey());
            if (!executions.contains(execution.getKey())) {
                problems.add(new PayloadProblem(port, "Unknown execution " + execution.getKey()));
            }
            for (JsonNode target : execution.getValue()) {
                if (!ids.contains(target.asString(""))) {
                    problems.add(new PayloadProblem(port, "Chains to missing node " + target.asString("")));
                }
            }
        }
    }

    private void validateEdge(
            String path, JsonNode edge, JsonNode instances, Set<String> macros, List<PayloadProblem> problems) {
        String source = edge.path("node").asString("");
        JsonNode sourceNode = instances.path(source);
        if (sourceNode.isMissingNode()) {
            problems.add(new PayloadProblem(path, "Connected to missing node " + source));
            return;
        }
        NodeType definition = nodes.get(sourceNode.path("type").asString(""));
        if (definition == null) return;
        String port = edge.path("port").asString("");
        if (!ids(definition.outputs(), PortInfo::id).contains(port)) {
            problems.add(new PayloadProblem(path, "Node %s has no output %s".formatted(source, port)));
        }
    }

    private static boolean isMacroUse(String type, Set<String> macros) {
        return type.startsWith("macro.") && macros.contains(type.substring("macro.".length()));
    }

    private static Set<String> macroIds(JsonNode macros) {
        Set<String> ids = new HashSet<>();
        for (JsonNode macro : macros) ids.add(macro.path("id").asString(""));
        return ids;
    }

    private static <T> Set<String> ids(@Nullable List<T> entries, Function<T, String> id) {
        if (entries == null) return Set.of();
        return entries.stream().map(id).collect(Collectors.toSet());
    }

    private static String escape(String segment) {
        return segment.replace("~", "~0").replace("/", "~1");
    }
}
