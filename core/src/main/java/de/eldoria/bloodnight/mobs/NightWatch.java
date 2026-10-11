/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs;

import org.bukkit.World;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Ends the night of a world when its time leaves the night, at sunrise or by a jump in time.
 * <p>
 * Night is tick 13000 up to 23000 of the day, when monsters spawn in the open. A world is only seen ending its night
 * after it was seen in one, so starting the server in the morning ends nothing. Worlds without a day cycle never
 * change. Stands in until a blood night cycle ends nights itself.
 */
public final class NightWatch {
    private static final long NIGHT_START = 13_000;
    private static final long NIGHT_END = 23_000;

    private final Set<UUID> inNight = new HashSet<>();
    private final Consumer<World> end;

    /**
     * @param end called with every world whose night just ended
     */
    public NightWatch(Consumer<World> end) {
        this.end = end;
    }

    /**
     * Looks at the time of each world and ends the night of those that left it since the last check.
     *
     * @param worlds the worlds to look at
     */
    public void check(Collection<? extends World> worlds) {
        for (World world : worlds) {
            if (isNight(world.getTime())) {
                inNight.add(world.getUID());
            } else if (inNight.remove(world.getUID())) {
                end.accept(world);
            }
        }
    }

    /**
     * @param time time of day in ticks
     * @return whether it is night at that time
     */
    static boolean isNight(long time) {
        long ofDay = Math.floorMod(time, 24_000L);
        return ofDay >= NIGHT_START && ofDay < NIGHT_END;
    }
}
