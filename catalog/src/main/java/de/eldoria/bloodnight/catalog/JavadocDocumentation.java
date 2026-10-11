/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.catalog;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.CallableDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.comments.JavadocComment;
import de.eldoria.graph.reflect.Documentation;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Descriptions taken from the javadoc in sources jars.
 * <p>
 * Source files are parsed when a class is first asked for. A description is the first sentence of the javadoc, with
 * inline tags reduced to their text and HTML removed. Methods without javadoc of their own take it from the method
 * they override, searching superclasses and interfaces.
 */
public final class JavadocDocumentation implements Documentation, AutoCloseable {
    private static final Pattern INLINE_TAG = Pattern.compile("\\{@(?:link|linkplain|code|literal|value)\\s+([^}]*)}");
    private static final Pattern HTML = Pattern.compile("<[^>]+>");
    private static final Pattern SENTENCE_END = Pattern.compile("(?<=[.!?])\\s");

    private final List<ZipFile> jars;
    private final JavaParser parser =
            new JavaParser(new ParserConfiguration().setLanguageLevel(ParserConfiguration.LanguageLevel.BLEEDING_EDGE));
    private final Map<String, Optional<CompilationUnit>> units = new HashMap<>();

    /**
     * @param sources sources jars to read from
     */
    public JavadocDocumentation(List<Path> sources) {
        try {
            this.jars = sources.stream()
                    .map(path -> {
                        try {
                            return new ZipFile(path.toFile());
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .toList();
        } catch (UncheckedIOException e) {
            throw new IllegalArgumentException("Cannot open sources jar", e.getCause());
        }
    }

    @Override
    public Optional<String> of(Class<?> type) {
        return declaration(type)
                .flatMap(TypeDeclaration::getJavadocComment)
                .map(JavadocDocumentation::summary)
                .filter(text -> !text.isEmpty());
    }

    @Override
    public Optional<String> of(Method method) {
        Deque<Class<?>> queue = new ArrayDeque<>();
        Set<Class<?>> seen = new HashSet<>();
        queue.add(method.getDeclaringClass());
        while (!queue.isEmpty()) {
            Class<?> type = queue.poll();
            if (!seen.add(type)) continue;
            Optional<String> doc = own(type, method);
            if (doc.isPresent()) return doc;
            if (type.getSuperclass() != null) queue.add(type.getSuperclass());
            queue.addAll(List.of(type.getInterfaces()));
        }
        return Optional.empty();
    }

    @Override
    public Optional<String> of(Executable executable) {
        if (executable instanceof Method method) return of(method);
        return declaration(executable.getDeclaringClass()).flatMap(declaration -> declaration.getConstructors().stream()
                .filter(candidate -> matches(candidate, executable))
                .findFirst()
                .flatMap(ConstructorDeclaration::getJavadocComment)
                .map(JavadocDocumentation::summary)
                .filter(text -> !text.isEmpty()));
    }

    private Optional<String> own(Class<?> type, Method method) {
        return declaration(type).flatMap(declaration -> declaration.getMethodsByName(method.getName()).stream()
                .filter(candidate -> matches(candidate, method))
                .findFirst()
                .flatMap(MethodDeclaration::getJavadocComment)
                .map(JavadocDocumentation::summary)
                .filter(text -> !text.isEmpty()));
    }

    private static boolean matches(CallableDeclaration<?> declaration, Executable method) {
        if (declaration.getParameters().size() != method.getParameterCount()) return false;
        Class<?>[] types = method.getParameterTypes();
        for (int i = 0; i < types.length; i++) {
            String declared = declaration
                    .getParameter(i)
                    .getType()
                    .asString()
                    .replaceAll("<.*>", "")
                    .replace("...", "[]");
            String simple = declared.substring(declared.lastIndexOf('.') + 1);
            if (!simple.equals(types[i].getSimpleName())) return false;
        }
        return true;
    }

    private Optional<TypeDeclaration<?>> declaration(Class<?> type) {
        Class<?> top = type;
        while (top.getEnclosingClass() != null) top = top.getEnclosingClass();
        Optional<CompilationUnit> unit = units.computeIfAbsent(top.getName(), this::parse);
        if (unit.isEmpty()) return Optional.empty();
        String nested = type.getName().substring(top.getName().length());
        String topName = top.getSimpleName();
        Optional<? extends TypeDeclaration<?>> current = unit.get().getTypes().stream()
                .filter(declaration -> declaration.getNameAsString().equals(topName))
                .findFirst();
        for (String part : nested.split("\\$")) {
            if (part.isEmpty() || current.isEmpty()) continue;
            current = current.get().getMembers().stream()
                    .filter(member -> member instanceof TypeDeclaration<?> declaration
                            && declaration.getNameAsString().equals(part))
                    .map(member -> (TypeDeclaration<?>) member)
                    .findFirst();
        }
        return current.map(declaration -> declaration);
    }

    private Optional<CompilationUnit> parse(String className) {
        String path = className.replace('.', '/') + ".java";
        for (ZipFile jar : jars) {
            ZipEntry entry = jar.getEntry(path);
            if (entry == null) continue;
            try {
                String source = new String(jar.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8);
                return parser.parse(source).getResult();
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        return Optional.empty();
    }

    /**
     * First sentence of a javadoc comment as plain text.
     */
    static String summary(JavadocComment comment) {
        String text = comment.parse().getDescription().toText();
        Matcher tags = INLINE_TAG.matcher(text);
        StringBuilder plain = new StringBuilder();
        while (tags.find()) {
            String target = tags.group(1).trim();
            String label = target.contains(" ")
                    ? target.substring(target.indexOf(' ') + 1)
                    : target.substring(target.lastIndexOf('#') + 1);
            tags.appendReplacement(plain, Matcher.quoteReplacement(label));
        }
        tags.appendTail(plain);
        String cleaned =
                HTML.matcher(plain).replaceAll("").replaceAll("\\s+", " ").trim();
        String[] sentences = SENTENCE_END.split(cleaned, 2);
        return sentences[0].trim();
    }

    @Override
    public void close() throws IOException {
        for (ZipFile jar : jars) jar.close();
    }
}
