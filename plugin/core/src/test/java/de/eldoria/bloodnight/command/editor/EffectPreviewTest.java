/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.command.editor;

import de.eldoria.bloodnight.editor.payload.EditorPayload;
import org.bukkit.Location;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;
import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class EffectPreviewTest {
    private ServerMock server;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void theSessionsOwnEffectsAndTheBuiltInOnesArePreviewed() {
        JsonNode payload = EditorPayload.MAPPER.readTree("{\"particleEffects\":{\"glow\":{\"name\":\"Glow\"}}}");
        assertEquals("Glow", EffectPreview.effect(payload, "glow").name());
        assertNotNull(EffectPreview.effect(payload, "bloodnight:blood_aura"));
        assertNull(EffectPreview.effect(payload, "gone"));
    }

    @Test
    void theEffectPlaysOnTheGroundInFrontOfThePlayer() {
        PlayerMock player = server.addPlayer();
        player.teleport(new Location(server.addSimpleWorld("world"), 0, 64, 0, 0, -40));
        Location spot = EffectPreview.spot(player);
        assertEquals(64, spot.getY(), 1e-9);
        assertEquals(4, spot.getZ(), 1e-9);
    }
}
