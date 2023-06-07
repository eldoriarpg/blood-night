package de.eldoria.bloodnight.nodes.registry.meta;

import de.eldoria.bloodnight.nodes.registry.meta.NodeType;

public record NodeRegistrationMeta(String name, String description, NodeType nodeType, String category) {
}
