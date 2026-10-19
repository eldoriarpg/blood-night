/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.debug;

import de.eldoria.bloodnight.BloodNight;
import de.eldoria.bloodnight.configuration.world.WorldSettings;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.MobOrigin;
import de.eldoria.eldoutilities.debug.DebugDataProvider;
import de.eldoria.eldoutilities.debug.DebugSettings;
import de.eldoria.eldoutilities.debug.HealthCheck;
import de.eldoria.eldoutilities.debug.data.EntryData;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.stream.Stream;

/**
 * What a debug report on Lyna tells about BloodNight beyond the server and the log: the worlds and their blood nights,
 * the installed mobs and their problems, the plugins it works with and the editor, and checks of its own setup.
 * <p>
 * The report carries {@code config.yml}, {@code loot_tables.yml}, {@code particle_effects.yml} and the world files. The mob, item and macro files
 * stay home, since graphs are large and the editor shows them, and so does {@code editor-session.json}, which holds a
 * session's token.
 */
public final class DebugReport implements DebugDataProvider {
    private static final List<String> INTEGRATIONS = List.of(
            "PlaceholderAPI", "MiniPlaceholders", "WorldGuard", "MythicMobs", "Vault", "Nexo", "Oraxen", "ItemsAdder");

    private final BloodNight plugin;

    /**
     * @param plugin the plugin reported on
     */
    public DebugReport(BloodNight plugin) {
        this.plugin = plugin;
    }

    /**
     * @return where reports go and what they carry: the default filters and BloodNight's settings files
     */
    public static DebugSettings settings() {
        return DebugSettings.builder()
                .withDefaultFilter()
                .withConfigFiles(DebugReport::settingsFiles)
                .build();
    }

    @Override
    public EntryData[] getDebugInformations() {
        return new EntryData[] {
            new EntryData("Worlds", worlds()),
            new EntryData("Mobs", mobs()),
            new EntryData("Mob problems", mobProblems()),
            new EntryData("Integrations", integrations()),
            new EntryData("Editor", editor())
        };
    }

    @Override
    public List<HealthCheck> getHealthChecks() {
        List<HealthCheck> checks = new ArrayList<>();
        Map<String, String> files = plugin.configuration().problems();
        checks.add(
                files.isEmpty()
                        ? HealthCheck.ok("Settings files", "Every file was read")
                        : HealthCheck.error(
                                "Settings files", "Could not be read: " + String.join(", ", files.keySet())));
        Map<String, String> problems = mobProblems();
        checks.add(
                problems.isEmpty()
                        ? HealthCheck.ok("Mobs", "Every mob compiled and ran without errors")
                        : HealthCheck.warn("Mobs", problems.size() + " mobs have problems, see Mob problems"));
        long enabled = plugin.getServer().getWorlds().stream()
                .filter(world -> settings(world).enabled())
                .count();
        checks.add(
                enabled > 0
                        ? HealthCheck.ok("Worlds", enabled + " worlds have blood nights")
                        : HealthCheck.warn("Worlds", "No world has blood nights; run /bn setup"));
        var pack = plugin.configuration().main().resourcePack();
        checks.add(
                !pack.offer() || !pack.url().isBlank()
                        ? HealthCheck.ok("Resource pack", pack.offer() ? "Offered from " + pack.url() : "Not offered")
                        : HealthCheck.error("Resource pack", "Offered, but without an address"));
        return checks;
    }

    private Map<String, String> worlds() {
        Map<String, String> rows = new LinkedHashMap<>();
        for (World world : plugin.getServer().getWorlds()) {
            WorldSettings settings = settings(world);
            String state = !settings.enabled()
                    ? "off"
                    : "on, " + settings.nightSelection().type()
                            + (plugin.bloodNights().running(world).isPresent() ? ", blood night running" : "");
            rows.put(world.getName() + " (" + world.getEnvironment() + ")", state);
        }
        return rows;
    }

    private Map<String, String> mobs() {
        Map<String, String> rows = new TreeMap<>();
        for (CustomMob mob : plugin.configuration().mobs().all()) {
            MobOrigin from = mob.origin();
            String origin = from == null ? "own" : "store " + from.store() + "/" + from.entry();
            rows.put(mob.id(), mob.attributes().entityType() + ", " + origin);
        }
        return rows;
    }

    private Map<String, String> mobProblems() {
        Map<String, List<String>> problems = new TreeMap<>();
        plugin.behaviours()
                .problems()
                .forEach((mob, list) -> list.forEach(problem ->
                        problems.computeIfAbsent(mob, key -> new ArrayList<>()).add(problem.toString())));
        plugin.coordinator().failures().forEach((mob, list) -> problems.computeIfAbsent(mob, key -> new ArrayList<>())
                .addAll(list));
        Map<String, String> rows = new TreeMap<>();
        problems.forEach((mob, list) -> rows.put(mob, String.join("\n", list)));
        return rows;
    }

    private Map<String, String> integrations() {
        Map<String, String> rows = new LinkedHashMap<>();
        for (String name : INTEGRATIONS) {
            Plugin other = plugin.getServer().getPluginManager().getPlugin(name);
            if (other == null) continue;
            rows.put(name, other.getPluginMeta().getVersion() + (other.isEnabled() ? "" : " (disabled)"));
        }
        if (rows.isEmpty()) rows.put("None", "");
        return rows;
    }

    private Map<String, String> editor() {
        Map<String, String> rows = new LinkedHashMap<>();
        rows.put("Address", plugin.configuration().main().editor().url());
        rows.put("Session open", String.valueOf(Files.exists(dataFolder(plugin).resolve("editor-session.json"))));
        return rows;
    }

    private WorldSettings settings(World world) {
        return plugin.configuration().worldConfig(world);
    }

    private static List<Path> settingsFiles(Plugin plugin) {
        Path folder = dataFolder(plugin);
        List<Path> files = new ArrayList<>();
        for (String name : List.of("config.yml", "loot_tables.yml", "particle_effects.yml")) {
            Path file = folder.resolve(name);
            if (Files.isRegularFile(file)) files.add(file);
        }
        Path worlds = folder.resolve("worlds");
        if (Files.isDirectory(worlds)) {
            try (Stream<Path> entries = Files.list(worlds)) {
                Set<Path> sorted = new TreeSet<>();
                entries.filter(path -> path.toString().endsWith(".yml")).forEach(sorted::add);
                files.addAll(sorted);
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Could not list the world files for a debug report.", e);
            }
        }
        return files;
    }

    private static Path dataFolder(Plugin plugin) {
        return plugin.getDataFolder().toPath();
    }
}
