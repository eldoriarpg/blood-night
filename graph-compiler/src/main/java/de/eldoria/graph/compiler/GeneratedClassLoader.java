package de.eldoria.graph.compiler;

import java.util.Map;

/**
 * Class loader for one compilation. Defines the generated classes and delegates everything else to its parent.
 */
public final class GeneratedClassLoader extends ClassLoader {
    static {
        registerAsParallelCapable();
    }

    private final Map<String, byte[]> bytecode;

    GeneratedClassLoader(Map<String, byte[]> bytecode, ClassLoader parent) {
        super("generated", parent);
        this.bytecode = Map.copyOf(bytecode);
    }

    /**
     * @return bytecode of every generated class, keyed by binary name
     */
    public Map<String, byte[]> bytecode() {
        return bytecode;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        byte[] bytes = bytecode.get(name);
        if (bytes == null) throw new ClassNotFoundException(name);
        return defineClass(name, bytes, 0, bytes.length);
    }
}
