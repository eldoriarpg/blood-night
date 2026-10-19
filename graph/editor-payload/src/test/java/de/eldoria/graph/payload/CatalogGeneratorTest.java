/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.payload;

import com.palantir.javapoet.CodeBlock;
import de.eldoria.graph.definition.EntryDefinition;
import de.eldoria.graph.definition.ExpressionDefinition;
import de.eldoria.graph.definition.descriptor.NodeDescriptor;
import de.eldoria.graph.definition.descriptor.NodeKind;
import de.eldoria.graph.payload.node.NodeType;
import de.eldoria.graph.payload.node.PortInfo;
import de.eldoria.graph.platform.GraphPlatform;
import de.eldoria.graph.std.StandardNodes;
import de.eldoria.graph.types.TypeMapping;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.node.StringNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.DayOfWeek;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CatalogGeneratorTest {
    private static final String SNAPSHOT = "catalog-snapshot.json";

    private static GraphPlatform platform() {
        EntryDefinition run = new EntryDefinition(
                NodeDescriptor.builder("entry.run", NodeKind.ENTRY)
                        .execution("next")
                        .build(),
                "run",
                List.of(),
                (port, ctx) -> CodeBlock.of(""));
        ExpressionDefinition weekday = new ExpressionDefinition(
                NodeDescriptor.builder("value.weekday", NodeKind.VALUE)
                        .config("value", DayOfWeek.class, StringNode.valueOf("MONDAY"))
                        .output("value", DayOfWeek.class)
                        .build(),
                (port, ctx) -> ctx.config("value"));
        return GraphPlatform.builder(SnapshotHost.class)
                .define(StandardNodes.all())
                .define(StandardNodes.typed(TypeMapping.defaults()))
                .define(run, weekday)
                .build();
    }

    private static NodeType node(Catalog catalog, String id) {
        return catalog.nodes().stream()
                .filter(node -> node.id().equals(id))
                .findFirst()
                .orElseThrow();
    }

    private static TypeInfo type(Catalog catalog, String id) {
        return catalog.types().stream()
                .filter(type -> type.id().equals(id))
                .findFirst()
                .orElseThrow();
    }

    @Test
    void matchesTheSnapshot() throws IOException {
        String json = CatalogGenerator.write(CatalogGenerator.generate(platform()));
        Path snapshot = Path.of(System.getProperty("snapshot.dir"), SNAPSHOT);
        if (Boolean.getBoolean("snapshot.update") || !Files.exists(snapshot)) Files.writeString(snapshot, json);

        assertEquals(
                Files.readString(snapshot),
                json,
                "The catalog is the editor contract. If the change is intended, run with -Dsnapshot.update=true and review the diff.");
    }

    @Test
    void readsWhatItWrites() {
        Catalog catalog = CatalogGenerator.generate(platform());

        assertEquals(
                CatalogGenerator.write(catalog),
                CatalogGenerator.write(CatalogGenerator.read(CatalogGenerator.write(catalog))));
    }

    @Test
    void descriptionsCanBeTakenFromAnotherCatalog() {
        Catalog plain = CatalogGenerator.generate(platform());
        NodeType add = node(plain, "math.add");
        PortInfo first = add.inputs().getFirst();
        NodeType documentedAdd = new NodeType(
                add.id(),
                add.kind(),
                add.category(),
                add.name(),
                "Documented add",
                add.macroOnly(),
                List.of(new PortInfo(
                        first.id(),
                        first.name(),
                        "The first summand",
                        first.type(),
                        first.optional(),
                        first.literal(),
                        first.group())),
                add.outputs(),
                add.executions(),
                add.config());
        Catalog documented = new Catalog(plain.format(), "x", List.of(documentedAdd), List.of(), plain.selectable());
        NodeType undocumentedAdd = new NodeType(
                add.id(),
                add.kind(),
                add.category(),
                add.name(),
                add.description(),
                add.macroOnly(),
                List.of(new PortInfo(
                        first.id(), first.name(), "", first.type(), first.optional(), first.literal(), first.group())),
                add.outputs(),
                add.executions(),
                add.config());
        Catalog undocumented = new Catalog(
                plain.format(),
                plain.version(),
                plain.nodes().stream()
                        .map(node -> node.id().equals("math.add") ? undocumentedAdd : node)
                        .toList(),
                plain.types(),
                plain.selectable());

        Catalog merged = CatalogGenerator.withDescriptions(undocumented, documented);

        assertEquals(add.description(), node(merged, "math.add").description(), "existing descriptions win");
        assertEquals(
                "The first summand",
                node(merged, "math.add").inputs().getFirst().description());
        assertEquals(
                first.description(),
                node(CatalogGenerator.withDescriptions(plain, documented), "math.add")
                        .inputs()
                        .getFirst()
                        .description(),
                "existing port descriptions win");
        assertEquals(plain.nodes().size(), merged.nodes().size());
        assertFalse(plain.version().equals(merged.version()));
    }

    @Test
    void isDeterministic() {
        assertEquals(
                CatalogGenerator.write(CatalogGenerator.generate(platform())),
                CatalogGenerator.write(CatalogGenerator.generate(platform())));
    }

    @Test
    void describesWhatTheEditorNeeds() {
        Catalog catalog = CatalogGenerator.generate(platform());

        NodeType add = node(catalog, "math.add");
        assertEquals("pure", add.kind());
        assertTrue(add.inputs().getFirst().literal());
        assertEquals("N", add.inputs().getFirst().type().variable());
        assertTrue(add.inputs().getFirst().type().numeric());

        NodeType print = node(catalog, "debug.print");
        assertTrue(print.inputs().getFirst().literal(), "Object inputs take typed-in scalars");

        NodeType forEach = node(catalog, "flow.for_each");
        assertEquals("java.lang.Iterable", forEach.inputs().getFirst().type().raw());
        assertEquals(
                "T", forEach.inputs().getFirst().type().arguments().getFirst().variable());
        assertFalse(forEach.inputs().getFirst().literal(), "Lists cannot be typed in");
        assertTrue(forEach.executions().stream().anyMatch(port -> port.id().equals("body") && port.repeating()));

        assertTrue(node(catalog, "macro.start").macroOnly());
        assertEquals(
                List.of("boolean", "integer", "number", "text"),
                node(catalog, "macro.input").config().stream()
                        .filter(field -> field.id().equals("type"))
                        .findFirst()
                        .orElseThrow()
                        .choices());

        TypeInfo weekday = type(catalog, "java.time.DayOfWeek");
        assertTrue(weekday.literal());
        assertEquals("MONDAY", weekday.values().getFirst());
        assertTrue(type(catalog, "int").assignableTo().contains("double"));
        assertFalse(type(catalog, "double").assignableTo().contains("int"));
        assertEquals("double", catalog.selectable().get("number"));
        assertEquals(16, catalog.version().length());
    }
}
