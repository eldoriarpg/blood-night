package de.eldoria.bloodnight.nodes.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Container containing {@link Output} fields.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Outputs {
    /**
     * Array of output fields
     *
     * @return output fields
     */
    Output[] value();
}
