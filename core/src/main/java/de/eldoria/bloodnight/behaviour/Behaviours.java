package de.eldoria.bloodnight.behaviour;

import de.eldoria.bloodnight.api.CompiledBehaviour;
import de.eldoria.graph.compiler.CompiledGraph;
import de.eldoria.graph.compiler.GraphProblem;
import de.eldoria.graph.compiler.MacroLibrary;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Outcome of compiling every mob: the classes that compiled, the problems of those that did not, and which triggers
 * the compiled graphs use.
 *
 * @param compiled      compiled behaviour per mob id
 * @param problems      errors and warnings per mob id; mobs with errors are not in {@code compiled}
 * @param macroProblems problems per macro id
 * @param usedTriggers  ids of the entry nodes the compiled graphs contain
 * @param fromCache     how many mobs were restored from the cache instead of compiled
 */
public record Behaviours(Map<String, CompiledGraph<CompiledBehaviour>> compiled,
                         Map<String, List<GraphProblem>> problems,
                         Map<String, List<GraphProblem>> macroProblems,
                         Set<String> usedTriggers,
                         int fromCache) {
    public Behaviours {
        compiled = Map.copyOf(compiled);
        problems = Map.copyOf(problems);
        macroProblems = Map.copyOf(macroProblems);
        usedTriggers = Set.copyOf(usedTriggers);
    }

    /**
     * @return a result without any mob
     */
    public static Behaviours empty() {
        return new Behaviours(Map.of(), Map.of(), Map.of(), Set.of(), 0);
    }

    /**
     * @param mob a mob id
     * @return its compiled behaviour, if it compiled
     */
    public Optional<CompiledGraph<CompiledBehaviour>> of(String mob) {
        return Optional.ofNullable(compiled.get(mob));
    }

    /**
     * @param macros the macro library the mobs were compiled with
     * @return this result with the library's problems
     */
    Behaviours withMacros(MacroLibrary macros) {
        return new Behaviours(compiled, problems, macros.problems(), usedTriggers, fromCache);
    }
}
