/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.compiler;

import com.palantir.javapoet.CodeBlock;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Maps lines of generated source back to node ids.
 * <p>
 * Before the code of each node the emitter writes a marker comment holding an index and the node's type, never the
 * node id itself: ids come from the graph and must not reach the source, types come from the platform. After rendering, every marker's line is recorded, and a compiler
 * error on some line belongs to the nearest marker above it.
 */
final class SourceMap {
    private static final Pattern MARKER = Pattern.compile("^\\s*// node #(\\d+)(?: [A-Za-z0-9_.]+)?$");

    private final List<@Nullable String> nodes = new ArrayList<>();
    private final TreeMap<Long, @Nullable String> lines = new TreeMap<>();

    /**
     * @param node node id
     * @param type id of the node's type, shown to readers of the source
     * @return a marker comment line for the node
     */
    CodeBlock marker(String node, String type) {
        nodes.add(node);
        return CodeBlock.of("// node #$L $L\n", nodes.size() - 1, type.replaceAll("[^A-Za-z0-9_.]", ""));
    }

    /**
     * @return a marker for code that belongs to no node, ending the previous node's range
     */
    CodeBlock boundary() {
        nodes.add(null);
        return CodeBlock.of("// node #$L\n", nodes.size() - 1);
    }

    /**
     * Records where the markers ended up.
     *
     * @param source the rendered source
     */
    void index(String source) {
        lines.clear();
        String[] split = source.split("\n", -1);
        for (int i = 0; i < split.length; i++) {
            Matcher matcher = MARKER.matcher(split[i]);
            if (matcher.matches()) lines.put(i + 1L, nodes.get(Integer.parseInt(matcher.group(1))));
        }
    }

    /**
     * @param line one-based line in the rendered source
     * @return the node whose code contains the line, if any
     */
    Optional<String> nodeAt(long line) {
        var entry = lines.floorEntry(line);
        return entry == null ? Optional.empty() : Optional.ofNullable(entry.getValue());
    }
}
