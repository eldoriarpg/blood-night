package de.eldoria.graph.compiler;

/**
 * Host used to check macro bodies on their own; {@code macro.start} is its only entry point.
 */
public interface MacroBody {
    void start();
}
