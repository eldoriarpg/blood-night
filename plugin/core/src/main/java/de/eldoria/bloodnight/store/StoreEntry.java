/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.store;

import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.store.build.MobLogic;
import de.eldoria.bloodnight.store.files.MobPackage;

/**
 * A mob a store offers: its package and the hash of its logic ({@link MobLogic}); a changed hash is a new version.
 *
 * @param mobPackage the package as the store ships it
 * @param hash       the hash of the mob's logic
 */
public record StoreEntry(MobPackage mobPackage, String hash) {
    /**
     * @param mobPackage the package as the store ships it
     * @return the entry, hashed
     */
    public static StoreEntry of(MobPackage mobPackage) {
        return new StoreEntry(mobPackage, MobLogic.hash(mobPackage.mob()));
    }

    /**
     * @return the entry's id, the id the mob installs as
     */
    public String id() {
        return mobPackage.id();
    }

    /**
     * @return the id of the pack the entry belongs to
     */
    public String pack() {
        return mobPackage.pack();
    }

    /**
     * @return the mob as it is installed
     */
    public CustomMob mob() {
        return mobPackage.mob();
    }
}
