package de.eldoria.bloodnight.editor;

import de.eldoria.bloodnight.mobs.CustomMob;
import de.eldoria.bloodnight.nodes.registry.meta.NodeRegistration;

import java.util.List;

public record EditorPayload(List<CustomMob> mobs, List<NodeRegistration> nodes) {
}
