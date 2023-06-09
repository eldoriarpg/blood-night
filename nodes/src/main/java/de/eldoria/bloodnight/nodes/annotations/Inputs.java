package de.eldoria.bloodnight.nodes.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container containing {@link Input} fields.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Inputs {
    /**
     * Array of input fields
     *
     * @return input fields
     */
    Input[] value();
}
