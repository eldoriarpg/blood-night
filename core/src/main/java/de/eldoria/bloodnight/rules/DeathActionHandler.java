/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.rules;

import de.eldoria.bloodnight.configuration.elements.WorldSettings;
import de.eldoria.bloodnight.configuration.elements.world.death.DeathActions;
import de.eldoria.bloodnight.configuration.elements.world.death.LightningSettings;
import de.eldoria.bloodnight.configuration.elements.world.death.PlayerDeathActions;
import de.eldoria.bloodnight.configuration.elements.world.death.TimedEffect;
import de.eldoria.bloodnight.night.BloodNights;
import de.eldoria.bloodnight.util.Effects;
import de.eldoria.bloodnight.util.MobTags;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Enemy;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.function.Function;
import java.util.random.RandomGenerator;

/**
 * Runs a world's death actions during a blood night: lightning and shockwave where a player or any enemy dies (not
 * for mobs from ignored spawn reasons), and for players the loss of items and experience by chance, death commands,
 * and respawn effects and commands when they respawn into a world with a running blood night.
 */
public final class DeathActionHandler implements Listener {
    private static final Sound THUNDER =
            Sound.sound(Key.key("entity.lightning_bolt.thunder"), Sound.Source.WEATHER, 1, 1);
    private final Server server;
    private final Plugin plugin;
    private final BloodNights nights;
    private final Function<World, WorldSettings> settings;
    private final RandomGenerator random;

    /**
     * @param plugin   schedules commands and respawn actions for the next tick
     * @param nights   the running blood nights
     * @param settings the settings of a world
     * @param random   decides the chances
     */
    public DeathActionHandler(
            Plugin plugin, BloodNights nights, Function<World, WorldSettings> settings, RandomGenerator random) {
        this.server = plugin.getServer();
        this.plugin = plugin;
        this.nights = nights;
        this.settings = settings;
        this.random = random;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnemyDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Enemy) || MobTags.isExtension(entity)) return;
        World world = entity.getWorld();
        if (nights.running(world).isEmpty()) return;
        WorldSettings worldSettings = settings.apply(world);
        if (worldSettings.mobSettings().spawning().ignoredReasons().contains(entity.getEntitySpawnReason())) return;
        perform(entity.getLocation(), worldSettings.deathActions().mob());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        if (nights.running(world).isEmpty()) return;
        PlayerDeathActions actions = settings.apply(world).deathActions().player();
        perform(player.getLocation(), actions);
        if (chance(actions.loseInventoryChance())) event.getDrops().clear();
        if (chance(actions.loseExperienceChance())) event.setDroppedExp(0);
        server.getScheduler().runTask(plugin, () -> run(actions.deathCommands(), player, world));
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        server.getScheduler().runTask(plugin, () -> respawned(player));
    }

    private void respawned(Player player) {
        World world = player.getWorld();
        if (!player.isOnline() || nights.running(world).isEmpty()) return;
        PlayerDeathActions actions = settings.apply(world).deathActions().player();
        for (TimedEffect effect : actions.respawnEffects()) {
            PotionEffectType type = Effects.effect(effect.effect());
            if (type != null)
                player.addPotionEffect(new PotionEffect(type, (int) Math.round(effect.seconds() * 20), 1));
        }
        run(actions.respawnCommands(), player, world);
    }

    private void perform(Location location, DeathActions actions) {
        if (chance(actions.shockwave().chance())) Shockwave.dispatch(location, actions.shockwave(), random);
        lightning(location, actions.lightning());
    }

    private void lightning(Location location, LightningSettings lightning) {
        if (lightning.lightning() && chance(lightning.lightningChance())) {
            location.getWorld().strikeLightningEffect(location.clone().add(0, 10, 0));
        } else if (lightning.thunder() && chance(lightning.thunderChance())) {
            location.getWorld().getPlayers().forEach(player -> player.playSound(THUNDER));
        }
    }

    private void run(List<String> commands, Player player, World world) {
        for (String command : commands) {
            server.dispatchCommand(
                    server.getConsoleSender(),
                    command.replace("{player}", player.getName()).replace("{world}", world.getName()));
        }
    }

    private boolean chance(int percent) {
        return percent > random.nextInt(100);
    }
}
