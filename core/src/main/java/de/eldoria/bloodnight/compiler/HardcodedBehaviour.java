package de.eldoria.bloodnight.compiler;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeSpec;
import de.eldoria.bloodnight.api.CompiledBehaviour;
import org.bukkit.event.entity.EntityDamageEvent;

import javax.lang.model.element.Modifier;
import java.util.logging.Logger;

/**
 * A fixed behaviour built with JavaPoet, used to prove that generated code compiles and runs inside the server before
 * any node graph is translated.
 * <p>
 * The generated class doubles all damage, logs it, and logs a heartbeat every 100 ticks.
 */
public final class HardcodedBehaviour {
    public static final String PACKAGE = "de.eldoria.bloodnight.generated";
    public static final String NAME = "HardcodedBehaviour";
    public static final String BINARY_NAME = PACKAGE + "." + NAME;

    private HardcodedBehaviour() {
    }

    /**
     * @return the source of the behaviour; its constructor takes a {@link Logger}
     */
    public static JavaFile source() {
        TypeSpec type = TypeSpec.classBuilder(NAME)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(ClassName.get(CompiledBehaviour.class))
                .addField(FieldSpec.builder(Logger.class, "logger", Modifier.PRIVATE, Modifier.FINAL).build())
                .addField(FieldSpec.builder(long.class, "ticks", Modifier.PRIVATE).build())
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Logger.class, "logger")
                        .addStatement("this.logger = logger")
                        .build())
                .addMethod(MethodSpec.methodBuilder("onTick")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .beginControlFlow("if (++ticks % 100 == 0)")
                        .addStatement("logger.info($S + ticks)", "Generated tick: ")
                        .endControlFlow()
                        .build())
                .addMethod(MethodSpec.methodBuilder("onEntityDamage")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(EntityDamageEvent.class, "event")
                        .addStatement("double before = event.getDamage()")
                        .addStatement("event.setDamage(before * 2)")
                        .addStatement("logger.info($S.formatted(event.getEntityType(), event.getCause(), before, event.getDamage()))",
                                "Generated damage handler: %s hit by %s, %.1f -> %.1f")
                        .build())
                .build();
        return JavaFile.builder(PACKAGE, type).build();
    }
}
