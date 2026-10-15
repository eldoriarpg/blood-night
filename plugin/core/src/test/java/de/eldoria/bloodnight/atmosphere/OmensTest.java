/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.atmosphere;

import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Wolf;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OmensTest extends AtmosphereTest {
    private final List<String> commands = new ArrayList<>();
    private final List<Entity> running = new ArrayList<>();
    private long until = -1;
    private Omens omens;
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        Timelines timelines = new Timelines(nights, ignored -> EnumSet.of(TimelineGroup.OMEN), commands::add);
        omens = new Omens(
                server,
                ignored -> until,
                ignored -> atmosphere,
                timelines,
                (entity, work) -> work.run(),
                () -> 0L,
                (mob, to) -> running.add(mob));
        player = server.addPlayer();
        player.teleport(world.getSpawnLocation());
    }

    private String omenAt(long ticks) {
        return "execute in %s run minecraft:time of bloodnight:omen set %d"
                .formatted(world.getKey().asString(), ticks);
    }

    @Test
    void theOmenStartsOnceBeforeTheNightAndCatchesUpWhenLate() {
        until = 1000;
        omens.tick();
        world.setFullTime(world.getFullTime() + Omens.STEP);
        until -= Omens.STEP;
        omens.tick();

        assertEquals(List.of(omenAt(TimelineGroup.OMEN_TICKS - 1000)), commands);
    }

    @Test
    void nothingHappensWithoutABloodNightComingSoon() {
        until = TimelineGroup.OMEN_TICKS + 1;
        omens.tick();
        until = -1;
        omens.tick();

        assertTrue(commands.isEmpty());
        assertTrue(player.getHeardSounds().isEmpty());
    }

    @Test
    void playersHearDistantSounds() {
        until = 500;
        omens.tick();

        assertFalse(player.getHeardSounds().isEmpty());
    }

    @Test
    void switchedOffTheyHearNothing() {
        atmosphere = MAPPER.readValue("{\"omenSounds\":false,\"restlessAnimals\":false}", atmosphere.getClass());
        until = 500;
        omens.tick();

        assertTrue(player.getHeardSounds().isEmpty());
    }

    @Test
    void animalsBoltButTamedOnesStayCalm() {
        Cow cow = world.spawn(player.getLocation().add(4, 0, 0), Cow.class);
        Wolf wolf = world.spawn(player.getLocation().add(-4, 0, 0), Wolf.class);
        wolf.setOwner(player);

        omens.bolt(player);

        assertEquals(List.of(cow), running);
    }
}
