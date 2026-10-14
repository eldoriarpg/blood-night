/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.announce;

import de.eldoria.bloodnight.api.event.BloodNightBeginEvent;
import de.eldoria.bloodnight.api.event.BloodNightEndEvent;
import de.eldoria.bloodnight.configuration.elements.Announcements;
import de.eldoria.bloodnight.configuration.elements.WorldSettings;
import de.eldoria.bloodnight.configuration.elements.world.announcement.NightEffect;
import de.eldoria.bloodnight.night.BloodNights;
import de.eldoria.bloodnight.night.NightTimes;
import de.eldoria.bloodnight.tasks.Tasks;
import de.eldoria.bloodnight.util.Effects;
import net.kyori.adventure.text.minimessage.translation.Argument;
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

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Lets players know about blood nights: announces begin and end, shows a boss bar while one runs, applies the night
 * effect at begin and end, plays the begin, end and ambience sounds, warns players entering or leaving a world with a
 * running blood night and tells sleepers that a blood night cannot be slept through.
 * <p>
 * {@link #tick()} runs every second on the global region and keeps boss bars and ambience going. Everything a player
 * sees or hears is done on that player's thread.
 */
public final class Announcer implements Listener {
    private final Server server;
    private final BloodNights nights;
    private final Supplier<Announcements> announcements;
    private final Function<World, WorldSettings> settings;
    private final Function<World, NightTimes> times;
    private final Tasks tasks;
    private final NightBars bars = new NightBars();
    private final Ambience ambience = new Ambience();

    /**
     * @param server        the server
     * @param nights        the running blood nights
     * @param announcements the server-wide announcement settings
     * @param settings      the settings of a world
     * @param times         the night times of a world
     * @param tasks         runs work on the players' threads
     */
    public Announcer(
            Server server,
            BloodNights nights,
            Supplier<Announcements> announcements,
            Function<World, WorldSettings> settings,
            Function<World, NightTimes> times,
            Tasks tasks) {
        this.server = server;
        this.nights = nights;
        this.announcements = announcements;
        this.settings = settings;
        this.times = times;
        this.tasks = tasks;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBegin(BloodNightBeginEvent event) {
        World world = event.getWorld();
        WorldSettings worldSettings = settings.apply(world);
        broadcast(world, Messages.NIGHT_BEGIN);
        for (Player player : world.getPlayers()) {
            tasks.entityNow(player, () -> {
                Ambience.play(
                        player,
                        worldSettings.soundSettings(),
                        worldSettings.soundSettings().begin());
                applyEffect(player, worldSettings.announcement().effect());
                bars.update(player, worldSettings.announcement().bossBar(), times.apply(world), event.night());
            });
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnd(BloodNightEndEvent event) {
        World world = event.getWorld();
        WorldSettings worldSettings = settings.apply(world);
        broadcast(world, Messages.NIGHT_END);
        for (Player player : world.getPlayers()) {
            tasks.entityNow(player, () -> {
                bars.hide(player);
                ambience.forget(player);
                Ambience.play(
                        player,
                        worldSettings.soundSettings(),
                        worldSettings.soundSettings().end());
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
            tell(player, Messages.NIGHT_LEFT);
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
        tell(event.getPlayer(), Messages.NIGHT_SLEEP);
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
    public void clear() {
        for (Player player : server.getOnlinePlayers()) tasks.entityNow(player, () -> bars.hide(player));
    }

    private void tick(Player player) {
        World world = player.getWorld();
        var night = nights.running(world);
        if (night.isEmpty()) {
            bars.hide(player);
            return;
        }
        WorldSettings worldSettings = settings.apply(world);
        bars.update(player, worldSettings.announcement().bossBar(), times.apply(world), night.getAsLong());
        ambience.tick(player, worldSettings.soundSettings());
    }

    private void entered(Player player) {
        World world = player.getWorld();
        var night = nights.running(world);
        if (night.isEmpty()) return;
        WorldSettings worldSettings = settings.apply(world);
        if (worldSettings.announcement().joinWarning()) tell(player, Messages.NIGHT_JOINED);
        applyEffect(player, worldSettings.announcement().effect());
        bars.update(player, worldSettings.announcement().bossBar(), times.apply(world), night.getAsLong());
    }

    private void broadcast(World world, String key) {
        Announcements config = announcements.get();
        Collection<? extends Player> audience =
                switch (config.reach()) {
                    case SERVER -> server.getOnlinePlayers();
                    case WORLD -> world.getPlayers();
                    case NONE -> List.of();
                };
        var message = Messages.message(key, Argument.string("world", world.getName()));
        for (Player player : audience) {
            tasks.entityNow(player, () -> Delivery.send(player, config.method(), config.prefix(), message));
        }
    }

    private void tell(Player player, String key) {
        Announcements config = announcements.get();
        Delivery.send(
                player,
                config.personalMethod(),
                config.prefix(),
                Messages.message(key, Argument.string("world", player.getWorld().getName())));
    }

    private static void applyEffect(Player player, NightEffect effect) {
        if (!effect.enabled()) return;
        PotionEffectType type = Effects.effect(effect.effect());
        if (type == null) return;
        player.addPotionEffect(new PotionEffect(type, effect.seconds() * 20, 0, true, false));
    }
}
