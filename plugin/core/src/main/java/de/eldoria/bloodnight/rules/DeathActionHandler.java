/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.rules;

import de.eldoria.bloodnight.api.event.DeathAction;
import de.eldoria.bloodnight.api.event.DeathActionEvent;
import de.eldoria.bloodnight.configuration.elements.WorldSettings;
import de.eldoria.bloodnight.configuration.elements.world.death.DeathActions;
import de.eldoria.bloodnight.configuration.elements.world.death.LightningSettings;
import de.eldoria.bloodnight.configuration.elements.world.death.PlayerDeathActions;
import de.eldoria.bloodnight.configuration.elements.world.death.TimedEffect;
import de.eldoria.bloodnight.integration.placeholders.CommandText;
import de.eldoria.bloodnight.integration.regions.RegionFlag;
import de.eldoria.bloodnight.integration.regions.RegionRules;
import de.eldoria.bloodnight.mobs.ForeignMobs;
import de.eldoria.bloodnight.night.BloodNights;
import de.eldoria.bloodnight.tasks.Tasks;
import de.eldoria.bloodnight.util.Commands;
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
    private final Tasks tasks;
    private final BloodNights nights;
    private final Function<World, WorldSettings> settings;
    private final RandomGenerator random;
    private final CommandText text;
    private final ForeignMobs foreign;
    private final RegionRules regions;

    /**
     * @param server   runs the commands
     * @param tasks    runs commands on the global region and respawn actions on the player's thread, next tick
     * @param nights   the running blood nights
     * @param settings the settings of a world
     * @param text     fills in other plugins' placeholders
     * @param foreign  tells mobs of other plugins, which cause no death actions
     * @param random   decides the chances; used from several threads
     * @param regions  where regions switch the death actions off
     */
    public DeathActionHandler(
            Server server,
            Tasks tasks,
            BloodNights nights,
            Function<World, WorldSettings> settings,
            CommandText text,
            ForeignMobs foreign,
            RandomGenerator random,
            RegionRules regions) {
        this.regions = regions;
        this.server = server;
        this.text = text;
        this.foreign = foreign;
        this.tasks = tasks;
        this.nights = nights;
        this.settings = settings;
        this.random = random;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEnemyDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Enemy) || MobTags.isExtension(entity) || !foreign.ruled(entity)) return;
        if (!regions.allows(entity.getLocation(), RegionFlag.RULES)) return;
        World world = entity.getWorld();
        if (nights.running(world).isEmpty()) return;
        WorldSettings worldSettings = settings.apply(world);
        if (worldSettings.mobSettings().spawning().ignoredReasons().contains(entity.getEntitySpawnReason())) return;
        perform(entity, worldSettings.deathActions().mob());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        if (nights.running(world).isEmpty() || !regions.allows(player.getLocation(), RegionFlag.RULES)) return;
        PlayerDeathActions actions = settings.apply(world).deathActions().player();
        perform(player, actions);
        if (chance(actions.loseInventoryChance())) event.getDrops().clear();
        if (chance(actions.loseExperienceChance())) event.setDroppedExp(0);
        String name = player.getName();
        tasks.global(() -> run(actions.deathCommands(), name, player, world));
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        tasks.entity(player, () -> respawned(player));
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
        String name = player.getName();
        tasks.global(() -> run(actions.respawnCommands(), name, player, world));
    }

    private void perform(LivingEntity dead, DeathActions actions) {
        Location location = dead.getLocation();
        if (chance(actions.shockwave().chance()) && allowed(dead, DeathAction.SHOCKWAVE)) {
            Shockwave.dispatch(location, actions.shockwave(), random);
        }
        lightning(dead, location, actions.lightning());
    }

    private void lightning(LivingEntity dead, Location location, LightningSettings lightning) {
        if (lightning.lightning() && chance(lightning.lightningChance())) {
            if (allowed(dead, DeathAction.LIGHTNING)) {
                location.getWorld().strikeLightningEffect(location.clone().add(0, 10, 0));
            }
        } else if (lightning.thunder() && chance(lightning.thunderChance()) && allowed(dead, DeathAction.THUNDER)) {
            location.getWorld()
                    .getPlayers()
                    .forEach(player -> tasks.entityNow(player, () -> player.playSound(THUNDER)));
        }
    }

    private void run(List<String> commands, String name, Player player, World world) {
        for (String command : commands) {
            server.dispatchCommand(
                    server.getConsoleSender(),
                    text.resolve(
                            Commands.withoutSlash(command)
                                    .replace("{player}", name)
                                    .replace("{world}", world.getName()),
                            player));
        }
    }

    /**
     * Asks other plugins whether a death action may happen.
     */
    private boolean allowed(LivingEntity dead, DeathAction action) {
        DeathActionEvent event = new DeathActionEvent(dead, action);
        server.getPluginManager().callEvent(event);
        return !event.isCancelled();
    }

    private boolean chance(int percent) {
        return percent > random.nextInt(100);
    }
}
