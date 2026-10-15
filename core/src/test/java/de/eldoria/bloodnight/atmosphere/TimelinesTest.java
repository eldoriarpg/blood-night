/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.atmosphere;

import de.eldoria.bloodnight.configuration.elements.world.NightRuleSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TimelinesTest extends AtmosphereTest {
    private final List<String> commands = new ArrayList<>();
    private Set<TimelineGroup> groups = EnumSet.of(TimelineGroup.SKY);
    private Timelines timelines;

    @BeforeEach
    void setUp() {
        timelines = new Timelines(nights, ignored -> groups, commands::add);
        server.getPluginManager().registerEvents(timelines, plugin);
    }

    private String set(TimelineGroup group, long ticks) {
        return command(group, "set " + ticks);
    }

    private String command(TimelineGroup group, String change) {
        return "execute in %s run minecraft:time of %s %s"
                .formatted(world.getKey().asString(), group.key(), change);
    }

    @Test
    void theSkyTurnsRedForTheNightAndBackAfter() {
        bloodNight();
        day();

        assertEquals(List.of(set(TimelineGroup.SKY, 0), set(TimelineGroup.SKY, TimelineGroup.FADE_OUT)), commands);
    }

    @Test
    void theRedDawnStartsWhenTheNightEnds() {
        groups = EnumSet.of(TimelineGroup.SKY, TimelineGroup.RED_DAWN);
        bloodNight();
        commands.clear();
        day();

        assertEquals(List.of(set(TimelineGroup.SKY, TimelineGroup.FADE_OUT), set(TimelineGroup.RED_DAWN, 0)), commands);
    }

    @Test
    void worldsWithEveryGroupOffKeepTheirClocksAtRest() {
        groups = Set.of();
        bloodNight();
        day();

        assertTrue(commands.isEmpty());
    }

    @Test
    void restartsTakeTheRunningNightOver() {
        bloodNight();
        commands.clear();

        timelines.restore(List.of(world));
        timelines.clear(List.of(world), true);

        List<String> expected = new ArrayList<>();
        for (TimelineGroup group : TimelineGroup.values()) {
            expected.add(set(group, group == TimelineGroup.SKY ? TimelineGroup.FULL : group.rest()));
            expected.add(command(group, "resume"));
        }
        expected.add(set(TimelineGroup.SKY, TimelineGroup.SKY.rest()));
        assertEquals(expected, commands);
    }

    @Test
    void settingsSwitchTheGroups() {
        NightRuleSettings rules = MAPPER.readValue(
                "{\"monstersDontBurn\":true,\"creakingsWake\":false,\"raids\":\"DENY\"}", NightRuleSettings.class);
        atmosphere = MAPPER.readValue("{\"sky\":false,\"darkness\":true}", atmosphere.getClass());

        assertEquals(
                EnumSet.of(
                        TimelineGroup.RED_DAWN,
                        TimelineGroup.DARKNESS,
                        TimelineGroup.WORLD_PARTICLES,
                        TimelineGroup.MONSTERS_DONT_BURN,
                        TimelineGroup.RAIDS_DENIED,
                        TimelineGroup.OMEN),
                TimelineGroup.of(atmosphere, rules));
    }

    @Test
    void everyTimelineRestsWhereItsGroupSaysAndChangesNothingThere() throws IOException {
        for (TimelineGroup group : TimelineGroup.values()) {
            String name = group.key().substring("bloodnight:".length());
            assertNotNull(
                    TimelinesTest.class.getResource("/datapack/data/bloodnight/world_clock/%s.json".formatted(name)));
            JsonNode timeline = read("/datapack/data/bloodnight/timeline/%s.json".formatted(name));
            assertEquals(group.key(), timeline.path("clock").asString());
            for (var track : timeline.path("tracks").properties()) {
                JsonNode keyframes = track.getValue().path("keyframes");
                JsonNode first = keyframes.get(0);
                JsonNode last = keyframes.get(keyframes.size() - 1);
                String where = group + " " + track.getKey();
                assertNotEquals("override", track.getValue().path("modifier").asString("override"), where);
                assertEquals(0, first.path("ticks").asInt(), where);
                assertEquals(group.rest(), last.path("ticks").asInt(), where);
                assertEquals(first.path("value"), last.path("value"), where);
                if (group.start() == TimelineGroup.Start.NIGHT) {
                    assertNotEquals(first.path("value"), value(keyframes, TimelineGroup.FULL), where);
                    assertNotEquals(first.path("value"), value(keyframes, TimelineGroup.FADE_OUT), where);
                }
            }
        }
        for (String dimension : List.of("in_overworld", "in_nether", "in_end")) {
            JsonNode tag = read("/datapack/data/minecraft/tags/timeline/%s.json".formatted(dimension));
            List<String> values = new ArrayList<>();
            tag.path("values").forEach(value -> values.add(value.asString()));
            assertEquals(
                    EnumSet.allOf(TimelineGroup.class).stream()
                            .map(TimelineGroup::key)
                            .toList(),
                    values,
                    dimension);
        }
    }

    /**
     * @return the value of the last keyframe at or before the ticks, which is what the track holds there for the
     *     keyframes these tests look at
     */
    private static JsonNode value(JsonNode keyframes, int ticks) {
        JsonNode value = null;
        for (JsonNode keyframe : keyframes) {
            if (keyframe.path("ticks").asInt() <= ticks) value = keyframe.path("value");
        }
        return value;
    }

    private static JsonNode read(String resource) throws IOException {
        try (InputStream in = Objects.requireNonNull(TimelinesTest.class.getResourceAsStream(resource))) {
            return MAPPER.readTree(in);
        }
    }
}
