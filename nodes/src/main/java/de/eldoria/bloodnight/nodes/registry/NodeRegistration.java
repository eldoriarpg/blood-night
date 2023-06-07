package de.eldoria.bloodnight.nodes.registry;

import de.eldoria.bloodnight.nodes.DataType;
import de.eldoria.bloodnight.nodes.base.Node;

import java.util.Map;
import java.util.Set;

public record NodeRegistration(Class<? extends Node> nodeClass, NodeRegistrationMeta meta, Map<String, DataType> inputs,
                               Map<String, DataType> outputs, Set<String> executions) {
}
