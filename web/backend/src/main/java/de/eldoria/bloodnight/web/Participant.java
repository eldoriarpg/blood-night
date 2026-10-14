/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.web;

import de.eldoria.bloodnight.web.api.editor.EditorArea;

import java.util.EnumSet;
import java.util.Set;

/**
 * Someone allowed to edit a session.
 *
 * @param tokenHash the hash of their participant token, which identifies them
 * @param name      their name, shown to the others
 * @param areas     what they may change
 */
record Participant(String tokenHash, String name, Set<EditorArea> areas) {
    /**
     * @return whether they may change everything, e.g. replace the whole payload
     */
    boolean mayChangeAll() {
        return areas.containsAll(EnumSet.allOf(EditorArea.class));
    }

    /**
     * @param section a lockable section, e.g. {@code mobs/speed_zombie}
     * @return whether they may change it
     */
    boolean mayChange(String section) {
        EditorArea area = EditorArea.of(section);
        return area != null && areas.contains(area);
    }
}
