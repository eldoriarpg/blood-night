/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph.runtime.world;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Zombie;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.world.WorldMock;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TraceTest {
    private ServerMock server;
    private TracingWorld world;

    /**
     * A world whose ray traces give a set result and remember how they were asked, since MockBukkit traces nothing.
     */
    private static final class TracingWorld extends WorldMock {
        private RayTraceResult result;
        private final List<String> asked = new ArrayList<>();
        private Predicate<? super Entity> filter = entity -> true;

        @Override
        public RayTraceResult rayTrace(
                Location start,
                Vector direction,
                double maxDistance,
                FluidCollisionMode fluids,
                boolean ignorePassable,
                double raySize,
                Predicate<? super Entity> filter) {
            asked.add("both");
            this.filter = filter;
            return result;
        }

        @Override
        public RayTraceResult rayTraceBlocks(
                Location start,
                Vector direction,
                double maxDistance,
                FluidCollisionMode fluids,
                boolean ignorePassable) {
            asked.add("blocks");
            return result;
        }

        @Override
        public RayTraceResult rayTraceEntities(
                Location start, Vector direction, double maxDistance, Predicate<? super Entity> filter) {
            asked.add("entities");
            this.filter = filter;
            return result;
        }
    }

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        world = new TracingWorld();
        server.addWorld(world);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void aHitTellsWhatAndHowFar() {
        Zombie zombie = world.spawn(new Location(world, 5, 64, 0), Zombie.class);
        world.result = new RayTraceResult(new Vector(5, 65, 0), zombie, null);

        Trace trace = Trace.of(new Location(world, 1, 65, 0), new Vector(2, 0, 0), 10, null, TraceHits.BOTH);

        assertTrue(trace.hit());
        assertSame(zombie, trace.entity());
        assertNull(trace.block());
        assertEquals(new Location(world, 5, 65, 0), trace.location());
        assertEquals(4, trace.distance());
    }

    @Test
    void aMissGoesTheFullDistance() {
        Trace trace = Trace.of(new Location(world, 0, 65, 0), new Vector(0, 0, 1), 8, null, TraceHits.BLOCKS);

        assertFalse(trace.hit());
        assertNull(trace.location());
        assertEquals(8, trace.distance());
        assertEquals(List.of("blocks"), world.asked);
    }

    @Test
    void theIgnoredEntityIsPassedThrough() {
        Zombie self = world.spawn(new Location(world, 0, 64, 0), Zombie.class);
        Zombie other = world.spawn(new Location(world, 3, 64, 0), Zombie.class);

        Trace.of(new Location(world, 0, 65, 0), new Vector(1, 0, 0), 8, self, TraceHits.ENTITIES);

        assertEquals(List.of("entities"), world.asked);
        assertFalse(world.filter.test(self));
        assertTrue(world.filter.test(other));
    }

    @Test
    void noDirectionHitsNothing() {
        Trace trace = Trace.of(new Location(world, 0, 65, 0), new Vector(), 8, null, TraceHits.BOTH);

        assertFalse(trace.hit());
        assertEquals(List.of(), world.asked);
    }
}
