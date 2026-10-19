/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.graph.runtime;

import com.palantir.javapoet.CodeBlock;
import de.eldoria.graph.definition.emit.EmitContext;
import de.eldoria.graph.reflect.member.CallGuard;

import java.util.Map;

/**
 * Keeps the reflected World and Block nodes on the threads that own what they touch.
 * <p>
 * On Folia, world-wide state such as time, weather and difficulty belongs to the global region, a block to the region
 * it is in, and no thread owns a whole world. So changes to a world run on the global region and changes to a block on
 * its region, both a tick later when the behaviour's thread does not own them; reading a whole world's entities,
 * players or counts fails with a clear message. On Paper all of it happens right away, as written.
 */
public final class FoliaGuards {
    /**
     * World getters that need every region of the world, by method, with the name the failure uses.
     */
    private static final Map<String, String> WHOLE_WORLD = Map.of(
            "getEntities", "entities",
            "getLivingEntities", "living entities",
            "getPlayers", "players",
            "getEntityCount", "entity count",
            "getPlayerCount", "player count",
            "getChunkCount", "chunk count",
            "getTileEntityCount", "tile entity count",
            "getTickableTileEntityCount", "tickable tile entity count");

    public static final CallGuard WORLD = new CallGuard() {
        @Override
        public CodeBlock read(EmitContext ctx, String method, CodeBlock call) {
            String property = WHOLE_WORLD.get(method);
            if (property == null) return call;
            return CodeBlock.of("$T.wholeWorld($S, () -> $L)", BukkitRuntime.class, property, call);
        }

        @Override
        public CodeBlock write(EmitContext ctx, CodeBlock receiver, CodeBlock changes) {
            return CodeBlock.builder()
                    .add("$L.global(() -> {\n", ctx.context())
                    .indent()
                    .add(changes)
                    .unindent()
                    .addStatement("})")
                    .build();
        }
    };

    public static final CallGuard BLOCK = new CallGuard() {
        @Override
        public CodeBlock write(EmitContext ctx, CodeBlock receiver, CodeBlock changes) {
            return CodeBlock.builder()
                    .add("$L.at($L.getLocation(), () -> {\n", ctx.context(), receiver)
                    .indent()
                    .add(changes)
                    .unindent()
                    .addStatement("})")
                    .build();
        }
    };

    private FoliaGuards() {}
}
