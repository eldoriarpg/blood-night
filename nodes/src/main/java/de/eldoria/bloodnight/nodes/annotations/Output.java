package de.eldoria.bloodnight.nodes.annotations;

import de.eldoria.bloodnight.nodes.DataStruct;
import de.eldoria.bloodnight.nodes.DataType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class representing an ouput field on a node.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Outputs.class)
public @interface Output {
    /**
     * Unique name of the output field
     *
     * @return name
     */
    String name();

    /**
     * Type of the input field.
     * <p>
     * If set to {@link DataType#LINKED} the {@link #link()} field needs to be set.
     *
     * @return type
     */
    DataType type();

    /**
     * The name of the linked input field.
     * <p>
     * If a name is set the {@link #type()} needs to be set to {@link DataType#LINKED}.
     * <p>
     * A linked output field uses the type of the linked input field.
     *
     * @return name of the linked input field.
     */
    String link() default "";

    /**
     * The structure of the data type.
     *
     * @return structure
     */
    DataStruct struct() default DataStruct.SINGLE;
}
