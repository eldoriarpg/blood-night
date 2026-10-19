/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight;

import de.eldoria.EldoUtilities;
import de.eldoria.bloodnight.advancements.BloodAdvancements;
import de.eldoria.bloodnight.announce.Announcer;
import de.eldoria.bloodnight.announce.Messages;
import de.eldoria.bloodnight.announce.Texts;
import de.eldoria.bloodnight.announce.night.IntroDialog;
import de.eldoria.bloodnight.api.BloodNightService;
import de.eldoria.bloodnight.api.event.BloodNightSettingsChangeEvent;
import de.eldoria.bloodnight.api.event.SettingsChangeCause;
import de.eldoria.bloodnight.atmosphere.Haunting;
import de.eldoria.bloodnight.atmosphere.Omens;
import de.eldoria.bloodnight.atmosphere.pack.BloodPack;
import de.eldoria.bloodnight.atmosphere.pack.PackMerge;
import de.eldoria.bloodnight.atmosphere.timeline.TimelineGroup;
import de.eldoria.bloodnight.atmosphere.timeline.Timelines;
import de.eldoria.bloodnight.behaviour.BehaviourService;
import de.eldoria.bloodnight.behaviour.Behaviours;
import de.eldoria.bloodnight.behaviour.BytecodeCache;
import de.eldoria.bloodnight.command.BloodNightCommandBrigadier;
import de.eldoria.bloodnight.command.item.ItemDropBox;
import de.eldoria.bloodnight.command.store.StoreIds;
import de.eldoria.bloodnight.command.suggest.MobIds;
import de.eldoria.bloodnight.configuration.Configuration;
import de.eldoria.bloodnight.configuration.server.Announcements;
import de.eldoria.bloodnight.configuration.world.MobSettings;
import de.eldoria.bloodnight.configuration.world.mobsettings.AttributeOverride;
import de.eldoria.bloodnight.debug.DebugReport;
import de.eldoria.bloodnight.editor.catalog.RuntimeCatalog;
import de.eldoria.bloodnight.enchantments.BloodEnchantments;
import de.eldoria.bloodnight.enchantments.EnchantmentEffects;
import de.eldoria.bloodnight.enchantments.NightGear;
import de.eldoria.bloodnight.graph.BloodNightPlatform;
import de.eldoria.bloodnight.graph.CompileClasspath;
import de.eldoria.bloodnight.graph.PluginNodes;
import de.eldoria.bloodnight.graph.trigger.TriggerDispatcher;
import de.eldoria.bloodnight.integration.placeholders.CommandText;
import de.eldoria.bloodnight.integration.placeholders.MiniExpansion;
import de.eldoria.bloodnight.integration.placeholders.PapiCommandText;
import de.eldoria.bloodnight.integration.placeholders.PapiExpansion;
import de.eldoria.bloodnight.integration.placeholders.Placeholders;
import de.eldoria.bloodnight.integration.regions.RegionRules;
import de.eldoria.bloodnight.integration.regions.WorldGuardRules;
import de.eldoria.bloodnight.integration.worlds.WorldNames;
import de.eldoria.bloodnight.legacy.LegacyFolder;
import de.eldoria.bloodnight.living.MobCoordinator;
import de.eldoria.bloodnight.living.MobLifecycle;
import de.eldoria.bloodnight.living.combat.DamageHandler;
import de.eldoria.bloodnight.living.combat.HeldPassengers;
import de.eldoria.bloodnight.living.combat.MobDeaths;
import de.eldoria.bloodnight.living.combat.TargetPolicy;
import de.eldoria.bloodnight.living.movement.PaperSteering;
import de.eldoria.bloodnight.living.spawn.ForeignMobs;
import de.eldoria.bloodnight.living.spawn.MobOutfitter;
import de.eldoria.bloodnight.living.spawn.MobSpawner;
import de.eldoria.bloodnight.loot.drop.DropHandler;
import de.eldoria.bloodnight.loot.drop.PickupGuard;
import de.eldoria.bloodnight.loot.drop.RewardLoot;
import de.eldoria.bloodnight.loot.money.DamageShares;
import de.eldoria.bloodnight.loot.money.MoneyRewards;
import de.eldoria.bloodnight.loot.money.VaultWallet;
import de.eldoria.bloodnight.metrics.BloodNightMetrics;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.attributes.MobName;
import de.eldoria.bloodnight.mob.defaults.DefaultMobs;
import de.eldoria.bloodnight.night.BloodNights;
import de.eldoria.bloodnight.night.DragonFight;
import de.eldoria.bloodnight.night.NightCommands;
import de.eldoria.bloodnight.night.NightSkipGuard;
import de.eldoria.bloodnight.night.NightStretch;
import de.eldoria.bloodnight.night.selection.NightSelector;
import de.eldoria.bloodnight.night.selection.NightTimes;
import de.eldoria.bloodnight.night.selection.Prediction;
import de.eldoria.bloodnight.particles.EffectLibrary;
import de.eldoria.bloodnight.particles.play.Budget;
import de.eldoria.bloodnight.particles.play.EffectPlayer;
import de.eldoria.bloodnight.particles.play.Stage;
import de.eldoria.bloodnight.particles.play.Viewers;
import de.eldoria.bloodnight.rules.CommandBlocker;
import de.eldoria.bloodnight.rules.MobDamage;
import de.eldoria.bloodnight.rules.PhantomForce;
import de.eldoria.bloodnight.rules.WorldRules;
import de.eldoria.bloodnight.rules.death.DeathActionHandler;
import de.eldoria.bloodnight.rules.explosion.CreeperGuard;
import de.eldoria.bloodnight.rules.explosion.HarmlessExplosions;
import de.eldoria.bloodnight.service.PluginService;
import de.eldoria.bloodnight.setup.SetupNotice;
import de.eldoria.bloodnight.store.MobStore;
import de.eldoria.bloodnight.store.StoreEntry;
import de.eldoria.bloodnight.store.StoreNotice;
import de.eldoria.bloodnight.store.build.BuiltInStore;
import de.eldoria.bloodnight.tasks.Tasks;
import de.eldoria.bloodnight.updates.LynaSource;
import de.eldoria.bloodnight.updates.UpdateNotice;
import de.eldoria.eldoutilities.debug.DebugDataProvider;
import de.eldoria.eldoutilities.debug.data.EntryData;
import de.eldoria.graph.payload.Catalog;
import de.eldoria.graph.reflect.Documentation;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bstats.bukkit.Metrics;
import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
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
 * and {@code trigger} help testing. {@code debug} uploads a debug report to Lyna; the plugin is the report's entry
 * point (see {@link DebugReport}).
 */
public final class BloodNight extends JavaPlugin implements DebugDataProvider {
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
    private Timelines timelines;
    private Haunting haunting;
    private NightGear nightGear;
    private BloodPack bloodPack;
    private PluginService service;
    private @Nullable Object miniPlaceholders;
    private PhantomForce phantoms;
    private MobStore mobStore;
    private @Nullable Metrics metrics;
    private WorldNames worldNames;
    private RegionRules regions = RegionRules.NONE;
    private WorldRules worldRules;
    private final Messages messages = new Messages();
    private NightSelector selector;
    private BloodNights bloodNights;
    private Behaviours behaviours = Behaviours.empty();
    private Catalog catalog;
    private Viewers viewers;
    private EffectPlayer effects;

    /**
     * Registers the WorldGuard flags while plugins load, as WorldGuard requires.
     */
    @Override
    public void onLoad() {
        EldoUtilities.preWarm(this);
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
        EldoUtilities.ignite(this);
        tasks = new Tasks(this);
        moveLegacyAside();
        configuration = new Configuration(this);
        Announcements announcements = configuration.main().announcements();
        Texts.create(
                this, announcements.prefix(), announcements.fallbackLocale().name());
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
        viewers = new Viewers();
        getServer().getPluginManager().registerEvents(viewers, this);
        Budget budget = new Budget(() -> configuration.main().particles().budgetPerTick());
        tasks.globalTimer(1, budget::reset);
        effects = new EffectPlayer(
                new EffectLibrary(() -> configuration.particleEffects()),
                new Stage(
                        tasks,
                        viewers,
                        budget,
                        () -> configuration.main().particles(),
                        id -> configuration.items().item(id),
                        tasks::send),
                this);
        PaperSteering steering = new PaperSteering(getServer().getMobGoals());
        getServer().getPluginManager().registerEvents(steering, this);
        coordinator = new MobCoordinator(
                new MobOutfitter(
                        () -> configuration.items(),
                        world -> configuration.worldConfig(world).mobSettings(),
                        () -> configuration
                                .main()
                                .announcements()
                                .fallbackLocale()
                                .language(),
                        () -> configuration.main().resourcePack().translateMobNames()),
                () -> configuration.items(),
                id -> configuration.mobs().get(id),
                bloodNights::running,
                tasks,
                steering,
                getLogger(),
                getServer().getPluginManager()::callEvent,
                effects);
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
                () -> configuration.mobs().all(),
                world -> configuration.worldConfig(world));
        getServer().getServicesManager().register(BloodNightService.class, service, this, ServicePriority.Normal);
        var plugins = getServer().getPluginManager();
        worldNames = new WorldNames(
                world -> configuration.worldConfig(world).displayName(),
                () -> pluginLoader("Multiverse-Core"),
                () -> pluginLoader("Hyperverse"));
        CommandText commandText = hookPlaceholders();
        plugins.registerEvents(new DamageHandler(), this);
        plugins.registerEvents(new TargetPolicy(), this);
        plugins.registerEvents(new HeldPassengers(), this);
        plugins.registerEvents(new MobLifecycle(coordinator), this);
        plugins.registerEvents(new MobDeaths(plugins), this);
        plugins.registerEvents(ItemDropBox.closing(this), this);
        ForeignMobs foreign =
                new ForeignMobs(() -> configuration.main().foreignMobs(), () -> pluginLoader("MythicMobs"));
        DamageShares damageShares = new DamageShares();
        plugins.registerEvents(damageShares, this);
        plugins.registerEvents(
                new DropHandler(
                        id -> configuration.mobs().get(id),
                        world -> configuration.worldConfig(world).loot(),
                        () -> configuration.main().loot(),
                        id -> configuration.lootTables().get(id),
                        id -> configuration.items().item(id),
                        world -> bloodNights.running(world).isPresent(),
                        foreign,
                        RANDOM,
                        regions,
                        RewardLoot::rollFor,
                        plugins::callEvent,
                        new MoneyRewards(
                                new VaultWallet(tasks), damageShares, getServer()::getPlayer, () -> configuration
                                        .main()
                                        .announcements()),
                        getLogger()),
                this);
        plugins.registerEvents(new EnchantmentEffects(bloodNights, BloodEnchantments::level, tasks::entityNow), this);
        plugins.registerEvents(
                new BloodAdvancements(
                        bloodNights,
                        () -> configuration.main().advancements(),
                        mobStore.store().entries().stream().map(StoreEntry::id).collect(Collectors.toSet()),
                        DefaultMobs.BLOOD_DRAGON,
                        BloodAdvancements.progress(getServer()::getAdvancement, tasks::entityNow),
                        BloodEnchantments::level),
                this);
        nightGear = new NightGear(getServer(), bloodNights, BloodEnchantments::level, tasks::entityNow);
        plugins.registerEvents(nightGear, this);
        plugins.registerEvents(
                new PickupGuard(
                        world -> bloodNights.running(world).isPresent(),
                        world -> configuration.worldConfig(world).loot().ordinary()),
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
        messages.register(configuration.main().announcements().fallbackLocale().name());
        StoreNotice storeNotice =
                new StoreNotice(mobStore, () -> configuration.mobs().all());
        plugins.registerEvents(storeNotice, this);
        List<String> waiting = storeNotice.waiting();
        if (!waiting.isEmpty()) {
            getLogger()
                    .info("Mobs you changed have updates in the store: %s. Take them with /bloodnight store update"
                            .formatted(String.join(", ", waiting)));
        }
        plugins.registerEvents(
                new SetupNotice(() -> configuration.main().setup().done()), this);
        if (!configuration.main().setup().done()) {
            getLogger().info("BloodNight is not set up yet, so no world has blood nights. Run /bloodnight setup");
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
        plugins.registerEvents(
                new IntroDialog(
                        bloodNights,
                        world -> configuration.worldConfig(world).announcement(),
                        (entity, ticks, work) -> tasks.entityLater(entity, ticks, work, () -> {}),
                        IntroDialog::show),
                this);
        phantoms = new PhantomForce(
                bloodNights,
                world -> configuration.worldConfig(world).mobSettings().forcePhantoms(),
                tasks);
        plugins.registerEvents(phantoms, this);
        plugins.registerEvents(new CreeperGuard(bloodNights, configuration::worldConfig), this);
        plugins.registerEvents(new HarmlessExplosions(), this);
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
        timelines = new Timelines(
                bloodNights,
                world -> TimelineGroup.of(
                        configuration.worldConfig(world).atmosphere(),
                        configuration.worldConfig(world).nightRules()),
                Timelines.silently(getServer()));
        plugins.registerEvents(timelines, this);
        Omens omens = new Omens(
                getServer(),
                world -> configuration.worldConfig(world).enabled()
                        ? selector.next(world, 1).map(Prediction::ticksUntil).orElse(-1L)
                        : -1L,
                world -> configuration.worldConfig(world).atmosphere(),
                timelines,
                tasks::entityNow,
                RANDOM,
                Omens::runTo);
        haunting = new Haunting(
                getServer(),
                bloodNights,
                world -> configuration.worldConfig(world).atmosphere(),
                tasks,
                getServer()::createWorldBorder,
                regions,
                player -> BloodEnchantments.wears(player, BloodEnchantments.STEADY_HEART),
                announcer::darken);
        plugins.registerEvents(haunting, this);
        mergePack();
        bloodPack = new BloodPack(() -> configuration.main().resourcePack(), tasks, getLogger(), () -> !packTargets()
                .isEmpty());
        plugins.registerEvents(bloodPack, this);
        bloodPack.refresh();
        tasks.global(() -> timelines.restore(getServer().getWorlds()));
        tasks.global(() -> worldRules.restoreLeftovers(getServer().getWorlds()));
        tasks.globalTimer(Haunting.STEP, haunting::tick);
        tasks.globalTimer(Omens.STEP, omens::tick);
        tasks.globalTimer(NightGear.STEP, nightGear::tick);
        tasks.globalTimer(20, announcer::tick);
        UpdateNotice updates = new UpdateNotice(
                LynaSource.bloodNight(),
                getPluginMeta().getVersion(),
                () -> configuration.main().updates().notice(),
                getLogger());
        plugins.registerEvents(updates, this);
        tasks.asyncTimer(Duration.ofHours(12), updates::check);
        metrics = BloodNightMetrics.start(this, configuration);
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
        if (metrics != null) metrics.shutdown();
        StoreIds.clear();
        if (tasks != null) tasks.cancelAll();
        if (stretch != null) stretch.clear();
        if (bloodNights != null) bloodNights.endAll(getServer().getWorlds());
        if (announcer != null) announcer.clear();
        if (haunting != null) haunting.clear();
        if (nightGear != null) nightGear.clear();
        if (timelines != null)
            timelines.clear(getServer().getWorlds(), getServer().isGlobalTickThread());
        if (miniPlaceholders != null) MiniExpansion.unregister(miniPlaceholders);
        if (phantoms != null) phantoms.restoreAll(getServer().getOnlinePlayers());
        messages.unregister();
        if (dispatcher != null) dispatcher.unregister();
        if (behaviourService != null) behaviourService.close();
        EldoUtilities.shutdown();
    }

    @Override
    public EntryData[] getDebugInformations() {
        return new EntryData[0];
    }

    @Override
    public List<DebugDataProvider> getDebugProviders() {
        return List.of(new DebugReport(this));
    }

    /**
     * Registers every mob's name, compiles every mob in the background and switches living mobs over on the main thread.
     *
     * @return completes on the main thread once the new behaviours are active
     */
    public CompletableFuture<Behaviours> recompile() {
        configuration.mobs();
        configuration.macros();
        registerMobNames();
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
     * @return the names players see for worlds
     */
    public WorldNames worldNames() {
        return worldNames;
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
            PackMerge pack = PackMerge.bundled().withMobNames(mobNames());
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
     * Tells other plugins that the settings changed and are in use now. Call it on the global thread.
     *
     * @param cause what made them change
     */
    public void settingsChanged(SettingsChangeCause cause) {
        getServer().getPluginManager().callEvent(new BloodNightSettingsChangeEvent(cause));
    }

    /**
     * @return every configured mob's name by its key below {@code bloodnight.mob.}, and a name a world gives a mob
     *     under that world's key (see {@link MobSettings#nameKey})
     */
    public Map<String, MobName> mobNames() {
        Map<String, MobName> names = new TreeMap<>();
        for (CustomMob mob : configuration.mobs().all()) {
            if (!mob.attributes().name().isEmpty())
                names.put(mob.id(), mob.attributes().name());
            for (World world : getServer().getWorlds()) {
                AttributeOverride override = configuration
                        .worldConfig(world)
                        .mobSettings()
                        .overrides()
                        .get(mob.id());
                if (override != null && override.renames()) {
                    names.put(
                            mob.id() + "." + world.getUID(),
                            override.apply(mob.attributes()).name());
                }
            }
        }
        return names;
    }

    /**
     * Lets the server name every configured mob in each player's language, and a mob a world renames under that
     * world's key (see {@link MobSettings#nameKey}).
     */
    private void registerMobNames() {
        Map<String, Map<String, String>> byLanguage = new TreeMap<>();
        mobNames().forEach((key, name) -> name.texts().forEach((language, text) -> byLanguage
                .computeIfAbsent(language, ignored -> new TreeMap<>())
                .put(MobName.translationKey(key), MiniMessage.miniMessage().escapeTags(text))));
        messages.names(
                byLanguage,
                configuration.main().announcements().fallbackLocale().language());
    }

    /**
     * @return offers the resource pack to joining players
     */
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
     * @return who sees particle effects
     */
    public Viewers viewers() {
        return viewers;
    }

    /**
     * @return plays particle effects
     */
    public EffectPlayer effects() {
        return effects;
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

    /**
     * Reads the text files again and applies the announcement settings to them: the prefix of chat messages and the
     * fallback language.
     */
    public void applyTextSettings() {
        Announcements announcements = configuration.main().announcements();
        messages.fallback(announcements.fallbackLocale().name());
        Texts.create(
                this, announcements.prefix(), announcements.fallbackLocale().name());
    }
}
