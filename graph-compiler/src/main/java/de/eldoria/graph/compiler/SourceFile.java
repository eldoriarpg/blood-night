package de.eldoria.graph.compiler;

import com.palantir.javapoet.JavaFile;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;

/**
 * A compilation unit held in memory.
 */
public final class SourceFile extends SimpleJavaFileObject {
    private final String binaryName;
    private final String source;

    private SourceFile(String binaryName, String source) {
        super(URI.create("string:///" + binaryName.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
        this.binaryName = binaryName;
        this.source = source;
    }

    /**
     * Renders a JavaPoet file.
     *
     * @param file file to render
     * @return the source file
     */
    public static SourceFile of(JavaFile file) {
        String packageName = file.packageName();
        String binaryName = packageName.isEmpty() ? file.typeSpec().name() : packageName + "." + file.typeSpec().name();
        return new SourceFile(binaryName, file.toString());
    }

    /**
     * @return binary name of the top level type
     */
    public String binaryName() {
        return binaryName;
    }

    /**
     * @return the full source text
     */
    public String source() {
        return source;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return source;
    }
}
