/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.graph.compiler;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardLocation;

/**
 * File manager that keeps class files in memory instead of writing them to disk.
 * <p>
 * Also reports every {@link SourceFile} as part of the source path, because the Eclipse compiler rejects sources it
 * can neither find there nor on disk. The source path itself has to be set, even if empty, for that check to run.
 */
final class InMemoryFileManager extends ForwardingJavaFileManager<JavaFileManager> {
    private final Map<String, ByteArrayOutputStream> outputs = new HashMap<>();

    InMemoryFileManager(JavaFileManager fileManager) {
        super(fileManager);
    }

    @Override
    public boolean contains(Location location, FileObject file) throws IOException {
        if (location == StandardLocation.SOURCE_PATH && file instanceof SourceFile) return true;
        return super.contains(location, file);
    }

    @Override
    public JavaFileObject getJavaFileForOutput(
            Location location, String className, JavaFileObject.Kind kind, FileObject sibling) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        outputs.put(binaryName(className, kind), buffer);
        return new SimpleJavaFileObject(URI.create("mem:///" + className.replace('.', '/') + kind.extension), kind) {
            @Override
            public OutputStream openOutputStream() {
                return buffer;
            }
        };
    }

    /**
     * The Eclipse compiler passes a path relative to the output root instead of a binary name.
     */
    private static String binaryName(String className, JavaFileObject.Kind kind) {
        String name = className.endsWith(kind.extension)
                ? className.substring(0, className.length() - kind.extension.length())
                : className;
        return name.replace('/', '.').replace('\\', '.');
    }

    /**
     * @return bytecode of every class written so far, keyed by binary name
     */
    Map<String, byte[]> bytecode() {
        Map<String, byte[]> result = new HashMap<>();
        outputs.forEach((name, buffer) -> result.put(name, buffer.toByteArray()));
        return result;
    }
}
