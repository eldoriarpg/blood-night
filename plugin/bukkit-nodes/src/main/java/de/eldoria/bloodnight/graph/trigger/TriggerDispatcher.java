/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph.trigger;

import de.eldoria.bloodnight.api.internal.CompiledBehaviour;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Listens to the events of the trigger table and calls the behaviour of the custom mob each event concerns.
 * <p>
 * No listener class per event: one executor is registered per combination of event class, priority and
 * cancelled-handling, and only for triggers some graph uses. Subclass events arrive through their parent's handler
 * list, so each trigger checks the event class itself. A failing behaviour is reported and does not stop the event.
 */
public final class TriggerDispatcher {
    private final Listener owner = new Listener() {};
    private final Function<Entity, Optional<CompiledBehaviour>> behaviours;
    private final Function<Entity, Optional<CompiledBehaviour>> extensions;
    private final Failures errors;
    private volatile @Nullable Plugin plugin;

    /**
     * A dispatcher for mobs without extensions: extension triggers never fire.
     *
     * @param behaviours the behaviour of a custom mob entity, empty for other entities
     * @param errors     receives exceptions thrown by behaviours or while dispatching
     */
    public TriggerDispatcher(Function<Entity, Optional<CompiledBehaviour>> behaviours, Failures errors) {
        this(behaviours, entity -> Optional.empty(), errors);
    }

    /**
     * @param behaviours the behaviour of a custom mob entity, empty for other entities
     * @param extensions the behaviour of the mob an entity is the extension of, empty for other entities
     * @param errors     receives exceptions thrown by behaviours or while dispatching
     */
    public TriggerDispatcher(
            Function<Entity, Optional<CompiledBehaviour>> behaviours,
            Function<Entity, Optional<CompiledBehaviour>> extensions,
            Failures errors) {
        this.behaviours = behaviours;
        this.extensions = extensions;
        this.errors = errors;
    }

    /**
     * Listens for the given triggers, replacing earlier registrations.
     *
     * @param plugin the owning plugin
     * @param used   ids of the triggers some graph uses
     */
    public void register(Plugin plugin, Collection<String> used) {
        unregister();
        this.plugin = plugin;
        Map<Group, List<Trigger<?>>> groups = Triggers.EVENTS.stream()
                .filter(trigger -> used.contains(trigger.id()))
                .collect(Collectors.groupingBy(Group::of, LinkedHashMap::new, Collectors.toList()));
        groups.forEach((group, triggers) -> plugin.getServer()
                .getPluginManager()
                .registerEvent(
                        group.event(),
                        owner,
                        group.priority(),
                        (listener, event) -> triggers.forEach(trigger -> dispatch(trigger, event)),
                        plugin,
                        group.ignoreCancelled()));
    }

    /**
     * Stops listening.
     */
    public void unregister() {
        HandlerList.unregisterAll(owner);
    }

    private void dispatch(Trigger<?> trigger, Event event) {
        try {
            trigger.dispatch(
                    event,
                    trigger.extension() ? extensions : behaviours,
                    this::elsewhere,
                    (entity, e) -> errors.failed(trigger, entity, e));
        } catch (RuntimeException e) {
            errors.failed(trigger, null, e);
        }
    }

    private void elsewhere(Entity entity, Runnable work) {
        Plugin owner = plugin;
        if (owner != null) entity.getScheduler().execute(owner, work, null, 1);
    }

    /**
     * Receives what goes wrong while a trigger runs.
     */
    @FunctionalInterface
    public interface Failures {
        /**
         * @param trigger the trigger
         * @param entity  the entity the event happened to, the mob or its extension; {@code null} when the trigger
         *                failed before a behaviour ran
         * @param error   what was thrown
         */
        void failed(Trigger<?> trigger, @Nullable Entity entity, RuntimeException error);
    }

    private record Group(Class<? extends Event> event, EventPriority priority, boolean ignoreCancelled) {
        static Group of(Trigger<?> trigger) {
            return new Group(trigger.event(), trigger.priority(), trigger.ignoreCancelled());
        }
    }
}
