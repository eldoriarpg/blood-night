package de.eldoria.bloodnight.nodes.base.io;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Class holding meta information about the node itself like the position in the editor.
 *
 * @param position position of the node inside the editor
 * @param comment  a comment about the node
 */
public record EditorMeta(@NotNull Vec2D position, @Nullable String comment) {
}
