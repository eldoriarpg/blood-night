/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.model;

import java.util.Objects;

/**
 * A frame on the canvas that names and explains a part of a graph. Only the editor reads it: the nodes inside it, by
 * their position, belong to it and move with it, and a collapsed frame hides them behind one box. Compiling ignores
 * it.
 *
 * @param title     one line naming the part
 * @param note      a few lines more, empty for none
 * @param color     the frame's colour, by the name of a colour of the editor's palette, e.g. {@code red}
 * @param x         the left edge, in canvas units
 * @param y         the top edge, in canvas units
 * @param width     the width when open, in canvas units
 * @param height    the height when open, in canvas units
 * @param collapsed whether it is shown closed, hiding the nodes inside
 */
public record Comment(
        String title, String note, String color, double x, double y, double width, double height, boolean collapsed) {
    public Comment {
        title = Objects.requireNonNullElse(title, "");
        note = Objects.requireNonNullElse(note, "");
        color = Objects.requireNonNullElse(color, "grey");
        width = Math.max(0, width);
        height = Math.max(0, height);
    }
}
