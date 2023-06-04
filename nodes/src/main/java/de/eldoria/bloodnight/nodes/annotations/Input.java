package de.eldoria.bloodnight.nodes.annotations;

import de.eldoria.bloodnight.nodes.DataStruct;
import de.eldoria.bloodnight.nodes.DataType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class representing an input field on a node.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Inputs.class)
public @interface Input {
    /**
     * Unique name of the input field
     * @return name
     */
    String name();

    /**
     * Type of the input field
     * @return type
     */
    DataType type();

    /**
     * Structure of the input type.
     * @return structure
     */
    DataStruct struct() default DataStruct.SINGLE;

}
