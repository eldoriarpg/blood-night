package de.eldoria.bloodnight.mobs.exceptions;

import de.eldoria.bloodnight.mob.CustomMob;

/**
 * Exception thrown when trying to register a CustomMob with an id that is already taken by another CustomMob.
 */
public class MobIdAlreadyTakenException extends RuntimeException {
    private final CustomMob newMob;
    private final CustomMob currentMob;

    public MobIdAlreadyTakenException(CustomMob newMob, CustomMob currentMob) {
        super("Could not register %s. The id %s is already taken by %s".formatted(newMob.getClass().getName(), newMob.id(), currentMob.getClass().getName()));
        this.newMob = newMob;
        this.currentMob = currentMob;
    }

    public CustomMob newMob() {
        return newMob;
    }

    public CustomMob currentMob() {
        return currentMob;
    }
}
