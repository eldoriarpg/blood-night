/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.compiler;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import de.eldoria.graph.definition.EmitContext;
import de.eldoria.graph.definition.EntryContext;
import de.eldoria.graph.definition.EntryDefinition;
import de.eldoria.graph.definition.EntryParameter;
import de.eldoria.graph.definition.ExecutionPort;
import de.eldoria.graph.definition.ExpressionDefinition;
import de.eldoria.graph.definition.MacroDefinition;
import de.eldoria.graph.definition.PortType;
import de.eldoria.graph.definition.StatementContext;
import de.eldoria.graph.definition.StatementDefinition;
import de.eldoria.graph.model.Edge;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Modifier;

/**
 * Turns a validated graph into the source of one class implementing the platform's host interface.
 * <p>
 * Shape of the result:
 * <ul>
 *     <li>a final field and constructor parameter for the context object, when the platform has one;</li>
 *     <li>one field per stateful node, initialised in the constructor after the context is set;</li>
 *     <li>one method per used entry point, running the chains of all its instances in id order;</li>
 *     <li>an empty body for every abstract host method no entry point covers.</li>
 * </ul>
 * Code is emitted top-down along execution edges. Data inputs are pulled when a definition asks for them: an
 * expression node's output becomes a local declared just before the statement that first needs it in the current
 * scope (see {@link Scope}). Reads that are not possible at the point of use, like an entry's output outside its
 * chain or a loop variable after the loop, are reported as problems on the reading node.
 */
final class GraphEmitter {
    private static final String CONTEXT_FIELD = "context";

    private final ValidatedGraph graph;
    private final List<GraphProblem> problems = new ArrayList<>();
    private final Set<String> reported = new LinkedHashSet<>();
    private final SourceMap sourceMap = new SourceMap();
    private final Map<String, CodeBlock> fields = new HashMap<>();
    private final List<FieldSpec> fieldSpecs = new ArrayList<>();
    private final CodeBlock.Builder fieldInitializers = CodeBlock.builder();
    private int names;

    GraphEmitter(ValidatedGraph graph) {
        this.graph = graph;
    }

    /**
     * The emitted class with the map needed to locate compiler errors in it.
     *
     * @param file      the source
     * @param sourceMap marker positions, indexed against the rendered source
     */
    record Emitted(JavaFile file, SourceMap sourceMap) {}

    /**
     * @param name name of the generated class
     * @return the source
     * @throws GraphCompilationException when a node reads a value it cannot see, or a definition fails
     */
    Emitted emit(ClassName name) throws GraphCompilationException {
        Class<?> host = graph.platform().host();
        TypeSpec.Builder type = TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(host);

        Map<Method, List<String>> entriesByMethod = new LinkedHashMap<>();
        for (String entry : graph.entries()) {
            EntryDefinition definition =
                    (EntryDefinition) graph.nodes().get(entry).definition();
            entriesByMethod
                    .computeIfAbsent(hostMethod(definition), key -> new ArrayList<>())
                    .add(entry);
        }
        entriesByMethod.forEach((method, entries) -> type.addMethod(entryMethod(method, entries)));
        for (Method method : host.getMethods()) {
            if (java.lang.reflect.Modifier.isAbstract(method.getModifiers()) && !entriesByMethod.containsKey(method)) {
                type.addMethod(emptyMethod(method));
            }
        }

        graph.platform()
                .context()
                .ifPresent(context ->
                        type.addField(FieldSpec.builder(context, CONTEXT_FIELD, Modifier.PRIVATE, Modifier.FINAL)
                                .build()));
        type.addFields(fieldSpecs);
        type.addMethod(constructor());

        List<GraphProblem> all = new ArrayList<>(graph.warnings());
        all.addAll(problems);
        if (!problems.isEmpty()) throw new GraphCompilationException(all, null);
        JavaFile file = JavaFile.builder(name.packageName(), type.build())
                .skipJavaLangImports(true)
                .build();
        sourceMap.index(file.toString());
        return new Emitted(file, sourceMap);
    }

    private MethodSpec constructor() {
        MethodSpec.Builder constructor = MethodSpec.constructorBuilder().addModifiers(Modifier.PUBLIC);
        graph.platform().context().ifPresent(context -> {
            constructor.addParameter(context, CONTEXT_FIELD);
            constructor.addStatement("this.$N = $N", CONTEXT_FIELD, CONTEXT_FIELD);
        });
        return constructor.addCode(fieldInitializers.build()).build();
    }

    private Method hostMethod(EntryDefinition definition) {
        Class<?>[] parameters = definition.parameters().stream()
                .map(parameter -> ((PortType.Concrete) parameter.type()).raw())
                .toArray(Class<?>[]::new);
        try {
            return graph.platform().host().getMethod(definition.method(), parameters);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException("Platform accepted an entry without host method: " + definition.id(), e);
        }
    }

    private MethodSpec entryMethod(Method method, List<String> entries) {
        EntryDefinition definition =
                (EntryDefinition) graph.nodes().get(entries.getFirst()).definition();
        MethodSpec.Builder builder = override(
                method,
                definition.parameters().stream().map(EntryParameter::name).toList());
        for (String entry : entries) {
            Scope root = Scope.root(entry);
            builder.addCode(sourceMap.marker(entry));
            for (ExecutionPort port : definition.descriptor().executions()) {
                builder.addCode(chain(entry, port, root, Map.of()));
            }
        }
        return builder.addCode(sourceMap.boundary()).build();
    }

    private MethodSpec emptyMethod(Method method) {
        List<String> names = new ArrayList<>();
        for (int i = 0; i < method.getParameterCount(); i++) names.add("arg" + i);
        return override(method, names).build();
    }

    private static MethodSpec.Builder override(Method method, List<String> parameterNames) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(method.getName())
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(TypeName.get(method.getGenericReturnType()));
        Type[] types = method.getGenericParameterTypes();
        for (int i = 0; i < types.length; i++) {
            builder.addParameter(ParameterSpec.builder(TypeName.get(types[i]), parameterNames.get(i))
                    .build());
        }
        for (Type exception : method.getGenericExceptionTypes()) builder.addException(TypeName.get(exception));
        return builder;
    }

    private CodeBlock chain(String node, ExecutionPort port, Scope owner, Map<String, CodeBlock> bindings) {
        Scope scope = owner.child(port.repeating());
        bindings.forEach(scope::bind);
        CodeBlock.Builder code = CodeBlock.builder();
        for (String target : graph.nodes().get(node).instance().executions().getOrDefault(port.id(), List.of())) {
            code.add(statement(target, scope));
        }
        return code.build();
    }

    private CodeBlock statement(String node, Scope scope) {
        ValidatedGraph.Node info = graph.nodes().get(node);
        CodeBlock.Builder prelude = CodeBlock.builder();
        Statement context = new Statement(info, scope, prelude);
        CodeBlock body;
        try {
            body = ((StatementDefinition) info.definition()).emitter().emit(context);
        } catch (RuntimeException e) {
            report(GraphProblem.error(
                    node,
                    null,
                    "Definition %s failed: %s".formatted(info.definition().id(), e.getMessage())));
            body = CodeBlock.of("");
        }
        return CodeBlock.builder()
                .add(sourceMap.marker(node))
                .add(prelude.build())
                .add(body)
                .build();
    }

    /**
     * Resolves a data edge to an expression usable at the current point, declaring locals into {@code prelude}.
     */
    private CodeBlock read(String reader, String readerPort, Edge edge, Scope scope, CodeBlock.Builder prelude) {
        String key = Scope.key(edge.node(), edge.port());
        CodeBlock existing = scope.findBound(key);
        if (existing == null) existing = scope.findComputed(key);
        if (existing != null) return existing;

        ValidatedGraph.Node source = graph.nodes().get(edge.node());
        return switch (source.definition()) {
            case ExpressionDefinition expression ->
                compute(source, edge.port(), scope, prelude, () -> expression
                        .emitter()
                        .output(edge.port(), new Expression(source, scope, prelude)));
            case EntryDefinition entry -> {
                if (!scope.within(source.id())) {
                    yield fail(
                            reader,
                            readerPort,
                            "Output %s of entry %s is only available in the chain it starts"
                                    .formatted(edge.port(), source.id()));
                }
                yield compute(source, edge.port(), scope, prelude, () -> entry.emitter()
                        .output(edge.port(), new Entry(source, scope, prelude)));
            }
            case MacroDefinition macro ->
                throw new IllegalStateException("Macro %s was not expanded".formatted(source.id()));
            case StatementDefinition statement ->
                fail(
                        reader,
                        readerPort,
                        "Output %s of %s is only available in the code %s runs itself"
                                .formatted(edge.port(), source.id(), source.id()));
        };
    }

    private CodeBlock compute(
            ValidatedGraph.Node source, String port, Scope scope, CodeBlock.Builder prelude, EmitterCall call) {
        PortType type = source.outputTypes().get(port);
        if (type.hasVariables()) return fail(source.id(), port, "Type of output %s cannot be inferred".formatted(port));
        CodeBlock expression;
        try {
            expression = call.emit();
        } catch (RuntimeException e) {
            return fail(
                    source.id(),
                    port,
                    "Definition %s failed: %s".formatted(source.definition().id(), e.getMessage()));
        }
        CodeBlock local = CodeBlock.of("$N", name("v", source.id(), port));
        prelude.add(sourceMap.marker(source.id()));
        prelude.addStatement("final $T $L = $L", type.typeName(), local, expression);
        scope.compute(Scope.key(source.id(), port), local);
        return local;
    }

    @FunctionalInterface
    private interface EmitterCall {
        CodeBlock emit();
    }

    private CodeBlock fail(String node, String port, String message) {
        report(GraphProblem.error(node, port, message));
        return CodeBlock.of("null");
    }

    private void report(GraphProblem problem) {
        if (reported.add(problem.toString())) problems.add(problem);
    }

    /**
     * Builds a Java identifier from a prefix, a node id and a part name. Characters outside {@code [A-Za-z0-9_]} are
     * dropped, so graph ids cannot alter the code; a running number keeps names unique.
     */
    private String name(String prefix, String node, String part) {
        return "%s%d_%s_%s".formatted(prefix, names++, sanitize(node), sanitize(part));
    }

    private static String sanitize(String text) {
        String cleaned = text.replaceAll("[^A-Za-z0-9_]", "");
        return cleaned.length() > 32 ? cleaned.substring(0, 32) : cleaned;
    }

    private class Base implements EmitContext {
        protected final ValidatedGraph.Node node;
        protected final Scope scope;
        protected final CodeBlock.Builder prelude;

        Base(ValidatedGraph.Node node, Scope scope, CodeBlock.Builder prelude) {
            this.node = node;
            this.scope = scope;
            this.prelude = prelude;
        }

        @Override
        public String nodeId() {
            return node.id();
        }

        @Override
        public CodeBlock input(String port) {
            if (node.definition().descriptor().input(port).isEmpty()) {
                throw new IllegalArgumentException(
                        "%s has no input %s".formatted(node.definition().id(), port));
            }
            CodeBlock literal = node.literals().get(port);
            if (literal != null) return literal;
            Edge edge = node.instance().inputs().get(port);
            if (edge == null)
                throw new IllegalArgumentException(
                        "Optional input %s is not connected; check connected() first".formatted(port));
            return read(node.id(), port, edge, scope, prelude);
        }

        @Override
        public boolean connected(String port) {
            return node.instance().inputs().containsKey(port) || node.literals().containsKey(port);
        }

        @Override
        public CodeBlock config(String field) {
            CodeBlock literal = node.config().get(field);
            if (literal == null)
                throw new IllegalArgumentException(
                        "%s has no config field %s".formatted(node.definition().id(), field));
            return literal;
        }

        @Override
        public TypeName type(String port) {
            PortType type = node.inputTypes().get(port);
            if (type == null) type = node.outputTypes().get(port);
            if (type == null)
                throw new IllegalArgumentException(
                        "%s has no port %s".formatted(node.definition().id(), port));
            return type.typeName();
        }

        @Override
        public CodeBlock field(String name, TypeName type, CodeBlock initializer) {
            return fields.computeIfAbsent(Scope.key(node.id(), name), key -> {
                String field = GraphEmitter.this.name("f", node.id(), name);
                fieldSpecs.add(FieldSpec.builder(type, field, Modifier.PRIVATE).build());
                fieldInitializers.add(sourceMap.marker(node.id()));
                fieldInitializers.addStatement("this.$N = $L", field, initializer);
                return CodeBlock.of("this.$N", field);
            });
        }

        @Override
        public CodeBlock sharedField(String name, TypeName type, CodeBlock initializer) {
            return fields.computeIfAbsent(Scope.key("", name), key -> {
                String field = GraphEmitter.this.name("s", "shared", name);
                fieldSpecs.add(FieldSpec.builder(type, field, Modifier.PRIVATE).build());
                fieldInitializers.add(sourceMap.marker(node.id()));
                fieldInitializers.addStatement("this.$N = $L", field, initializer);
                return CodeBlock.of("this.$N", field);
            });
        }

        @Override
        public CodeBlock context() {
            if (graph.platform().context().isEmpty())
                throw new IllegalStateException("The platform has no context type");
            return CodeBlock.of("this.$N", CONTEXT_FIELD);
        }
    }

    private final class Expression extends Base {
        Expression(ValidatedGraph.Node node, Scope scope, CodeBlock.Builder prelude) {
            super(node, scope, prelude);
        }
    }

    private final class Entry extends Base implements EntryContext {
        Entry(ValidatedGraph.Node node, Scope scope, CodeBlock.Builder prelude) {
            super(node, scope, prelude);
        }

        @Override
        public CodeBlock parameter(String name) {
            EntryDefinition definition = (EntryDefinition) node.definition();
            if (definition.parameters().stream()
                    .noneMatch(parameter -> parameter.name().equals(name))) {
                throw new IllegalArgumentException("%s has no parameter %s".formatted(definition.id(), name));
            }
            return CodeBlock.of("$N", name);
        }
    }

    private final class Statement extends Base implements StatementContext {
        private final Map<String, Map<String, CodeBlock>> bindings = new HashMap<>();

        Statement(ValidatedGraph.Node node, Scope scope, CodeBlock.Builder prelude) {
            super(node, scope, prelude);
        }

        @Override
        public CodeBlock chain(String port) {
            return GraphEmitter.this.chain(node.id(), execution(port), scope, bindings.getOrDefault(port, Map.of()));
        }

        @Override
        public CodeBlock bind(String port, String... visibleIn) {
            if (node.definition().descriptor().output(port).isEmpty()) {
                throw new IllegalArgumentException(
                        "%s has no output %s".formatted(node.definition().id(), port));
            }
            if (visibleIn.length == 0)
                throw new IllegalArgumentException(
                        "Output %s must be visible in at least one execution port".formatted(port));
            CodeBlock local = CodeBlock.of("$N", name("v", node.id(), port));
            for (String execution : visibleIn) {
                bindings.computeIfAbsent(execution(execution).id(), key -> new HashMap<>())
                        .put(Scope.key(node.id(), port), local);
            }
            return local;
        }

        private ExecutionPort execution(String port) {
            return node.definition()
                    .descriptor()
                    .execution(port)
                    .orElseThrow(() -> new IllegalArgumentException("%s has no execution port %s"
                            .formatted(node.definition().id(), port)));
        }
    }
}
