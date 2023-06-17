package de.eldoria.bloodnight.nodes.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An execution output field on a node
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Executions.class)
public @interface Execution {
    /**
     * Unique name of the output execution field
     *
     * @return string
     */
    String value();
}
