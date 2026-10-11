/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph;

import de.eldoria.bloodnight.api.CompiledBehaviour;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * One row of the trigger table: when an event means something happened to a custom mob.
 * <p>
 * Reflection derives the entry node from the host method; what it cannot know is written here: which entity in the
 * event is "this mob", which events of the class count, and when to listen.
 *
 * @param id              stable node id, e.g. {@code event.damage}
 * @param name            editor name
 * @param description     editor description
 * @param event           the event class
 * @param method          name of the {@link CompiledBehaviour} method
 * @param subject         the entity the event happened to from the mob's view, or {@code null} when there is none
 * @param filter          which events of the class count
 * @param priority        listener priority
 * @param ignoreCancelled whether cancelled events are skipped
 * @param invoke          calls the behaviour method
 * @param <E>             the event type
 */
public record Trigger<E extends Event>(
        String id,
        String name,
        String description,
        Class<E> event,
        String method,
        Function<E, @Nullable Entity> subject,
        Predicate<E> filter,
        EventPriority priority,
        boolean ignoreCancelled,
        BiConsumer<CompiledBehaviour, E> invoke) {
    public Trigger {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(event, "event");
        Objects.requireNonNull(method, "method");
        Objects.requireNonNull(subject, "subject");
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(priority, "priority");
        Objects.requireNonNull(invoke, "invoke");
    }

    /**
     * Runs the trigger for an event, if it applies.
     *
     * @param raw        an event of the listened handler list; may be of a sibling class
     * @param behaviours the behaviour of a custom mob entity
     * @return whether a behaviour was called
     */
    public boolean dispatch(Event raw, Function<Entity, Optional<CompiledBehaviour>> behaviours) {
        if (!event.isInstance(raw)) return false;
        E typed = event.cast(raw);
        if (!filter.test(typed)) return false;
        Entity entity = subject.apply(typed);
        if (entity == null) return false;
        Optional<CompiledBehaviour> behaviour = behaviours.apply(entity);
        behaviour.ifPresent(target -> invoke.accept(target, typed));
        return behaviour.isPresent();
    }
}
