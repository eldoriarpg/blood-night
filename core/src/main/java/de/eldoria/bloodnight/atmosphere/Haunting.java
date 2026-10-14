/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.atmosphere;

import de.eldoria.bloodnight.configuration.elements.world.AtmosphereSettings;
import de.eldoria.bloodnight.configuration.elements.world.atmosphere.AmbientParticle;
import de.eldoria.bloodnight.configuration.elements.world.atmosphere.Scare;
import de.eldoria.bloodnight.night.BloodNights;
import de.eldoria.bloodnight.tasks.Tasks;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Server;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * What a blood night does to each player in its world, on their own client, as the world's atmosphere settings say: a
 * red screen edge, short darkness pulses, rain, particles around the camera, a heartbeat, the resource pack's screen
 * effect and, when switched on, scares. Everything ends with the night, when the player leaves the world or quits.
 * <p>
 * The red edge is a world border only this player sees, far around them, with a warning distance that reaches them:
 * the client tints the screen the way it warns of a near border. It follows the player before they could come close
 * to it. Darkness pulses are sent as an effect only the client knows of. {@link #tick()} runs every
 * {@value #STEP} ticks on the global region and hands each player to their own thread.
 */
public final class Haunting implements Listener {
    /**
     * Ticks between two steps of {@link #tick()}.
     */
    public static final int STEP = 5;

    private static final int BORDER_RADIUS = 1000;
    private static final int WARNING = 1600;
    private static final int RECENTER = 400;
    private static final double PARTICLE_RADIUS = 8;
    private static final int HEARTBEAT_STEPS = 5;
    private static final int MINUTE_STEPS = 20 * 60 / STEP;
    private static final Sound HEARTBEAT =
            Sound.sound(Key.key("entity.warden.heartbeat"), Sound.Source.AMBIENT, 0.6f, 1f);
    private static final Sound HURT = Sound.sound(Key.key("entity.player.hurt"), Sound.Source.PLAYER, 1f, 1f);
    private static final List<Key> SCREAMS =
            List.of(Key.key("entity.ghast.scream"), Key.key("entity.enderman.scream"), Key.key("entity.fox.screech"));
    private final Server server;
    private final BloodNights nights;
    private final Function<World, AtmosphereSettings> settings;
    private final Tasks tasks;
    private final Supplier<WorldBorder> borders;
    private final Map<UUID, Haunted> haunted = new ConcurrentHashMap<>();

    /**
     * @param server   the server
     * @param nights   the running blood nights
     * @param settings the atmosphere settings of a world
     * @param tasks    runs each player on their thread
     * @param borders  creates a world border no world has, e.g. {@link Server#createWorldBorder()}
     */
    public Haunting(
            Server server,
            BloodNights nights,
            Function<World, AtmosphereSettings> settings,
            Tasks tasks,
            Supplier<WorldBorder> borders) {
        this.server = server;
        this.nights = nights;
        this.settings = settings;
        this.tasks = tasks;
        this.borders = borders;
    }

    /**
     * Haunts the players of worlds with a running blood night and lets go of the others.
     */
    public void tick() {
        for (Player player : server.getOnlinePlayers()) tasks.entityNow(player, () -> tick(player));
    }

    /**
     * Gives every player their ordinary view back, e.g. when the plugin disables.
     */
    public void clear() {
        for (Player player : server.getOnlinePlayers()) tasks.entityNow(player, () -> leave(player));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        haunted.remove(event.getPlayer().getUniqueId());
    }

    private void tick(Player player) {
        World world = player.getWorld();
        Haunted state = haunted.get(player.getUniqueId());
        if (state != null && !state.world.equals(world.getUID())) {
            leave(player);
            state = null;
        }
        if (nights.running(world).isEmpty()) {
            if (state != null) leave(player);
            return;
        }
        AtmosphereSettings atmosphere = settings.apply(world);
        if (state == null) state = enter(player, atmosphere);
        haunt(player, state, atmosphere);
    }

    private Haunted enter(Player player, AtmosphereSettings atmosphere) {
        Haunted state = new Haunted(player.getWorld().getUID());
        haunted.put(player.getUniqueId(), state);
        if (atmosphere.vignette()) {
            state.border = borders.get();
            state.border.setSize(BORDER_RADIUS * 2);
            state.border.setWarningDistance(WARNING);
            centre(player, state);
            player.setWorldBorder(state.border);
        }
        if (atmosphere.rain()) player.setPlayerWeather(WeatherType.DOWNFALL);
        if (atmosphere.screenEffect()) ScreenEffect.show(player);
        state.nextPulse = pulseDelay();
        return state;
    }

    private void leave(Player player) {
        Haunted state = haunted.remove(player.getUniqueId());
        if (state == null) return;
        if (state.border != null) player.setWorldBorder(null);
        player.resetPlayerWeather();
        ScreenEffect.hide(player);
    }

    private void haunt(Player player, Haunted state, AtmosphereSettings atmosphere) {
        long step = state.steps++;
        if (state.border != null && distance(player, state.border) > RECENTER) centre(player, state);
        particles(player, atmosphere.particles());
        if (atmosphere.darknessPulses() && step >= state.nextPulse) {
            player.sendPotionEffectChange(player, new PotionEffect(PotionEffectType.DARKNESS, 80, 0, false, false));
            state.nextPulse = step + pulseDelay();
        }
        if (atmosphere.heartbeat() && step % HEARTBEAT_STEPS == 0) player.playSound(HEARTBEAT, Sound.Emitter.self());
        if (atmosphere.scares().enabled()
                && step % MINUTE_STEPS == MINUTE_STEPS - 1
                && ThreadLocalRandom.current().nextInt(100)
                        < atmosphere.scares().chancePerMinute()) {
            scare(player, atmosphere.scares().kinds().stream().toList());
        }
    }

    private void particles(Player player, List<AmbientParticle> particles) {
        Location eye = player.getEyeLocation();
        ThreadLocalRandom random = ThreadLocalRandom.current();
        for (AmbientParticle ambient : particles) {
            Particle particle = particle(ambient.particle());
            if (particle == null || particle.getDataType() != Void.class) continue;
            double perStep = ambient.perSecond() * STEP / 20.0;
            int count = (int) perStep + (random.nextDouble() < perStep % 1 ? 1 : 0);
            for (int i = 0; i < count; i++) {
                Vector offset = new Vector(random.nextGaussian(), random.nextGaussian() * 0.5, random.nextGaussian())
                        .multiply(PARTICLE_RADIUS / 2);
                player.spawnParticle(particle, eye.clone().add(offset), 1, 0, 0, 0, 0);
            }
        }
    }

    private void scare(Player player, List<Scare> kinds) {
        if (kinds.isEmpty()) return;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        switch (kinds.get(random.nextInt(kinds.size()))) {
            case FLINCH -> {
                player.sendHurtAnimation(180);
                player.playSound(HURT, Sound.Emitter.self());
            }
            case SCREAM -> {
                Location behind = player.getLocation()
                        .add(player.getLocation().getDirection().multiply(-12));
                player.playSound(
                        Sound.sound(SCREAMS.get(random.nextInt(SCREAMS.size())), Sound.Source.HOSTILE, 1.5f, 0.6f),
                        behind.getX(),
                        behind.getY(),
                        behind.getZ());
            }
            case GUARDIAN -> player.showElderGuardian(false);
        }
    }

    private static void centre(Player player, Haunted state) {
        if (state.border == null) return;
        state.border.setCenter(player.getLocation().getX(), player.getLocation().getZ());
    }

    private static double distance(Player player, WorldBorder border) {
        Location centre = border.getCenter();
        return Math.hypot(
                player.getLocation().getX() - centre.getX(),
                player.getLocation().getZ() - centre.getZ());
    }

    private static long pulseDelay() {
        return ThreadLocalRandom.current().nextLong(20 * 20 / STEP, 45 * 20 / STEP);
    }

    private static @Nullable Particle particle(String key) {
        NamespacedKey namespaced = NamespacedKey.fromString(key);
        return namespaced == null
                ? null
                : RegistryAccess.registryAccess()
                        .getRegistry(RegistryKey.PARTICLE_TYPE)
                        .get(namespaced);
    }

    /**
     * What a haunted player has: the world it began in, the border only they see, and when their next pulse comes.
     * Only touched on the player's thread.
     */
    private static final class Haunted {
        private final UUID world;
        private @Nullable WorldBorder border;
        private long steps;
        private long nextPulse;

        private Haunted(UUID world) {
            this.world = world;
        }
    }
}
