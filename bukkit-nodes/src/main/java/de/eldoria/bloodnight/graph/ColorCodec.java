/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph;

import com.palantir.javapoet.CodeBlock;
import de.eldoria.graph.types.LiteralCodec;
import org.bukkit.Color;
import tools.jackson.databind.JsonNode;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Colours typed in as {@code #rrggbb}, which the editor offers a colour picker for.
 */
public final class ColorCodec implements LiteralCodec<Integer> {
    private static final Pattern HEX = Pattern.compile("#[0-9a-fA-F]{6}");

    @Override
    public Class<?> type() {
        return Color.class;
    }

    @Override
    public Integer read(JsonNode node) {
        if (node == null || !node.isString() || !HEX.matcher(node.stringValue()).matches()) {
            throw new IllegalArgumentException("Not a colour of the form #rrggbb: " + node);
        }
        return Integer.parseInt(node.stringValue().substring(1), 16);
    }

    @Override
    public CodeBlock emit(Integer value) {
        return CodeBlock.of("$T.fromRGB(0x$L)", Color.class, String.format(Locale.ROOT, "%06X", value));
    }

    @Override
    public String format() {
        return "color";
    }
}
