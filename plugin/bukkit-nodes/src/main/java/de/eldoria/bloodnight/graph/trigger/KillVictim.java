/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph.trigger;

import com.palantir.javapoet.CodeBlock;
import de.eldoria.graph.definition.entry.EntryContext;
import org.bukkit.entity.Player;

import java.util.Optional;

/**
 * Which kills an On Kill entry reacts to, picked per entry node; the check is left out of the generated code when
 * every kill counts.
 */
public enum KillVictim {
    ANY,
    PLAYER,
    ENTITY;

    public static final String FIELD = "victim";

    /**
     * @param ctx the kill entry's context
     * @return the condition its chains run under, empty for {@link #ANY}
     */
    public static Optional<CodeBlock> guard(EntryContext ctx) {
        CodeBlock victim = CodeBlock.of("$L.getEntity()", ctx.parameter("event"));
        return switch (valueOf(ctx.configValue(FIELD).asString(ANY.name()))) {
            case ANY -> Optional.empty();
            case PLAYER -> Optional.of(CodeBlock.of("$L instanceof $T", victim, Player.class));
            case ENTITY -> Optional.of(CodeBlock.of("!($L instanceof $T)", victim, Player.class));
        };
    }
}
