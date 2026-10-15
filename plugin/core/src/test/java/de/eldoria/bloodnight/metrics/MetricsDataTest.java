/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.metrics;

import de.eldoria.bloodnight.Mobs;
import de.eldoria.bloodnight.mob.meta.MobOrigin;
import de.eldoria.bloodnight.rules.Settings;
import de.eldoria.graph.model.Graph;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MetricsDataTest {
    @Test
    void nightSelectionsCountWorldsWithBloodNights() {
        assertEquals(Map.of("RANDOM", 2), MetricsData.nightSelections(List.of(Settings.enabled(), Settings.enabled())));
    }

    @Test
    void mobsAreCountedByWhereTheyCameFrom() {
        var own = Mobs.zombie("own", Graph.builder().build());
        var store = Mobs.zombie("store", Graph.builder().build()).withOrigin(new MobOrigin("bloodnight", "store", "h"));

        assertEquals(Map.of("own", 1, "store", 1), MetricsData.mobOrigins(List.of(own, store)));
    }

    @Test
    void onlyInstalledIntegrationsCount() {
        assertEquals(Map.of("Vault", 1), MetricsData.integrations(name -> name.equals("Vault")));
    }

    @Test
    void theEditorServiceIsHostedOrOwn() {
        assertEquals("hosted", MetricsData.editorService("https://a", "https://a"));
        assertEquals("own", MetricsData.editorService("https://b", "https://a"));
    }
}
