/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.definition.port;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.ParameterizedTypeName;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeVariableName;

import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * The Java type of a port.
 * <p>
 * Either a concrete class with optional type arguments, or a type variable that the validator resolves from the edges
 * of a node, e.g. the element type of a for-each loop.
 */
public sealed interface PortType permits PortType.Concrete, PortType.Variable {

    /**
     * @param type a class or primitive
     * @return the port type
     */
    static PortType of(Class<?> type) {
        if (type.isArray()) throw new IllegalArgumentException("Array ports are not supported: " + type.getName());
        return new Concrete(type, List.of());
    }

    /**
     * Converts a reflected type. Wildcards become their upper bound.
     *
     * @param type a class, parameterized type, type variable or wildcard
     * @return the port type
     */
    static PortType of(Type type) {
        return switch (type) {
            case Class<?> clazz -> of(clazz);
            case ParameterizedType parameterized ->
                new Concrete(
                        (Class<?>) parameterized.getRawType(),
                        Arrays.stream(parameterized.getActualTypeArguments())
                                .map(PortType::of)
                                .toList());
            case TypeVariable<?> variable -> new Variable(variable.getName());
            case WildcardType wildcard -> of(wildcard.getUpperBounds()[0]);
            case GenericArrayType array ->
                throw new IllegalArgumentException("Array ports are not supported: " + array);
            default -> throw new IllegalArgumentException("Unsupported type " + type);
        };
    }

    /**
     * @param raw       the generic class
     * @param arguments its type arguments
     * @return the port type
     */
    static PortType parameterized(Class<?> raw, PortType... arguments) {
        if (raw.getTypeParameters().length != arguments.length) {
            throw new IllegalArgumentException("%s takes %d type arguments, got %d"
                    .formatted(raw.getName(), raw.getTypeParameters().length, arguments.length));
        }
        return new Concrete(raw, List.of(arguments));
    }

    /**
     * @param name name of the variable, unique within one node definition
     * @return the port type
     */
    static PortType variable(String name) {
        return new Variable(name);
    }

    /**
     * A variable that only numbers bind to. Unlike other variables it binds to the primitive type and, given numbers of
     * different types, to the widest of them (at least {@code int}), so one node adds integers as well as decimals:
     * whole numbers in, a whole number out; any decimal in, a decimal out. Left unbound it is {@code double}.
     *
     * @param name name of the variable, unique within one node definition
     * @return the port type
     */
    static PortType number(String name) {
        return new Variable(name, true);
    }

    /**
     * Whether a type is a primitive number or its wrapper.
     *
     * @param type a type
     * @return true for {@code byte} through {@code double} and their wrappers
     */
    static boolean isNumber(PortType type) {
        return type instanceof Concrete concrete
                && Primitives.number(concrete.raw()).isPresent();
    }

    /**
     * The type a numeric variable binds to when numbers of two types meet it.
     *
     * @param a a number type
     * @param b a number type
     * @return the wider of both as primitive, e.g. {@code double} for {@code int} and {@code Double}
     * @throws IllegalArgumentException when either is not a number
     */
    static PortType widerNumber(PortType a, PortType b) {
        return of(Primitives.wider(primitiveNumber(a), primitiveNumber(b)));
    }

    /**
     * @param type a number type
     * @return its primitive form, e.g. {@code int} for {@code Integer}
     * @throws IllegalArgumentException when it is not a number
     */
    static Class<?> primitiveNumber(PortType type) {
        if (type instanceof Concrete concrete) {
            Optional<Class<?>> number = Primitives.number(concrete.raw());
            if (number.isPresent()) return number.get();
        }
        throw new IllegalArgumentException(type.id() + " is no number");
    }

    /**
     * @return the type as JavaPoet renders it into generated code
     */
    TypeName typeName();

    /**
     * Checks whether a value of {@code source} can be passed where this type is expected, without a cast.
     * <p>
     * Allows subtyping, primitive widening, boxing and unboxing. Type arguments must match exactly. Variables accept
     * and are accepted by anything here; whether a variable resolves consistently is checked per node by the validator.
     *
     * @param source type of the value
     * @return true when assignable
     */
    boolean accepts(PortType source);

    /**
     * @return a canonical, human-readable id, e.g. {@code java.util.List<java.lang.String>}
     */
    String id();

    /**
     * @return the wrapper type for primitives, otherwise this type
     */
    PortType boxed();

    /**
     * @return whether a type variable occurs anywhere in this type
     */
    boolean hasVariables();

    /**
     * Replaces variables.
     *
     * @param bindings type for each variable name
     * @return this type with every bound variable replaced
     */
    PortType resolve(Map<String, PortType> bindings);

    /**
     * A class or primitive, optionally with type arguments.
     *
     * @param raw       the class
     * @param arguments type arguments; empty for non-generic or raw use
     */
    record Concrete(Class<?> raw, List<PortType> arguments) implements PortType {
        private static final List<Class<?>> WIDENING =
                List.of(byte.class, short.class, int.class, long.class, float.class, double.class);

        public Concrete {
            Objects.requireNonNull(raw, "raw");
            arguments = List.copyOf(arguments);
        }

        @Override
        public TypeName typeName() {
            if (arguments.isEmpty()) return TypeName.get(raw);
            return ParameterizedTypeName.get(
                    ClassName.get(raw),
                    arguments.stream().map(PortType::typeName).toArray(TypeName[]::new));
        }

        @Override
        public boolean accepts(PortType source) {
            if (!(source instanceof Concrete concrete)) return true;
            if (!acceptsRaw(concrete.raw)) return false;
            if (arguments.isEmpty() || raw.isPrimitive()) return true;
            Optional<Concrete> lifted = concrete.asSupertype(raw);
            if (lifted.isEmpty() || lifted.get().arguments.isEmpty()) return true;
            for (int i = 0; i < arguments.size(); i++) {
                PortType expected = arguments.get(i);
                PortType actual = lifted.get().arguments.get(i);
                if (expected.hasVariables() || actual.hasVariables()) continue;
                if (!expected.equals(actual)) return false;
            }
            return true;
        }

        /**
         * Views this type as one of its supertypes, carrying type arguments over, e.g. {@code ArrayList<String>} as
         * {@code Collection<String>}.
         *
         * @param target a superclass or interface of {@link #raw()}
         * @return the supertype with arguments, without arguments when this type is used raw, or empty when
         * {@code target} is not a supertype
         */
        public Optional<Concrete> asSupertype(Class<?> target) {
            if (raw == target) return Optional.of(this);
            if (!target.isAssignableFrom(Primitives.box(raw))) return Optional.empty();
            if (arguments.isEmpty() && raw.getTypeParameters().length > 0)
                return Optional.of(new Concrete(target, List.of()));
            Map<String, PortType> bindings = new HashMap<>();
            TypeVariable<?>[] parameters = raw.getTypeParameters();
            for (int i = 0; i < parameters.length && i < arguments.size(); i++)
                bindings.put(parameters[i].getName(), arguments.get(i));
            List<Type> supertypes = new ArrayList<>(Arrays.asList(raw.getGenericInterfaces()));
            if (raw.getGenericSuperclass() != null) supertypes.add(raw.getGenericSuperclass());
            for (Type supertype : supertypes) {
                if (!(PortType.of(supertype).resolve(bindings) instanceof Concrete parent)) continue;
                if (!target.isAssignableFrom(parent.raw)) continue;
                Optional<Concrete> found = parent.asSupertype(target);
                if (found.isPresent()) return found;
            }
            return Optional.of(new Concrete(target, List.of()));
        }

        @Override
        public PortType boxed() {
            return raw.isPrimitive() ? new Concrete(Primitives.box(raw), List.of()) : this;
        }

        @Override
        public boolean hasVariables() {
            return arguments.stream().anyMatch(PortType::hasVariables);
        }

        private boolean acceptsRaw(Class<?> source) {
            if (raw.equals(source)) return true;
            if (raw.isPrimitive() && source.isPrimitive()) return widens(source, raw);
            if (raw.isPrimitive())
                return Primitives.unbox(source)
                        .map(unboxed -> unboxed == raw || widens(unboxed, raw))
                        .orElse(false);
            if (source.isPrimitive()) return raw.isAssignableFrom(Primitives.box(source));
            return raw.isAssignableFrom(source);
        }

        private static boolean widens(Class<?> from, Class<?> to) {
            if (from == char.class) return WIDENING.indexOf(to) >= WIDENING.indexOf(int.class);
            int fromIndex = WIDENING.indexOf(from);
            int toIndex = WIDENING.indexOf(to);
            return fromIndex >= 0 && toIndex >= 0 && fromIndex <= toIndex;
        }

        @Override
        public String id() {
            if (arguments.isEmpty()) return raw.getName();
            return arguments.stream().map(PortType::id).collect(Collectors.joining(",", raw.getName() + "<", ">"));
        }

        @Override
        public PortType resolve(Map<String, PortType> bindings) {
            if (arguments.isEmpty()) return this;
            return new Concrete(
                    raw,
                    arguments.stream()
                            .map(argument -> argument.resolve(bindings))
                            .toList());
        }

        @Override
        public String toString() {
            return id();
        }
    }

    /**
     * A type variable, resolved per node instance.
     *
     * @param name variable name
     */
    /**
     * A type variable, bound per node by what is connected.
     *
     * @param name    name of the variable
     * @param numeric whether only numbers bind to it; see {@link #number(String)}
     */
    record Variable(String name, boolean numeric) implements PortType {
        /**
         * A variable any type binds to.
         */
        public Variable(String name) {
            this(name, false);
        }

        public Variable {
            Objects.requireNonNull(name, "name");
        }

        @Override
        public TypeName typeName() {
            return TypeVariableName.get(name);
        }

        @Override
        public boolean accepts(PortType source) {
            return !numeric || source instanceof Variable || isNumber(source);
        }

        @Override
        public String id() {
            return name;
        }

        @Override
        public PortType resolve(Map<String, PortType> bindings) {
            return bindings.getOrDefault(name, this);
        }

        @Override
        public PortType boxed() {
            return this;
        }

        @Override
        public boolean hasVariables() {
            return true;
        }

        @Override
        public String toString() {
            return id();
        }
    }
}
