package de.eldoria.graph.toy;

/**
 * Interface with record-style accessors; only pairs can identify them.
 */
public interface ToyFluentApi {
    String title();

    void title(String title);

    int count();
}
