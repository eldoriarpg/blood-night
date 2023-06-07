package de.eldoria.bloodnight.nodes.registry.meta;

import de.eldoria.bloodnight.nodes.base.Node;

import java.util.List;

public record NodeRegistration(Class<? extends Node> nodeClass, NodeRegistrationMeta meta,
                               List<InputMeta> inputs, List<OutputMeta> outputs, List<ExecutionMeta> executions) {
}
