/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph.runtime.world;

import de.eldoria.bloodnight.graph.runtime.Displays;
import org.bukkit.Color;
import org.bukkit.Material;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    void aCircleIsARingOfBarsAlongItsEdge() {
        double radius = 4;
        List<MarkerShape.Bar> bars = MarkerShape.CIRCLE.bars(radius);
        assertEquals(42, bars.size());
        for (MarkerShape.Bar bar : bars) {
            double centreX = bar.x() + bar.width() / 2 * Math.cos(bar.yaw()) + bar.depth() / 2 * Math.sin(bar.yaw());
            double centreZ = bar.z() - bar.width() / 2 * Math.sin(bar.yaw()) + bar.depth() / 2 * Math.cos(bar.yaw());
            assertEquals(radius, Math.hypot(centreX, centreZ), 1e-9);
            double along = Math.cos(bar.yaw()) * centreX - Math.sin(bar.yaw()) * centreZ;
            assertEquals(0, along, 1e-9);
        }
        double circumference = 2 * Math.PI * radius;
        assertTrue(bars.stream().mapToDouble(MarkerShape.Bar::width).sum() > circumference);
    }

    @Test
    void smallAndLargeCirclesKeepASensibleNumberOfBars() {
        assertEquals(16, MarkerShape.CIRCLE.bars(0.5).size());
        assertEquals(48, MarkerShape.CIRCLE.bars(30).size());
    }

    @Test
    void theGlassIsTheNearestDyeColour() {
        Assertions.assertEquals(Material.RED_STAINED_GLASS, Displays.glass(Color.fromRGB(180, 40, 40)));
        assertEquals(Material.PURPLE_STAINED_GLASS, Displays.glass(Color.fromRGB(130, 50, 180)));
    }
}
