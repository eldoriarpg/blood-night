/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.std;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import de.eldoria.graph.definition.ExpressionDefinition;
import de.eldoria.graph.definition.NodeDefinition;
import de.eldoria.graph.definition.NodeDescriptor;
import de.eldoria.graph.definition.NodeKind;
import de.eldoria.graph.definition.PortType;
import de.eldoria.graph.definition.StatementDefinition;
import de.eldoria.graph.std.runtime.Cooldown;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.BooleanNode;
import tools.jackson.databind.node.DoubleNode;
import tools.jackson.databind.node.IntNode;
import tools.jackson.databind.node.StringNode;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;

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

    public static final ExpressionDefinition BOOLEAN =
            value("value.boolean", boolean.class, BooleanNode.FALSE, "A fixed true or false");
    public static final ExpressionDefinition INTEGER =
            value("value.integer", int.class, IntNode.valueOf(0), "A fixed integer number");
    public static final ExpressionDefinition NUMBER =
            value("value.number", double.class, DoubleNode.valueOf(0), "A fixed decimal number");
    public static final ExpressionDefinition TEXT =
            value("value.text", String.class, StringNode.valueOf(""), "A fixed text");

    public static final ExpressionDefinition ADD = arithmetic("math.add", "+", "Adds two numbers");
    public static final ExpressionDefinition SUBTRACT =
            arithmetic("math.subtract", "-", "Subtracts the second number from the first");
    public static final ExpressionDefinition MULTIPLY = arithmetic("math.multiply", "*", "Multiplies two numbers");
    public static final ExpressionDefinition DIVIDE =
            arithmetic("math.divide", "/", "Divides the first number by the second");
    public static final ExpressionDefinition ABS = unary(
            "math.abs",
            double.class,
            double.class,
            "Absolute value of a number",
            value -> CodeBlock.of("$T.abs($L)", Math.class, value));
    public static final ExpressionDefinition ABS_INTEGER = unary(
            "math.abs_integer",
            double.class,
            int.class,
            "Absolute value of a number as integer, decimals cut off",
            value -> CodeBlock.of("$T.abs((int) $L)", Math.class, value));
    public static final ExpressionDefinition CEIL = unary(
            "math.ceil",
            double.class,
            int.class,
            "Rounds up to the next integer",
            value -> CodeBlock.of("(int) $T.ceil($L)", Math.class, value));
    public static final ExpressionDefinition FLOOR = unary(
            "math.floor",
            double.class,
            int.class,
            "Rounds down to the next integer",
            value -> CodeBlock.of("(int) $T.floor($L)", Math.class, value));
    public static final ExpressionDefinition ROUND = unary(
            "math.round",
            double.class,
            int.class,
            "Rounds to the closest integer",
            value -> CodeBlock.of("(int) $T.round($L)", Math.class, value));
    public static final ExpressionDefinition CLAMP =
            clamp("math.clamp", double.class, "Keeps a number within a lower and upper bound", "$L");
    public static final ExpressionDefinition CLAMP_INTEGER = clamp(
            "math.clamp_integer",
            int.class,
            "Keeps a number within bounds, as integer with decimals cut off",
            "(int) $L");

    public static final ExpressionDefinition AND =
            binary("logic.and", boolean.class, boolean.class, "&&", "True when both are true");
    public static final ExpressionDefinition OR =
            binary("logic.or", boolean.class, boolean.class, "||", "True when at least one is true");
    public static final ExpressionDefinition NOT =
            unary("logic.not", boolean.class, boolean.class, "Inverts a boolean", value -> CodeBlock.of("!$L", value));
    public static final ExpressionDefinition GREATER =
            binary("logic.greater", double.class, boolean.class, ">", "True when the first number is greater");
    public static final ExpressionDefinition GREATER_OR_EQUAL = binary(
            "logic.greater_or_equal",
            double.class,
            boolean.class,
            ">=",
            "True when the first number is greater or equal");
    public static final ExpressionDefinition LESS =
            binary("logic.less", double.class, boolean.class, "<", "True when the first number is less");
    public static final ExpressionDefinition LESS_OR_EQUAL = binary(
            "logic.less_or_equal", double.class, boolean.class, "<=", "True when the first number is less or equal");
    public static final ExpressionDefinition EQUAL = new ExpressionDefinition(
            NodeDescriptor.builder("logic.equal", NodeKind.PURE)
                    .description("True when both values are equal; two missing values are equal")
                    .input(FIRST, Object.class)
                    .input(SECOND, Object.class)
                    .output(RESULT, boolean.class)
                    .build(),
            (port, ctx) -> CodeBlock.of("$T.equals($L, $L)", Objects.class, ctx.input(FIRST), ctx.input(SECOND)));
    public static final ExpressionDefinition IS_NULL = unary(
            "logic.is_null",
            Object.class,
            boolean.class,
            "True when the value is missing",
            value -> CodeBlock.of("($L == null)", value));

    public static final ExpressionDefinition COOLDOWN = new ExpressionDefinition(
            NodeDescriptor.builder("function.cooldown", NodeKind.PURE)
                    .description("True at most once every n milliseconds; the first evaluation is true")
                    .input(VALUE, "Milliseconds", PortType.of(int.class), false)
                    .output(RESULT, boolean.class)
                    .build(),
            (port, ctx) -> CodeBlock.of(
                    "$L.ready($L)",
                    ctx.field("cooldown", ClassName.get(Cooldown.class), CodeBlock.of("new $T()", Cooldown.class)),
                    ctx.input(VALUE)));
    public static final ExpressionDefinition RANDOM_INTEGER = new ExpressionDefinition(
            NodeDescriptor.builder("function.random_integer", NodeKind.PURE)
                    .description("A random integer between lower and upper, both inclusive")
                    .input(LOWER, int.class)
                    .input(UPPER, int.class)
                    .output(RESULT, int.class)
                    .build(),
            (port, ctx) -> CodeBlock.of(
                    "$T.current().nextInt($L, $L + 1)", ThreadLocalRandom.class, ctx.input(LOWER), ctx.input(UPPER)));
    public static final ExpressionDefinition RANDOM_NUMBER = new ExpressionDefinition(
            NodeDescriptor.builder("function.random_number", NodeKind.PURE)
                    .description("A random number from lower, inclusive, to upper, exclusive")
                    .input(LOWER, double.class)
                    .input(UPPER, double.class)
                    .output(RESULT, double.class)
                    .build(),
            (port, ctx) -> CodeBlock.of(
                    "$T.current().nextDouble($L, $L)", ThreadLocalRandom.class, ctx.input(LOWER), ctx.input(UPPER)));

    public static final StatementDefinition BRANCH = new StatementDefinition(
            NodeDescriptor.builder("flow.branch", NodeKind.CONTROL_FLOW)
                    .description("Runs one of two chains depending on a condition, then continues")
                    .input("condition", boolean.class)
                    .execution("true")
                    .execution("false")
                    .execution(NEXT)
                    .build(),
            ctx -> CodeBlock.builder()
                    .beginControlFlow("if ($L)", ctx.input("condition"))
                    .add(ctx.chain("true"))
                    .nextControlFlow("else")
                    .add(ctx.chain("false"))
                    .endControlFlow()
                    .add(ctx.chain(NEXT))
                    .build());
    public static final StatementDefinition BATCH = new StatementDefinition(
            NodeDescriptor.builder("flow.batch", NodeKind.CONTROL_FLOW)
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
            NodeDescriptor.builder("flow.for_each", NodeKind.CONTROL_FLOW)
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

    public static final StatementDefinition PRINT = new StatementDefinition(
            NodeDescriptor.builder("debug.print", NodeKind.ACTION)
                    .description("Prints a value to the console")
                    .input(VALUE, Object.class)
                    .execution(NEXT)
                    .build(),
            ctx -> CodeBlock.builder()
                    .addStatement("$T.out.println($L)", System.class, ctx.input(VALUE))
                    .add(ctx.chain(NEXT))
                    .build());

    private StandardNodes() {}

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
                RANDOM_INTEGER,
                RANDOM_NUMBER,
                BRANCH,
                BATCH,
                FOR_EACH,
                PRINT);
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
