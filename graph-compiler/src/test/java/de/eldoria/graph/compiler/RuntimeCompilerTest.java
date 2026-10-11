/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.compiler;

import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.FieldSpec;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeSpec;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.IntUnaryOperator;

import javax.lang.model.element.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuntimeCompilerTest {

    @Test
    void compilesAgainstJdkTypes() throws CompilationException {
        TypeSpec type = TypeSpec.classBuilder("Twice")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(IntUnaryOperator.class)
                .addMethod(MethodSpec.methodBuilder("applyAsInt")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(int.class)
                        .addParameter(int.class, "value")
                        .addStatement("return value * 2")
                        .build())
                .build();

        CompilationResult result =
                compiler().compile(JavaFile.builder("generated.test", type).build());

        assertEquals(
                42,
                result.instantiate("generated.test.Twice", IntUnaryOperator.class)
                        .applyAsInt(21));
    }

    @Test
    void compilesAgainstClasspathAndKeepsStatePerInstance() throws CompilationException {
        TypeSpec type = TypeSpec.classBuilder("StartingCounter")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addSuperinterface(ClassName.get(Counter.class))
                .addField(
                        FieldSpec.builder(int.class, "count", Modifier.PRIVATE).build())
                .addMethod(MethodSpec.constructorBuilder()
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(Integer.class, "start")
                        .addStatement("this.count = start")
                        .build())
                .addMethod(MethodSpec.methodBuilder("next")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .returns(int.class)
                        .addStatement("return ++count")
                        .build())
                .build();

        CompilationResult result =
                compiler().compile(JavaFile.builder("generated.test", type).build());
        Counter first = result.instantiate("generated.test.StartingCounter", Counter.class, 10);
        Counter second = result.instantiate("generated.test.StartingCounter", Counter.class, 0);

        assertEquals(11, first.next());
        assertEquals(12, first.next());
        assertEquals(1, second.next());
    }

    @Test
    void eachCompilationGetsItsOwnLoader() throws CompilationException {
        JavaFile file = JavaFile.builder(
                        "generated.test",
                        TypeSpec.classBuilder("Empty")
                                .addModifiers(Modifier.PUBLIC)
                                .build())
                .build();

        Class<?> first = compiler().compile(file).load("generated.test.Empty", Object.class);
        Class<?> second = compiler().compile(file).load("generated.test.Empty", Object.class);

        assertNotSame(first, second);
    }

    @Test
    void reportsErrorsWithSource() {
        TypeSpec type = TypeSpec.classBuilder("Broken")
                .addMethod(MethodSpec.methodBuilder("broken")
                        .returns(int.class)
                        .addStatement("return $S", "not a number")
                        .build())
                .build();

        CompilationException exception = assertThrows(CompilationException.class, () -> compiler()
                .compile(JavaFile.builder("generated.test", type).build()));

        assertTrue(exception.getMessage().contains("generated.test.Broken"), exception.getMessage());
        assertTrue(exception.getMessage().contains("line 5"), exception.getMessage());
        assertEquals(1, exception.sources().size());
    }

    private static RuntimeCompiler compiler() {
        return new RuntimeCompiler(List.of(codeSource(Counter.class)), RuntimeCompilerTest.class.getClassLoader());
    }

    private static Path codeSource(Class<?> clazz) {
        try {
            return Path.of(
                    clazz.getProtectionDomain().getCodeSource().getLocation().toURI());
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}
