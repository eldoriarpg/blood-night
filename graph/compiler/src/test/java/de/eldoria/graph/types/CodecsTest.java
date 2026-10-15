/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.types;

import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeSpec;
import de.eldoria.graph.compiler.CompilationException;
import de.eldoria.graph.compiler.RuntimeCompiler;
import de.eldoria.graph.definition.PortType;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.BooleanNode;
import tools.jackson.databind.node.DoubleNode;
import tools.jackson.databind.node.IntNode;
import tools.jackson.databind.node.LongNode;
import tools.jackson.databind.node.StringNode;

import java.time.DayOfWeek;
import java.util.List;
import java.util.concurrent.Callable;

import javax.lang.model.element.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodecsTest {
    private static final TypeMapping TYPES = TypeMapping.defaults();

    private static Object evaluate(Class<?> type, JsonNode value) throws Exception {
        CodeBlock literal = TYPES.literal(PortType.of(type), value);
        TypeSpec spec = TypeSpec.classBuilder("Literal")
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(Callable.class)
                .addMethod(MethodSpec.methodBuilder("call")
                        .addModifiers(Modifier.PUBLIC)
                        .returns(Object.class)
                        .addStatement("return $L", literal)
                        .build())
                .build();
        try {
            return new RuntimeCompiler(List.of(), CodecsTest.class.getClassLoader())
                    .compile(JavaFile.builder("generated.literal", spec).build())
                    .instantiate("generated.literal.Literal", Callable.class)
                    .call();
        } catch (CompilationException e) {
            throw new AssertionError(e.getMessage(), e);
        }
    }

    @Test
    void stringsCannotEscapeTheLiteral() throws Exception {
        String hostile = "\"); System.exit(1); //\n\u0000\\";

        assertEquals(hostile, evaluate(String.class, StringNode.valueOf(hostile)));
    }

    @Test
    void numbersKeepTheirType() throws Exception {
        assertEquals(5.0, evaluate(double.class, IntNode.valueOf(5)));
        assertEquals(1e300, evaluate(double.class, DoubleNode.valueOf(1e300)));
        assertEquals(Long.MAX_VALUE, evaluate(long.class, LongNode.valueOf(Long.MAX_VALUE)));
        assertEquals(-7, evaluate(int.class, IntNode.valueOf(-7)));
        assertEquals(true, evaluate(Boolean.class, BooleanNode.TRUE));
    }

    @Test
    void enumsEmitConstants() throws Exception {
        assertEquals(DayOfWeek.FRIDAY, evaluate(DayOfWeek.class, StringNode.valueOf("FRIDAY")));
        assertEquals(
                List.of("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"),
                TYPES.enumValues(PortType.of(DayOfWeek.class)));
    }

    @Test
    void rejectsValuesOfTheWrongShape() {
        assertThrows(
                IllegalArgumentException.class, () -> TYPES.literal(PortType.of(int.class), DoubleNode.valueOf(1.5)));
        assertThrows(
                IllegalArgumentException.class,
                () -> TYPES.literal(PortType.of(int.class), LongNode.valueOf(Long.MAX_VALUE)));
        assertThrows(
                IllegalArgumentException.class,
                () -> TYPES.literal(PortType.of(double.class), StringNode.valueOf("1")));
        assertThrows(
                IllegalArgumentException.class, () -> TYPES.literal(PortType.of(String.class), IntNode.valueOf(1)));
        assertThrows(
                IllegalArgumentException.class,
                () -> TYPES.literal(PortType.of(DayOfWeek.class), StringNode.valueOf("FRIDAY; System.exit(1)")));
        assertThrows(
                IllegalArgumentException.class,
                () -> TYPES.literal(PortType.of(DayOfWeek.class), StringNode.valueOf("friday")));
    }

    @Test
    void typesWithoutCodecHaveNoLiteral() {
        assertTrue(TYPES.codec(PortType.of(Object.class)).isEmpty());
        assertTrue(TYPES.codec(PortType.parameterized(List.class, PortType.of(String.class)))
                .isEmpty());
        assertThrows(
                IllegalArgumentException.class,
                () -> TYPES.literal(PortType.of(Object.class), StringNode.valueOf("x")));
    }

    @Test
    void editorNames() {
        TypeMapping types =
                TypeMapping.builder().name(DayOfWeek.class, "Weekday").build();

        assertEquals("Number", types.name(PortType.of(double.class)));
        assertEquals("Weekday", types.name(PortType.of(DayOfWeek.class)));
        assertEquals("List<Weekday>", types.name(PortType.parameterized(List.class, PortType.of(DayOfWeek.class))));
    }
}
