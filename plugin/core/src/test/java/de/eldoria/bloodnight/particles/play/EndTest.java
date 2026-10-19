/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles.play;

import de.eldoria.bloodnight.particles.effect.Anchor;
import de.eldoria.bloodnight.particles.geometry.Vec3;
import org.bukkit.Location;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Zombie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Points on the body of an end: the head of a body made of parts, and the point an anchor aims at on the other end.
 */
class EndTest {
    private ServerMock server;
    private WorldMock world;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = server.addSimpleWorld("world");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void theHeadOfABodyOfPartsIsItsSmallestPart() {
        Zombie body = world.spawn(new Location(world, 0, 64, 0), Zombie.class);
        Chicken head = world.spawn(new Location(world, 5, 70, 2), Chicken.class);

        Vec3 point = End.head(List.of(body, head));

        assertEquals(new Vec3(5, 70 + head.getHeight() / 2, 2), point);
    }

    @Test
    void anAnchorAimsAtItsOwnPointUnlessToldOtherwise() {
        assertEquals(Anchor.BodyPoint.EYES, new Anchor(Anchor.BodyPoint.EYES, null, null, null).aimed());
        assertEquals(
                Anchor.BodyPoint.CENTER,
                new Anchor(Anchor.BodyPoint.EYES, null, null, Anchor.BodyPoint.CENTER).aimed());
    }
}
