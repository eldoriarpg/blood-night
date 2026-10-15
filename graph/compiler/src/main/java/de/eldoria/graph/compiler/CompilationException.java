/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.compiler;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * Thrown when generated source does not compile. Carries the compiler errors and the offending sources.
 */
public final class CompilationException extends Exception {
    private final List<Diagnostic<? extends JavaFileObject>> diagnostics;
    private final List<SourceFile> sources;

    CompilationException(List<Diagnostic<? extends JavaFileObject>> diagnostics, List<SourceFile> sources) {
        super(describe(diagnostics, sources));
        this.diagnostics = List.copyOf(diagnostics);
        this.sources = List.copyOf(sources);
    }

    /**
     * @return every diagnostic the compiler reported
     */
    public List<Diagnostic<? extends JavaFileObject>> diagnostics() {
        return diagnostics;
    }

    /**
     * @return the sources that failed to compile
     */
    public List<SourceFile> sources() {
        return sources;
    }

    /**
     * Lists the errors by line. The Eclipse compiler reports no source for in-memory files, so the header names the
     * compiled types instead.
     */
    private static String describe(List<Diagnostic<? extends JavaFileObject>> diagnostics, List<SourceFile> sources) {
        String types = sources.stream().map(SourceFile::binaryName).collect(Collectors.joining(", "));
        return diagnostics.stream()
                .filter(d -> d.getKind() == Diagnostic.Kind.ERROR)
                .map(d -> "line %d: %s".formatted(d.getLineNumber(), d.getMessage(Locale.ROOT)))
                .collect(Collectors.joining("\n", "Generated code does not compile (%s):\n".formatted(types), ""));
    }
}
