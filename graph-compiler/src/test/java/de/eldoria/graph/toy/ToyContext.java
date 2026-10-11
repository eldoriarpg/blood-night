package de.eldoria.graph.toy;

import java.util.ArrayList;
import java.util.List;

/**
 * Context object of the test consumer; collects what generated code prints.
 */
public final class ToyContext {
    private final List<String> printed = new ArrayList<>();

    public void print(Object value) {
        printed.add(String.valueOf(value));
    }

    public List<String> printed() {
        return printed;
    }
}
