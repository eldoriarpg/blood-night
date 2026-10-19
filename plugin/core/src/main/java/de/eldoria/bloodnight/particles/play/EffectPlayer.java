/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles.play;

import de.eldoria.bloodnight.configuration.server.ParticleSettings;
import de.eldoria.bloodnight.particles.EffectLibrary;
import de.eldoria.bloodnight.particles.effect.Anchor;
import de.eldoria.bloodnight.particles.effect.Emitter;
import de.eldoria.bloodnight.particles.effect.ParticleEffect;
import de.eldoria.bloodnight.particles.effect.Piece;
import de.eldoria.bloodnight.particles.piece.PieceRun;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

/**
 * Plays particle effects: once, on a loop of the source's thread (an entity's scheduler, or the region of a place),
 * and over and over for the effect a mob wears, inside the mob's own tick so it costs no task of its own.
 * <p>
 * A mob plays at most as many effects at once as {@link ParticleSettings#perMob()} says, the one it wears included;
 * more do not play. The effect a mob wears skips its ticks while no player is near enough to see it.
 */
public final class EffectPlayer implements Effects {
    private final EffectLibrary library;
    private final Stage stage;
    private final Plugin plugin;
    private final Map<UUID, AtomicInteger> running = new ConcurrentHashMap<>();
    private final Map<UUID, Worn> worn = new ConcurrentHashMap<>();

    /**
     * The effect a mob wears and its current play.
     */
    private record Worn(String id, Play play, Runnable release) {}

    /**
     * @param library every effect by id
     * @param stage   what plays need
     * @param plugin  the plugin owning the displays of pieces
     */
    public EffectPlayer(EffectLibrary library, Stage stage, Plugin plugin) {
        this.library = library;
        this.stage = stage;
        this.plugin = plugin;
    }

    @Override
    public long play(String id, Object source, @Nullable Object target, @Nullable Player viewer, @Nullable UUID owner) {
        ParticleEffect effect = library.get(id);
        return effect == null ? -1 : play(effect, source, target, viewer, owner);
    }

    /**
     * Plays an effect once, e.g. one not saved yet, from the editor.
     *
     * @see Effects#play(String, Object, Object, Player, UUID)
     */
    public long play(
            ParticleEffect effect,
            Object source,
            @Nullable Object target,
            @Nullable Player viewer,
            @Nullable UUID owner) {
        if (!stage.settings().get().enabled()) return -1;
        End from = End.of(source);
        if (from == null) return -1;
        End to = End.of(target);
        Runnable release = claim(owner);
        if (release == null) return -1;
        Play play = new Play(effect, from, to, viewer, false, stage);
        if (!PieceRun.start(play, stage, plugin, owner)) {
            release.run();
            return -1;
        }
        BooleanSupplier step = () -> {
            boolean more = play.step();
            if (!more) release.run();
            return more;
        };
        Entity entity = from.entity();
        if (entity != null) stage.tasks().entityLoop(entity, step, release);
        else stage.tasks().locationLoop(from.location(), step);
        return to == null ? 0 : arrival(effect);
    }

    @Override
    public void spawned(LivingEntity mob, @Nullable String id) {
        if (id != null) play(id, mob, null, null, mob.getUniqueId());
    }

    @Override
    public void ticked(LivingEntity mob, @Nullable String ambient) {
        ParticleSettings settings = stage.settings().get();
        if (ambient == null || !settings.enabled() || !settings.ambient()) {
            forget(mob.getUniqueId());
            return;
        }
        Worn current = worn.get(mob.getUniqueId());
        if (current == null || !current.id().equals(ambient)) {
            forget(mob.getUniqueId());
            current = wear(mob, ambient);
            if (current == null) return;
        }
        End source = current.play().source();
        if (stage.viewers()
                .near(
                        mob.getWorld(),
                        source.point(Anchor.BodyPoint.CENTER),
                        current.play().effect().viewDistance(settings.viewDistance()),
                        null,
                        List.of())
                .isEmpty()) {
            return;
        }
        if (!current.play().step()) {
            Play next = new Play(current.play().effect(), source, null, null, true, stage);
            PieceRun.start(next, stage, plugin, mob.getUniqueId());
            worn.put(mob.getUniqueId(), new Worn(ambient, next, current.release()));
        }
    }

    @Override
    public void died(LivingEntity mob, @Nullable String id) {
        forget(mob.getUniqueId());
        if (id != null) play(id, mob.getLocation(), null, null, null);
    }

    @Override
    public void forget(UUID mob) {
        Worn gone = worn.remove(mob);
        if (gone != null) gone.release().run();
    }

    private @Nullable Worn wear(LivingEntity mob, String id) {
        ParticleEffect effect = library.get(id);
        End source = End.of(mob);
        if (effect == null || source == null) return null;
        Runnable release = claim(mob.getUniqueId());
        if (release == null) return null;
        Play play = new Play(effect, source, null, null, true, stage);
        PieceRun.start(play, stage, plugin, mob.getUniqueId());
        Worn wearing = new Worn(id, play, release);
        worn.put(mob.getUniqueId(), wearing);
        return wearing;
    }

    /**
     * Takes one of a mob's effect slots.
     *
     * @return what frees it again, once; {@code null} when the mob has none left
     */
    private @Nullable Runnable claim(@Nullable UUID owner) {
        if (owner == null) return () -> {};
        AtomicInteger count = running.computeIfAbsent(owner, ignored -> new AtomicInteger());
        if (count.incrementAndGet() > stage.settings().get().perMob()) {
            free(owner, count);
            return null;
        }
        AtomicBoolean freed = new AtomicBoolean();
        return () -> {
            if (freed.compareAndSet(false, true)) free(owner, count);
        };
    }

    private void free(UUID owner, AtomicInteger count) {
        if (count.decrementAndGet() <= 0) running.remove(owner, count);
    }

    /**
     * @return ticks until the slowest emitter or piece that travels reaches the target, 0 when none travels
     */
    static long arrival(ParticleEffect effect) {
        long latest = 0;
        for (Emitter emitter : effect.emitters()) {
            var motion = emitter.motion();
            if (motion != null) latest = Math.max(latest, emitter.delay() + motion.ticks());
        }
        for (Piece piece : effect.pieces()) latest = Math.max(latest, PieceRun.arrival(piece));
        return latest;
    }
}
