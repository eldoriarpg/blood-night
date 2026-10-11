package de.eldoria.graph.definition;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PortTypeTest {
    private static final PortType DOUBLE = PortType.of(double.class);
    private static final PortType INT = PortType.of(int.class);
    private static final PortType BOXED_DOUBLE = PortType.of(Double.class);
    private static final PortType BOXED_INT = PortType.of(Integer.class);
    private static final PortType OBJECT = PortType.of(Object.class);
    private static final PortType NUMBER = PortType.of(Number.class);
    private static final PortType STRING = PortType.of(String.class);

    @SuppressWarnings("unused")
    private static List<? extends CharSequence> wildcard() {
        return null;
    }

    @SuppressWarnings("unused")
    private static <T> Map<String, T> variable() {
        return null;
    }

    @Test
    void subtypes() {
        assertTrue(OBJECT.accepts(STRING));
        assertTrue(PortType.of(CharSequence.class).accepts(STRING));
        assertFalse(STRING.accepts(OBJECT));
    }

    @Test
    void primitiveWidening() {
        assertTrue(DOUBLE.accepts(INT));
        assertTrue(PortType.of(long.class).accepts(INT));
        assertTrue(INT.accepts(PortType.of(char.class)));
        assertFalse(INT.accepts(DOUBLE));
        assertFalse(PortType.of(boolean.class).accepts(INT));
    }

    @Test
    void boxingAndUnboxing() {
        assertTrue(BOXED_DOUBLE.accepts(DOUBLE));
        assertTrue(DOUBLE.accepts(BOXED_DOUBLE));
        assertTrue(DOUBLE.accepts(BOXED_INT));
        assertTrue(OBJECT.accepts(INT));
        assertTrue(NUMBER.accepts(INT));
        assertFalse(BOXED_DOUBLE.accepts(INT));
    }

    @Test
    void typeArgumentsMustMatch() {
        PortType strings = PortType.parameterized(List.class, STRING);
        PortType objects = PortType.parameterized(List.class, OBJECT);

        assertTrue(PortType.parameterized(Collection.class, STRING).accepts(strings));
        assertFalse(objects.accepts(strings));
        assertTrue(PortType.of(List.class).accepts(strings));
        assertTrue(PortType.parameterized(List.class, PortType.variable("T")).accepts(strings));
        assertTrue(strings.accepts(PortType.of(ArrayList.class)));
    }

    @Test
    void variablesAcceptAnything() {
        assertTrue(PortType.variable("T").accepts(STRING));
        assertTrue(STRING.accepts(PortType.variable("T")));
    }

    @Test
    void fromReflection() throws NoSuchMethodException {
        Type wildcard = PortTypeTest.class.getDeclaredMethod("wildcard").getGenericReturnType();
        Type variable = PortTypeTest.class.getDeclaredMethod("variable").getGenericReturnType();

        assertEquals("java.util.List<java.lang.CharSequence>", PortType.of(wildcard).id());
        PortType map = PortType.of(variable);
        assertInstanceOf(PortType.Variable.class, ((PortType.Concrete) map).arguments().get(1));
        assertEquals("java.util.Map<java.lang.String,java.lang.Integer>", map.resolve(Map.of("T", BOXED_INT)).id());
    }

    @Test
    void rendersJavaPoetTypes() {
        assertEquals("double", DOUBLE.typeName().toString());
        assertEquals("java.util.List<java.lang.String>", PortType.parameterized(List.class, STRING).typeName().toString());
        assertEquals("T", PortType.variable("T").typeName().toString());
    }

    @Test
    void rejectsArraysAndWrongArity() {
        assertThrows(IllegalArgumentException.class, () -> PortType.of(String[].class));
        assertThrows(IllegalArgumentException.class, () -> PortType.parameterized(Map.class, STRING));
    }
}
