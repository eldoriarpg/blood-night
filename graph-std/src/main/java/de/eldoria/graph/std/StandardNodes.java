/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.std;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import de.eldoria.graph.definition.EmitContext;
import de.eldoria.graph.definition.ExpressionDefinition;
import de.eldoria.graph.definition.NodeDefinition;
import de.eldoria.graph.definition.NodeDescriptor;
import de.eldoria.graph.definition.NodeKind;
import de.eldoria.graph.definition.PortType;
import de.eldoria.graph.definition.StatementContext;
import de.eldoria.graph.definition.StatementDefinition;
import de.eldoria.graph.std.runtime.Cooldown;
import de.eldoria.graph.std.runtime.Randoms;
import de.eldoria.graph.std.runtime.Timer;
import de.eldoria.graph.types.TypeMapping;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.BooleanNode;
import tools.jackson.databind.node.DoubleNode;
import tools.jackson.databind.node.IntNode;
import tools.jackson.databind.node.StringNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generic nodes every consumer can use: values, math, logic, functions, control flow and debugging.
 * <p>
 * None of them know anything about the consumer. Port ids follow the nodes they replace ({@code first},
 * {@code second}, {@code value}, {@code result}, {@code lower}, {@code upper}), so old graphs migrate by node type
 * alone; control flow is the exception, since it now separates the repeated {@code body} from the following
 * {@code next}.
 * <p>
 * Generated code calls into {@link de.eldoria.graph.std.runtime}, so this module has to be on the compile classpath
 * of generated classes.
 */
public final class StandardNodes {
    private static final String VALUE = "value";
    private static final String RESULT = "result";
    private static final String FIRST = "first";
    private static final String SECOND = "second";
    private static final String LOWER = "lower";
    private static final String UPPER = "upper";
    private static final String NEXT = "next";
    private static final String BODY = "body";
    private static final String TIMER = "timer";
    private static final ClassName RANDOMS = ClassName.get(Randoms.class);
    private static final Pattern EQUALITY = Pattern.compile("\\(([A-Za-z_$][A-Za-z0-9_$]*) == ([A-Za-z0-9_$]+)\\)");
    private static final Pattern SIMPLE = Pattern.compile("[A-Za-z_$][A-Za-z0-9_$.]*(\\(\\))?");
    private static final String DEFAULT_TIMER = "timer";

    public static final ExpressionDefinition BOOLEAN =
            value(StdNodes.VALUE_BOOLEAN, boolean.class, BooleanNode.FALSE, "A fixed true or false");
    public static final ExpressionDefinition INTEGER =
            value(StdNodes.VALUE_INTEGER, int.class, IntNode.valueOf(0), "A fixed integer number");
    public static final ExpressionDefinition NUMBER =
            value(StdNodes.VALUE_NUMBER, double.class, DoubleNode.valueOf(0), "A fixed decimal number");
    public static final ExpressionDefinition TEXT =
            value(StdNodes.VALUE_TEXT, String.class, StringNode.valueOf(""), "A fixed text");

    public static final ExpressionDefinition ADD = arithmetic(StdNodes.MATH_ADD, "+", "Adds two numbers");
    public static final ExpressionDefinition SUBTRACT =
            arithmetic(StdNodes.MATH_SUBTRACT, "-", "Subtracts the second number from the first");
    public static final ExpressionDefinition MULTIPLY =
            arithmetic(StdNodes.MATH_MULTIPLY, "*", "Multiplies two numbers");
    public static final ExpressionDefinition DIVIDE =
            arithmetic(StdNodes.MATH_DIVIDE, "/", "Divides the first number by the second");
    public static final ExpressionDefinition ABS = unary(
            StdNodes.MATH_ABS,
            double.class,
            double.class,
            "Absolute value of a number",
            value -> CodeBlock.of("$T.abs($L)", Math.class, value));
    public static final ExpressionDefinition ABS_INTEGER = unary(
            StdNodes.MATH_ABS_INTEGER,
            double.class,
            int.class,
            "Absolute value of a number as integer, decimals cut off",
            value -> CodeBlock.of("$T.abs((int) $L)", Math.class, value));
    public static final ExpressionDefinition CEIL = unary(
            StdNodes.MATH_CEIL,
            double.class,
            int.class,
            "Rounds up to the next integer",
            value -> CodeBlock.of("(int) $T.ceil($L)", Math.class, value));
    public static final ExpressionDefinition FLOOR = unary(
            StdNodes.MATH_FLOOR,
            double.class,
            int.class,
            "Rounds down to the next integer",
            value -> CodeBlock.of("(int) $T.floor($L)", Math.class, value));
    public static final ExpressionDefinition ROUND = unary(
            StdNodes.MATH_ROUND,
            double.class,
            int.class,
            "Rounds to the closest integer",
            value -> CodeBlock.of("(int) $T.round($L)", Math.class, value));
    public static final ExpressionDefinition CLAMP =
            clamp(StdNodes.MATH_CLAMP, double.class, "Keeps a number within a lower and upper bound", "$L");
    public static final ExpressionDefinition CLAMP_INTEGER = clamp(
            StdNodes.MATH_CLAMP_INTEGER,
            int.class,
            "Keeps a number within bounds, as integer with decimals cut off",
            "(int) $L");

    public static final ExpressionDefinition AND =
            binary(StdNodes.LOGIC_AND, boolean.class, boolean.class, "&&", "True when both are true");
    public static final ExpressionDefinition OR =
            binary(StdNodes.LOGIC_OR, boolean.class, boolean.class, "||", "True when at least one is true");
    public static final ExpressionDefinition NOT = unary(
            StdNodes.LOGIC_NOT, boolean.class, boolean.class, "Inverts a boolean", value -> CodeBlock.of("!$L", value));
    public static final ExpressionDefinition GREATER =
            binary(StdNodes.LOGIC_GREATER, double.class, boolean.class, ">", "True when the first number is greater");
    public static final ExpressionDefinition GREATER_OR_EQUAL = binary(
            StdNodes.LOGIC_GREATER_OR_EQUAL,
            double.class,
            boolean.class,
            ">=",
            "True when the first number is greater or equal");
    public static final ExpressionDefinition LESS =
            binary(StdNodes.LOGIC_LESS, double.class, boolean.class, "<", "True when the first number is less");
    public static final ExpressionDefinition LESS_OR_EQUAL = binary(
            StdNodes.LOGIC_LESS_OR_EQUAL,
            double.class,
            boolean.class,
            "<=",
            "True when the first number is less or equal");
    public static final ExpressionDefinition EQUAL = new ExpressionDefinition(
            NodeDescriptor.builder(StdNodes.LOGIC_EQUAL, NodeKind.PURE)
                    .description("True when both values are equal; two missing values are equal")
                    .input(FIRST, Object.class)
                    .input(SECOND, Object.class)
                    .output(RESULT, boolean.class)
                    .build(),
            (port, ctx) -> CodeBlock.of("$T.equals($L, $L)", Objects.class, ctx.input(FIRST), ctx.input(SECOND)));
    public static final ExpressionDefinition IS_NULL = unary(
            StdNodes.LOGIC_IS_NULL,
            Object.class,
            boolean.class,
            "True when the value is missing",
            value -> CodeBlock.of("($L == null)", value));

    public static final ExpressionDefinition COOLDOWN = new ExpressionDefinition(
            NodeDescriptor.builder(StdNodes.FUNCTION_COOLDOWN, NodeKind.PURE)
                    .stateful()
                    .description("True at most once every n milliseconds; the first evaluation is true")
                    .input(VALUE, "Milliseconds", PortType.of(int.class), false)
                    .output(RESULT, boolean.class)
                    .build(),
            (port, ctx) -> CodeBlock.of(
                    "$L.ready($L)",
                    ctx.field("cooldown", ClassName.get(Cooldown.class), CodeBlock.of("new $T()", Cooldown.class)),
                    ctx.input(VALUE)));
    public static final ExpressionDefinition RANDOM_INTEGER = new ExpressionDefinition(
            NodeDescriptor.builder(StdNodes.FUNCTION_RANDOM_INTEGER, NodeKind.PURE)
                    .description("A random integer between lower and upper, both inclusive")
                    .input(LOWER, int.class)
                    .input(UPPER, int.class)
                    .output(RESULT, int.class)
                    .build(),
            (port, ctx) -> ctx.staticCall(RANDOMS, "nextInt", ctx.input(LOWER), ctx.input(UPPER)));
    public static final ExpressionDefinition RANDOM_NUMBER = new ExpressionDefinition(
            NodeDescriptor.builder(StdNodes.FUNCTION_RANDOM_NUMBER, NodeKind.PURE)
                    .description("A random number from lower, inclusive, to upper, exclusive")
                    .input(LOWER, double.class)
                    .input(UPPER, double.class)
                    .output(RESULT, double.class)
                    .build(),
            (port, ctx) -> ctx.staticCall(RANDOMS, "nextDouble", ctx.input(LOWER), ctx.input(UPPER)));

    public static final StatementDefinition BRANCH = new StatementDefinition(
            NodeDescriptor.builder(StdNodes.FLOW_BRANCH, NodeKind.CONTROL_FLOW)
                    .description("Runs one of two chains depending on a condition, then continues")
                    .input("condition", boolean.class)
                    .execution("true")
                    .execution("false")
                    .execution(NEXT)
                    .build(),
            StandardNodes::branch);
    public static final StatementDefinition BATCH = new StatementDefinition(
            NodeDescriptor.builder(StdNodes.FLOW_BATCH, NodeKind.CONTROL_FLOW)
                    .description("Runs a chain several times, then continues")
                    .input("count", int.class)
                    .output("index", int.class)
                    .repeatingExecution(BODY)
                    .execution(NEXT)
                    .build(),
            ctx -> {
                CodeBlock index = ctx.bind("index", BODY);
                return CodeBlock.builder()
                        .beginControlFlow("for (int $L = 0; $L < $L; $L++)", index, index, ctx.input("count"), index)
                        .add(ctx.chain(BODY))
                        .endControlFlow()
                        .add(ctx.chain(NEXT))
                        .build();
            });
    public static final StatementDefinition FOR_EACH = new StatementDefinition(
            NodeDescriptor.builder(StdNodes.FLOW_FOR_EACH, NodeKind.CONTROL_FLOW)
                    .description("Runs a chain for every element of a list, then continues")
                    .input("list", PortType.parameterized(Iterable.class, PortType.variable("T")))
                    .output("element", PortType.variable("T"))
                    .repeatingExecution(BODY)
                    .execution(NEXT)
                    .build(),
            ctx -> {
                CodeBlock element = ctx.bind("element", BODY);
                return CodeBlock.builder()
                        .beginControlFlow("for ($T $L : $L)", ctx.type("element"), element, ctx.input("list"))
                        .add(ctx.chain(BODY))
                        .endControlFlow()
                        .add(ctx.chain(NEXT))
                        .build();
            });

    public static final int SWITCH_CASES = 8;
    public static final StatementDefinition SWITCH = switchNode();
    public static final ExpressionDefinition CHANCE = new ExpressionDefinition(
            NodeDescriptor.builder(StdNodes.FUNCTION_CHANCE, NodeKind.PURE)
                    .description("True with the given probability in percent")
                    .input("percent", double.class)
                    .output(RESULT, boolean.class)
                    .build(),
            (port, ctx) -> ctx.staticCall(RANDOMS, "chance", ctx.input("percent")));

    public static final StatementDefinition PRINT = new StatementDefinition(
            NodeDescriptor.builder(StdNodes.DEBUG_PRINT, NodeKind.ACTION)
                    .description("Prints a value to the console")
                    .input(VALUE, Object.class)
                    .execution(NEXT)
                    .build(),
            ctx -> CodeBlock.builder()
                    .addStatement("$T.out.println($L)", System.class, ctx.input(VALUE))
                    .add(ctx.chain(NEXT))
                    .build());

    public static final StatementDefinition TIMER_RESET = new StatementDefinition(
            NodeDescriptor.builder(StdNodes.TIMER_RESET, NodeKind.ACTION)
                    .description("Starts a named timer again; every timer node with the same name shares it")
                    .config(TIMER, String.class, StringNode.valueOf(DEFAULT_TIMER))
                    .execution(NEXT)
                    .build(),
            ctx -> CodeBlock.builder()
                    .addStatement("$L.reset()", timer(ctx))
                    .add(ctx.chain(NEXT))
                    .build());

    public static final ExpressionDefinition TIMER_ELAPSED = new ExpressionDefinition(
            NodeDescriptor.builder(StdNodes.TIMER_ELAPSED, NodeKind.PURE)
                    .stateful()
                    .description("Seconds since the mob appeared or a named timer was last reset")
                    .config(TIMER, String.class, StringNode.valueOf(DEFAULT_TIMER))
                    .output("seconds", double.class)
                    .build(),
            (port, ctx) -> CodeBlock.of("$L.seconds()", timer(ctx)));

    private StandardNodes() {}

    private static CodeBlock timer(EmitContext ctx) {
        String name = ctx.config(TIMER).toString().replace("\"", "");
        return ctx.sharedField(
                name.equals(DEFAULT_TIMER) ? name : name + " timer",
                ClassName.get(Timer.class),
                CodeBlock.of("new $T()", Timer.class));
    }

    /**
     * Nodes that narrow a value to a more specific type, one per type users can select: {@code cast.<id>}, named
     * "As <Name>". Output {@code result} is the value as that type, or missing when it is not one; {@code matches}
     * tells which, so a branch can guard the use. Primitive and generic types are left out, since no value is an
     * instance of them.
     *
     * @param types the platform's type mapping
     * @return one cast node per selectable reference type
     */
    public static List<NodeDefinition> casts(TypeMapping types) {
        List<NodeDefinition> casts = new ArrayList<>();
        types.selectable().forEach((id, type) -> {
            if (!(type instanceof PortType.Concrete concrete)
                    || concrete.raw().isPrimitive()
                    || !concrete.arguments().isEmpty()) {
                return;
            }
            Class<?> raw = concrete.raw();
            String name = types.name(type);
            casts.add(new ExpressionDefinition(
                    NodeDescriptor.builder(StdNodes.cast(id), NodeKind.PURE)
                            .category("logic")
                            .name("As " + name)
                            .description(
                                    "The value as %s, missing when it is none; matches tells which".formatted(name))
                            .input(VALUE, Object.class)
                            .output(RESULT, type)
                            .output("matches", boolean.class)
                            .build(),
                    (port, ctx) -> {
                        CodeBlock value = ctx.input(VALUE);
                        if (ctx.inputType(VALUE).isPrimitive()) {
                            return port.equals("matches")
                                    ? CodeBlock.of("$T.class.isInstance($L)", raw, value)
                                    : CodeBlock.of(
                                            "($T.class.isInstance($L) ? $T.class.cast($L) : null)",
                                            raw,
                                            value,
                                            raw,
                                            value);
                        }
                        return port.equals("matches")
                                ? CodeBlock.of("($L instanceof $T)", value, raw)
                                : CodeBlock.of("($L instanceof $T ? ($T) $L : null)", value, raw, raw, value);
                    }));
        });
        return casts;
    }

    /**
     * @return every standard node
     */
    public static List<NodeDefinition> all() {
        return List.of(
                BOOLEAN,
                INTEGER,
                NUMBER,
                TEXT,
                ADD,
                SUBTRACT,
                MULTIPLY,
                DIVIDE,
                ABS,
                ABS_INTEGER,
                CEIL,
                FLOOR,
                ROUND,
                CLAMP,
                CLAMP_INTEGER,
                AND,
                OR,
                NOT,
                GREATER,
                GREATER_OR_EQUAL,
                LESS,
                LESS_OR_EQUAL,
                EQUAL,
                IS_NULL,
                COOLDOWN,
                TIMER_RESET,
                TIMER_ELAPSED,
                CHANCE,
                RANDOM_INTEGER,
                RANDOM_NUMBER,
                BRANCH,
                SWITCH,
                BATCH,
                FOR_EACH,
                PRINT);
    }

    /**
     * Runs the chain of the case matching the value, {@code default} for any other value, then {@code next}. Case
     * ports are fixed per node type, so there are {@value #SWITCH_CASES}, {@code case_0} to {@code case_7}.
     */
    private static StatementDefinition switchNode() {
        NodeDescriptor.Builder descriptor = NodeDescriptor.builder(StdNodes.FLOW_SWITCH, NodeKind.CONTROL_FLOW)
                .description("Runs the chain of the case matching the value, default otherwise, then continues")
                .input(VALUE, int.class);
        for (int i = 0; i < SWITCH_CASES; i++) descriptor.execution("case_" + i, "Case " + i);
        descriptor.execution("default").execution(NEXT);
        return new StatementDefinition(descriptor.build(), ctx -> {
            CodeBlock value = ctx.input(VALUE);
            CodeBlock.Builder cases = CodeBlock.builder();
            for (int i = 0; i < SWITCH_CASES; i++) {
                CodeBlock chain = ctx.chain("case_" + i);
                if (!chain.isEmpty())
                    cases.add("case $L -> {\n$>", i).add(chain).add("$<}\n");
            }
            CodeBlock otherwise = ctx.chain("default");
            if (!otherwise.isEmpty())
                cases.add("default -> {\n$>").add(otherwise).add("$<}\n");
            CodeBlock.Builder code = CodeBlock.builder();
            if (!cases.isEmpty()) {
                code.beginControlFlow("switch ($L)", value).add(cases.build()).endControlFlow();
            }
            return code.add(ctx.chain(NEXT)).build();
        });
    }

    /**
     * Writes only the arms that run something: {@code if (c)}, {@code if (!c)} or both. When the branch is the last
     * code of its method and only one arm runs something, it returns early instead: {@code if (!c) return;} followed
     * by the arm.
     */
    private static CodeBlock branch(StatementContext ctx) {
        CodeBlock condition = ctx.input("condition");
        boolean whenTrueChained = ctx.chained("true");
        if (ctx.last() && !ctx.chained(NEXT) && whenTrueChained != ctx.chained("false")) {
            return CodeBlock.builder()
                    .addStatement("if $L return", whenTrueChained ? negated(condition) : parenthesised(condition))
                    .add(ctx.lastChain(whenTrueChained ? "true" : "false"))
                    .build();
        }
        CodeBlock whenTrue = ctx.chain("true");
        CodeBlock whenFalse = ctx.chain("false");
        CodeBlock.Builder code = CodeBlock.builder();
        if (!whenTrue.isEmpty()) {
            code.beginControlFlow("if $L", parenthesised(condition)).add(whenTrue);
            if (!whenFalse.isEmpty()) code.nextControlFlow("else").add(whenFalse);
            code.endControlFlow();
        } else if (!whenFalse.isEmpty()) {
            code.beginControlFlow("if $L", negated(condition)).add(whenFalse).endControlFlow();
        }
        return code.add(ctx.chain(NEXT)).build();
    }

    /**
     * The condition in the parentheses an {@code if} needs, reusing its own when it is wrapped as a whole.
     */
    private static CodeBlock parenthesised(CodeBlock condition) {
        return wrappedWhole(condition.toString()) ? condition : CodeBlock.of("($L)", condition);
    }

    private static CodeBlock negated(CodeBlock condition) {
        String text = condition.toString();
        Matcher equality = EQUALITY.matcher(text);
        if (equality.matches()) return CodeBlock.of("($L != $L)", equality.group(1), equality.group(2));
        if (SIMPLE.matcher(text).matches() || wrappedWhole(text)) return CodeBlock.of("(!$L)", condition);
        return CodeBlock.of("(!($L))", condition);
    }

    private static boolean wrappedWhole(String expression) {
        if (!expression.startsWith("(") || !expression.endsWith(")")) return false;
        int depth = 0;
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            if (c == '"') return false;
            if (c == '(') depth++;
            if (c == ')' && --depth == 0) return i == expression.length() - 1;
        }
        return false;
    }

    private static ExpressionDefinition value(String id, Class<?> type, JsonNode defaultValue, String description) {
        return new ExpressionDefinition(
                NodeDescriptor.builder(id, NodeKind.VALUE)
                        .description(description)
                        .config(VALUE, type, defaultValue)
                        .output(VALUE, type)
                        .build(),
                (port, ctx) -> ctx.config(VALUE));
    }

    private static ExpressionDefinition arithmetic(String id, String operator, String description) {
        return binary(id, double.class, double.class, operator, description);
    }

    private static ExpressionDefinition binary(
            String id, Class<?> operands, Class<?> result, String operator, String description) {
        return new ExpressionDefinition(
                NodeDescriptor.builder(id, NodeKind.PURE)
                        .description(description)
                        .input(FIRST, operands)
                        .input(SECOND, operands)
                        .output(RESULT, result)
                        .build(),
                (port, ctx) -> CodeBlock.of("($L " + operator + " $L)", ctx.input(FIRST), ctx.input(SECOND)));
    }

    private static ExpressionDefinition unary(
            String id, Class<?> input, Class<?> result, String description, Function<CodeBlock, CodeBlock> expression) {
        return new ExpressionDefinition(
                NodeDescriptor.builder(id, NodeKind.PURE)
                        .description(description)
                        .input(VALUE, input)
                        .output(RESULT, result)
                        .build(),
                (port, ctx) -> expression.apply(ctx.input(VALUE)));
    }

    private static ExpressionDefinition clamp(String id, Class<?> result, String description, String cast) {
        return new ExpressionDefinition(
                NodeDescriptor.builder(id, NodeKind.PURE)
                        .description(description)
                        .input(VALUE, double.class)
                        .input(LOWER, double.class)
                        .input(UPPER, double.class)
                        .output(RESULT, result)
                        .build(),
                (port, ctx) -> CodeBlock.of(
                        cast,
                        CodeBlock.of(
                                "$T.max($L, $T.min($L, $L))",
                                Math.class,
                                ctx.input(LOWER),
                                Math.class,
                                ctx.input(UPPER),
                                ctx.input(VALUE))));
    }
}
