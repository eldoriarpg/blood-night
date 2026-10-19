/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.compiler.emit;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.ParameterSpec;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import de.eldoria.graph.compiler.GraphCompilationException;
import de.eldoria.graph.compiler.java.SourceMap;
import de.eldoria.graph.compiler.validate.GraphProblem;
import de.eldoria.graph.compiler.validate.ValidatedGraph;
import de.eldoria.graph.definition.EntryDefinition;
import de.eldoria.graph.definition.ExpressionDefinition;
import de.eldoria.graph.definition.MacroDefinition;
import de.eldoria.graph.definition.StatementDefinition;
import de.eldoria.graph.definition.emit.EmitContext;
import de.eldoria.graph.definition.emit.StatementContext;
import de.eldoria.graph.definition.entry.EntryContext;
import de.eldoria.graph.definition.entry.EntryParameter;
import de.eldoria.graph.definition.port.ConfigField;
import de.eldoria.graph.definition.port.ExecutionPort;
import de.eldoria.graph.definition.port.PortType;
import de.eldoria.graph.model.Edge;
import de.eldoria.graph.model.NodeInstance;
import de.eldoria.graph.runtime.StateMachine;
import org.jspecify.annotations.Nullable;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.NullNode;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
 * <p>
 * The code is meant to be read: locals are named after what they hold, literals that are not plain Java literals
 * (registry entries, enum constants) become constants, and a value read exactly once where it is computed is written
 * straight into its reader. To know what is read once, each entry method is emitted twice: the first pass names every
 * local with a unique token and counts the tokens in its output, the second pass inlines the locals counted once.
 * Values of stateful nodes, values read from a nested block, values of nodes keeping state in fields and numeric
 * values computed from operands of another type (whose local converts them, e.g. two ints to a double) always stay
 * locals, so nothing changes how often or whether such a value is computed. Emission is deterministic, which keeps
 * both passes in step.
 */
public final class GraphEmitter {
    private static final String CONTEXT_FIELD = "context";
    private static final Set<String> GENERIC_PORTS = Set.of("result", "value", "entity");
    private static final Set<String> GENERIC_TAILS = Set.of("create", "get", "of");

    private final ValidatedGraph graph;
    private final List<GraphProblem> problems = new ArrayList<>();
    private final Set<String> reported = new LinkedHashSet<>();
    private final SourceMap sourceMap = new SourceMap();
    private final Map<String, CodeBlock> fields = new HashMap<>();
    private final List<FieldSpec> fieldSpecs = new ArrayList<>();
    private final CodeBlock.Builder fieldInitializers = CodeBlock.builder();
    private final Identifiers fieldNames = new Identifiers(CONTEXT_FIELD);
    private final Map<String, CodeBlock> constants = new HashMap<>();
    private final List<FieldSpec> constantSpecs = new ArrayList<>();
    private final Map<String, ClassName> staticImports = new LinkedHashMap<>();
    private final Set<CodeBlock> ticking = new LinkedHashSet<>();
    private final Map<String, TypeName> sharedTypes = new HashMap<>();
    private Pass pass = Pass.counting();

    private final boolean inline;
    private final Map<String, Routines.Routine> routines;
    private final States.@Nullable Layout layout;
    private final Map<String, Set<CodeBlock>> leaving = new LinkedHashMap<>();
    private @Nullable String machineField;

    /**
     * @param graph  the graph
     * @param inline whether values read once are written into their reader
     */
    public GraphEmitter(ValidatedGraph graph, boolean inline) {
        this.graph = graph;
        this.inline = inline;
        Map<String, NodeInstance> instances = new LinkedHashMap<>();
        graph.nodes().forEach((id, node) -> instances.put(id, node.instance()));
        this.routines = Routines.of(instances, graph.platform().types());
        this.layout = graph.states().isEmpty() ? null : States.layout(graph.states());
    }

    /**
     * The emitted class with the map needed to locate compiler errors in it.
     *
     * @param file      the source
     * @param sourceMap marker positions, indexed against the rendered source
     */
    public record Emitted(JavaFile file, SourceMap sourceMap) {}

    /**
     * @param name name of the generated class
     * @return the source
     * @throws GraphCompilationException when a node reads a value it cannot see, or a definition fails
     */
    public Emitted emit(ClassName name) throws GraphCompilationException {
        Class<?> host = graph.platform().host();
        TypeSpec.Builder type = TypeSpec.classBuilder(name)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(host);

        stateMachine();
        Map<Method, List<String>> entriesByMethod = new LinkedHashMap<>();
        for (String entry : graph.entries()) {
            EntryDefinition definition =
                    (EntryDefinition) graph.nodes().get(entry).definition();
            if (definition.id().equals(Routines.DEFINE)
                    || definition.id().equals(States.ENTER)
                    || definition.id().equals(States.EXIT)) continue;
            entriesByMethod
                    .computeIfAbsent(hostMethod(definition), key -> new ArrayList<>())
                    .add(entry);
        }
        Map<Method, MethodSpec> methods = new LinkedHashMap<>();
        entriesByMethod.forEach((method, entries) -> methods.put(method, entryMethod(method, entries)));
        List<MethodSpec> routineMethods = new ArrayList<>();
        routines.values().forEach(routine -> routineMethod(routine).ifPresent(routineMethods::add));
        if (layout != null) {
            graph.platform().clock().ifPresent(clock -> ticking.add(CodeBlock.of("this.$N.start()", machineField)));
            routineMethods.addAll(stateMethods(layout));
        }
        graph.platform()
                .clock()
                .filter(clock -> !ticking.isEmpty())
                .ifPresent(clock -> methods.put(clock, ticked(methods.getOrDefault(clock, emptyMethod(clock)))));
        methods.values().forEach(type::addMethod);
        routineMethods.forEach(type::addMethod);
        for (Method method : host.getMethods()) {
            if (java.lang.reflect.Modifier.isAbstract(method.getModifiers()) && !methods.containsKey(method)) {
                type.addMethod(emptyMethod(method));
            }
        }

        type.addFields(constantSpecs);
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
        JavaFile.Builder builder = JavaFile.builder(name.packageName(), type.build());
        staticImports.forEach((method, owner) -> builder.addStaticImport(owner, method));
        JavaFile file = builder.skipJavaLangImports(true).indent("    ").build();
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
        return twoPass(() -> emitEntryMethod(method, entries));
    }

    /**
     * A routine as a private method. Its name must not hide a method of the host, which a call would reach
     * instead.
     */
    private Optional<MethodSpec> routineMethod(Routines.Routine routine) {
        boolean taken = Stream.concat(
                        Arrays.stream(graph.platform().host().getMethods()), Arrays.stream(Object.class.getMethods()))
                .anyMatch(method -> method.getName().equals(routine.method()));
        if (taken) {
            report(GraphProblem.error(
                    routine.define(),
                    Routines.NAME,
                    "Routine name %s is taken by a method of the behaviour".formatted(routine.name())));
            return Optional.empty();
        }
        return Optional.of(twoPass(() -> emitRoutineMethod(routine)));
    }

    private MethodSpec emitRoutineMethod(Routines.Routine routine) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(routine.method()).addModifiers(Modifier.PRIVATE);
        Set<String> methodNames = new HashSet<>();
        routine.parameters().forEach((name, type) -> {
            String parameter = Routines.javaName(name);
            builder.addParameter(type.typeName(), parameter);
            methodNames.add(parameter);
        });
        Scope root = Scope.root(routine.define(), methodNames);
        builder.addCode(marker(routine.define()));
        for (ExecutionPort port :
                graph.nodes().get(routine.define()).definition().descriptor().executions()) {
            builder.addCode(chain(routine.define(), port, root, Map.of(), true));
        }
        return builder.addCode(sourceMap.boundary()).build();
    }

    /**
     * Declares the field holding the graph's state machine, built in the constructor after every other field.
     */
    private void stateMachine() {
        if (machineField != null || layout == null) return;
        machineField = fieldNames.claim("states");
        fieldSpecs.add(
                FieldSpec.builder(ClassName.get(StateMachine.class), machineField, Modifier.PRIVATE, Modifier.FINAL)
                        .build());
    }

    /**
     * The methods states need: per state its enter, exit and enter condition where it has them, and three methods
     * picking them by number for the {@link StateMachine}. The exits come last, since they also end what the nodes of
     * the state asked to end on leaving it.
     */
    private List<MethodSpec> stateMethods(States.Layout layout) {
        Map<String, String> enters = new LinkedHashMap<>();
        Map<String, String> exits = new LinkedHashMap<>();
        Map<String, String> conditions = new LinkedHashMap<>();
        graph.nodes().forEach((id, node) -> {
            String state = node.instance().state();
            if (state == null) return;
            switch (node.definition().id()) {
                case States.ENTER -> enters.put(state, id);
                case States.EXIT -> exits.put(state, id);
                case States.CAN_ENTER -> conditions.put(state, id);
                default -> {}
            }
        });
        List<MethodSpec> methods = new ArrayList<>();
        Map<String, String> enterMethods = new LinkedHashMap<>();
        enters.forEach((state, node) -> {
            String name = fieldNames.claim(Identifiers.lowerCamel("enter_" + state));
            enterMethods.put(state, name);
            methods.add(twoPass(() -> chainMethod(name, node)));
        });
        Map<String, String> conditionMethods = new LinkedHashMap<>();
        conditions.forEach((state, node) -> {
            String name = fieldNames.claim(Identifiers.lowerCamel("can_enter_" + state));
            conditionMethods.put(state, name);
            methods.add(twoPass(() -> conditionMethod(name, node)));
        });
        Map<String, String> exitMethods = new LinkedHashMap<>();
        List<MethodSpec> exitSpecs = new ArrayList<>();
        exits.forEach((state, node) -> {
            String name = fieldNames.claim(Identifiers.lowerCamel("exit_" + state));
            exitMethods.put(state, name);
            exitSpecs.add(twoPass(() -> chainMethod(name, node)));
        });
        methods.addAll(exitSpecs);
        methods.add(dispatch(
                "enterState",
                layout,
                state -> enterMethods.containsKey(state) ? CodeBlock.of("$N();\n", enterMethods.get(state)) : null));
        methods.add(dispatch("exitState", layout, state -> {
            CodeBlock.Builder code = CodeBlock.builder();
            if (exitMethods.containsKey(state)) code.addStatement("$N()", exitMethods.get(state));
            leaving.getOrDefault(state, Set.of()).forEach(code::addStatement);
            return code.isEmpty() ? null : code.build();
        }));
        MethodSpec.Builder allowed = MethodSpec.methodBuilder("mayEnterState")
                .addModifiers(Modifier.PRIVATE)
                .returns(boolean.class)
                .addParameter(int.class, "state");
        allowed.beginControlFlow("switch (state)");
        conditionMethods.forEach(
                (state, method) -> allowed.addStatement("case $L: return $N()", layout.number(state), method));
        allowed.addStatement("default: return true").endControlFlow();
        methods.add(allowed.build());
        fieldInitializers.addStatement(
                "this.$N = new $T(new String[] {$L}, new int[] {$L}, new int[] {$L}, new int[] {$L}, $L,"
                        + " this::enterState, this::exitState, this::mayEnterState)",
                machineField,
                StateMachine.class,
                layout.names().stream().map(name -> CodeBlock.of("$S", name)).collect(CodeBlock.joining(", ")),
                numbers(layout.parents()),
                numbers(layout.lasts()),
                numbers(layout.initialChildren()),
                layout.initial());
        return methods;
    }

    private static CodeBlock numbers(int[] values) {
        return Arrays.stream(values)
                .mapToObj(value -> CodeBlock.of("$L", value))
                .collect(CodeBlock.joining(", "));
    }

    private MethodSpec dispatch(String name, States.Layout layout, Function<String, @Nullable CodeBlock> body) {
        MethodSpec.Builder method =
                MethodSpec.methodBuilder(name).addModifiers(Modifier.PRIVATE).addParameter(int.class, "state");
        method.beginControlFlow("switch (state)");
        for (String state : layout.names()) {
            CodeBlock code = body.apply(state);
            if (code == null) continue;
            method.addCode("case $L:\n", layout.number(state))
                    .addCode("$>$L", code)
                    .addStatement("break$<");
        }
        method.addCode("default:\n").addStatement("$>break$<").endControlFlow();
        return method.build();
    }

    /**
     * A private method running the chain of a state's enter or exit node.
     */
    private MethodSpec chainMethod(String name, String node) {
        MethodSpec.Builder builder = MethodSpec.methodBuilder(name).addModifiers(Modifier.PRIVATE);
        Scope root = Scope.root(node, new HashSet<>());
        builder.addCode(marker(node));
        for (ExecutionPort port :
                graph.nodes().get(node).definition().descriptor().executions()) {
            builder.addCode(chain(node, port, root, Map.of(), true));
        }
        return builder.addCode(sourceMap.boundary()).build();
    }

    /**
     * A private method returning a state's enter condition.
     */
    private MethodSpec conditionMethod(String name, String node) {
        MethodSpec.Builder builder =
                MethodSpec.methodBuilder(name).addModifiers(Modifier.PRIVATE).returns(boolean.class);
        Scope root = Scope.root(node, new HashSet<>());
        CodeBlock.Builder prelude = CodeBlock.builder();
        CodeBlock allowed = new Expression(graph.nodes().get(node), root, prelude).input(States.ALLOWED);
        return builder.addCode(marker(node))
                .addCode(prelude.build())
                .addStatement("return $L", allowed)
                .addCode(sourceMap.boundary())
                .build();
    }

    /**
     * Emits a method twice, see the class documentation: counting reads, then naming and inlining.
     */
    private MethodSpec twoPass(Supplier<MethodSpec> emit) {
        pass = Pass.counting();
        MethodSpec counted = emit.get();
        pass = inline ? pass.inlining(counted.toString()) : Pass.naming();
        MethodSpec emitted = emit.get();
        pass = Pass.counting();
        return emitted;
    }

    /**
     * A routine parameter is the parameter of the routine's method, and only visible inside it.
     */
    private CodeBlock parameter(String reader, String readerPort, ValidatedGraph.Node source, Scope scope) {
        JsonNode routineName = source.instance().config().get(Routines.ROUTINE);
        JsonNode name = source.instance().config().get(Routines.NAME);
        Routines.Routine routine = routineName == null ? null : routines.get(routineName.asString());
        if (routine == null || name == null || !scope.within(routine.define())) {
            return fail(
                    reader,
                    readerPort,
                    "Parameter %s of routine %s is only available inside that routine"
                            .formatted(
                                    name == null ? "?" : name.asString(),
                                    routineName == null ? "?" : routineName.asString()));
        }
        return CodeBlock.of("$N", Routines.javaName(name.asString()));
    }

    private MethodSpec emitEntryMethod(Method method, List<String> entries) {
        EntryDefinition definition =
                (EntryDefinition) graph.nodes().get(entries.getFirst()).definition();
        List<String> parameters =
                definition.parameters().stream().map(EntryParameter::name).toList();
        Set<String> methodNames = new HashSet<>(parameters);
        MethodSpec.Builder builder = override(method, parameters);
        boolean returnsNothing = method.getReturnType() == void.class;
        List<ExecutionPort> ports = definition.descriptor().executions();
        if (layout != null) builder.addStatement("this.$N.start()", machineField);
        for (String entry : entries) {
            Scope root = Scope.root(entry, methodNames);
            builder.addCode(marker(entry));
            String state = graph.nodes().get(entry).instance().state();
            if (state != null && layout != null) {
                builder.beginControlFlow("if (this.$N.in($L))", machineField, layout.number(state));
            }
            Optional<CodeBlock> guard =
                    definition.guard().condition(new Entry(graph.nodes().get(entry), root, CodeBlock.builder()));
            guard.ifPresent(condition -> builder.beginControlFlow("if ($L)", condition));
            for (ExecutionPort port : ports) {
                boolean last = returnsNothing
                        && state == null
                        && entry.equals(entries.getLast())
                        && port.equals(ports.getLast());
                builder.addCode(chain(entry, port, root, Map.of(), last));
            }
            guard.ifPresent(condition -> builder.endControlFlow());
            if (state != null && layout != null) builder.endControlFlow();
        }
        return builder.addCode(sourceMap.boundary()).build();
    }

    /**
     * The clock method with the statements nodes run every tick in front of its own code.
     */
    private MethodSpec ticked(MethodSpec method) {
        CodeBlock.Builder code = CodeBlock.builder();
        ticking.forEach(statement -> code.addStatement(statement));
        return MethodSpec.methodBuilder(method.name())
                .addAnnotations(method.annotations())
                .addModifiers(method.modifiers())
                .returns(method.returnType())
                .addParameters(method.parameters())
                .addExceptions(method.exceptions())
                .addCode(code.build())
                .addCode(method.code())
                .build();
    }

    private CodeBlock marker(String node) {
        return sourceMap.marker(node, graph.nodes().get(node).definition().id());
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

    /**
     * @param last whether the chain is the last code of its method, so its last node may return early
     */
    private CodeBlock chain(
            String node, ExecutionPort port, Scope owner, Map<String, CodeBlock> bindings, boolean last) {
        Scope scope = owner.child(port.repeating());
        bindings.forEach(scope::bind);
        CodeBlock.Builder code = CodeBlock.builder();
        List<String> targets = graph.nodes().get(node).instance().executions().getOrDefault(port.id(), List.of());
        for (int i = 0; i < targets.size(); i++) {
            code.add(statement(targets.get(i), scope, last && !port.repeating() && i == targets.size() - 1));
        }
        return code.build();
    }

    private CodeBlock statement(String node, Scope scope, boolean last) {
        ValidatedGraph.Node info = graph.nodes().get(node);
        CodeBlock.Builder prelude = CodeBlock.builder();
        Statement context = new Statement(info, scope, prelude, last);
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
        scope.closeChildren();
        return CodeBlock.builder()
                .add(marker(node))
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
        if (existing == null) {
            existing = scope.findComputed(key);
            if (existing != null && !scope.holds(key)) pass.pin(existing);
        }
        if (existing != null) return existing;

        ValidatedGraph.Node source = graph.nodes().get(edge.node());
        if (source.definition().id().equals(Routines.PARAMETER)) {
            return parameter(reader, readerPort, source, scope);
        }
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
        int index = pass.next();
        boolean outerTouched = pass.touchedState;
        pass.touchedState = false;
        CodeBlock expression;
        try {
            expression = call.emit();
        } catch (RuntimeException e) {
            return fail(
                    source.id(),
                    port,
                    "Definition %s failed: %s".formatted(source.definition().id(), e.getMessage()));
        } finally {
            boolean touched = pass.touchedState;
            pass.touchedState = outerTouched;
            if (touched || source.definition().descriptor().stateful()) pass.pinIndex(index);
        }
        String key = Scope.key(source.id(), port);
        if (inline && Expressions.isName(expression)) {
            scope.compute(key, expression);
            return expression;
        }
        if (Expressions.isConditional(expression)) pass.pinIndex(index);
        if (type instanceof PortType.Concrete concrete
                && concrete.raw().isPrimitive()
                && concrete.raw() != boolean.class
                && !Expressions.isCallOrLiteral(expression)
                && !source.sourceTypes().values().stream().allMatch(type::equals)) {
            pass.pinIndex(index);
        }
        if (pass.inlines(index)) {
            CodeBlock inlined = Expressions.grouped(expression);
            scope.compute(key, inlined);
            return inlined;
        }
        CodeBlock local = pass.counting ? pass.token(index) : CodeBlock.of("$N", localName(source, port, scope));
        prelude.add(marker(source.id()));
        prelude.addStatement("final $T $L = $L", type.typeName(), local, expression);
        scope.compute(key, local);
        return local;
    }

    /**
     * Names a local after the role of the value: the port, or for ports with a generic name the node type. When the
     * name is taken and the node reads a {@code target}, the target's name is put in front, e.g.
     * {@code targetLocation}.
     */
    private String localName(ValidatedGraph.Node node, String port, Scope scope) {
        String name = Identifiers.lowerCamel(role(node, port));
        if (!scope.free(name)) {
            Edge target = node.instance().inputs().get("target");
            CodeBlock owner = target == null ? null : scope.findComputed(Scope.key(target.node(), target.port()));
            if (owner == null && target != null) owner = scope.findBound(Scope.key(target.node(), target.port()));
            if (owner != null && Expressions.isName(owner)) {
                name = Identifiers.lowerCamel(owner + "_" + name);
            }
        }
        return declare(scope, name);
    }

    private static String declare(Scope scope, String name) {
        String claimed = Identifiers.first(name, scope::free);
        scope.declare(claimed);
        return claimed;
    }

    private static String role(ValidatedGraph.Node node, String port) {
        String type = node.definition().id();
        String tail = type.substring(type.lastIndexOf('.') + 1);
        if (type.startsWith("cast.")) return (port.equals("matches") ? "is_" : "as_") + tail;
        if (!GENERIC_PORTS.contains(port) || node.definition() instanceof EntryDefinition) return port;
        if (GENERIC_TAILS.contains(tail)) {
            String owner = type.substring(0, type.lastIndexOf('.'));
            return owner.substring(owner.lastIndexOf('.') + 1);
        }
        if (tail.startsWith("create_")) return tail.substring("create_".length());
        return tail;
    }

    /**
     * Turns a literal into a constant of the class unless it is a plain Java literal. The same type and value share
     * one constant, named after the value, e.g. {@code minecraft:speed} becomes {@code SPEED}.
     */
    private CodeBlock constant(PortType type, CodeBlock literal, @Nullable JsonNode value) {
        if (Expressions.isPlainLiteral(literal)) return literal;
        return constants.computeIfAbsent(type.id() + '\u0000' + literal, key -> {
            String typeName = type.typeName() instanceof ClassName className ? className.simpleName() : "value";
            String name = value != null && value.isString()
                    ? Identifiers.upperSnake(
                            value.asString().substring(value.asString().indexOf(':') + 1))
                    : Identifiers.upperSnake(typeName);
            if (name.isEmpty() || fieldNames.taken(name)) name = Identifiers.upperSnake(typeName + "_" + name);
            String field = fieldNames.claim(name);
            constantSpecs.add(
                    FieldSpec.builder(type.typeName(), field, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                            .initializer(literal)
                            .build());
            return CodeBlock.of("$N", field);
        });
    }

    @FunctionalInterface
    private interface EmitterCall {
        public CodeBlock emit();
    }

    private static String simple(TypeName type) {
        String name = type.toString();
        return name.substring(name.lastIndexOf('.') + 1);
    }

    private CodeBlock fail(String node, String port, String message) {
        report(GraphProblem.error(node, port, message));
        return CodeBlock.of("null");
    }

    private void report(GraphProblem problem) {
        if (reported.add(problem.toString())) problems.add(problem);
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
            if (literal != null) {
                return constant(
                        node.inputTypes().get(port),
                        literal,
                        node.instance().literals().get(port));
            }
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
            ConfigField declared =
                    node.definition().descriptor().configField(field).orElse(null);
            if (literal == null || declared == null)
                throw new IllegalArgumentException(
                        "%s has no config field %s".formatted(node.definition().id(), field));
            JsonNode value = node.instance().config().get(field);
            return constant(
                    node.configTypes().getOrDefault(field, declared.type()),
                    literal,
                    value == null ? declared.defaultValue() : value);
        }

        @Override
        public JsonNode configValue(String field) {
            ConfigField declared =
                    node.definition().descriptor().configField(field).orElse(null);
            if (declared == null)
                throw new IllegalArgumentException(
                        "%s has no config field %s".formatted(node.definition().id(), field));
            JsonNode value = node.instance().config().get(field);
            if (value != null) return value;
            return declared.defaultIfAbsent().orElse(NullNode.getInstance());
        }

        @Override
        public CodeBlock staticCall(ClassName owner, String method, CodeBlock... arguments) {
            CodeBlock joined = CodeBlock.join(List.of(arguments), ", ");
            boolean inherited = Arrays.stream(graph.platform().host().getMethods())
                            .anyMatch(member -> member.getName().equals(method))
                    || routines.values().stream()
                            .anyMatch(routine -> routine.method().equals(method));
            if (!inherited) {
                ClassName holder = staticImports.putIfAbsent(method, owner);
                if (holder == null || holder.equals(owner)) return CodeBlock.of("$N($L)", method, joined);
            }
            return CodeBlock.of("$T.$N($L)", owner, method, joined);
        }

        @Override
        public boolean isRead(String port) {
            return graph.nodes().values().stream()
                    .flatMap(other -> other.instance().inputs().values().stream())
                    .anyMatch(
                            edge -> edge.node().equals(node.id()) && edge.port().equals(port));
        }

        @Override
        public TypeName inputType(String port) {
            PortType source = node.sourceTypes().get(port);
            return source == null ? type(port) : source.typeName();
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
            pass.touchedState = true;
            return fields.computeIfAbsent(Scope.key(node.id(), name), key -> {
                String field = fieldNames.claim(Identifiers.lowerCamel(name));
                fieldSpecs.add(FieldSpec.builder(type, field, Modifier.PRIVATE).build());
                fieldInitializers.add(marker(node.id()));
                fieldInitializers.addStatement("this.$N = $L", field, initializer);
                return CodeBlock.of("this.$N", field);
            });
        }

        @Override
        public CodeBlock sharedField(String name, TypeName type, CodeBlock initializer) {
            pass.touchedState = true;
            TypeName declared = sharedTypes.putIfAbsent(name, type);
            if (declared != null && !declared.equals(type)) {
                report(GraphProblem.error(
                        node.id(),
                        null,
                        "%s already holds %s values, not %s".formatted(name, simple(declared), simple(type))));
            }
            return fields.computeIfAbsent(Scope.key("", name), key -> {
                String field = fieldNames.claim(Identifiers.lowerCamel(name));
                fieldSpecs.add(FieldSpec.builder(type, field, Modifier.PRIVATE).build());
                fieldInitializers.add(marker(node.id()));
                fieldInitializers.addStatement("this.$N = $L", field, initializer);
                return CodeBlock.of("this.$N", field);
            });
        }

        @Override
        public CodeBlock stateField(String name, TypeName type, CodeBlock initializer) {
            String state = node.instance().state();
            if (state == null) return sharedField(name, type, initializer);
            pass.touchedState = true;
            return fields.computeIfAbsent(Scope.key("state " + state, name), key -> {
                String field = fieldNames.claim(Identifiers.lowerCamel(name + "_" + state));
                fieldSpecs.add(FieldSpec.builder(type, field, Modifier.PRIVATE).build());
                fieldInitializers.add(marker(node.id()));
                fieldInitializers.addStatement("this.$N = $L", field, initializer);
                return CodeBlock.of("this.$N", field);
            });
        }

        @Override
        public void onLeave(CodeBlock statement) {
            String state = node.instance().state();
            if (state != null)
                leaving.computeIfAbsent(state, key -> new LinkedHashSet<>()).add(statement);
        }

        @Override
        public CodeBlock stateMachine() {
            if (layout == null) throw new IllegalStateException("The graph has no states");
            GraphEmitter.this.stateMachine();
            return CodeBlock.of("this.$N", machineField);
        }

        @Override
        public int stateNumber(String name) {
            if (layout == null) throw new IllegalArgumentException("The graph has no states");
            return layout.number(name);
        }

        @Override
        public CodeBlock context() {
            if (graph.platform().context().isEmpty())
                throw new IllegalStateException("The platform has no context type");
            return CodeBlock.of("this.$N", CONTEXT_FIELD);
        }

        @Override
        public void everyTick(CodeBlock statement) {
            if (graph.platform().clock().isEmpty())
                throw new IllegalStateException("The platform has no clock, nothing can wait");
            ticking.add(statement);
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
        private final boolean last;

        Statement(ValidatedGraph.Node node, Scope scope, CodeBlock.Builder prelude, boolean last) {
            super(node, scope, prelude);
            this.last = last;
        }

        @Override
        public CodeBlock chain(String port) {
            return GraphEmitter.this.chain(
                    node.id(), execution(port), scope, bindings.getOrDefault(port, Map.of()), false);
        }

        @Override
        public boolean chained(String port) {
            execution(port);
            return !node.instance().executions().getOrDefault(port, List.of()).isEmpty();
        }

        @Override
        public boolean last() {
            return last;
        }

        @Override
        public CodeBlock lastChain(String port) {
            if (!last) throw new IllegalStateException(node.id() + " is not the last node of its method");
            return GraphEmitter.this.chain(
                    node.id(), execution(port), scope, bindings.getOrDefault(port, Map.of()), true);
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
            CodeBlock local = CodeBlock.of("$N", declare(scope, Identifiers.lowerCamel(role(node, port))));
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

    /**
     * State of one emission of an entry method; see the class documentation.
     */
    private static final class Pass {
        private static final Pattern TOKEN = Pattern.compile("\\b__v(\\d+)\\b");

        private final boolean counting;
        private final Set<Integer> inline;
        private final Set<Integer> pinned = new HashSet<>();
        private int computed;
        private boolean touchedState;

        private Pass(boolean counting, Set<Integer> inline) {
            this.counting = counting;
            this.inline = inline;
        }

        static Pass counting() {
            return new Pass(true, Set.of());
        }

        /**
         * @return a pass giving every local its name and inlining nothing
         */
        static Pass naming() {
            return new Pass(false, Set.of());
        }

        /**
         * @param counted the method as the counting pass emitted it
         * @return the pass inlining every local that is not pinned and appears twice in it: declared and read once
         */
        Pass inlining(String counted) {
            Map<Integer, Integer> occurrences = new HashMap<>();
            Matcher matcher = TOKEN.matcher(counted);
            while (matcher.find()) occurrences.merge(Integer.parseInt(matcher.group(1)), 1, Integer::sum);
            Set<Integer> once = new HashSet<>();
            occurrences.forEach((index, count) -> {
                if (count == 2 && !pinned.contains(index)) once.add(index);
            });
            return new Pass(false, once);
        }

        int next() {
            return computed++;
        }

        CodeBlock token(int index) {
            return CodeBlock.of("$L", "__v" + index);
        }

        boolean inlines(int index) {
            return inline.contains(index);
        }

        void pinIndex(int index) {
            pinned.add(index);
        }

        void pin(CodeBlock value) {
            Matcher matcher = TOKEN.matcher(value.toString());
            if (counting && matcher.matches()) pinned.add(Integer.parseInt(matcher.group(1)));
        }
    }
}
