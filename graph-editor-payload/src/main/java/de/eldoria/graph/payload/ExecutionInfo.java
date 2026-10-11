package de.eldoria.graph.payload;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * An execution output nodes are chained into.
 *
 * @param id        stable port id
 * @param name      editor name
 * @param repeating whether the chain may run several times, as in a loop body
 */
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record ExecutionInfo(String id, String name, boolean repeating) {
}
