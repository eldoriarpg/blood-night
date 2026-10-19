/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {Comment, Graph} from '~/api/generated/payload'
import type {EdgeView} from '~/util/graph/graphView'
import type {PinKind} from '~/util/graph/pins'
import type {Position} from '~/util/graph/graphLayout'

/** The colours a comment frame can take, by the name stored with it. */
export const COMMENT_COLORS = ['grey', 'red', 'orange', 'yellow', 'green', 'blue', 'purple'] as const

/** The prefix of a comment frame's id on the canvas, so frames never clash with node ids. */
export const COMMENT_NODE = 'comment:'

/** Space between a frame and the nodes it is made around; the title bar adds its own height on top. */
const MARGIN = 22
const TITLE_BAR = 44

/** A rectangle on the canvas. */
export interface Box {
    x: number
    y: number
    width: number
    height: number
}

/** A comment frame with what it holds, by position. */
export interface CommentView {
    id: string
    comment: Comment
    /** The nodes inside the frame, nested frames' nodes included. */
    members: string[]
    /** The frames inside this one. */
    nested: string[]
    /** Whether a closed frame around it hides it. */
    hidden: boolean
}

/** A wire between a hidden node and a visible one, which ends at the closed frame instead. */
export interface FramePin {
    /** The handle id on the closed frame. */
    handle: string
    pin: PinKind
    /** The hidden node and port it stands for. */
    node: string
    port: string
}

/** A closed frame's pins: wires coming in on its left, wires going out on its right. */
export interface FramePins {
    inputs: FramePin[]
    outputs: FramePin[]
}

/**
 * @param point a node's position (its top left corner)
 * @param box a frame
 * @returns whether the frame holds the point
 */
function inside(point: Position, box: Box): boolean {
    return point.x >= box.x && point.x <= box.x + box.width && point.y >= box.y && point.y <= box.y + box.height
}

function area(comment: Comment): number {
    return comment.width * comment.height
}

/**
 * Which nodes and frames each frame of a graph holds. A node belongs to every frame its top left corner lies in; a
 * frame belongs to a larger frame its top left corner lies in. A frame inside a closed frame is hidden.
 *
 * @param graph the graph
 * @param positions where each node is
 */
export function commentViews(graph: Graph, positions: ReadonlyMap<string, Position>): CommentView[] {
    const comments = Object.entries(graph.comments ?? {})
    const views = comments.map(([id, comment]) => ({
        id,
        comment,
        members: [...positions].filter(([, position]) => inside(position, comment)).map(([node]) => node).sort(),
        nested: comments
            .filter(([other, frame]) => other !== id && area(frame) < area(comment) && inside(frame, comment))
            .map(([other]) => other)
            .sort(),
        hidden: false,
    }))
    const closed = views.filter(view => view.comment.collapsed)
    for (const view of views) view.hidden = closed.some(outer => outer.nested.includes(view.id))
    return views
}

/**
 * @param views the frames of a graph
 * @returns each node a closed frame hides, with the outermost closed frame hiding it
 */
export function hiddenNodes(views: readonly CommentView[]): Map<string, string> {
    const hidden = new Map<string, string>()
    const closed = views.filter(view => view.comment.collapsed && !view.hidden)
    for (const view of closed) for (const node of view.members) hidden.set(node, view.id)
    return hidden
}

/**
 * Wires as drawn while frames are closed: a wire with one end at a hidden node ends at that node's closed frame, on a
 * pin of its own; a wire inside one closed frame is not drawn.
 *
 * @param edges the graph's wires
 * @param hidden each hidden node with the frame hiding it
 * @returns the wires to draw, and the pins of each closed frame
 */
export function reroutedEdges(edges: readonly EdgeView[], hidden: ReadonlyMap<string, string>): {edges: EdgeView[], pins: Map<string, FramePins>} {
    const pins = new Map<string, FramePins>()
    const pinsOf = (frame: string) => {
        let found = pins.get(frame)
        if (!found) pins.set(frame, found = {inputs: [], outputs: []})
        return found
    }
    const drawn: EdgeView[] = []
    for (const edge of edges) {
        const from = hidden.get(edge.source)
        const to = hidden.get(edge.target)
        if (from === undefined && to === undefined) {
            drawn.push(edge)
            continue
        }
        if (from !== undefined && from === to) continue
        const routed = {...edge}
        if (from !== undefined) {
            routed.source = COMMENT_NODE + from
            routed.sourceHandle = `cout:${edge.id}`
            pinsOf(from).outputs.push({handle: routed.sourceHandle, pin: edge.pin, node: edge.source, port: edge.sourceHandle})
        }
        if (to !== undefined) {
            routed.target = COMMENT_NODE + to
            routed.targetHandle = `cin:${edge.id}`
            pinsOf(to).inputs.push({handle: routed.targetHandle, pin: edge.pin, node: edge.target, port: edge.targetHandle})
        }
        drawn.push(routed)
    }
    return {edges: drawn, pins}
}

/**
 * @param boxes the nodes to frame, with their size
 * @param grid the grid frames snap to
 * @returns a frame around them with room for its title bar, on the grid
 */
export function frameAround(boxes: readonly Box[], grid: number): Box {
    const left = Math.min(...boxes.map(box => box.x)) - MARGIN
    const top = Math.min(...boxes.map(box => box.y)) - MARGIN - TITLE_BAR
    const right = Math.max(...boxes.map(box => box.x + box.width)) + MARGIN
    const bottom = Math.max(...boxes.map(box => box.y + box.height)) + MARGIN
    const x = Math.floor(left / grid) * grid
    const y = Math.floor(top / grid) * grid
    return {x, y, width: Math.ceil((right - x) / grid) * grid, height: Math.ceil((bottom - y) / grid) * grid}
}
