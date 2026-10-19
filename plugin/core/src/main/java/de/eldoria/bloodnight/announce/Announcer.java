/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.announce;

import de.eldoria.bloodnight.announce.night.Ambience;
import de.eldoria.bloodnight.announce.night.NightBars;
import de.eldoria.bloodnight.api.event.night.BloodNightBeginEvent;
import de.eldoria.bloodnight.api.event.night.BloodNightEndEvent;
import de.eldoria.bloodnight.configuration.server.Announcements;
import de.eldoria.bloodnight.configuration.server.Announcements.Method;
import de.eldoria.bloodnight.configuration.world.WorldSettings;
import de.eldoria.bloodnight.configuration.world.announcement.NightEffect;
import de.eldoria.bloodnight.integration.regions.RegionFlag;
import de.eldoria.bloodnight.integration.regions.RegionRules;
import de.eldoria.bloodnight.night.BloodNights;
import de.eldoria.bloodnight.night.selection.NightTimes;
import de.eldoria.bloodnight.tasks.Tasks;
import de.eldoria.bloodnight.util.Effects;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.OptionalDouble;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Lets players know about blood nights: announces begin and end (one way to the players in the world, another to
 * everyone else), shows a boss bar while one runs, applies the night
 * effect at begin and end, plays the begin, end and ambience sounds, warns players entering or leaving a world with a
 * running blood night and tells sleepers that a blood night cannot be slept through.
 * <p>
 * {@link #tick()} runs every second on the global region and keeps boss bars and ambience going. Everything a player
 * sees or hears is done on that player's thread.
 */
public final class Announcer implements Listener {
    static final String NIGHT_SLEEP = "bloodnight.night.sleep";
    private final Server server;
    private final BloodNights nights;
    private final Supplier<Announcements> announcements;
    private final Function<World, WorldSettings> settings;
    private final Function<World, NightTimes> times;
    private final Tasks tasks;
    private final Function<World, Component> names;
    private final NightBars bars = new NightBars();
    private final Ambience ambience = new Ambience();
    private final Set<UUID> dragonFights = ConcurrentHashMap.newKeySet();
    private final RegionRules regions;

    /**
     * @param server        the server
     * @param nights        the running blood nights
     * @param announcements the server-wide announcement settings
     * @param settings      the settings of a world
     * @param times         the night times of a world
     * @param tasks         runs work on the players' threads
     * @param names         the name players see for a world
     * @param regions       where regions keep effects and the boss bar away
     */
    public Announcer(
            Server server,
            BloodNights nights,
            Supplier<Announcements> announcements,
            Function<World, WorldSettings> settings,
            Function<World, NightTimes> times,
            Tasks tasks,
            Function<World, Component> names,
            RegionRules regions) {
        this.regions = regions;
        this.server = server;
        this.nights = nights;
        this.announcements = announcements;
        this.settings = settings;
        this.times = times;
        this.tasks = tasks;
        this.names = names;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBegin(BloodNightBeginEvent event) {
        World world = event.getWorld();
        WorldSettings worldSettings = settings.apply(world);
        NightKind kind = nights.held(world) ? NightKind.DRAGON : NightKind.of(world);
        if (kind == NightKind.DRAGON) dragonFights.add(world.getUID());
        broadcast(world, kind.key("begin"));
        for (Player player : world.getPlayers()) {
            tasks.entityNow(player, () -> {
                Ambience.play(
                        player,
                        worldSettings.soundSettings(),
                        worldSettings.soundSettings().begin());
                if (!affected(player)) return;
                applyEffect(player, worldSettings.announcement().effect());
                bars.update(
                        player,
                        worldSettings.announcement().bossBar(),
                        left(world, event.night()),
                        NightKind.of(world).key("name"));
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnd(BloodNightEndEvent event) {
        World world = event.getWorld();
        WorldSettings worldSettings = settings.apply(world);
        broadcast(world, (dragonFights.remove(world.getUID()) ? NightKind.DRAGON : NightKind.of(world)).key("end"));
        for (Player player : world.getPlayers()) {
            tasks.entityNow(player, () -> {
                bars.hide(player);
                ambience.forget(player);
                Ambience.play(
                        player,
                        worldSettings.soundSettings(),
                        worldSettings.soundSettings().end());
                if (affected(player))
                    applyEffect(player, worldSettings.announcement().effect());
            });
        }
    }

    @EventHandler
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World from = event.getFrom();
        bars.hide(player);
        ambience.forget(player);
        if (nights.running(from).isPresent()
                && settings.apply(from).announcement().joinWarning()) {
            tell(player, NightKind.of(from).key("left"));
        }
        entered(player);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        entered(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        bars.hide(event.getPlayer());
        ambience.forget(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBedEnter(PlayerBedEnterEvent event) {
        World world = event.getPlayer().getWorld();
        if (nights.running(world).isEmpty()
                || settings.apply(world).nightSettings().skippable()) return;
        tell(event.getPlayer(), NIGHT_SLEEP);
    }

    /**
     * Updates the boss bars of running blood nights and plays ambience; runs every second, each player on their
     * thread.
     */
    public void tick() {
        for (Player player : server.getOnlinePlayers()) tasks.entityNow(player, () -> tick(player));
    }

    /**
     * Hides every boss bar, e.g. when the plugin disables.
     */
    /**
     * Darkens the player's world through their blood night bar, or lets it brighten again; nothing happens where the
     * world shows no bar. Runs on the player's thread.
     */
    public void darken(Player player, boolean dark) {
        bars.darken(player, dark);
    }

    public void clear() {
        for (Player player : server.getOnlinePlayers()) tasks.entityNow(player, () -> bars.hide(player));
    }

    private void tick(Player player) {
        World world = player.getWorld();
        var night = nights.running(world);
        if (night.isEmpty() || !affected(player)) {
            bars.hide(player);
            return;
        }
        WorldSettings worldSettings = settings.apply(world);
        bars.update(
                player,
                worldSettings.announcement().bossBar(),
                left(world, night.getAsLong()),
                NightKind.of(world).key("name"));
        ambience.tick(player, worldSettings.soundSettings());
    }

    private void entered(Player player) {
        World world = player.getWorld();
        var night = nights.running(world);
        if (night.isEmpty()) return;
        WorldSettings worldSettings = settings.apply(world);
        if (worldSettings.announcement().joinWarning())
            tell(player, NightKind.of(world).key("joined"));
        if (!affected(player)) return;
        applyEffect(player, worldSettings.announcement().effect());
        bars.update(
                player,
                worldSettings.announcement().bossBar(),
                left(world, night.getAsLong()),
                NightKind.of(world).key("name"));
    }

    /**
     * @return the share of the running night still to come for the boss bar: what is left of the hold, e.g. the
     *     dragon's health, when the hold ends the night, otherwise of its time
     */
    private float left(World world, long night) {
        OptionalDouble held = nights.holdLeft(world);
        if (held.isPresent()) return (float) held.getAsDouble();
        NightTimes worldTimes = times.apply(world);
        return NightBars.remaining(worldTimes, nights.endOf(world, worldTimes, night), NightTimes.now(world));
    }

    private void broadcast(World world, String key) {
        Announcements config = announcements.get();
        var worldName = Placeholder.component("world", names.apply(world));
        for (Player player : server.getOnlinePlayers()) {
            Method method = player.getWorld().equals(world) ? config.method() : config.elsewhereMethod();
            if (method != Method.NONE) tasks.entityNow(player, () -> Delivery.send(player, method, key, worldName));
        }
    }

    private void tell(Player player, String key) {
        Announcements config = announcements.get();
        Delivery.send(
                player, config.personalMethod(), key, Placeholder.component("world", names.apply(player.getWorld())));
    }

    private boolean affected(Player player) {
        return regions.allows(player.getLocation(), RegionFlag.EFFECTS);
    }

    private static void applyEffect(Player player, NightEffect effect) {
        if (!effect.enabled()) return;
        PotionEffectType type = Effects.effect(effect.effect());
        if (type == null) return;
        player.addPotionEffect(new PotionEffect(type, effect.seconds() * 20, 0, true, false));
    }
}
