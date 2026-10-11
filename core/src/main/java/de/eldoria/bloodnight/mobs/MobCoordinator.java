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
import de.eldoria.bloodnight.util.MobTags;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Holds every living custom mob and its behaviour instance.
 * <p>
 * Each mob gets its own instance of its compiled behaviour, so state like cooldowns is per mob. After a reload,
 * {@link #rebind} gives every living mob a fresh instance of its new behaviour; nothing references the old classes
 * afterwards, so their class loaders can be collected. Exceptions thrown by a behaviour are logged once per mob and
 * message, then swallowed, so one broken graph cannot stop the tick loop.
 */
public final class MobCoordinator {
    private static final CompiledBehaviour NONE = new CompiledBehaviour() {};

    private final Map<Integer, ActiveMob> mobs = new HashMap<>();
    private final Set<String> reported = new HashSet<>();
    private final MobOutfitter outfitter;
    private final Supplier<ItemRegistry> items;
    private final Function<String, CustomMob> definitions;
    private final Logger logger;
    private Behaviours behaviours = Behaviours.empty();

    /**
     * @param outfitter   turns entities into custom mobs
     * @param items       the current item registry
     * @param definitions mob definition by id, or {@code null}
     * @param logger      receives behaviour failures
     */
    public MobCoordinator(
            MobOutfitter outfitter,
            Supplier<ItemRegistry> items,
            Function<String, CustomMob> definitions,
            Logger logger) {
        this.outfitter = outfitter;
        this.items = items;
        this.definitions = definitions;
        this.logger = logger;
    }

    /**
     * Turns an entity into a custom mob: outfits it and gives it its behaviour.
     *
     * @param entity the entity
     * @param mob    the mob definition
     * @return the active mob
     */
    public ActiveMob register(LivingEntity entity, CustomMob mob) {
        Entity extension = outfitter.outfit(entity, mob);
        return track(entity, mob, extension);
    }

    /**
     * Takes over an entity that already is a custom mob, e.g. after its chunk was loaded again.
     *
     * @param entity the entity
     * @return the active mob, if the entity is tagged with a known mob
     */
    public Optional<ActiveMob> attach(LivingEntity entity) {
        if (mobs.containsKey(entity.getEntityId())) return Optional.of(mobs.get(entity.getEntityId()));
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
        ActiveMob mob = mobs.get(entity.getEntityId());
        return mob == null ? Optional.empty() : Optional.of(mob.behaviour());
    }

    /**
     * @param entity any entity
     * @return the active mob, if the entity is one
     */
    public Optional<ActiveMob> mob(Entity entity) {
        return Optional.ofNullable(mobs.get(entity.getEntityId()));
    }

    /**
     * Forgets an entity, e.g. when it died or was unloaded.
     *
     * @param entity the entity
     */
    public void remove(Entity entity) {
        mobs.remove(entity.getEntityId());
    }

    /**
     * Runs the tick entry of every mob and forgets mobs that are no longer valid.
     */
    public void tick() {
        for (ActiveMob mob : new ArrayList<>(mobs.values())) {
            if (!mob.entity().isValid()) {
                mobs.remove(mob.entity().getEntityId());
                continue;
            }
            guard(mob, () -> mob.behaviour().onTick());
        }
    }

    /**
     * Fires the debug entry.
     *
     * @param mobId only mobs of this id, or {@code null} for all
     * @return how many mobs were triggered
     */
    public int debug(@Nullable String mobId) {
        int count = 0;
        for (ActiveMob mob : new ArrayList<>(mobs.values())) {
            if (mobId != null && !mob.mob().id().equals(mobId)) continue;
            guard(mob, () -> mob.behaviour().onDebug());
            count++;
        }
        return count;
    }

    /**
     * Switches to new behaviours: every living mob gets a fresh instance, built from its current definition.
     *
     * @param behaviours the newly compiled behaviours
     */
    public void rebind(Behaviours behaviours) {
        this.behaviours = behaviours;
        reported.clear();
        for (ActiveMob mob : new ArrayList<>(mobs.values())) {
            CustomMob definition =
                    Optional.ofNullable(definitions.apply(mob.mob().id())).orElse(mob.mob());
            mobs.put(
                    mob.entity().getEntityId(),
                    new ActiveMob(
                            definition,
                            mob.entity(),
                            mob.extension(),
                            instantiate(definition, mob.entity(), mob.extension())));
        }
    }

    /**
     * @return every active mob
     */
    public Collection<ActiveMob> active() {
        return Collections.unmodifiableCollection(mobs.values());
    }

    private ActiveMob track(LivingEntity entity, CustomMob mob, @Nullable Entity extension) {
        ActiveMob active = new ActiveMob(mob, entity, extension, instantiate(mob, entity, extension));
        mobs.put(entity.getEntityId(), active);
        return active;
    }

    private CompiledBehaviour instantiate(CustomMob mob, LivingEntity entity, @Nullable Entity extension) {
        return behaviours
                .of(mob.id())
                .map(compiled -> compiled.instantiate(new LivingMobContext(entity, extension, items)))
                .orElse(NONE);
    }

    private void guard(ActiveMob mob, Runnable call) {
        try {
            call.run();
        } catch (RuntimeException e) {
            String key = mob.mob().id() + ":" + e;
            if (reported.add(key))
                logger.warning(
                        "Behaviour of mob %s failed: %s".formatted(mob.mob().id(), e));
        }
    }
}
