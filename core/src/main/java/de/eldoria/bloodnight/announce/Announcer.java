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
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.minimessage.translation.Argument;
import org.bukkit.NamespacedKey;
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
import org.jspecify.annotations.Nullable;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Lets players know about blood nights: announces begin and end, shows a boss bar while one runs, applies the begin
 * effect, plays the begin, end and ambience sounds, warns players entering or leaving a world with a running blood
 * night and tells sleepers that a blood night cannot be slept through.
 * <p>
 * {@link #tick()} runs every second and keeps boss bars and ambience going.
 */
public final class Announcer implements Listener {
    private final Server server;
    private final BloodNights nights;
    private final Supplier<Announcements> announcements;
    private final Function<World, WorldSettings> settings;
    private final Function<World, NightTimes> times;
    private final NightBars bars;
    private final Ambience ambience = new Ambience();

    /**
     * @param server        the server
     * @param nights        the running blood nights
     * @param announcements the server-wide announcement settings
     * @param settings      the settings of a world
     * @param times         the night times of a world
     */
    public Announcer(
            Server server,
            BloodNights nights,
            Supplier<Announcements> announcements,
            Function<World, WorldSettings> settings,
            Function<World, NightTimes> times) {
        this.server = server;
        this.nights = nights;
        this.announcements = announcements;
        this.settings = settings;
        this.times = times;
        this.bars = new NightBars(server::getOnlinePlayers);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBegin(BloodNightBeginEvent event) {
        World world = event.getWorld();
        WorldSettings worldSettings = settings.apply(world);
        broadcast(world, Messages.NIGHT_BEGIN);
        Ambience.play(
                world,
                worldSettings.soundSettings(),
                worldSettings.soundSettings().begin());
        applyEffect(world, worldSettings.announcement().effect());
        bars.update(world, worldSettings.announcement().bossBar(), times.apply(world), event.night());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onEnd(BloodNightEndEvent event) {
        World world = event.getWorld();
        WorldSettings worldSettings = settings.apply(world);
        bars.remove(world);
        world.getPlayers().forEach(ambience::forget);
        broadcast(world, Messages.NIGHT_END);
        Ambience.play(
                world,
                worldSettings.soundSettings(),
                worldSettings.soundSettings().end());
    }

    @EventHandler
    public void onChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World from = event.getFrom();
        bars.hide(from, player);
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
     * Updates the boss bars of running blood nights and plays ambience; runs every second.
     */
    public void tick() {
        for (World world : server.getWorlds()) {
            var night = nights.running(world);
            if (night.isEmpty()) {
                bars.remove(world);
                continue;
            }
            WorldSettings worldSettings = settings.apply(world);
            bars.update(world, worldSettings.announcement().bossBar(), times.apply(world), night.getAsLong());
            world.getPlayers().forEach(player -> ambience.tick(player, worldSettings.soundSettings()));
        }
    }

    /**
     * Hides every boss bar, e.g. when the plugin disables.
     */
    public void clear() {
        bars.clear();
    }

    private void entered(Player player) {
        World world = player.getWorld();
        var night = nights.running(world);
        if (night.isEmpty()) return;
        WorldSettings worldSettings = settings.apply(world);
        if (worldSettings.announcement().joinWarning()) tell(player, Messages.NIGHT_JOINED);
        applyEffect(player, worldSettings.announcement().effect());
        bars.update(world, worldSettings.announcement().bossBar(), times.apply(world), night.getAsLong());
    }

    private void broadcast(World world, String key) {
        Announcements config = announcements.get();
        Audience audience =
                switch (config.reach()) {
                    case SERVER -> Audience.audience(server.getOnlinePlayers());
                    case WORLD -> Audience.audience(world.getPlayers());
                    case NONE -> Audience.empty();
                };
        Delivery.send(
                audience,
                config.method(),
                config.prefix(),
                Messages.message(key, Argument.string("world", world.getName())));
    }

    private void tell(Player player, String key) {
        Announcements config = announcements.get();
        Delivery.send(
                player,
                config.personalMethod(),
                config.prefix(),
                Messages.message(key, Argument.string("world", player.getWorld().getName())));
    }

    private static void applyEffect(World world, NightEffect effect) {
        world.getPlayers().forEach(player -> applyEffect(player, effect));
    }

    private static void applyEffect(Player player, NightEffect effect) {
        if (!effect.enabled()) return;
        PotionEffectType type = effectType(effect.effect());
        if (type == null) return;
        player.addPotionEffect(new PotionEffect(type, effect.seconds() * 20, 0, true, false));
    }

    private static @Nullable PotionEffectType effectType(String key) {
        NamespacedKey namespaced = NamespacedKey.fromString(key);
        if (namespaced == null) return null;
        return RegistryAccess.registryAccess()
                .getRegistry(RegistryKey.MOB_EFFECT)
                .get(namespaced);
    }
}
