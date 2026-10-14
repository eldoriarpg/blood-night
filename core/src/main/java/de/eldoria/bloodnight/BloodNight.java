/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight;

import de.eldoria.bloodnight.announce.Announcer;
import de.eldoria.bloodnight.announce.Messages;
import de.eldoria.bloodnight.behaviour.BehaviourService;
import de.eldoria.bloodnight.behaviour.Behaviours;
import de.eldoria.bloodnight.behaviour.BytecodeCache;
import de.eldoria.bloodnight.command.BloodNightCommandBrigadier;
import de.eldoria.bloodnight.command.MobIds;
import de.eldoria.bloodnight.configuration.Configuration;
import de.eldoria.bloodnight.editor.RuntimeCatalog;
import de.eldoria.bloodnight.graph.BloodNightPlatform;
import de.eldoria.bloodnight.graph.CompileClasspath;
import de.eldoria.bloodnight.graph.TriggerDispatcher;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.defaults.DefaultMobs;
import de.eldoria.bloodnight.mobs.DamageHandler;
import de.eldoria.bloodnight.mobs.DropHandler;
import de.eldoria.bloodnight.mobs.MobCoordinator;
import de.eldoria.bloodnight.mobs.MobLifecycle;
import de.eldoria.bloodnight.mobs.MobOutfitter;
import de.eldoria.bloodnight.mobs.MobSpawner;
import de.eldoria.bloodnight.mobs.NightDespawner;
import de.eldoria.bloodnight.mobs.TargetPolicy;
import de.eldoria.bloodnight.night.BloodNights;
import de.eldoria.bloodnight.night.NightCommands;
import de.eldoria.bloodnight.night.NightSelector;
import de.eldoria.bloodnight.night.NightSkipGuard;
import de.eldoria.bloodnight.night.NightStretch;
import de.eldoria.bloodnight.night.NightTimes;
import de.eldoria.bloodnight.rules.CommandBlocker;
import de.eldoria.bloodnight.rules.CreeperGuard;
import de.eldoria.bloodnight.rules.DeathActionHandler;
import de.eldoria.bloodnight.rules.MobDamage;
import de.eldoria.bloodnight.rules.PhantomForce;
import de.eldoria.graph.payload.Catalog;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.random.RandomGenerator;

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
    private BukkitTask stretchTask;
    private BukkitTask despawnTask;
    private NightDespawner despawner;
    private NightStretch stretch;
    private BukkitTask announceTask;
    private Announcer announcer;
    private PhantomForce phantoms;
    private final Messages messages = new Messages();
    private NightSelector selector;
    private BloodNights bloodNights;
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
                new MobOutfitter(
                        () -> configuration.items(),
                        world -> configuration.worldConfig(world).mobSettings()),
                () -> configuration.items(),
                id -> configuration.mobs().get(id),
                getLogger());
        dispatcher = new TriggerDispatcher(
                coordinator::behaviour, coordinator::extensionBehaviour, (trigger, entity, error) -> {
                    if (entity != null) {
                        coordinator.failed(trigger.id(), entity, error);
                    } else {
                        getLogger().log(Level.WARNING, "Trigger %s failed".formatted(trigger.id()), error);
                    }
                });

        var plugins = getServer().getPluginManager();
        plugins.registerEvents(new DamageHandler(), this);
        plugins.registerEvents(new TargetPolicy(), this);
        plugins.registerEvents(new MobLifecycle(coordinator), this);
        plugins.registerEvents(
                new DropHandler(
                        id -> configuration.mobs().get(id),
                        world -> configuration.worldConfig(world).worldDrops(),
                        () -> configuration.globalDrops(),
                        id -> configuration.items().item(id),
                        RandomGenerator.getDefault()),
                this);
        getServer().getWorlds().forEach(world -> world.getLivingEntities().forEach(coordinator::attach));
        tickTask = getServer().getScheduler().runTaskTimer(this, coordinator::tick, 1, 1);
        MobIds.use(() -> configuration.mobs().all().stream().map(CustomMob::id).toList());
        getLifecycleManager()
                .registerEventHandler(LifecycleEvents.COMMANDS.newHandler(
                        event -> BloodNightCommandBrigadier.register(event.registrar(), this)));
        selector = new NightSelector(
                world -> configuration.worldConfig(world).nightSelection(),
                world -> NightTimes.of(configuration.worldConfig(world).nightSettings()),
                Clock.systemUTC(),
                () -> getServer().getServerTickManager().getTickRate());
        bloodNights = new BloodNights(
                selector,
                world -> configuration.worldConfig(world).enabled(),
                getServer().getPluginManager(),
                coordinator::endNight,
                getLogger());
        plugins.registerEvents(
                new MobSpawner(
                        () -> configuration.mobs().all(),
                        configuration::worldConfig,
                        coordinator,
                        bloodNights,
                        this,
                        RandomGenerator.getDefault()),
                this);
        despawner = new NightDespawner(coordinator, bloodNights);
        despawnTask = getServer().getScheduler().runTaskTimer(this, despawner::tick, 1, 1);
        stretch = new NightStretch(
                getServer(),
                world -> configuration.worldConfig(world).nightSettings(),
                selector::times,
                selector::seed);
        plugins.registerEvents(
                new NightSkipGuard(
                        bloodNights,
                        world ->
                                configuration.worldConfig(world).nightSettings().skippable()),
                this);
        plugins.registerEvents(
                new NightCommands(
                        getServer(), world -> configuration.worldConfig(world).nightSettings()),
                this);
        plugins.registerEvents(stretch, this);
        messages.register(configuration.main().announcements().fallbackLocale());
        announcer = new Announcer(
                getServer(),
                bloodNights,
                () -> configuration.main().announcements(),
                configuration::worldConfig,
                selector::times);
        plugins.registerEvents(announcer, this);
        phantoms = new PhantomForce(
                bloodNights,
                world -> configuration.worldConfig(world).mobSettings().forcePhantoms());
        plugins.registerEvents(phantoms, this);
        plugins.registerEvents(new CreeperGuard(bloodNights, configuration::worldConfig), this);
        plugins.registerEvents(
                new MobDamage(
                        bloodNights, world -> configuration.worldConfig(world).mobSettings()),
                this);
        plugins.registerEvents(
                new CommandBlocker(bloodNights, getServer().getCommandMap(), world -> configuration
                        .worldConfig(world)
                        .nightSettings()
                        .blockedCommands()),
                this);
        plugins.registerEvents(
                new DeathActionHandler(this, bloodNights, configuration::worldConfig, RandomGenerator.getDefault()),
                this);
        announceTask = getServer().getScheduler().runTaskTimer(this, announcer::tick, 20, 20);
        stretchTask = getServer().getScheduler().runTaskTimer(this, stretch::tick, 1, 1);
        nightTask = getServer()
                .getScheduler()
                .runTaskTimer(
                        this,
                        () -> {
                            bloodNights.check(getServer().getWorlds());
                            despawner.sweep();
                        },
                        20,
                        20);
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
        MobIds.clear();
        if (tickTask != null) tickTask.cancel();
        if (nightTask != null) nightTask.cancel();
        if (stretchTask != null) stretchTask.cancel();
        if (despawnTask != null) despawnTask.cancel();
        if (stretch != null) stretch.clear();
        if (announceTask != null) announceTask.cancel();
        if (bloodNights != null) bloodNights.endAll(getServer().getWorlds());
        if (announcer != null) announcer.clear();
        if (phantoms != null) phantoms.restoreAll(getServer().getOnlinePlayers());
        messages.unregister();
        if (dispatcher != null) dispatcher.unregister();
        if (behaviourService != null) behaviourService.close();
    }

    /**
     * Compiles every mob in the background and switches living mobs over on the main thread.
     *
     * @return completes on the main thread once the new behaviours are active
     */
    public CompletableFuture<Behaviours> recompile() {
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

    /**
     * @return the configuration files
     */
    public Configuration configuration() {
        return configuration;
    }

    /**
     * @return the living custom mobs
     */
    public MobCoordinator coordinator() {
        return coordinator;
    }

    /**
     * @return the behaviours of the last compilation
     */
    public Behaviours behaviours() {
        return behaviours;
    }

    /**
     * @return the editor catalog of this server
     */
    public Catalog catalog() {
        return catalog;
    }

    /**
     * @return decides which nights are blood nights
     */
    public NightSelector selector() {
        return selector;
    }

    /**
     * @return the blood nights running in the worlds
     */
    public BloodNights bloodNights() {
        return bloodNights;
    }

    /**
     * @return the plugin's translated texts
     */
    public Messages messages() {
        return messages;
    }
}
