/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.updates;

import de.eldoria.bloodnight.configuration.ConfigFile;
import de.eldoria.bloodnight.configuration.server.UpdateSettings;
import de.eldoria.eldoutilities.updater.AutoUpdateDecision;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdatesTest {
    @Test
    void turningAutoUpdateOnInChatIsKept() {
        UpdateSettings updates =
                Updates.decide(new ConfigFile(), AutoUpdateDecision.ENABLED).updates();

        assertTrue(updates.autoUpdate());
        assertTrue(updates.askForAutoUpdate());
    }

    @Test
    void neverAskingAgainIsKept() {
        UpdateSettings updates =
                Updates.decide(new ConfigFile(), AutoUpdateDecision.NEVER_ASK).updates();

        assertFalse(updates.askForAutoUpdate());
        assertFalse(updates.autoUpdate());
    }
}
