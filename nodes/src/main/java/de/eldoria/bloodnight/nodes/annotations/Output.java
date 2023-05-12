package de.eldoria.bloodnight.nodes.annotations;

import de.eldoria.bloodnight.nodes.DataType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Outputs.class)
public @interface Output {
    String name();

    DataType type();
}
