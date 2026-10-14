/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.mobs;

import de.eldoria.bloodnight.api.CompiledBehaviour;
import de.eldoria.bloodnight.behaviour.Behaviours;
import de.eldoria.bloodnight.items.ItemRegistry;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.bloodnight.mob.meta.NightEnd;
import de.eldoria.bloodnight.tasks.Tasks;
import de.eldoria.bloodnight.util.MobTags;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Holds every living custom mob and its behaviour instance, and runs each mob on its own entity's thread.
 * <p>
 * Each mob gets its own instance of its compiled behaviour, so state like cooldowns is per mob. Every mob has a task on
 * its entity's scheduler that runs its tick entry each tick. The same task notices when the blood night of the mob's
 * world ends: it runs the night end entry once, and a mob that spawned in that night is removed or turned back into
 * an ordinary mob, as its {@link NightEnd} says, within the following second (spread over the second, so a night end with many mobs does not stall a tick). On Folia
 * each mob therefore only ever runs on the thread of its region; on Paper all of it runs on the main thread. A mob in a
 * chunk that is loaded but does not tick runs nothing, and goes once it ticks again.
 * <p>
 * After a reload, {@link #rebind} gives every living mob a fresh instance of its new behaviour; nothing references the
 * old classes afterwards, so their class loaders can be collected. Exceptions thrown by a behaviour are logged once per
 * mob and message, then swallowed, so one broken graph cannot stop other mobs.
 */
public final class MobCoordinator {
    private static final CompiledBehaviour NONE = new CompiledBehaviour() {};
    private static final int SPREAD = 20;

    private final Map<UUID, Tracked> mobs = new ConcurrentHashMap<>();
    private final Map<UUID, UUID> baseByExtension = new ConcurrentHashMap<>();
    private final Set<String> reported = ConcurrentHashMap.newKeySet();
    private final Map<String, List<String>> failures = new ConcurrentHashMap<>();
    private final MobOutfitter outfitter;
    private final Supplier<ItemRegistry> items;
    private final Function<String, CustomMob> definitions;
    private final Function<World, OptionalLong> runningNight;
    private final Tasks tasks;
    private final Logger logger;
    private volatile Behaviours behaviours = Behaviours.empty();

    /**
     * @param outfitter    turns entities into custom mobs
     * @param items        the current item registry
     * @param definitions  mob definition by id, or {@code null}
     * @param runningNight the number of the blood night running in a world, if any
     * @param tasks        runs work on the mobs' threads
     * @param logger       receives behaviour failures
     */
    public MobCoordinator(
            MobOutfitter outfitter,
            Supplier<ItemRegistry> items,
            Function<String, CustomMob> definitions,
            Function<World, OptionalLong> runningNight,
            Tasks tasks,
            Logger logger) {
        this.outfitter = outfitter;
        this.items = items;
        this.definitions = definitions;
        this.runningNight = runningNight;
        this.tasks = tasks;
        this.logger = logger;
    }

    /**
     * Turns an entity into a custom mob: outfits it, gives it its behaviour and runs the behaviour's spawn entry. Runs
     * on the entity's thread.
     *
     * @param entity the entity
     * @param mob    the mob definition
     * @return the active mob
     */
    public ActiveMob register(LivingEntity entity, CustomMob mob) {
        Entity extension = outfitter.outfit(entity, mob);
        ActiveMob active = track(entity, mob, extension);
        guard(active, "mob.spawn", () -> active.behaviour().onSpawn());
        return active;
    }

    /**
     * Takes over an entity that already is a custom mob, e.g. after its chunk was loaded again. An extension carries
     * its mob's id too, but is never a mob of its own. Runs on the entity's thread.
     *
     * @param entity the entity
     * @return the active mob, if the entity is tagged with a known mob
     */
    public Optional<ActiveMob> attach(LivingEntity entity) {
        Tracked known = mobs.get(entity.getUniqueId());
        if (known != null) return Optional.of(known.active);
        if (MobTags.isExtension(entity)) return Optional.empty();
        Optional<CustomMob> mob = MobTags.getCustomMobId(entity).map(definitions);
        if (mob.isEmpty()) return Optional.empty();
        Entity extension = MobTags.getExtension(entity)
                .map(id -> entity.getServer().getEntity(id))
                .orElse(null);
        return Optional.of(track(entity, mob.get(), extension));
    }

    /**
     * @param entity any entity
     * @return the behaviour, if the entity is a custom mob
     */
    public Optional<CompiledBehaviour> behaviour(Entity entity) {
        return mob(entity).map(ActiveMob::behaviour);
    }

    /**
     * @param entity any entity
     * @return the behaviour of the mob the entity rides or carries, if it is a custom mob's extension
     */
    public Optional<CompiledBehaviour> extensionBehaviour(Entity entity) {
        return base(entity).map(ActiveMob::behaviour);
    }

    /**
     * @param entity any entity
     * @return the active mob, if the entity is one
     */
    public Optional<ActiveMob> mob(Entity entity) {
        Tracked tracked = mobs.get(entity.getUniqueId());
        return tracked == null ? Optional.empty() : Optional.of(tracked.active);
    }

    /**
     * Forgets an entity, e.g. when it died or was unloaded.
     *
     * @param entity the entity
     */
    public void remove(Entity entity) {
        forget(entity.getUniqueId());
    }

    /**
     * Fires the debug entry, each mob on its own thread.
     *
     * @param mobId only mobs of this id, or {@code null} for all
     * @return how many mobs were triggered
     */
    public int debug(@Nullable String mobId) {
        int count = 0;
        for (Tracked tracked : mobs.values()) {
            ActiveMob mob = tracked.active;
            if (mobId != null && !mob.mob().id().equals(mobId)) continue;
            tasks.entity(
                    mob.entity(),
                    () -> guard(current(mob), "debug.trigger", () -> current(mob)
                            .behaviour()
                            .onDebug()));
            count++;
        }
        return count;
    }

    /**
     * Switches to new behaviours: every living mob gets a fresh instance, built from its current definition. A mob
     * running right now finishes with its old instance.
     *
     * @param behaviours the newly compiled behaviours
     */
    public void rebind(Behaviours behaviours) {
        this.behaviours = behaviours;
        reported.clear();
        failures.clear();
        for (Tracked tracked : mobs.values()) {
            ActiveMob mob = tracked.active;
            CustomMob definition =
                    Optional.ofNullable(definitions.apply(mob.mob().id())).orElse(mob.mob());
            tracked.active = new ActiveMob(
                    definition, mob.entity(), mob.extension(), instantiate(definition, mob.entity(), mob.extension()));
        }
    }

    /**
     * @return every active mob
     */
    public Collection<ActiveMob> active() {
        return mobs.values().stream().map(tracked -> tracked.active).toList();
    }

    private ActiveMob track(LivingEntity entity, CustomMob mob, @Nullable Entity extension) {
        ActiveMob active = new ActiveMob(mob, entity, extension, instantiate(mob, entity, extension));
        UUID id = entity.getUniqueId();
        Tracked tracked =
                new Tracked(active, runningNight.apply(entity.getWorld()), Math.floorMod(id.hashCode(), SPREAD));
        mobs.put(id, tracked);
        if (extension != null) baseByExtension.put(extension.getUniqueId(), id);
        tasks.entityLoop(entity, () -> tick(id, tracked), () -> mobs.remove(id, tracked));
        return active;
    }

    /**
     * One tick of a mob, on its thread.
     *
     * @return whether the mob goes on: false once it is forgotten, taken over by a newer tracking, gone or despawned
     */
    private boolean tick(UUID id, Tracked tracked) {
        if (mobs.get(id) != tracked) return false;
        ActiveMob mob = tracked.active;
        if (!mob.entity().isValid()) {
            forget(id);
            return false;
        }
        OptionalLong running = runningNight.apply(mob.entity().getWorld());
        if (tracked.seenNight.isPresent() && !tracked.seenNight.equals(running)) {
            guard(mob, "mob.night_end", () -> mob.behaviour().onNightEnd());
        }
        tracked.seenNight = running;
        if (tracked.age++ % SPREAD == tracked.offset && expired(mob, running)) {
            if (mob.mob().spawn().nightEnd() == NightEnd.REVERT) {
                revert(mob);
            } else {
                despawn(mob);
            }
            return false;
        }
        guard(mob, "mob.tick", () -> mob.behaviour().onTick());
        return true;
    }

    private static boolean expired(ActiveMob mob, OptionalLong running) {
        if (mob.mob().spawn().nightEnd() == NightEnd.KEEP) return false;
        OptionalLong night = MobTags.getNight(mob.entity());
        if (night.isEmpty()) return false;
        return running.isEmpty() || running.getAsLong() != night.getAsLong();
    }

    private void despawn(ActiveMob mob) {
        Entity extension = mob.extension();
        if (extension != null) tasks.entityNow(extension, extension::remove);
        mob.entity().remove();
        forget(mob.entity().getUniqueId());
    }

    private void revert(ActiveMob mob) {
        Entity extension = mob.extension();
        if (extension != null) tasks.entityNow(extension, extension::remove);
        MobOutfitter.revert(mob.entity());
        forget(mob.entity().getUniqueId());
    }

    private void forget(UUID id) {
        Tracked tracked = mobs.remove(id);
        if (tracked == null) return;
        Entity extension = tracked.active.extension();
        if (extension != null) baseByExtension.remove(extension.getUniqueId());
    }

    private Optional<ActiveMob> base(Entity extension) {
        UUID base = baseByExtension.get(extension.getUniqueId());
        Tracked tracked = base == null ? null : mobs.get(base);
        return tracked == null ? Optional.empty() : Optional.of(tracked.active);
    }

    private ActiveMob current(ActiveMob mob) {
        Tracked tracked = mobs.get(mob.entity().getUniqueId());
        return tracked == null ? mob : tracked.active;
    }

    private CompiledBehaviour instantiate(CustomMob mob, LivingEntity entity, @Nullable Entity extension) {
        return behaviours
                .of(mob.id())
                .map(compiled -> compiled.instantiate(new LivingMobContext(entity, extension, items, tasks)))
                .orElse(NONE);
    }

    /**
     * Records a behaviour failure that happened while an event was dispatched.
     *
     * @param entry  the entry that ran, e.g. a trigger id
     * @param entity the entity the event happened to: a mob or the extension of one
     * @param error  what was thrown
     */
    public void failed(String entry, Entity entity, RuntimeException error) {
        mob(entity).or(() -> base(entity)).ifPresent(mob -> report(mob, entry, error));
    }

    /**
     * @return what failed at runtime since the behaviours were last compiled, per mob id, each failure once
     */
    public Map<String, List<String>> failures() {
        Map<String, List<String>> copy = new TreeMap<>();
        failures.forEach((mob, list) -> copy.put(mob, List.copyOf(list)));
        return copy;
    }

    private void guard(ActiveMob mob, String entry, Runnable call) {
        try {
            call.run();
        } catch (RuntimeException e) {
            report(mob, entry, e);
        }
    }

    /**
     * Logs a failure once per mob, entry and error, and keeps it for {@link #failures()}.
     */
    private void report(ActiveMob mob, String entry, RuntimeException error) {
        String failure = "%s: %s".formatted(entry, error);
        if (!reported.add(mob.mob().id() + ":" + failure)) return;
        failures.computeIfAbsent(mob.mob().id(), key -> new CopyOnWriteArrayList<>())
                .add(failure);
        logger.warning("Behaviour of mob %s failed in %s".formatted(mob.mob().id(), failure));
    }

    /**
     * A living mob and what its loop has seen; everything but {@link #active} is only touched by the mob's own thread.
     * A tracking replaced by a newer one for the same entity stops its loop on its next tick.
     */
    private static final class Tracked {
        private volatile ActiveMob active;
        private OptionalLong seenNight;
        private final int offset;
        private long age;

        private Tracked(ActiveMob active, OptionalLong seenNight, int offset) {
            this.active = active;
            this.seenNight = seenNight;
            this.offset = offset;
        }
    }
}
