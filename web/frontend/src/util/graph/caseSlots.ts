/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {NodeView} from '~/util/graph/graphView'

/** Which numbered cases of a node are drawn, and the one adding a case brings in. */
export interface CaseSlots {
    /** The ports of cases not drawn. */
    hidden: ReadonlySet<string>
    /** The ports of cases drawn, in order. */
    shown: readonly string[]
    /** The first case not drawn, none when all are. */
    next: string | undefined
    /** The last case drawn where it may be cleared away: unwired and not the only one. */
    removable: string | undefined
}

/**
 * The cases a node draws, of the numbered ports it has a fixed number of with a setting beside each, such as a
 * switch's `case_0` to `case_7` or a select's `option_0` to `option_7`. Cases are drawn from the first up to the last
 * one wired, holding a value or just added, at least the first, so none is skipped and there is always one to fill in.
 * Only the last can be cleared away, which keeps the numbers of the others.
 *
 * @param node the node
 * @param added the cases added since the node was drawn
 */
export function caseSlots(node: NodeView, added: ReadonlySet<string>): CaseSlots {
    const besides = new Map(node.config.flatMap(field => field.beside === undefined ? [] : [[field.beside, field] as const]))
    const ports = [...node.executions, ...node.inputs].filter(port => /_\d+$/.test(port.id) && besides.has(port.id))
    const used = (port: typeof ports[number]) => {
        const value = besides.get(port.id)?.value
        return port.connected || added.has(port.id) || (value !== undefined && value !== null && value !== '')
    }
    const count = ports.length === 0 ? 0 : Math.max(1, ports.findLastIndex(used) + 1)
    const shown = ports.slice(0, count)
    const last = shown.at(-1)
    return {
        hidden: new Set(ports.slice(count).map(port => port.id)),
        shown: shown.map(port => port.id),
        next: ports[count]?.id,
        removable: count > 1 && last && !last.connected ? last.id : undefined,
    }
}
