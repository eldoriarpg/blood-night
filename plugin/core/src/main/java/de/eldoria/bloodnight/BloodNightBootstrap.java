/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * Registers what has to exist before the worlds load: the data pack in the jar's {@code datapack} folder, holding the
 * blood night world clocks and their timelines, the damage type {@code bloodnight:blood_curse}, the enchantments and
 * the reward loot table. Clients receive the clocks and timelines when they join, so a blood night can change the sky
 * by moving a clock.
 */
public final class BloodNightBootstrap implements PluginBootstrap {
    @Override
    public void bootstrap(BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.DATAPACK_DISCOVERY.newHandler(event -> {
            try {
                event.registrar()
                        .discoverPack(
                                Objects.requireNonNull(getClass().getResource("/datapack"))
                                        .toURI(),
                                "data",
                                pack -> pack.title(Component.text("BloodNight")).autoEnableOnServerStart(true));
            } catch (URISyntaxException | IOException e) {
                context.getLogger().error("The BloodNight data pack could not be registered", e);
            }
        }));
    }
}
