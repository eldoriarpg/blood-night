/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.definition;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.CodeBlock;
import com.palantir.javapoet.TypeName;

/**
 * What a node definition can use while emitting code for one node instance.
 * <p>
 * Everything comes back as finished {@link CodeBlock}s: inputs as expressions, config values as literals emitted by
 * their codec. A definition never sees a string from the graph, so it cannot splice user data into code by accident;
 * it embeds these blocks with {@code $L}.
 */
public interface EmitContext {
    /**
     * @return id of the node instance being emitted, for naming and diagnostics
     */
    String nodeId();

    /**
     * Value of a data input, either connected or typed into the node. Connected values are evaluated once per
     * execution scope, so reading them twice yields the same value; typed-in values are literals.
     *
     * @param port input port id
     * @return an expression of the input's type
     * @throws IllegalArgumentException when the port is not declared or an optional input is not connected
     */
    CodeBlock input(String port);

    /**
     * @param port input port id
     * @return whether the input has a value, connected or typed in
     */
    boolean connected(String port);

    /**
     * Value of a config field, or its default when the graph sets none.
     *
     * @param field config field id
     * @return a literal expression of the field's type
     */
    CodeBlock config(String field);

    /**
     * @param port output port id
     * @return whether any node reads this output; an unread result need not be kept in a variable
     */
    boolean isRead(String port);

    /**
     * Java type of the value given to an input: the type of the connected output, or the port's own type when the
     * value is typed in. Narrower than {@link #type(String)} when a port accepts a supertype, e.g. {@code Object}.
     *
     * @param port input port id
     * @return the type as rendered into code
     */
    TypeName inputType(String port);

    /**
     * Java type of a port after type variables are resolved for this instance.
     *
     * @param port input or output port id
     * @return the type as rendered into code
     */
    TypeName type(String port);

    /**
     * Declares a field on the generated class, one per node instance and per behaviour instance. This is where state
     * such as cooldowns lives.
     *
     * @param name        name, unique within this node
     * @param type        field type
     * @param initializer initial value
     * @return an expression referring to the field
     */
    CodeBlock field(String name, TypeName type, CodeBlock initializer);

    /**
     * Declares a field shared by every node instance of the graph asking for the same name, one per behaviour
     * instance. This is where state lives that several triggers read and write, such as named timers. The first
     * declaration decides type and initial value.
     *
     * @param name        name, shared across the graph
     * @param type        field type
     * @param initializer initial value
     * @return an expression referring to the field
     */
    CodeBlock sharedField(String name, TypeName type, CodeBlock initializer);

    /**
     * Calls a static helper method by its simple name, with a static import, e.g. {@code nextDouble(-2.0d, 2.0d)}. When
     * another class's method already holds that name in the generated class, the call is qualified instead.
     *
     * @param owner     the class declaring the method
     * @param method    the method name
     * @param arguments the arguments, in order
     * @return the call
     */
    CodeBlock staticCall(ClassName owner, String method, CodeBlock... arguments);

    /**
     * @return an expression referring to the context object the host passes in
     * @throws IllegalStateException when the platform declares no context type
     */
    CodeBlock context();
}
