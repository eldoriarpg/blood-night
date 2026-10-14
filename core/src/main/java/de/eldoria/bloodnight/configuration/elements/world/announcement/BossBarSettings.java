/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.configuration.elements.world.announcement;

import net.kyori.adventure.bossbar.BossBar;

import java.util.EnumSet;
import java.util.Set;

/**
 * The boss bar shown during a blood night; its progress is the share of the night left. {@code title} is MiniMessage;
 * {@code <name>} is the night's name in each player's language: blood night, Blood Tide in the Nether, Blood Eclipse
 * in the End.
 */
public class BossBarSettings {
    private boolean enabled = true;
    private String title = "<red><bold><name>";
    private BossBar.Color color = BossBar.Color.RED;
    private BossBar.Overlay overlay = BossBar.Overlay.PROGRESS;
    private Set<BossBar.Flag> flags = EnumSet.of(BossBar.Flag.CREATE_WORLD_FOG, BossBar.Flag.DARKEN_SCREEN);

    public boolean enabled() {
        return enabled;
    }

    public String title() {
        return title;
    }

    public BossBar.Color color() {
        return color;
    }

    public BossBar.Overlay overlay() {
        return overlay;
    }

    public Set<BossBar.Flag> flags() {
        return flags;
    }
}
