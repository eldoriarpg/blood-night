/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
package de.eldoria.bloodnight.behaviour;

import com.palantir.javapoet.ClassName;
import de.eldoria.bloodnight.api.CompiledBehaviour;
import de.eldoria.bloodnight.graph.Triggers;
import de.eldoria.bloodnight.mob.CustomMob;
import de.eldoria.graph.compiler.CompiledGraph;
import de.eldoria.graph.compiler.GraphCompilationException;
import de.eldoria.graph.compiler.GraphCompiler;
import de.eldoria.graph.compiler.GraphProblem;
import de.eldoria.graph.compiler.MacroLibrary;
import de.eldoria.graph.model.Macro;
import de.eldoria.graph.model.NodeInstance;
import de.eldoria.graph.platform.GraphPlatform;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Compiles the behaviours of all mobs off the main thread.
 * <p>
 * One background thread does all compiling, so a reload never competes with a running one. Unchanged graphs come
 * from the {@link BytecodeCache}; after each run, cache entries of graphs that no longer exist are removed. A mob
 * whose graph does not compile keeps its problems and gets no behaviour; the other mobs are unaffected.
 */
public final class BehaviourService implements AutoCloseable {
    private static final String PACKAGE = "de.eldoria.bloodnight.generated";
    private static final Set<String> TRIGGERS = new HashSet<>();

    static {
        Triggers.EVENTS.forEach(trigger -> TRIGGERS.add(trigger.id()));
        TRIGGERS.add("mob.tick");
        TRIGGERS.add("mob.spawn");
        TRIGGERS.add("mob.night_end");
        TRIGGERS.add("debug.trigger");
    }

    private final GraphPlatform platform;
    private final ClassLoader parent;
    private final BytecodeCache cache;
    private final String version;
    private final Logger logger;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "bloodnight-compiler");
        thread.setDaemon(true);
        return thread;
    });

    /**
     * @param platform the node platform
     * @param parent   class loader generated classes resolve against, usually the plugin's
     * @param cache    compiled behaviours on disk
     * @param version  plugin version, part of every cache key
     * @param logger   receives compile summaries and failures
     */
    public BehaviourService(
            GraphPlatform platform, ClassLoader parent, BytecodeCache cache, String version, Logger logger) {
        this.platform = platform;
        this.parent = parent;
        this.cache = cache;
        this.version = version;
        this.logger = logger;
    }

    /**
     * Compiles every mob in the background.
     *
     * @param mobs   the mobs
     * @param macros the macro library
     * @return the result, completed on the compiler thread
     */
    public CompletableFuture<Behaviours> compile(Collection<CustomMob> mobs, List<Macro> macros) {
        List<CustomMob> snapshot = List.copyOf(mobs);
        List<Macro> macroSnapshot = List.copyOf(macros);
        return CompletableFuture.supplyAsync(() -> compileNow(snapshot, macroSnapshot), executor);
    }

    /**
     * Compiles every mob on the calling thread.
     *
     * @param mobs   the mobs
     * @param macros the macro library
     * @return the result
     */
    public Behaviours compileNow(Collection<CustomMob> mobs, List<Macro> macros) {
        long start = System.nanoTime();
        MacroLibrary library = MacroLibrary.compile(platform, macros);
        library.problems().forEach((macro, problems) -> logger.warning("Macro %s: %s".formatted(macro, problems)));
        GraphCompiler<CompiledBehaviour> compiler =
                new GraphCompiler<>(platform, CompiledBehaviour.class, parent, library);

        Map<String, CompiledGraph<CompiledBehaviour>> compiled = new HashMap<>();
        Map<String, List<GraphProblem>> problems = new HashMap<>();
        Set<String> used = new HashSet<>();
        Set<String> keys = new HashSet<>();
        int fromCache = 0;
        for (CustomMob mob : mobs) {
            String key = BytecodeCache.key(version, mob.behaviour(), macros);
            keys.add(key);
            ClassName name = ClassName.get(PACKAGE, "Mob_" + mob.id().replaceAll("[^A-Za-z0-9_]", "_"));
            try {
                CompiledGraph<CompiledBehaviour> graph;
                var cached = cache.read(key).filter(entry -> entry.name().equals(name.reflectionName()));
                if (cached.isPresent()) {
                    graph = compiler.load(
                            name,
                            cached.get().source(),
                            cached.get().bytecode(),
                            cached.get().warnings());
                    fromCache++;
                } else {
                    graph = compiler.compile(mob.behaviour(), name);
                    cache.write(
                            key,
                            BytecodeCache.Entry.of(
                                    name.reflectionName(), graph.source(), graph.bytecode(), graph.warnings()));
                }
                compiled.put(mob.id(), graph);
                if (!graph.warnings().isEmpty()) problems.put(mob.id(), graph.warnings());
                mob.behaviour().nodes().values().stream()
                        .map(NodeInstance::type)
                        .filter(TRIGGERS::contains)
                        .forEach(used::add);
            } catch (GraphCompilationException e) {
                problems.put(mob.id(), e.problems());
                logger.warning("Mob %s does not compile:\n%s".formatted(mob.id(), e.getMessage()));
            } catch (RuntimeException e) {
                problems.put(mob.id(), List.of());
                logger.log(Level.SEVERE, "Compiling mob %s failed".formatted(mob.id()), e);
            }
        }
        cache.retain(keys);
        logger.info("Compiled %d of %d mob behaviours (%d from cache) in %d ms"
                .formatted(compiled.size(), mobs.size(), fromCache, (System.nanoTime() - start) / 1_000_000));
        return new Behaviours(compiled, problems, Map.of(), used, fromCache).withMacros(library);
    }

    @Override
    public void close() {
        executor.shutdownNow();
    }
}
