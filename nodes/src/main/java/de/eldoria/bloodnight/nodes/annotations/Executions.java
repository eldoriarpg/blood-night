package de.eldoria.bloodnight.nodes.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container containing {@link Execution} fields.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Executions {
    /**
     * Array of execution fields
     * @return execution fields
     */
    Execution[] value();
}
