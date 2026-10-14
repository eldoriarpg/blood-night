/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight;

import de.eldoria.bloodnight.announce.Announcer;
import de.eldoria.bloodnight.announce.Messages;
import de.eldoria.bloodnight.api.BloodNightService;
import de.eldoria.bloodnight.atmosphere.BloodPack;
import de.eldoria.bloodnight.atmosphere.BloodSky;
import de.eldoria.bloodnight.atmosphere.Haunting;
import de.eldoria.bloodnight.atmosphere.PackMerge;
import de.eldoria.bloodnight.behaviour.BehaviourService;
import de.eldoria.bloodnight.behaviour.Behaviours;
import de.eldoria.bloodnight.behaviour.BytecodeCache;
import de.eldoria.bloodnight.command.BloodNightCommandBrigadier;
import de.eldoria.bloodnight.command.MobIds;
import de.eldoria.bloodnight.command.StoreIds;
import de.eldoria.bloodnight.configuration.Configuration;
import de.eldoria.bloodnight.editor.RuntimeCatalog;
import de.eldoria.bloodnight.graph.BloodNightPlatform;
import de.eldoria.bloodnight.graph.CompileClasspath;
import de.eldoria.bloodnight.graph.PluginNodes;
import de.eldoria.bloodnight.graph.TriggerDispatcher;
import de.eldoria.bloodnight.integration.placeholders.CommandText;
import de.eldoria.bloodnight.integration.placeholders.MiniExpansion;
import de.eldoria.bloodnight.integration.placeholders.PapiCommandText;
import de.eldoria.bloodnight.integration.placeholders.PapiExpansion;
import de.eldoria.bloodnight.integration.placeholders.Placeholders;
import de.eldoria.bloodnight.integration.regions.RegionRules;
import de.eldoria.bloodnight.integration.regions.WorldGuardRules;
import de.eldoria.bloodnight.integration.worlds.WorldNames;
import de.eldoria.bloodnight.legacy.LegacyFolder;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mobs.DamageHandler;
import de.eldoria.bloodnight.mobs.DropHandler;
import de.eldoria.bloodnight.mobs.ForeignMobs;
import de.eldoria.bloodnight.mobs.MobCoordinator;
import de.eldoria.bloodnight.mobs.MobDeaths;
import de.eldoria.bloodnight.mobs.MobLifecycle;
import de.eldoria.bloodnight.mobs.MobOutfitter;
import de.eldoria.bloodnight.mobs.MobSpawner;
import de.eldoria.bloodnight.mobs.PickupGuard;
import de.eldoria.bloodnight.mobs.TargetPolicy;
import de.eldoria.bloodnight.night.BloodNights;
import de.eldoria.bloodnight.night.DragonFight;
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
import de.eldoria.bloodnight.rules.WorldRules;
import de.eldoria.bloodnight.service.PluginService;
import de.eldoria.bloodnight.store.BuiltInStore;
import de.eldoria.bloodnight.store.MobStore;
import de.eldoria.bloodnight.store.StoreEntry;
import de.eldoria.bloodnight.store.StoreNotice;
import de.eldoria.bloodnight.tasks.Tasks;
import de.eldoria.bloodnight.updates.LynaSource;
import de.eldoria.bloodnight.updates.UpdateNotice;
import de.eldoria.graph.payload.Catalog;
import de.eldoria.graph.reflect.Documentation;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.random.RandomGenerator;
import java.util.stream.Collectors;

/**
 * Plugin entry point: loads the configuration, compiles every mob's behaviour in the background, and wires the
 * compiled behaviours to spawned mobs, events and the tick loop.
 * <p>
 * Worlds load before plugins are enabled, so custom mobs already in loaded chunks are taken over on enable; they get
 * their behaviour once compilation finished. Mobs in chunks that are already loaded are taken over on enable; on
 * Folia no chunk is loaded when plugins enable, and every mob arrives through {@link MobLifecycle}.
 * <p>
 * {@code /bloodnight reload} reads the configuration again and recompiles; living mobs switch to the new behaviours
 * once compilation finished. {@code source} writes a mob's generated source to {@code debug/}, {@code problems} lists
 * why a mob does not compile, {@code export} writes the editor payload and catalog to {@code debug/}, {@code spawn}
 * and {@code debug} help testing.
 */
public final class BloodNight extends JavaPlugin {
    /**
     * Random numbers for listeners and tasks, which run on several threads on Folia: each thread draws from its own
     * generator.
     */
    private static final RandomGenerator RANDOM =
            () -> ThreadLocalRandom.current().nextLong();

    private Configuration configuration;
    private BehaviourService behaviourService;
    private MobCoordinator coordinator;
    private TriggerDispatcher dispatcher;
    private Tasks tasks;
    private NightStretch stretch;
    private Announcer announcer;
    private BloodSky bloodSky;
    private Haunting haunting;
    private BloodPack bloodPack;
    private PluginService service;
    private @Nullable Object miniPlaceholders;
    private PhantomForce phantoms;
    private MobStore mobStore;
    private WorldNames worldNames;
    private RegionRules regions = RegionRules.NONE;
    private WorldRules worldRules;
    private final Messages messages = new Messages();
    private NightSelector selector;
    private BloodNights bloodNights;
    private Behaviours behaviours = Behaviours.empty();
    private Catalog catalog;

    /**
     * Registers the WorldGuard flags while plugins load, as WorldGuard requires.
     */
    @Override
    public void onLoad() {
        if (getServer().getPluginManager().getPlugin("WorldGuard") == null) return;
        try {
            regions = WorldGuardRules.register(getLogger());
            getLogger().info("WorldGuard flags registered");
        } catch (LinkageError | RuntimeException e) {
            getLogger().warning("WorldGuard flags could not be registered: " + e);
        }
    }

    @Override
    public void onEnable() {
        tasks = new Tasks(this);
        moveLegacyAside();
        configuration = new Configuration(this);
        var platform = BloodNightPlatform.create(
                CompileClasspath.discover(getClassLoader()),
                Documentation.NONE,
                name -> getServer().getPluginManager().getPlugin(name) != null);
        mobStore = new MobStore(BuiltInStore.load());
        autoUpdate();
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
        selector = new NightSelector(
                world -> world.isFixedTime()
                        ? configuration.worldConfig(world).nightSettings().timed()
                        : configuration.worldConfig(world).nightSelection(),
                world -> NightTimes.of(world, configuration.worldConfig(world).nightSettings()),
                Clock.systemUTC(),
                () -> getServer().getServerTickManager().getTickRate());
        bloodNights = new BloodNights(
                selector,
                world -> configuration.worldConfig(world).enabled(),
                getServer().getPluginManager(),
                getLogger());
        coordinator = new MobCoordinator(
                new MobOutfitter(
                        () -> configuration.items(),
                        world -> configuration.worldConfig(world).mobSettings()),
                () -> configuration.items(),
                id -> configuration.mobs().get(id),
                bloodNights::running,
                tasks,
                getLogger());
        dispatcher = new TriggerDispatcher(
                coordinator::behaviour, coordinator::extensionBehaviour, (trigger, entity, error) -> {
                    if (entity != null) {
                        coordinator.failed(trigger.id(), entity, error);
                    } else {
                        getLogger().log(Level.WARNING, "Trigger %s failed".formatted(trigger.id()), error);
                    }
                });

        service = new PluginService(
                getServer(),
                bloodNights,
                selector,
                coordinator,
                id -> configuration.mobs().get(id),
                () -> configuration.mobs().all());
        getServer().getServicesManager().register(BloodNightService.class, service, this, ServicePriority.Normal);
        var plugins = getServer().getPluginManager();
        worldNames = new WorldNames(
                world -> configuration.worldConfig(world).displayName(),
                () -> pluginLoader("Multiverse-Core"),
                () -> pluginLoader("Hyperverse"));
        CommandText commandText = hookPlaceholders();
        plugins.registerEvents(new DamageHandler(), this);
        plugins.registerEvents(new TargetPolicy(), this);
        plugins.registerEvents(new MobLifecycle(coordinator), this);
        plugins.registerEvents(new MobDeaths(plugins), this);
        ForeignMobs foreign =
                new ForeignMobs(() -> configuration.main().foreignMobs(), () -> pluginLoader("MythicMobs"));
        plugins.registerEvents(
                new DropHandler(
                        id -> configuration.mobs().get(id),
                        world -> configuration.worldConfig(world).worldDrops(),
                        () -> configuration.globalDrops(),
                        id -> configuration.items().item(id),
                        world -> bloodNights.running(world).isPresent(),
                        foreign,
                        RANDOM,
                        regions),
                this);
        plugins.registerEvents(
                new PickupGuard(
                        world -> bloodNights.running(world).isPresent(),
                        world -> configuration.worldConfig(world).worldDrops().vanilla()),
                this);
        if (!Tasks.regionised()) {
            getServer().getWorlds().forEach(world -> world.getLivingEntities().forEach(coordinator::attach));
        }
        MobIds.use(() -> configuration.mobs().all().stream().map(CustomMob::id).toList());
        getLifecycleManager()
                .registerEventHandler(LifecycleEvents.COMMANDS.newHandler(
                        event -> BloodNightCommandBrigadier.register(event.registrar(), this)));
        plugins.registerEvents(
                new MobSpawner(
                        () -> configuration.mobs().all(),
                        configuration::worldConfig,
                        coordinator,
                        bloodNights,
                        tasks,
                        plugins,
                        foreign,
                        RANDOM,
                        regions),
                this);
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
                        getServer(), world -> configuration.worldConfig(world).nightSettings(), commandText),
                this);
        plugins.registerEvents(stretch, this);
        plugins.registerEvents(
                new DragonFight(
                        bloodNights,
                        world ->
                                configuration.worldConfig(world).nightSettings().timed(),
                        id -> configuration.mobs().get(id),
                        coordinator,
                        tasks),
                this);
        messages.register(configuration.main().announcements().fallbackLocale());
        registerMobNames();
        StoreNotice storeNotice =
                new StoreNotice(mobStore, () -> configuration.mobs().all());
        plugins.registerEvents(storeNotice, this);
        List<String> waiting = storeNotice.waiting();
        if (!waiting.isEmpty()) {
            getLogger()
                    .info("Mobs you changed have updates in the store: %s. Take them with /bloodnight store update"
                            .formatted(String.join(", ", waiting)));
        }
        StoreIds.use(mobStore.store());
        announcer = new Announcer(
                getServer(),
                bloodNights,
                () -> configuration.main().announcements(),
                configuration::worldConfig,
                selector::times,
                tasks,
                worldNames::name,
                regions);
        plugins.registerEvents(announcer, this);
        phantoms = new PhantomForce(
                bloodNights,
                world -> configuration.worldConfig(world).mobSettings().forcePhantoms(),
                tasks);
        plugins.registerEvents(phantoms, this);
        plugins.registerEvents(new CreeperGuard(bloodNights, configuration::worldConfig), this);
        worldRules = new WorldRules(
                bloodNights,
                world -> configuration.worldConfig(world).nightRules(),
                BloodNight::gameRule,
                getServer()::isGlobalTickThread,
                getLogger());
        plugins.registerEvents(worldRules, this);
        plugins.registerEvents(
                new MobDamage(
                        bloodNights, world -> configuration.worldConfig(world).mobSettings(), foreign, regions),
                this);
        plugins.registerEvents(
                new CommandBlocker(
                        bloodNights,
                        getServer().getCommandMap(),
                        world ->
                                configuration.worldConfig(world).nightSettings().blockedCommands(),
                        regions),
                this);
        plugins.registerEvents(
                new DeathActionHandler(
                        getServer(),
                        tasks,
                        bloodNights,
                        configuration::worldConfig,
                        commandText,
                        foreign,
                        RANDOM,
                        regions),
                this);
        bloodSky = new BloodSky(
                bloodNights, world -> configuration.worldConfig(world).atmosphere(), BloodSky.silently(getServer()));
        plugins.registerEvents(bloodSky, this);
        haunting = new Haunting(
                getServer(),
                bloodNights,
                world -> configuration.worldConfig(world).atmosphere(),
                tasks,
                getServer()::createWorldBorder,
                regions);
        plugins.registerEvents(haunting, this);
        mergePack();
        bloodPack = new BloodPack(() -> configuration.main().resourcePack(), tasks, getLogger(), () -> !packTargets()
                .isEmpty());
        plugins.registerEvents(bloodPack, this);
        bloodPack.refresh();
        tasks.global(() -> bloodSky.restore(getServer().getWorlds()));
        tasks.global(() -> worldRules.restoreLeftovers(getServer().getWorlds()));
        tasks.globalTimer(Haunting.STEP, haunting::tick);
        tasks.globalTimer(20, announcer::tick);
        UpdateNotice updates = new UpdateNotice(
                LynaSource.bloodNight(),
                getPluginMeta().getVersion(),
                () -> configuration.main().updates().notice(),
                getLogger());
        plugins.registerEvents(updates, this);
        tasks.asyncTimer(Duration.ofHours(12), updates::check);
        tasks.globalTimer(1, stretch::tick);
        tasks.globalTimer(20, () -> bloodNights.check(getServer().getWorlds()));
        recompile();
    }

    /**
     * Offers BloodNight's placeholders to PlaceholderAPI and MiniPlaceholders when they run. Their classes are only
     * touched then, so the plugin runs without them.
     *
     * @return fills PlaceholderAPI placeholders into BloodNight's commands, or leaves them as they are
     */
    private CommandText hookPlaceholders() {
        Placeholders placeholders = new Placeholders(
                service,
                name -> getServer().getWorld(name),
                () -> getServer().getServerTickManager().getTickRate(),
                () -> configuration.main().placeholders().revealNext(),
                worldNames::plain);
        String version = getPluginMeta().getVersion();
        var plugins = getServer().getPluginManager();
        if (plugins.isPluginEnabled("MiniPlaceholders")) {
            miniPlaceholders = MiniExpansion.register(placeholders, version);
            getLogger().info("Placeholders registered with MiniPlaceholders");
        }
        if (!plugins.isPluginEnabled("PlaceholderAPI")) return CommandText.NONE;
        new PapiExpansion(placeholders, version).register();
        getLogger().info("Placeholders registered with PlaceholderAPI");
        return new PapiCommandText();
    }

    /**
     * @param key a game rule key such as {@code minecraft:spawn_phantoms}
     * @return the game rule, if there is one
     */
    private static @Nullable GameRule<?> gameRule(String key) {
        NamespacedKey name = NamespacedKey.fromString(key);
        return name == null
                ? null
                : RegistryAccess.registryAccess()
                        .getRegistry(RegistryKey.GAME_RULE)
                        .get(name);
    }

    /**
     * @return the class loader of a plugin while it runs, so its classes can be reached without depending on it
     */
    private @Nullable ClassLoader pluginLoader(String name) {
        Plugin plugin = getServer().getPluginManager().getPlugin(name);
        return plugin != null && plugin.isEnabled() ? plugin.getClass().getClassLoader() : null;
    }

    /**
     * Identifies this exact build for the bytecode cache: the version alone would let a rebuilt jar with changed nodes
     * reuse classes generated by the previous build. The installed plugins that nodes are for count too, so their
     * warnings follow when one is added or removed.
     */
    private String installedIntegrations() {
        return PluginNodes.plugins().stream()
                .filter(name -> getServer().getPluginManager().getPlugin(name) != null)
                .collect(Collectors.joining(","));
    }

    private String buildFingerprint() {
        try {
            Path jar = Path.of(getClass()
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI());
            return "%s+%d+%d+%s"
                    .formatted(
                            getPluginMeta().getVersion(),
                            Files.size(jar),
                            Files.getLastModifiedTime(jar).toMillis(),
                            installedIntegrations());
        } catch (IOException | URISyntaxException | RuntimeException e) {
            return getPluginMeta().getVersion() + "+" + installedIntegrations();
        }
    }

    @Override
    public void onDisable() {
        MobIds.clear();
        StoreIds.clear();
        if (tasks != null) tasks.cancelAll();
        if (stretch != null) stretch.clear();
        if (bloodNights != null) bloodNights.endAll(getServer().getWorlds());
        if (announcer != null) announcer.clear();
        if (haunting != null) haunting.clear();
        if (bloodSky != null)
            bloodSky.clear(getServer().getWorlds(), getServer().isGlobalTickThread());
        if (miniPlaceholders != null) MiniExpansion.unregister(miniPlaceholders);
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
                    tasks.global(() -> {
                        behaviours = result;
                        coordinator.rebind(result);
                        dispatcher.register(this, result.usedTriggers());
                        applied.complete(result);
                    });
                });
        return applied;
    }

    /**
     * @return runs work on the thread owning what it touches
     */
    public Tasks tasks() {
        return tasks;
    }

    /**
     * @return what other plugins get through {@link BloodNightService}
     */
    public PluginService service() {
        return service;
    }

    /**
     * @return offers the resource pack to joining players
     */
    /**
     * @return the installed pack plugins BloodNight's pack goes into; empty when merging is off
     */
    public List<PackMerge.Target> packTargets() {
        if (!configuration.main().resourcePack().mergeIntoPackPlugins()) return List.of();
        return PackMerge.installed(name -> getServer().getPluginManager().getPlugin(name) != null);
    }

    /**
     * Puts the resource pack into the installed pack plugins, before they build their packs.
     */
    public void mergePack() {
        List<PackMerge.Target> targets = packTargets();
        if (targets.isEmpty()) return;
        try {
            PackMerge pack = PackMerge.bundled();
            Path plugins = getDataFolder().toPath().toAbsolutePath().getParent();
            for (PackMerge.Target target : targets) {
                if (pack.install(plugins, target)) {
                    getLogger()
                            .info("Resource pack added to %s. If its pack is already built, run %s"
                                    .formatted(target.plugin(), target.reload()));
                }
            }
        } catch (IOException | UncheckedIOException e) {
            getLogger().log(Level.WARNING, "The resource pack could not be merged into the pack plugins", e);
        }
    }

    /**
     * Moves the old BloodNight's files out of the shared folder before the configuration reads it.
     */
    private void moveLegacyAside() {
        try {
            LegacyFolder.moveAside(getDataFolder().toPath()).ifPresent(folder -> getLogger()
                    .warning(
                            "Found settings of the old BloodNight and moved them to %s. Run /bn convert to take them over"
                                    .formatted(getDataFolder().toPath().relativize(folder))));
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "The old BloodNight's settings could not be moved aside", e);
        }
    }

    public MobStore store() {
        return mobStore;
    }

    /**
     * Gives store mobs nobody changed the store's newer versions, before they are built.
     */
    private void autoUpdate() {
        if (!configuration.main().store().autoUpdate()) return;
        MobStore.Change change = mobStore.autoUpdate(configuration.mobs().all());
        if (change.done().isEmpty()) return;
        configuration.replaceMobs(change.mobs());
        getLogger().info("Updated from the store: " + String.join(", ", change.done()));
    }

    /**
     * Lets the server name store mobs in each player's language, from their packages.
     */
    private void registerMobNames() {
        Map<String, Map<String, String>> byLanguage = new TreeMap<>();
        for (StoreEntry entry : mobStore.store().entries()) {
            entry.mobPackage().names().forEach((language, name) -> byLanguage
                    .computeIfAbsent(language, ignored -> new TreeMap<>())
                    .put(
                            "bloodnight.mob." + entry.id(),
                            MiniMessage.miniMessage().escapeTags(name)));
        }
        byLanguage.forEach(messages::add);
    }

    public BloodPack bloodPack() {
        return bloodPack;
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
