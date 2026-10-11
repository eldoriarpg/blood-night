/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight;

import de.eldoria.bloodnight.behaviour.BehaviourService;
import de.eldoria.bloodnight.behaviour.Behaviours;
import de.eldoria.bloodnight.behaviour.BytecodeCache;
import de.eldoria.bloodnight.configuration.Configuration;
import de.eldoria.bloodnight.editor.EditorPayload;
import de.eldoria.bloodnight.editor.RuntimeCatalog;
import de.eldoria.bloodnight.graph.BloodNightPlatform;
import de.eldoria.bloodnight.graph.CompileClasspath;
import de.eldoria.bloodnight.graph.TriggerDispatcher;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.defaults.DefaultMobs;
import de.eldoria.bloodnight.mobs.DamageHandler;
import de.eldoria.bloodnight.mobs.MobCoordinator;
import de.eldoria.bloodnight.mobs.MobLifecycle;
import de.eldoria.bloodnight.mobs.MobOutfitter;
import de.eldoria.bloodnight.mobs.MobSpawner;
import de.eldoria.bloodnight.mobs.NightWatch;
import de.eldoria.bloodnight.mobs.TargetPolicy;
import de.eldoria.graph.compiler.GraphProblem;
import de.eldoria.graph.payload.Catalog;
import de.eldoria.graph.payload.CatalogGenerator;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Plugin entry point: loads the configuration, compiles every mob's behaviour in the background, and wires the
 * compiled behaviours to spawned mobs, events and the tick loop.
 * <p>
 * Worlds load before plugins are enabled, so custom mobs already in loaded chunks are taken over on enable; they get
 * their behaviour once compilation finished.
 * <p>
 * {@code /bloodnight reload} reads the configuration again and recompiles; living mobs switch to the new behaviours
 * once compilation finished. {@code source} writes a mob's generated source to {@code debug/}, {@code problems} lists
 * why a mob does not compile, {@code export} writes the editor payload and catalog to {@code debug/}, {@code spawn}
 * and {@code debug} help testing.
 */
public final class BloodNight extends JavaPlugin {
    private Configuration configuration;
    private BehaviourService behaviourService;
    private MobCoordinator coordinator;
    private TriggerDispatcher dispatcher;
    private BukkitTask tickTask;
    private BukkitTask nightTask;
    private Behaviours behaviours = Behaviours.empty();
    private Catalog catalog;

    @Override
    public void onEnable() {
        configuration = new Configuration(this);
        var platform = BloodNightPlatform.create(CompileClasspath.discover(getClassLoader()));
        configuration.seedMobs(() -> DefaultMobs.create(platform));
        long start = System.nanoTime();
        catalog = RuntimeCatalog.create(platform);
        getLogger()
                .info("Editor catalog %s: %d nodes in %d ms"
                        .formatted(catalog.version(), catalog.nodes().size(), (System.nanoTime() - start) / 1_000_000));
        behaviourService = new BehaviourService(
                platform,
                getClassLoader(),
                new BytecodeCache(getDataFolder().toPath().resolve("cache")),
                buildFingerprint(),
                getLogger());
        coordinator = new MobCoordinator(
                new MobOutfitter(() -> configuration.items()),
                () -> configuration.items(),
                id -> configuration.mobs().get(id),
                getLogger());
        dispatcher = new TriggerDispatcher(
                coordinator::behaviour, coordinator::extensionBehaviour, (trigger, error) -> getLogger()
                        .log(Level.WARNING, "Trigger %s failed".formatted(trigger.id()), error));

        var plugins = getServer().getPluginManager();
        plugins.registerEvents(new MobSpawner(configuration, coordinator, this), this);
        plugins.registerEvents(new DamageHandler(), this);
        plugins.registerEvents(new TargetPolicy(), this);
        plugins.registerEvents(new MobLifecycle(coordinator), this);
        getServer().getWorlds().forEach(world -> world.getLivingEntities().forEach(coordinator::attach));
        tickTask = getServer().getScheduler().runTaskTimer(this, coordinator::tick, 1, 1);
        NightWatch nights = new NightWatch(coordinator::endNight);
        nightTask = getServer()
                .getScheduler()
                .runTaskTimer(this, () -> nights.check(getServer().getWorlds()), 20, 20);
        recompile();
    }

    /**
     * Identifies this exact build for the bytecode cache: the version alone would let a rebuilt jar with changed nodes
     * reuse classes generated by the previous build.
     */
    private String buildFingerprint() {
        try {
            Path jar = Path.of(getClass()
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());
            return "%s+%d+%d"
                    .formatted(
                            getPluginMeta().getVersion(),
                            Files.size(jar),
                            Files.getLastModifiedTime(jar).toMillis());
        } catch (IOException | URISyntaxException | RuntimeException e) {
            return getPluginMeta().getVersion();
        }
    }

    @Override
    public void onDisable() {
        if (tickTask != null) tickTask.cancel();
        if (nightTask != null) nightTask.cancel();
        if (dispatcher != null) dispatcher.unregister();
        if (behaviourService != null) behaviourService.close();
    }

    /**
     * Compiles every mob in the background and switches living mobs over on the main thread.
     *
     * @return completes on the main thread once the new behaviours are active
     */
    CompletableFuture<Behaviours> recompile() {
        configuration.mobs();
        configuration.macros();
        configuration.problems().forEach((file, problem) -> getLogger()
                .severe(
                        "%s could not be read and was left untouched; its content is ignored until it is fixed and reloaded: %s"
                                .formatted(file, problem)));
        CompletableFuture<Behaviours> applied = new CompletableFuture<>();
        behaviourService
                .compile(configuration.mobs().all(), configuration.macros().macros())
                .whenComplete((result, error) -> {
                    if (error != null) {
                        getLogger().log(Level.SEVERE, "Compiling behaviours failed", error);
                        applied.completeExceptionally(error);
                        return;
                    }
                    getServer().getScheduler().runTask(this, () -> {
                        behaviours = result;
                        coordinator.rebind(result);
                        dispatcher.register(this, result.usedTriggers());
                        applied.complete(result);
                    });
                });
        return applied;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) return false;
        String mob = args.length > 1 ? args[1] : null;
        switch (args[0]) {
            case "reload" -> reload(sender);
            case "source" -> source(sender, mob);
            case "problems" -> problems(sender, mob);
            case "spawn" -> spawn(sender, mob);
            case "debug" -> sender.sendMessage("Debug trigger fired on %d mobs".formatted(coordinator.debug(mob)));
            case "export" -> export(sender);
            default -> {
                return false;
            }
        }
        return true;
    }

    private void reload(CommandSender sender) {
        configuration.reload();
        sender.sendMessage("Compiling behaviours...");
        configuration.mobs();
        configuration.items();
        configuration.macros();
        configuration
                .problems()
                .keySet()
                .forEach(file -> sender.sendMessage("%s could not be read, see the console".formatted(file)));
        recompile()
                .thenAccept(result -> sender.sendMessage("Compiled %d mobs (%d from cache), %d with problems"
                        .formatted(
                                result.compiled().size(),
                                result.fromCache(),
                                result.problems().size())));
    }

    private void source(CommandSender sender, String mob) {
        if (mob == null) {
            sender.sendMessage("Usage: /bloodnight source <mob>");
            return;
        }
        behaviours
                .of(mob)
                .ifPresentOrElse(
                        compiled -> {
                            try {
                                Path directory = getDataFolder().toPath().resolve("debug");
                                Files.createDirectories(directory);
                                Path file = directory.resolve(mob + ".java");
                                Files.writeString(file, compiled.source());
                                sender.sendMessage("Source of %s written to %s".formatted(mob, file));
                            } catch (IOException e) {
                                sender.sendMessage("Could not write source: " + e.getMessage());
                            }
                        },
                        () -> sender.sendMessage("Mob %s has no compiled behaviour".formatted(mob)));
    }

    private void export(CommandSender sender) {
        try {
            Path directory = getDataFolder().toPath().resolve("debug");
            Files.createDirectories(directory);
            Files.writeString(
                    directory.resolve("payload.json"),
                    EditorPayload.of(configuration, behaviours, catalog.version())
                            .asJson());
            Files.writeString(directory.resolve("catalog.json"), CatalogGenerator.write(catalog));
            sender.sendMessage("Editor payload and catalog written to " + directory);
        } catch (IOException e) {
            sender.sendMessage("Could not write the editor payload: " + e.getMessage());
        }
    }

    private void problems(CommandSender sender, String mob) {
        if (mob == null) {
            sender.sendMessage("Mobs with problems: "
                    + String.join(", ", behaviours.problems().keySet()));
            return;
        }
        List<GraphProblem> problems = behaviours.problems().getOrDefault(mob, List.of());
        if (problems.isEmpty()) sender.sendMessage("Mob %s has no problems".formatted(mob));
        problems.forEach(problem -> sender.sendMessage(problem.toString()));
    }

    private void spawn(CommandSender sender, String mob) {
        CustomMob definition = mob == null ? null : configuration.mobs().get(mob);
        if (definition == null) {
            sender.sendMessage("Unknown mob " + mob);
            return;
        }
        Location location = sender instanceof Player player
                ? player.getLocation()
                : getServer().getWorlds().getFirst().getSpawnLocation();
        Entity entity = location.getWorld()
                .spawnEntity(location, definition.attributes().entityType());
        if (!(entity instanceof LivingEntity living)) {
            entity.remove();
            sender.sendMessage("Mob %s is not a living entity type".formatted(mob));
            return;
        }
        coordinator.register(living, definition);
        sender.sendMessage("Spawned %s at %d %d %d"
                .formatted(mob, location.getBlockX(), location.getBlockY(), location.getBlockZ()));
    }
}
