/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.std;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.TypeName;
import de.eldoria.graph.definition.ExpressionDefinition;
import de.eldoria.graph.definition.NodeDefinition;
import de.eldoria.graph.definition.StatementDefinition;
import de.eldoria.graph.definition.descriptor.NodeDescriptor;
import de.eldoria.graph.definition.descriptor.NodeKind;
import de.eldoria.graph.definition.emit.EmitContext;
import de.eldoria.graph.definition.emit.StatementContext;
import de.eldoria.graph.definition.port.PortType;
import de.eldoria.graph.std.runtime.flow.Cooldown;
import de.eldoria.graph.std.runtime.flow.Delays;
import de.eldoria.graph.std.runtime.flow.GateChange;
import de.eldoria.graph.std.runtime.flow.Switches;
import de.eldoria.graph.std.runtime.flow.Timer;
import de.eldoria.graph.std.runtime.value.Integers;
import de.eldoria.graph.std.runtime.value.Randoms;
import de.eldoria.graph.std.runtime.value.TimeUnit;
import de.eldoria.graph.types.TypeMapping;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.StringNode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Generic nodes every consumer can use: values, math, logic, functions, control flow and debugging.
 * <p>
 * None of them know anything about the consumer. Port ids are shared across nodes ({@code first}, {@code second},
 * {@code value}, {@code result}, {@code lower}, {@code upper}); control flow separates the repeated {@code body} from
 * the following {@code next}.
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
    private static final String NUMBER_TYPE = "N";
    private static final String PICKED = "T";
    private static final String TYPE = "type";
    private static final String CASE = "case_";
    private static final String NAME = "name";
    private static final String DEFAULT_VARIABLE = "variable";
    private static final String DEFAULT_ONCE = "once";
    private static final String DEFAULT_GATE = "gate";
    private static final String COUNT = "count";
    private static final String COUNTER = "counter";
    private static final String IS_A = "is_a";
    private static final String KEY = "K";
    private static final String KEY_TYPE = "key_type";
    private static final String KEY_INPUT = "key";
    private static final String OPTION = "option_";
    private static final String KEY_FIELD = "key_";
    private static final String DEFAULT = "default";
    private static final String INTEGER = "integer";
    private static final String UNIT = "unit";
    private static final String DURATION = "duration";
    private static final String ELAPSED = "elapsed";

    public static final ExpressionDefinition ADD = arithmetic(StdNodes.MATH_ADD, "+", "Adds two numbers");
    public static final ExpressionDefinition SUBTRACT =
            arithmetic(StdNodes.MATH_SUBTRACT, "-", "Subtracts the second number from the first");
    public static final ExpressionDefinition MULTIPLY =
            arithmetic(StdNodes.MATH_MULTIPLY, "*", "Multiplies two numbers");
    public static final ExpressionDefinition DIVIDE = new ExpressionDefinition(
            NodeDescriptor.builder(StdNodes.MATH_DIVIDE, NodeKind.PURE)
                    .description("Divides the first number by the second; whole numbers round toward zero and give 0"
                            + " when dividing by zero")
                    .input(FIRST, PortType.number(NUMBER_TYPE))
                    .input(SECOND, PortType.number(NUMBER_TYPE))
                    .output(RESULT, PortType.number(NUMBER_TYPE))
                    .describe("first", "The number divided")
                    .describe("second", "The number divided by")
                    .describe("result", "The quotient; whole numbers round toward zero")
                    .build(),
            (port, ctx) -> isWhole(ctx.type(RESULT))
                    ? ctx.staticCall(ClassName.get(Integers.class), "divide", ctx.input(FIRST), ctx.input(SECOND))
                    : CodeBlock.of("($L / $L)", ctx.input(FIRST), ctx.input(SECOND)));
    public static final ExpressionDefinition ABS = new ExpressionDefinition(
            NodeDescriptor.builder(StdNodes.MATH_ABS, NodeKind.PURE)
                    .description("Absolute value of a number")
                    .input(VALUE, PortType.number(NUMBER_TYPE))
                    .output(RESULT, PortType.number(NUMBER_TYPE))
                    .describe("value", "A number")
                    .describe("result", "The number without its sign")
                    .build(),
            (port, ctx) -> CodeBlock.of("$T.abs($L)", Math.class, ctx.input(VALUE)));
    public static final ExpressionDefinition MODULO_INTEGER = integerHelper(
            StdNodes.MATH_MODULO_INTEGER,
            "modulo",
            "The remainder of dividing the first integer by the second, with the second's sign; 0 when dividing by"
                    + " zero");
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
    public static final ExpressionDefinition CLAMP = new ExpressionDefinition(
            NodeDescriptor.builder(StdNodes.MATH_CLAMP, NodeKind.PURE)
                    .description("Keeps a number within a lower and upper bound")
                    .input(VALUE, PortType.number(NUMBER_TYPE))
                    .input(LOWER, PortType.number(NUMBER_TYPE))
                    .input(UPPER, PortType.number(NUMBER_TYPE))
                    .output(RESULT, PortType.number(NUMBER_TYPE))
                    .describe("value", "The number to keep in bounds")
                    .describe("lower", "The smallest allowed value")
                    .describe("upper", "The largest allowed value")
                    .describe("result", "The number, moved into the bounds when outside")
                    .build(),
            (port, ctx) -> CodeBlock.of(
                    "$T.max($L, $T.min($L, $L))",
                    Math.class,
                    ctx.input(LOWER),
                    Math.class,
                    ctx.input(UPPER),
                    ctx.input(VALUE)));

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
                    .describe("first", "Any value")
                    .describe("second", "Any value to compare with")
                    .describe("result", "Whether both are equal")
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
                    .description("True at most once per duration; the first evaluation is true")
                    .config(UNIT, "Unit", PortType.of(TimeUnit.class), StringNode.valueOf(TimeUnit.SECONDS.name()))
                    .input(DURATION, double.class)
                    .output(RESULT, boolean.class)
                    .describe("duration", "How long to wait after passing, in the picked unit")
                    .describe("result", "True once the time has passed since it was last true")
                    .build(),
            (port, ctx) -> CodeBlock.of(
                    "$L.ready($L.toMillis($L))",
                    ctx.field("cooldown", ClassName.get(Cooldown.class), CodeBlock.of("new $T()", Cooldown.class)),
                    ctx.config(UNIT),
                    ctx.input(DURATION)));
    public static final ExpressionDefinition RANDOM_INTEGER = new ExpressionDefinition(
            NodeDescriptor.builder(StdNodes.FUNCTION_RANDOM_INTEGER, NodeKind.PURE)
                    .description("A random integer between lower and upper, both inclusive")
                    .input(LOWER, int.class)
                    .input(UPPER, int.class)
                    .output(RESULT, int.class)
                    .describe("lower", "The smallest possible integer")
                    .describe("upper", "The largest possible integer")
                    .describe("result", "A random integer between both, inclusive")
                    .build(),
            (port, ctx) -> ctx.staticCall(RANDOMS, "nextInt", ctx.input(LOWER), ctx.input(UPPER)));
    public static final ExpressionDefinition RANDOM_NUMBER = new ExpressionDefinition(
            NodeDescriptor.builder(StdNodes.FUNCTION_RANDOM_NUMBER, NodeKind.PURE)
                    .description("A random number from lower, inclusive, to upper, exclusive")
                    .input(LOWER, double.class)
                    .input(UPPER, double.class)
                    .output(RESULT, double.class)
                    .describe("lower", "The smallest possible number")
                    .describe("upper", "The largest possible number")
                    .describe("result", "A random number between both")
                    .build(),
            (port, ctx) -> ctx.staticCall(RANDOMS, "nextDouble", ctx.input(LOWER), ctx.input(UPPER)));

    public static final StatementDefinition BRANCH = new StatementDefinition(
            NodeDescriptor.builder(StdNodes.FLOW_BRANCH, NodeKind.CONTROL_FLOW)
                    .description("Runs one of two chains depending on a condition, then continues")
                    .input("condition", boolean.class)
                    .execution("true")
                    .execution("false")
                    .execution(NEXT)
                    .describe("condition", "Decides which chain runs")
                    .build(),
            StandardNodes::branch);
    public static final StatementDefinition BATCH = new StatementDefinition(
            NodeDescriptor.builder(StdNodes.FLOW_BATCH, NodeKind.CONTROL_FLOW)
                    .description("Runs a chain several times, then continues")
                    .input("count", int.class)
                    .output("index", int.class)
                    .repeatingExecution(BODY)
                    .execution(NEXT)
                    .describe("count", "How often the body runs")
                    .describe("index", "The current run, counting from 0")
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
                    .describe("list", "The values to go through")
                    .describe("element", "The current value, inside the body")
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
    public static final ExpressionDefinition CHANCE = new ExpressionDefinition(
            NodeDescriptor.builder(StdNodes.FUNCTION_CHANCE, NodeKind.PURE)
                    .description("True with the given probability in percent")
                    .input("percent", double.class)
                    .output(RESULT, boolean.class)
                    .describe("percent", "How likely it is true, from 0 to 100")
                    .describe("result", "True with the given chance")
                    .build(),
            (port, ctx) -> ctx.staticCall(RANDOMS, "chance", ctx.input("percent")));

    public static final StatementDefinition PRINT = new StatementDefinition(
            NodeDescriptor.builder(StdNodes.DEBUG_PRINT, NodeKind.ACTION)
                    .description("Prints a value to the console")
                    .input(VALUE, Object.class)
                    .execution(NEXT)
                    .describe("value", "What is written to the console")
                    .build(),
            ctx -> CodeBlock.builder()
                    .addStatement("$T.out.println($L)", System.class, ctx.input(VALUE))
                    .add(ctx.chain(NEXT))
                    .build());

    public static final StatementDefinition TIMER_RESET = new StatementDefinition(
            NodeDescriptor.builder(StdNodes.TIMER_RESET, NodeKind.ACTION)
                    .name("Reset Timer")
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
                    .name("Timer Elapsed")
                    .stateful()
                    .description("Time since the behaviour started or a named timer was last reset")
                    .config(TIMER, String.class, StringNode.valueOf(DEFAULT_TIMER))
                    .config(UNIT, "Unit", PortType.of(TimeUnit.class), StringNode.valueOf(TimeUnit.SECONDS.name()))
                    .output(ELAPSED, double.class)
                    .describe("elapsed", "Time passed in the picked unit, with fractions")
                    .build(),
            (port, ctx) -> CodeBlock.of("$L.elapsed($L)", timer(ctx), ctx.config(UNIT)));

    public static final ExpressionDefinition REROUTE = new ExpressionDefinition(
            NodeDescriptor.builder(StdNodes.REROUTE, NodeKind.PURE)
                    .category("layout")
                    .name("Reroute")
                    .description("Passes a value on unchanged, so a wire can take another path")
                    .input(VALUE, PortType.variable(PICKED))
                    .output(VALUE, PortType.variable(PICKED))
                    .describe("value", "Any value")
                    .build(),
            (port, ctx) -> ctx.input(VALUE));

    public static final StatementDefinition DELAY = delay(
            StdNodes.FLOW_DELAY,
            "Delay",
            "Runs the rest of the chain once the duration has passed; starting it again while it waits waits twice");
    public static final StatementDefinition RETRIGGERABLE_DELAY = delay(
            StdNodes.FLOW_RETRIGGERABLE_DELAY,
            "Retriggerable Delay",
            "Runs the rest of the chain once the duration has passed; starting it again while it waits starts the wait"
                    + " anew");

    public static final StatementDefinition DO_ONCE = new StatementDefinition(
            NodeDescriptor.builder(StdNodes.FLOW_DO_ONCE, NodeKind.ACTION)
                    .category("flow")
                    .name("Do Once")
                    .description("Runs the rest of the chain the first time only; named, Reset Do Once of the same name"
                            + " lets it run again")
                    .config(NAME, String.class, StringNode.valueOf(""))
                    .execution(NEXT)
                    .build(),
            ctx -> {
                CodeBlock runs = once(ctx);
                return CodeBlock.builder()
                        .beginControlFlow("if ($L < 1)", runs)
                        .addStatement("$L++", runs)
                        .add(ctx.chain(NEXT))
                        .endControlFlow()
                        .build();
            });

    public static final StatementDefinition DO_N = new StatementDefinition(
            NodeDescriptor.builder(StdNodes.FLOW_DO_N, NodeKind.ACTION)
                    .category("flow")
                    .name("Do N")
                    .description(
                            "Runs the rest of the chain the first n times; named, Reset Do Once of the same name lets"
                                    + " it run again")
                    .config(NAME, String.class, StringNode.valueOf(""))
                    .input(COUNT, int.class)
                    .output(COUNTER, int.class)
                    .execution(NEXT)
                    .describe("count", "How often the chain runs")
                    .describe("counter", "How often it has run, this time included")
                    .build(),
            ctx -> {
                CodeBlock runs = once(ctx);
                CodeBlock.Builder code = CodeBlock.builder()
                        .beginControlFlow("if ($L < $L)", runs, ctx.input(COUNT))
                        .addStatement("$L++", runs);
                if (ctx.isRead(COUNTER)) code.addStatement("final int $L = $L", ctx.bind(COUNTER, NEXT), runs);
                return code.add(ctx.chain(NEXT)).endControlFlow().build();
            });

    public static final StatementDefinition RESET_ONCE = new StatementDefinition(
            NodeDescriptor.builder(StdNodes.FLOW_RESET_ONCE, NodeKind.ACTION)
                    .category("flow")
                    .name("Reset Do Once")
                    .description("Lets the Do Once and Do N nodes of the same name run again")
                    .config(NAME, String.class, StringNode.valueOf(DEFAULT_ONCE))
                    .execution(NEXT)
                    .build(),
            ctx -> CodeBlock.builder()
                    .addStatement("$L = 0", once(ctx))
                    .add(ctx.chain(NEXT))
                    .build());

    public static final StatementDefinition FLIP_FLOP = new StatementDefinition(
            NodeDescriptor.builder(StdNodes.FLOW_FLIP_FLOP, NodeKind.ACTION)
                    .category("flow")
                    .name("Flip Flop")
                    .description("Runs A, then B the next time, then A again, and so on")
                    .output(IS_A, boolean.class)
                    .execution("a", "A")
                    .execution("b", "B")
                    .describe("is_a", "Whether this time runs A")
                    .build(),
            ctx -> {
                CodeBlock flipped = ctx.field("flip flop", TypeName.BOOLEAN, CodeBlock.of("false"));
                CodeBlock.Builder code = CodeBlock.builder().addStatement("$L = !$L", flipped, flipped);
                if (ctx.isRead(IS_A)) {
                    code.addStatement("final boolean $L = $L", ctx.bind(IS_A, "a", "b"), flipped);
                }
                return code.beginControlFlow("if ($L)", flipped)
                        .add(ctx.chain("a"))
                        .nextControlFlow("else")
                        .add(ctx.chain("b"))
                        .endControlFlow()
                        .build();
            });

    public static final StatementDefinition GATE = new StatementDefinition(
            NodeDescriptor.builder(StdNodes.FLOW_GATE, NodeKind.ACTION)
                    .category("flow")
                    .name("Gate")
                    .description("Runs the rest of the chain only while the gate of its name is open; gates start open")
                    .config(NAME, String.class, StringNode.valueOf(DEFAULT_GATE))
                    .execution(NEXT)
                    .build(),
            ctx -> CodeBlock.builder()
                    .beginControlFlow("if (!$L)", gate(ctx))
                    .add(ctx.chain(NEXT))
                    .endControlFlow()
                    .build());

    public static final StatementDefinition SET_GATE = new StatementDefinition(
            NodeDescriptor.builder(StdNodes.FLOW_SET_GATE, NodeKind.ACTION)
                    .category("flow")
                    .name("Set Gate")
                    .description("Opens, closes or toggles the gate of its name")
                    .config(NAME, String.class, StringNode.valueOf(DEFAULT_GATE))
                    .config(
                            "change",
                            "Change",
                            PortType.of(GateChange.class),
                            StringNode.valueOf(GateChange.CLOSE.name()))
                    .execution(NEXT)
                    .build(),
            ctx -> {
                CodeBlock closed = gate(ctx);
                CodeBlock change =
                        switch (GateChange.valueOf(ctx.configValue("change").asString())) {
                            case OPEN -> CodeBlock.of("$L = false", closed);
                            case CLOSE -> CodeBlock.of("$L = true", closed);
                            case TOGGLE -> CodeBlock.of("$L = !$L", closed, closed);
                        };
                return CodeBlock.builder()
                        .addStatement(change)
                        .add(ctx.chain(NEXT))
                        .build();
            });

    public static final StatementDefinition REROUTE_EXECUTION = new StatementDefinition(
            NodeDescriptor.builder(StdNodes.REROUTE_EXECUTION, NodeKind.ACTION)
                    .category("layout")
                    .name("Reroute")
                    .description("Continues the chain unchanged, so a wire can take another path")
                    .execution(NEXT)
                    .build(),
            ctx -> ctx.chain(NEXT));

    private StandardNodes() {}

    /**
     * A node that holds the rest of its chain back for a while, through the graph's {@link Delays}, which the
     * platform's clock hands back. Nodes after an enclosing node, such as the {@code next} of a branch around it, do
     * not wait. A retriggerable delay drops what it still holds back when it starts again. A delay inside a state
     * drops what it holds back when the graph leaves the state.
     */
    private static StatementDefinition delay(String id, String name, String description) {
        return new StatementDefinition(
                NodeDescriptor.builder(id, NodeKind.ACTION)
                        .category("flow")
                        .name(name)
                        .description(description)
                        .config(UNIT, "Unit", PortType.of(TimeUnit.class), StringNode.valueOf(TimeUnit.SECONDS.name()))
                        .input(DURATION, double.class)
                        .execution(NEXT)
                        .describe("duration", "How long to wait, in the picked unit")
                        .build(),
                ctx -> {
                    CodeBlock chain = ctx.chain(NEXT);
                    if (chain.isEmpty()) return CodeBlock.builder().build();
                    CodeBlock delays = ctx.stateField(
                            "delays", ClassName.get(Delays.class), CodeBlock.of("new $T()", Delays.class));
                    ctx.everyTick(CodeBlock.of("$L.tick()", delays));
                    ctx.onLeave(CodeBlock.of("$L.clear()", delays));
                    CodeBlock millis = CodeBlock.of("$L.toMillis($L)", ctx.config(UNIT), ctx.input(DURATION));
                    CodeBlock start = id.equals(StdNodes.FLOW_DELAY)
                            ? CodeBlock.of("$L.after($L, () -> {\n$>", delays, millis)
                            : CodeBlock.of("$L.restart($S, $L, () -> {\n$>", delays, ctx.nodeId(), millis);
                    return CodeBlock.builder()
                            .add(start)
                            .add(chain)
                            .add("$<});\n")
                            .build();
                });
    }

    /**
     * Reads a variable: a field per behaviour instance, shared by every variable node of the same name, which holds
     * the type's empty value until it is set (0, false, empty text, else missing). Any selectable type can be held.
     */
    private static ExpressionDefinition variableGet(TypeMapping types) {
        return new ExpressionDefinition(
                NodeDescriptor.builder(StdNodes.VARIABLE_GET, NodeKind.PURE)
                        .category("variable")
                        .name("Get Variable")
                        .description("The value a variable holds; every variable node with the same name shares it")
                        .config(NAME, String.class, StringNode.valueOf(DEFAULT_VARIABLE))
                        .typeConfig(TYPE, PICKED, List.copyOf(types.selectable().keySet()))
                        .output(VALUE, PortType.variable(PICKED))
                        .describe("value", "What the variable holds now")
                        .build(),
                (port, ctx) -> variable(ctx, ctx.type(VALUE)));
    }

    /**
     * Sets a variable; see {@link #variableGet}. Its output gives the value set, to chain on without reading again.
     */
    private static StatementDefinition variableSet(TypeMapping types) {
        return new StatementDefinition(
                NodeDescriptor.builder(StdNodes.VARIABLE_SET, NodeKind.ACTION)
                        .category("variable")
                        .name("Set Variable")
                        .description("Stores a value in a variable; every variable node with the same name shares it")
                        .config(NAME, String.class, StringNode.valueOf(DEFAULT_VARIABLE))
                        .typeConfig(TYPE, PICKED, List.copyOf(types.selectable().keySet()))
                        .input(VALUE, PortType.variable(PICKED))
                        .output(VALUE, PortType.variable(PICKED))
                        .execution(NEXT)
                        .describe("value", "What the variable holds from now on")
                        .build(),
                ctx -> {
                    CodeBlock field = variable(ctx, ctx.inputType(VALUE));
                    CodeBlock.Builder code = CodeBlock.builder().addStatement("$L = $L", field, ctx.input(VALUE));
                    if (ctx.isRead(VALUE)) {
                        code.addStatement("final $T $L = $L", ctx.type(VALUE), ctx.bind(VALUE, NEXT), field);
                    }
                    return code.add(ctx.chain(NEXT)).build();
                });
    }

    /**
     * Gives one of several values by a key, evaluating only the one it gives. The key's type is picked like a switch's;
     * each input {@code option_<n>} has a config field {@code key_<n>} beside it holding the key it stands for, and
     * {@code default} is given for any other key. A boolean key needs no keys: {@code option_0} is given for true,
     * {@code option_1} for false. An option without a key or a value is left out; without a default the value
     * type's empty value is given.
     */
    private static ExpressionDefinition select(TypeMapping types) {
        List<String> keys = types.selectable().entrySet().stream()
                .filter(entry -> types.codec(entry.getValue()).isPresent())
                .map(Map.Entry::getKey)
                .toList();
        NodeDescriptor.Builder descriptor = NodeDescriptor.builder(StdNodes.SELECT, NodeKind.PURE)
                .category("logic")
                .name("Select")
                .description("One of several values, picked by a key; only the picked one is computed")
                .typeConfig(KEY_TYPE, KEY, keys)
                .typeConfig(TYPE, PICKED, List.copyOf(types.selectable().keySet()))
                .input(KEY_INPUT, PortType.variable(KEY))
                .describe(KEY_INPUT, "Picks the option with the same key");
        for (int i = 0; i < SWITCH_CASES; i++) {
            descriptor.configBeside(
                    KEY_FIELD + i, "Key " + (i + 1), PortType.variable(KEY), NullNode.getInstance(), OPTION + i);
            descriptor.input(OPTION + i, "Option " + (i + 1), PortType.variable(PICKED), true);
        }
        descriptor
                .input(DEFAULT, "Default", PortType.variable(PICKED), true)
                .output(VALUE, PortType.variable(PICKED))
                .describe(DEFAULT, "Given when no option has the key")
                .describe(VALUE, "The picked option");
        return new ExpressionDefinition(descriptor.build(), (port, ctx) -> {
            CodeBlock otherwise = ctx.connected(DEFAULT) ? ctx.input(DEFAULT) : emptyValue(ctx.type(VALUE));
            if (ctx.inputType(KEY_INPUT).withoutAnnotations().equals(TypeName.BOOLEAN)
                    || ctx.inputType(KEY_INPUT).equals(TypeName.BOOLEAN.box())) {
                CodeBlock yes = ctx.connected(OPTION + 0) ? ctx.input(OPTION + 0) : otherwise;
                CodeBlock no = ctx.connected(OPTION + 1) ? ctx.input(OPTION + 1) : otherwise;
                return CodeBlock.of("($L ? $L : $L)", ctx.input(KEY_INPUT), yes, no);
            }
            List<CodeBlock> keyValues = new ArrayList<>();
            CodeBlock.Builder arms = CodeBlock.builder();
            for (int i = 0; i < SWITCH_CASES; i++) {
                if (!ctx.connected(OPTION + i) || ctx.configValue(KEY_FIELD + i).isNull()) continue;
                arms.add("case $L -> $L;\n", keyValues.size(), ctx.input(OPTION + i));
                keyValues.add(ctx.config(KEY_FIELD + i));
            }
            if (keyValues.isEmpty()) return otherwise;
            CodeBlock match = ctx.staticCall(
                    ClassName.get(Switches.class),
                    "match",
                    Stream.concat(Stream.of(ctx.input(KEY_INPUT)), keyValues.stream())
                            .toArray(CodeBlock[]::new));
            return CodeBlock.builder()
                    .add("(switch ($L) {\n$>", match)
                    .add(arms.build())
                    .add("default -> $L;\n$<})", otherwise)
                    .build();
        });
    }

    /**
     * How often a Do Once or Do N node has run since the last reset: its own count without a name, else the count all
     * of them with that name share, which Reset Do Once of the name clears.
     */
    private static CodeBlock once(EmitContext ctx) {
        String name = ctx.configValue(NAME).asString("");
        return name.isBlank()
                ? ctx.field("runs", TypeName.INT, CodeBlock.of("0"))
                : ctx.sharedField("once " + name, TypeName.INT, CodeBlock.of("0"));
    }

    /**
     * Whether the gate of a name is closed; gates start open.
     */
    private static CodeBlock gate(EmitContext ctx) {
        return ctx.sharedField(
                "gate " + ctx.configValue(NAME).asString(DEFAULT_GATE) + " closed",
                TypeName.BOOLEAN,
                CodeBlock.of("false"));
    }

    private static CodeBlock variable(EmitContext ctx, TypeName type) {
        String name = ctx.configValue(NAME).asString(DEFAULT_VARIABLE);
        return ctx.sharedField("variable " + name, type, emptyValue(type));
    }

    private static CodeBlock emptyValue(TypeName type) {
        if (type.equals(TypeName.BOOLEAN)) return CodeBlock.of("false");
        if (type.isPrimitive()) return CodeBlock.of("0");
        if (type.equals(ClassName.get(String.class))) return CodeBlock.of("\"\"");
        return CodeBlock.of("null");
    }

    private static CodeBlock timer(EmitContext ctx) {
        String name = ctx.config(TIMER).toString().replace("\"", "");
        return ctx.sharedField(
                name.equals(DEFAULT_TIMER) ? name : name + " timer",
                ClassName.get(Timer.class),
                CodeBlock.of("new $T()", Timer.class));
    }

    /**
     * The nodes whose type is picked in the editor, from the platform's selectable types:
     * <ul>
     *     <li>{@code cast}, "As": output {@code result} is the value as the picked type, or missing when it is not
     *     one; {@code matches} tells which, so a branch can guard the use. Only reference types without type
     *     arguments can be picked, since no value is an instance of anything else.</li>
     *     <li>{@code value}, "Value": a fixed value of the picked type, typed in; only types with a literal form can
     *     be picked.</li>
     *     <li>{@code flow.switch}, "Switch": runs the case whose typed-in value equals the value; see
     *     {@link #switchNode}.</li>
     *     <li>{@code select}, "Select": one of several values, by a key; see {@link #select}.</li>
     *     <li>{@code variable.get} and {@code variable.set}, "Get Variable" and "Set Variable": a value of the picked
     *     type kept per behaviour instance under a name; see {@link #variableGet}.</li>
     * </ul>
     *
     * @param types the platform's type mapping
     * @return the cast, value, switch and variable nodes
     */
    public static List<NodeDefinition> typed(TypeMapping types) {
        return List.of(
                cast(types), value(types), switchNode(types), select(types), variableGet(types), variableSet(types));
    }

    private static ExpressionDefinition cast(TypeMapping types) {
        List<String> choices = types.selectable().entrySet().stream()
                .filter(entry -> entry.getValue() instanceof PortType.Concrete concrete
                        && !concrete.raw().isPrimitive()
                        && concrete.arguments().isEmpty())
                .map(Map.Entry::getKey)
                .toList();
        return new ExpressionDefinition(
                NodeDescriptor.builder(StdNodes.CAST, NodeKind.PURE)
                        .category("logic")
                        .name("As")
                        .description("The value as the picked type, missing when it is none; matches tells which")
                        .typeConfig(TYPE, PICKED, choices)
                        .input(VALUE, Object.class)
                        .output(RESULT, PortType.variable(PICKED))
                        .output("matches", boolean.class)
                        .describe("value", "Any value")
                        .describe("result", "The value as the picked type; missing when it is none")
                        .describe("matches", "Whether the value is of the picked type")
                        .build(),
                (port, ctx) -> {
                    CodeBlock value = ctx.input(VALUE);
                    TypeName raw = ctx.type(RESULT);
                    if (ctx.inputType(VALUE).isPrimitive()) {
                        return port.equals("matches")
                                ? CodeBlock.of("$T.class.isInstance($L)", raw, value)
                                : CodeBlock.of(
                                        "($T.class.isInstance($L) ? $T.class.cast($L) : null)", raw, value, raw, value);
                    }
                    return port.equals("matches")
                            ? CodeBlock.of("($L instanceof $T)", value, raw)
                            : CodeBlock.of("($L instanceof $T ? ($T) $L : null)", value, raw, raw, value);
                });
    }

    private static ExpressionDefinition value(TypeMapping types) {
        List<String> choices = types.selectable().entrySet().stream()
                .filter(entry -> types.codec(entry.getValue()).isPresent())
                .map(Map.Entry::getKey)
                .toList();
        return new ExpressionDefinition(
                NodeDescriptor.builder(StdNodes.VALUE, NodeKind.VALUE)
                        .category("value")
                        .name("Value")
                        .description("A fixed value of the picked type")
                        .typeConfig(TYPE, PICKED, choices)
                        .config(VALUE, "Value", PortType.variable(PICKED), null)
                        .output(VALUE, PortType.variable(PICKED))
                        .describe("value", "The value typed in")
                        .build(),
                (port, ctx) -> ctx.config(VALUE));
    }

    /**
     * @return every standard node
     */
    public static List<NodeDefinition> all() {
        return List.of(
                ADD,
                SUBTRACT,
                MULTIPLY,
                DIVIDE,
                ABS,
                MODULO_INTEGER,
                CEIL,
                FLOOR,
                ROUND,
                CLAMP,
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
                BATCH,
                FOR_EACH,
                PRINT,
                REROUTE,
                REROUTE_EXECUTION,
                DELAY,
                RETRIGGERABLE_DELAY,
                DO_ONCE,
                DO_N,
                RESET_ONCE,
                FLIP_FLOP,
                GATE,
                SET_GATE);
    }

    /**
     * Runs the chain of the case whose value equals the switched value, {@code default} for any other value, then
     * {@code next}. The type of the value is picked, so a switch works on numbers, texts and every enum or registry
     * type with a literal form, and each case port {@code case_<n>} has a config field of the same id beside it,
     * holding its value. Case ports are fixed per node type, so there are {@value #SWITCH_CASES}; a case without a value or
     * without a chain is left out. The value is matched once ({@link Switches#match}) and the matching position
     * switched on, since registry entries are no constants a Java switch could name.
     */
    private static StatementDefinition switchNode(TypeMapping types) {
        List<String> choices = types.selectable().entrySet().stream()
                .filter(entry -> types.codec(entry.getValue()).isPresent())
                .filter(entry -> !entry.getValue().accepts(PortType.of(boolean.class)))
                .map(Map.Entry::getKey)
                .sorted(Comparator.comparing((String choice) -> !choice.equals(INTEGER)))
                .toList();
        NodeDescriptor.Builder descriptor = NodeDescriptor.builder(StdNodes.FLOW_SWITCH, NodeKind.CONTROL_FLOW)
                .description("Runs the chain of the case matching the value, default otherwise, then continues")
                .typeConfig(TYPE, PICKED, choices)
                .input(VALUE, PortType.variable(PICKED))
                .describe(VALUE, "Picks the case with the same value");
        for (int i = 0; i < SWITCH_CASES; i++) {
            descriptor.configBeside(
                    CASE + i, "Case " + (i + 1), PortType.variable(PICKED), NullNode.getInstance(), CASE + i);
            descriptor.execution(CASE + i, "Case " + (i + 1));
        }
        descriptor.execution("default").execution(NEXT);
        return new StatementDefinition(descriptor.build(), ctx -> {
            List<CodeBlock> values = new ArrayList<>();
            CodeBlock.Builder cases = CodeBlock.builder();
            for (int i = 0; i < SWITCH_CASES; i++) {
                CodeBlock chain = ctx.chain(CASE + i);
                if (chain.isEmpty() || ctx.configValue(CASE + i).isNull()) continue;
                cases.add("case $L -> {\n$>", values.size()).add(chain).add("$<}\n");
                values.add(ctx.config(CASE + i));
            }
            CodeBlock otherwise = ctx.chain("default");
            if (!otherwise.isEmpty())
                cases.add("default -> {\n$>").add(otherwise).add("$<}\n");
            CodeBlock.Builder code = CodeBlock.builder();
            if (!cases.isEmpty()) {
                CodeBlock match = values.isEmpty()
                        ? CodeBlock.of("-1")
                        : ctx.staticCall(
                                ClassName.get(Switches.class),
                                "match",
                                Stream.concat(Stream.of(ctx.input(VALUE)), values.stream())
                                        .toArray(CodeBlock[]::new));
                code.beginControlFlow("switch ($L)", match).add(cases.build()).endControlFlow();
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

    private static ExpressionDefinition integerHelper(String id, String method, String description) {
        return new ExpressionDefinition(
                NodeDescriptor.builder(id, NodeKind.PURE)
                        .description(description)
                        .input(FIRST, int.class)
                        .input(SECOND, int.class)
                        .output(RESULT, int.class)
                        .describe(FIRST, "The integer divided")
                        .describe(SECOND, "The integer divided by")
                        .describe(RESULT, "The remainder")
                        .build(),
                (port, ctx) ->
                        ctx.staticCall(ClassName.get(Integers.class), method, ctx.input(FIRST), ctx.input(SECOND)));
    }

    private static ExpressionDefinition arithmetic(String id, String operator, String description) {
        return new ExpressionDefinition(
                NodeDescriptor.builder(id, NodeKind.PURE)
                        .description(description + "; whole numbers give a whole number, any decimal a decimal")
                        .input(FIRST, PortType.number(NUMBER_TYPE))
                        .input(SECOND, PortType.number(NUMBER_TYPE))
                        .output(RESULT, PortType.number(NUMBER_TYPE))
                        .describe(FIRST, "The first number")
                        .describe(SECOND, "The second number")
                        .describe(RESULT, "The outcome, a whole number when both numbers are whole")
                        .build(),
                (port, ctx) -> CodeBlock.of("($L " + operator + " $L)", ctx.input(FIRST), ctx.input(SECOND)));
    }

    private static boolean isWhole(TypeName type) {
        return type.equals(TypeName.INT)
                || type.equals(TypeName.LONG)
                || type.equals(TypeName.SHORT)
                || type.equals(TypeName.BYTE);
    }

    private static ExpressionDefinition binary(
            String id, Class<?> operands, Class<?> result, String operator, String description) {
        return new ExpressionDefinition(
                NodeDescriptor.builder(id, NodeKind.PURE)
                        .description(description)
                        .input(FIRST, operands)
                        .input(SECOND, operands)
                        .output(RESULT, result)
                        .describe(FIRST, operands == boolean.class ? "A condition" : "The first number")
                        .describe(SECOND, operands == boolean.class ? "Another condition" : "The second number")
                        .describe(RESULT, description)
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
                        .describe(
                                VALUE,
                                input == boolean.class
                                        ? "A condition"
                                        : input == Object.class ? "Any value" : "A number")
                        .describe(RESULT, description)
                        .build(),
                (port, ctx) -> expression.apply(ctx.input(VALUE)));
    }
}
