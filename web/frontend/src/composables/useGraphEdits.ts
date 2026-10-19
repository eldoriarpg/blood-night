/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {Catalog} from '~/api/generated/schema'
import type {Graph} from '~/api/generated/payload'
import {GraphChanges} from '~/util/graph/graphChanges'
import {withVariants} from '~/util/graph/mutate'
import type {GraphBindings} from '~/util/graph/graphBindings'
import type {PatchOperation} from '~/util/patch/jsonPatch'

/** The graph a view edits: where it is in the payload, the graph itself, the catalog and its graph-wide types. */
export interface EditedGraph {
    base: string
    graph: Graph
    catalog: Catalog
    bindings: GraphBindings
    /** The state new nodes join; undefined for the part that always runs. */
    state?: string
}

/**
 * Edits of one graph: the operations for it as it is now, and sending them with every Mutate node turned into the
 * variant that fits what is wired into it.
 *
 * @param source the graph, read anew for every edit
 */
export function useGraphEdits(source: () => EditedGraph) {
    const changes = useEditorChanges()

    function editor(): GraphChanges {
        return new GraphChanges(source().base, source().graph, source().state)
    }

    function apply(operations: PatchOperation[]): Promise<boolean> {
        const {base, graph, catalog, bindings} = source()
        return changes.apply(withVariants(base, graph, catalog, operations, bindings))
    }

    return {changes, editor, apply}
}
