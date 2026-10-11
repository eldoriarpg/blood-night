package de.eldoria.graph.types;

import com.palantir.javapoet.CodeBlock;
import tools.jackson.databind.JsonNode;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Literal codecs for primitives, strings and enums.
 */
public final class Codecs {
    public static final LiteralCodec<Boolean> BOOLEAN = simple(boolean.class, JsonNode::isBoolean, JsonNode::booleanValue, value -> CodeBlock.of("$L", value));
    public static final LiteralCodec<Integer> INT = simple(int.class, node -> node.isIntegralNumber() && node.canConvertToInt(), JsonNode::intValue, value -> CodeBlock.of("$L", value));
    public static final LiteralCodec<Long> LONG = simple(long.class, node -> node.isIntegralNumber() && node.canConvertToLong(), JsonNode::longValue, value -> CodeBlock.of("$LL", value));
    public static final LiteralCodec<Double> DOUBLE = simple(double.class, node -> node.isNumber() && Double.isFinite(node.doubleValue()), JsonNode::doubleValue, value -> CodeBlock.of("$Ld", value));
    public static final LiteralCodec<Float> FLOAT = simple(float.class, node -> node.isNumber() && Float.isFinite(node.floatValue()), JsonNode::floatValue, value -> CodeBlock.of("$Lf", value));
    public static final LiteralCodec<String> STRING = simple(String.class, JsonNode::isString, JsonNode::stringValue, value -> CodeBlock.of("$S", value));

    /**
     * Codecs for primitive and string types.
     */
    public static final List<LiteralCodec<?>> DEFAULTS = List.of(BOOLEAN, INT, LONG, DOUBLE, FLOAT, STRING);

    private Codecs() {
    }

    /**
     * Codec for an enum. Values are constant names; anything else is rejected, so the emitted {@code $T.$N} always
     * names an existing constant.
     *
     * @param type the enum class
     * @param <E>  the enum type
     * @return the codec
     */
    public static <E extends Enum<E>> LiteralCodec<E> forEnum(Class<E> type) {
        return new LiteralCodec<>() {
            @Override
            public Class<?> type() {
                return type;
            }

            @Override
            public E read(JsonNode node) {
                if (!node.isString()) throw invalid(type, node);
                return Arrays.stream(type.getEnumConstants())
                        .filter(constant -> constant.name().equals(node.stringValue()))
                        .findFirst()
                        .orElseThrow(() -> invalid(type, node));
            }

            @Override
            public CodeBlock emit(E value) {
                return CodeBlock.of("$T.$N", type, value.name());
            }
        };
    }

    private static <T> LiteralCodec<T> simple(Class<?> type, Predicate<JsonNode> valid, Function<JsonNode, T> reader, Function<T, CodeBlock> emitter) {
        return new LiteralCodec<>() {
            @Override
            public Class<?> type() {
                return type;
            }

            @Override
            public T read(JsonNode node) {
                if (node == null || !valid.test(node)) throw invalid(type, node);
                return reader.apply(node);
            }

            @Override
            public CodeBlock emit(T value) {
                return emitter.apply(value);
            }
        };
    }

    private static IllegalArgumentException invalid(Class<?> type, JsonNode node) {
        return new IllegalArgumentException("Not a valid %s: %s".formatted(type.getSimpleName(), node));
    }
}
