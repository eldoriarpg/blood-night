package de.eldoria.bloodnight.nodes.trigger.base;

import java.io.Closeable;

public class TriggerLock implements Closeable {
    private boolean locked;

    public TriggerLock lock() {
        if (locked) {
            // This should only happen if two threads are working on the same node. Aka.: It should not happen.
            throw new IllegalStateException("Trigger is already locked.");
        }
        locked = true;
        return this;
    }

    public boolean locked() {
        return locked;
    }

    @Override
    public void close() {
        locked = false;
    }
}
