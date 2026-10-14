/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph.runtime;

import org.bukkit.Color;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MarkerShapeTest {
    @Test
    void anAreaIsAFlatSquareAroundTheSpot() {
        List<MarkerShape.Bar> bars = MarkerShape.AREA.bars(2);
        assertEquals(1, bars.size());
        MarkerShape.Bar area = bars.getFirst();
        assertEquals(4, area.width());
        assertEquals(4, area.depth());
        assertEquals(-2, area.x());
        assertEquals(-2, area.z());
    }

    @Test
    void aPillarRisesFromTheGroundAndACrossHasTwoBars() {
        MarkerShape.Bar pillar = MarkerShape.PILLAR.bars(3).getFirst();
        assertEquals(3, pillar.height());
        assertEquals(0, pillar.y());
        assertEquals(2, MarkerShape.CROSS.bars(3).size());
    }

    @Test
    void theGlassIsTheNearestDyeColour() {
        assertEquals(Material.RED_STAINED_GLASS, Displays.glass(Color.fromRGB(180, 40, 40)));
        assertEquals(Material.PURPLE_STAINED_GLASS, Displays.glass(Color.fromRGB(130, 50, 180)));
    }
}
