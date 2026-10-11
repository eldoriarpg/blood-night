package de.eldoria.graph.compiler;

import com.palantir.javapoet.CodeBlock;
import de.eldoria.graph.definition.EntryDefinition;
import de.eldoria.graph.definition.ExpressionDefinition;
import de.eldoria.graph.definition.NodeDefinition;
import de.eldoria.graph.definition.NodeDescriptor;
import de.eldoria.graph.definition.NodeKind;
import de.eldoria.graph.definition.PortType;
import de.eldoria.graph.definition.StatementDefinition;
import de.eldoria.graph.types.TypeMapping;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Nodes that mark the edges of a macro body. They only exist while editing a macro: every use of the macro replaces
 * them with the connections of the using node.
 * <ul>
 *     <li>{@code macro.input.<type>}: one per selectable type; config {@code name} becomes an input port of the macro,
 *     output {@code value} provides what is connected to it;</li>
 *     <li>{@code macro.output}: input {@code value}, config {@code name}; becomes an output port of that type;</li>
 *     <li>{@code macro.start}: where the execution enters; makes the macro an action;</li>
 *     <li>{@code macro.end}: config {@code name}; chaining it continues with what the user chained to that port.</li>
 * </ul>
 */
public final class MacroBoundary {
    public static final String INPUT_PREFIX = "macro.input.";
    public static final String OUTPUT = "macro.output";
    public static final String START = "macro.start";
    public static final String END = "macro.end";
    static final String PASS_PREFIX = "macro.pass.";
    static final String NAME = "name";
    static final String VALUE = "value";
    static final String NEXT = "next";

    private MacroBoundary() {
    }

    /**
     * @param types the platform's type mapping; one input node is created per selectable type
     * @return the boundary node definitions
     */
    public static List<NodeDefinition> definitions(TypeMapping types) {
        List<NodeDefinition> definitions = new ArrayList<>();
        types.selectable().forEach((id, type) -> definitions.add(new ExpressionDefinition(
                NodeDescriptor.builder(INPUT_PREFIX + id, NodeKind.VALUE)
                        .category("macro")
                        .name("Macro Input (%s)".formatted(types.name(type)))
                        .description("A value passed into the macro")
                        .config(NAME, String.class, null)
                        .output(VALUE, type)
                        .build(),
                MacroBoundary::neverEmitted)));
        definitions.add(new ExpressionDefinition(
                NodeDescriptor.builder(OUTPUT, NodeKind.PURE)
                        .category("macro")
                        .name("Macro Output")
                        .description("A value the macro provides")
                        .config(NAME, String.class, null)
                        .input(VALUE, PortType.variable("T"))
                        .output(VALUE, PortType.variable("T"))
                        .build(),
                MacroBoundary::neverEmitted));
        definitions.add(new EntryDefinition(
                NodeDescriptor.builder(START, NodeKind.ENTRY)
                        .category("macro")
                        .name("Macro Start")
                        .description("Where the macro starts running when it is chained")
                        .execution(NEXT)
                        .build(),
                "start", List.of(), MacroBoundary::neverEmitted));
        definitions.add(new StatementDefinition(
                NodeDescriptor.builder(END, NodeKind.ACTION)
                        .category("macro")
                        .name("Macro End")
                        .description("Continues with what is chained to this exit of the macro")
                        .config(NAME, String.class, null)
                        .build(),
                context -> neverEmitted(null, context)));
        return definitions;
    }

    /**
     * Typed pass-through nodes that stand in for macro inputs once a macro is expanded, so values connected to a
     * macro are checked against the declared input type. Internal to compilation; never shown in the editor.
     *
     * @param types the platform's type mapping
     * @return one pass node per selectable type, by node type
     */
    static Map<String, NodeDefinition> passDefinitions(TypeMapping types) {
        Map<String, NodeDefinition> definitions = new LinkedHashMap<>();
        types.selectable().forEach((id, type) -> definitions.put(PASS_PREFIX + id, new ExpressionDefinition(
                NodeDescriptor.builder(PASS_PREFIX + id, NodeKind.PURE)
                        .category("macro")
                        .input(VALUE, type)
                        .output(VALUE, type)
                        .build(),
                (port, context) -> context.input(VALUE))));
        return definitions;
    }

    static boolean isBoundary(String type) {
        return type.startsWith(INPUT_PREFIX) || type.equals(OUTPUT) || type.equals(START) || type.equals(END);
    }

    private static CodeBlock neverEmitted(String port, Object context) {
        throw new IllegalStateException("Macro boundary nodes are replaced before emitting");
    }
}
