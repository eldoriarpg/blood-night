/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.metrics;

import de.eldoria.bloodnight.configuration.Configuration;
import de.eldoria.bloodnight.configuration.elements.EditorSettings;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.AdvancedPie;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Sends anonymous usage numbers to bStats. Server owners switch bStats off for all plugins in
 * {@code plugins/bStats/config.yml}. The charts need to exist on bStats with the same ids.
 */
public final class BloodNightMetrics {
    /**
     * BloodNight's plugin id on bStats.
     */
    public static final int PLUGIN_ID = 34688;

    private BloodNightMetrics() {}

    /**
     * Starts sending.
     *
     * @return the running metrics, to shut down when the plugin disables
     */
    public static Metrics start(JavaPlugin plugin, Configuration configuration) {
        Metrics metrics = new Metrics(plugin, PLUGIN_ID);
        metrics.addCustomChart(new AdvancedPie(
                "night_selection",
                () -> MetricsData.nightSelections(plugin.getServer().getWorlds().stream()
                        .map(configuration::worldConfig)
                        .toList())));
        metrics.addCustomChart(new SingleLineChart(
                "custom_mobs", () -> configuration.mobs().all().size()));
        metrics.addCustomChart(new AdvancedPie(
                "mob_origin", () -> MetricsData.mobOrigins(configuration.mobs().all())));
        metrics.addCustomChart(new AdvancedPie(
                "integrations",
                () -> MetricsData.integrations(
                        name -> plugin.getServer().getPluginManager().getPlugin(name) != null)));
        metrics.addCustomChart(new SimplePie(
                "editor_service",
                () -> MetricsData.editorService(configuration.main().editor().url(), new EditorSettings().url())));
        metrics.addCustomChart(new SimplePie(
                "store_auto_update", () -> configuration.main().store().autoUpdate() ? "on" : "off"));
        metrics.addCustomChart(
                new SingleLineChart("blood_night_worlds", () -> (int) plugin.getServer().getWorlds().stream()
                        .map(configuration::worldConfig)
                        .filter(settings -> settings.enabled())
                        .count()));
        return metrics;
    }
}
