package de.eldoria.graph.toy;

/**
 * Host interface of the plain-Java test consumer.
 */
public interface ToyHost {
    default void onStart() {
    }

    default void onScore(int points, String player) {
    }

    default void onEvent(ToyEvent event) {
    }

    void onStop();
}
