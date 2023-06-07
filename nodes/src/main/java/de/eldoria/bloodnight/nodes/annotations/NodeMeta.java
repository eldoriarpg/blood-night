package de.eldoria.bloodnight.nodes.annotations;

import de.eldoria.bloodnight.nodes.Categories;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class representing an input field on a node.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NodeMeta {
    /**
     * Unique name of the node
     *
     * @return name
     */
    String name();

    /**
     * Description of the node
     *
     * @return name
     */
    String description();

    /**
     * The node category inside their type
     * @return category name
     */
    String category() default Categories.DEFAULT;
}
