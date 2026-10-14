/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.atmosphere;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BloodSkyTest extends AtmosphereTest {
    private final List<String> commands = new ArrayList<>();
    private BloodSky sky;

    @BeforeEach
    void setUp() {
        sky = new BloodSky(nights, ignored -> atmosphere, commands::add);
        server.getPluginManager().registerEvents(sky, plugin);
    }

    private String set(long ticks) {
        return "execute in %s run time of bloodnight:blood_night set %d"
                .formatted(world.getKey().asString(), ticks);
    }

    @Test
    void theSkyTurnsRedForTheNightAndBackAfter() {
        bloodNight();
        day();

        assertEquals(List.of(set(0), set(BloodSky.FADE_OUT)), commands);
    }

    @Test
    void worldsWithoutTheRedSkyKeepTheirs() {
        atmosphere = MAPPER.readValue("{\"sky\":false}", atmosphere.getClass());
        bloodNight();
        day();

        assertTrue(commands.isEmpty());
    }

    @Test
    void restartsTakeTheRunningNightOver() {
        bloodNight();
        commands.clear();

        sky.restore(List.of(world));
        sky.clear(List.of(world));

        assertEquals(
                List.of(
                        set(BloodSky.FULL),
                        set(BloodSky.REST).replace("set " + BloodSky.REST, "resume"),
                        set(BloodSky.REST)),
                commands);
    }

    @Test
    void theDataPackBlendsOnTheClockTimesThePluginSets() throws IOException {
        JsonNode timeline = read("/datapack/data/bloodnight/timeline/blood_night.json");
        assertEquals(BloodSky.CLOCK, timeline.path("clock").asString());
        for (JsonNode track : timeline.path("tracks")) {
            List<Long> ticks = new ArrayList<>();
            track.path("keyframes")
                    .forEach(keyframe -> ticks.add(keyframe.path("ticks").asLong()));
            assertEquals(List.of(0L, BloodSky.FULL, BloodSky.FADE_OUT, BloodSky.REST), ticks);
        }
        JsonNode tag = read("/datapack/data/minecraft/tags/timeline/in_overworld.json");
        assertEquals("bloodnight:blood_night", tag.path("values").get(0).asString());
    }

    private static JsonNode read(String resource) throws IOException {
        try (InputStream in = Objects.requireNonNull(BloodSkyTest.class.getResourceAsStream(resource))) {
            return MAPPER.readTree(in);
        }
    }
}
