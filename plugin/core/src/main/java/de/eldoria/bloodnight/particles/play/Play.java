/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.particles.play;

import com.destroystokyo.paper.ParticleBuilder;
import de.eldoria.bloodnight.particles.EffectChecks;
import de.eldoria.bloodnight.particles.data.DataKind;
import de.eldoria.bloodnight.particles.data.ParticleDatas;
import de.eldoria.bloodnight.particles.effect.Anchor;
import de.eldoria.bloodnight.particles.effect.Emitter;
import de.eldoria.bloodnight.particles.effect.Motion;
import de.eldoria.bloodnight.particles.effect.ParticleEffect;
import de.eldoria.bloodnight.particles.geometry.Frame;
import de.eldoria.bloodnight.particles.geometry.Moment;
import de.eldoria.bloodnight.particles.geometry.Shape;
import de.eldoria.bloodnight.particles.geometry.Shapes;
import de.eldoria.bloodnight.particles.geometry.Vec3;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicReferenceArray;

/**
 * One running play of an effect's emitters, stepped once a tick by whoever owns it: a loop on its source's thread, or
 * the tick of the mob wearing it. Its pieces run on their own and tell it where they are.
 */
public final class Play {
    private final ParticleEffect effect;
    private final End source;
    private final @Nullable End target;
    private final @Nullable Player viewer;
    private final List<Player> skip;
    private final boolean worn;
    private final Stage stage;
    private final AtomicReferenceArray<@Nullable Vec3> pieces;
    private final boolean[] broken;
    private int tick;

    /**
     * @param effect the effect
     * @param source where it plays
     * @param target the other end, if any
     * @param viewer the only player who sees it, or {@code null}
     * @param worn   whether a mob wears it, which limits it more
     * @param stage  what plays need
     */
    public Play(
            ParticleEffect effect,
            End source,
            @Nullable End target,
            @Nullable Player viewer,
            boolean worn,
            Stage stage) {
        this.effect = effect;
        this.source = source;
        this.target = target;
        this.viewer = viewer;
        this.worn = worn;
        this.stage = stage;
        this.pieces = new AtomicReferenceArray<>(effect.pieces().size());
        this.broken = new boolean[effect.emitters().size()];
        this.skip = new ArrayList<>();
        if (!effect.self()) {
            if (source.player() != null) skip.add(source.player());
            if (target != null && target.player() != null) skip.add(target.player());
        }
    }

    /**
     * @return the effect
     */
    public ParticleEffect effect() {
        return effect;
    }

    /**
     * @return where it plays
     */
    public End source() {
        return source;
    }

    /**
     * @return the other end, if any
     */
    public @Nullable End target() {
        return target;
    }

    /**
     * @return the only player who sees it, or {@code null}
     */
    public @Nullable Player viewer() {
        return viewer;
    }

    /**
     * @return the players who do not see it
     */
    public List<Player> skip() {
        return skip;
    }

    /**
     * Tells the play where one of its pieces is now, so emitters on the piece follow it.
     */
    public void piece(int index, @Nullable Vec3 position) {
        if (index < pieces.length()) pieces.set(index, position);
    }

    /**
     * Draws this tick's emissions.
     *
     * @return whether the play goes on
     */
    public boolean step() {
        if (source.gone() || tick > effect.duration()) return false;
        for (int index = 0; index < effect.emitters().size(); index++) {
            if (!broken[index]) emit(index, effect.emitters().get(index));
        }
        tick++;
        return tick <= effect.duration();
    }

    private void emit(int index, Emitter emitter) {
        int since = tick - emitter.delay();
        if (since < 0 || since % emitter.every() != 0) return;
        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (emitter.chance() < 1 && random.nextDouble() >= emitter.chance()) return;
        Anchor anchor = emitter.anchor();
        End own = anchor.on() == Anchor.EffectEnd.TARGET ? target : source;
        End other = own == source ? target : source;
        if (own == null) return;
        Vec3 origin = anchor.on() == Anchor.EffectEnd.PIECE ? pieces.get(anchor.piece()) : own.point(anchor.at());
        if (origin == null) return;
        Vec3 far = other == null ? null : other.point(anchor.aimed());
        Shape shape = emitter.shape();
        Frame frame = Frame.of(origin, own.forward(shape.facing(), origin, far));
        Vec3 base = frame.world(emitter.offset());
        Motion motion = emitter.motion();
        if (motion != null && far != null) base = motion.position(base, far, since);
        Moment moment = new Moment(
                effect.duration() == 0 ? 1 : (double) tick / effect.duration(), since / emitter.every(), tick);
        List<Vec3> points = shape.betweenEnds()
                ? Shapes.between(shape, base, far != null ? far : frame.at(base).world(shape.to()), moment, random)
                : Shapes.around(shape, frame.at(base), moment);
        if (points.isEmpty()) return;
        try {
            send(emitter, frame, own.world(), base, points);
        } catch (IllegalArgumentException e) {
            broken[index] = true;
        }
    }

    private void send(Emitter emitter, Frame frame, World world, Vec3 base, List<Vec3> points) {
        Particle particle = EffectChecks.particle(emitter.particle());
        if (particle == null) throw new IllegalArgumentException("Unknown particle " + emitter.particle());
        List<Player> seeing =
                stage.viewers().near(world, base, stage.settings().get().viewDistance(), viewer, skip);
        if (seeing.isEmpty()) return;
        if (!stage.budget().take(points.size() * emitter.count() * seeing.size(), worn)) return;
        boolean trail = DataKind.of(particle) == DataKind.TRAIL;
        Object data = trail ? null : ParticleDatas.build(particle, emitter.data(), null, stage.items());
        ParticleBuilder builder = new ParticleBuilder(particle)
                .count(emitter.count())
                .offset(
                        emitter.spread().x(),
                        emitter.spread().y(),
                        emitter.spread().z())
                .extra(emitter.speed())
                .receivers(seeing);
        for (Vec3 point : points) {
            if (trail)
                data = ParticleDatas.build(
                        particle, emitter.data(), trailEnd(world, frame, point, emitter), stage.items());
            builder.location(world, point.x(), point.y(), point.z()).data(data).spawn();
        }
    }

    private static Location trailEnd(World world, Frame frame, Vec3 point, Emitter emitter) {
        Vec3 toward = emitter.data().toward();
        Vec3 end = frame.at(point).world(toward == null ? new Vec3(0, 1, 0) : toward);
        return new Location(world, end.x(), end.y(), end.z());
    }
}
