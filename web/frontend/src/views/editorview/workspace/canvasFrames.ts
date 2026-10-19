/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
import type {ComputedRef, Ref} from 'vue'
import {useEventListener} from '@vueuse/core'
import type {NodeDragEvent, VueFlowStore} from '@vue-flow/core'
import type {Comment, Graph} from '~/api/generated/payload'
import type {GraphChanges} from '~/util/graph/graphChanges'
import type {EdgeView, NodeView} from '~/util/graph/graphView'
import type {Position} from '~/util/graph/graphLayout'
import type {PatchOperation} from '~/util/patch/jsonPatch'
import {COMMENT_NODE, commentViews, frameAround, hiddenNodes, reroutedEdges, type Box, type CommentView} from '~/util/graph/comments'

/** What the comment frames of a canvas need from it. */
export interface FrameCanvas {
    graph: () => Graph
    base: () => string
    readOnly: () => boolean
    view: ComputedRef<{nodes: NodeView[], edges: EdgeView[]}>
    shownPositions: Ref<ReadonlyMap<string, Position>>
    flow: VueFlowStore
    playback: {current: Ref<string | null>, order: Ref<ReadonlyMap<string, number>>, wires: Ref<ReadonlySet<string>>}
    problems: () => readonly {path: string}[]
    editor: () => GraphChanges
    apply: (operations: PatchOperation[]) => Promise<boolean>
    grid: number
    viewCenter: () => Position
    /** Whether C frames the selected nodes; a preview takes no keys. */
    keys: boolean
}

/** Where everything is: the frames by what they hold, the nodes closed frames hide and the wires as drawn. */
type FrameLayout = ReturnType<typeof useFrameLayout>

/** What a dragged frame takes along: its nodes and smaller frames that are not dragged themselves. */
interface FrameDrag {
    frame: CommentView
    start: Position
    nodes: string[]
    frames: string[]
}

/**
 * What a graph canvas draws, with its comment frames: the frames behind the nodes, the nodes except those closed
 * frames hide, the wires with those of hidden nodes rerouted to the closed frames, and every edit of a frame. A frame
 * moves what it holds along while it is dragged, ahead of the change that saves it. C frames the selected nodes,
 * unless a text field has the focus.
 */
export function useCanvasFrames(canvas: FrameCanvas) {
    const layout = useFrameLayout(canvas)
    const edits = useFrameEdits(canvas, layout)
    const elements = useCanvasElements(canvas, layout, edits.naming)
    useFrameDrag(canvas, layout)
    if (canvas.keys) {
        useEventListener('keydown', (event: KeyboardEvent) => {
            if (event.key.toLowerCase() === 'c' && !event.ctrlKey && !event.metaKey && !event.altKey && !typing(event.target)) edits.frameSelection()
        })
    }
    return {frames: layout.frames, routed: layout.routed, ...elements, ...edits}
}

function useFrameLayout(canvas: FrameCanvas) {
    const dragged = ref<ReadonlyMap<string, Position>>(new Map())
    const nodePositions = computed(() => new Map(canvas.view.value.nodes.map(node => [node.id, canvas.shownPositions.value.get(node.id) ?? node.position])))
    const frames = computed(() => commentViews(canvas.graph(), nodePositions.value))
    const framesById = computed(() => new Map(frames.value.map(frame => [frame.id, frame])))
    const hidden = computed(() => hiddenNodes(frames.value))
    const routed = computed(() => reroutedEdges(canvas.view.value.edges, hidden.value))
    const frameOf = (id: string) => id.startsWith(COMMENT_NODE) ? framesById.value.get(id.slice(COMMENT_NODE.length)) : undefined
    return {dragged, nodePositions, frames, framesById, hidden, routed, frameOf}
}

function useCanvasElements(canvas: FrameCanvas, layout: FrameLayout, naming: Ref<string | null>) {
    const {dragged, frames, hidden, routed} = layout
    const litClass = (ids: readonly string[]) => {
        const current = canvas.playback.current.value
        if (current !== null && ids.includes(current)) return 'bn-lit bn-current'
        return ids.some(id => canvas.playback.order.value.has(id)) ? 'bn-lit' : ''
    }
    const problemsIn = (nodes: readonly string[]) => {
        const paths = nodes.map(node => `${canvas.base()}/nodes/${node}`)
        return canvas.problems().filter(problem => paths.some(path => problem.path === path || problem.path.startsWith(path + '/'))).length
    }
    const frameNodes = computed(() => frames.value.filter(frame => !frame.hidden).map(frame => ({
        id: COMMENT_NODE + frame.id,
        type: 'comment',
        position: dragged.value.get(COMMENT_NODE + frame.id) ?? {x: frame.comment.x, y: frame.comment.y},
        data: frame,
        zIndex: frame.comment.collapsed ? 2 : 0,
        style: frame.comment.collapsed ? undefined : {pointerEvents: 'none' as const},
        dragHandle: '.bn-frame-bar',
        connectable: false,
        deletable: !frame.comment.collapsed,
        class: frame.comment.collapsed ? litClass(frame.members) : '',
    })))
    const nodes = computed(() => [...frameNodes.value, ...canvas.view.value.nodes.filter(node => !hidden.value.has(node.id)).map(node => ({
        id: node.id,
        type: 'blueprint',
        zIndex: 1,
        position: dragged.value.get(node.id) ?? canvas.shownPositions.value.get(node.id) ?? node.position,
        data: node,
        class: litClass([node.id]),
    }))])
    const flowNodeIds = computed(() => new Set(canvas.flow.nodes.value.map(node => node.id)))
    const edges = computed(() => routed.value.edges.filter(edge => flowNodeIds.value.has(edge.source) && flowNodeIds.value.has(edge.target)).map(edge => {
        const lit = canvas.playback.wires.value.has(edge.id)
        return {
            id: edge.id,
            source: edge.source,
            sourceHandle: edge.sourceHandle,
            target: edge.target,
            targetHandle: edge.targetHandle,
            animated: lit,
            class: lit ? 'bn-lit-wire' : '',
            style: {stroke: `var(--color-pin-${edge.pin})`, strokeWidth: lit ? 4 : edge.pin === 'exec' ? 2.5 : 2},
        }
    }))
    /** The properties a frame component draws one frame with, besides whether it is selected. */
    const frameBindings = (frame: CommentView) => ({
        comment: frame.comment,
        members: frame.members.length,
        naming: naming.value === frame.id,
        pins: routed.value.pins.get(frame.id),
        problems: frame.comment.collapsed ? problemsIn(frame.members) : 0,
        readOnly: canvas.readOnly(),
        zoom: canvas.flow.viewport.value.zoom,
        grid: canvas.grid,
    })
    return {nodes, edges, frameBindings}
}

function useFrameDrag(canvas: FrameCanvas, layout: FrameLayout): void {
    let drags: FrameDrag[] = []
    const distance = (event: NodeDragEvent, drag: FrameDrag): Position | undefined => {
        const node = event.nodes.find(moved => moved.id === COMMENT_NODE + drag.frame.id)
        return node ? {x: Math.round(node.position.x - drag.start.x), y: Math.round(node.position.y - drag.start.y)} : undefined
    }
    canvas.flow.onNodeDragStart(event => {
        const moving = new Set(event.nodes.map(node => node.id))
        drags = event.nodes.flatMap(node => {
            const frame = layout.frameOf(node.id)
            if (!frame) return []
            const nodes = frame.members.filter(id => !moving.has(id))
            return [{frame, start: {x: frame.comment.x, y: frame.comment.y}, nodes, frames: frame.nested.filter(id => !moving.has(COMMENT_NODE + id))}]
        })
    })
    canvas.flow.onNodeDrag(event => {
        if (drags.length === 0) return
        const positions = new Map<string, Position>(event.nodes.map(node => [node.id, node.position]))
        for (const drag of drags) {
            const by = distance(event, drag)
            if (!by) continue
            for (const id of drag.nodes) {
                const at = layout.nodePositions.value.get(id)
                if (at) positions.set(id, {x: at.x + by.x, y: at.y + by.y})
            }
            for (const id of drag.frames) {
                const nested = layout.framesById.value.get(id)
                if (nested) positions.set(COMMENT_NODE + id, {x: nested.comment.x + by.x, y: nested.comment.y + by.y})
            }
        }
        layout.dragged.value = positions
    })
    canvas.flow.onNodeDragStop(event => {
        const operations = canvas.readOnly() ? [] : event.nodes.filter(node => !node.id.startsWith(COMMENT_NODE)).flatMap(node => canvas.editor().move(node.id, node.position))
        for (const drag of canvas.readOnly() ? [] : drags) {
            const by = distance(event, drag)
            if (by && (by.x !== 0 || by.y !== 0)) operations.push(...canvas.editor().shift([drag.frame.id, ...drag.frames], drag.nodes, by))
        }
        drags = []
        void (operations.length > 0 ? canvas.apply(operations) : Promise.resolve(true)).finally(() => {
            layout.dragged.value = new Map()
        })
    })
}

function useFrameEdits(canvas: FrameCanvas, layout: FrameLayout) {
    const naming = ref<string | null>(null)
    const edit = (id: string, changes: Partial<Comment>) => {
        if (!canvas.readOnly()) void canvas.apply(canvas.editor().editComment(id, changes))
    }
    const toggle = (id: string) => {
        const frame = layout.framesById.value.get(id)
        if (frame) edit(id, {collapsed: !frame.comment.collapsed})
    }
    /** Frames the selected nodes in a new comment, or makes an empty one in the middle of the view; its title is typed next. */
    const frameSelection = () => {
        if (canvas.readOnly()) return
        const selected = canvas.flow.getSelectedNodes.value.filter(node => !node.id.startsWith(COMMENT_NODE))
        const boxes: Box[] = selected.map(node => ({x: node.position.x, y: node.position.y, width: node.dimensions.width, height: node.dimensions.height}))
        if (boxes.length === 0) {
            const center = canvas.viewCenter()
            boxes.push({x: center.x - 154, y: center.y - 66, width: 308, height: 132})
        }
        const {id, operations} = canvas.editor().addComment({title: '', note: '', color: 'grey', ...frameAround(boxes, canvas.grid), collapsed: false})
        naming.value = id
        void canvas.apply(operations)
    }
    /** Opens a closed frame double clicked on the canvas; whether the node was a frame at all. */
    const doubleClicked = (id: string) => {
        const frame = layout.frameOf(id)
        if (frame?.comment.collapsed) toggle(frame.id)
        return frame !== undefined
    }
    /** Whether Arrange lays the canvas node out: every node and closed frame, but no open frame. */
    const arranged = (id: string) => !id.startsWith(COMMENT_NODE) || layout.frameOf(id)?.comment.collapsed === true
    const afterArrange = (before: readonly CommentView[], positions: ReadonlyMap<string, Position>, sizes: ReadonlyMap<string, Pick<Box, 'width' | 'height'>>) =>
        framesAfterArrange(canvas.editor(), canvas.grid, before, positions, sizes)
    return {naming, edit, toggle, frameSelection, doubleClicked, arranged, afterArrange}
}

/**
 * The changes that keep frames with their nodes after Arrange: a closed frame moves its hidden nodes by as much as it
 * moved, an open frame is fitted around the nodes it held before.
 *
 * @param changes builds the operations
 * @param grid the grid frames snap to
 * @param before the frames before arranging
 * @param positions where Arrange put every laid out node and closed frame
 * @param sizes the size of every node on the canvas
 */
function framesAfterArrange(
    changes: GraphChanges, grid: number, before: readonly CommentView[], positions: ReadonlyMap<string, Position>, sizes: ReadonlyMap<string, Pick<Box, 'width' | 'height'>>,
): PatchOperation[] {
    return before.filter(frame => !frame.hidden).flatMap(frame => {
        const box = positions.get(COMMENT_NODE + frame.id)
        if (frame.comment.collapsed) {
            return box ? changes.shift([frame.id, ...frame.nested], frame.members, {x: box.x - frame.comment.x, y: box.y - frame.comment.y}) : []
        }
        const boxes = frame.members.flatMap(id => {
            const at = positions.get(id)
            const size = sizes.get(id)
            return at && size ? [{x: at.x, y: at.y, width: size.width, height: size.height}] : []
        })
        return boxes.length > 0 ? changes.editComment(frame.id, frameAround(boxes, grid)) : []
    })
}

function typing(target: EventTarget | null): boolean {
    return target instanceof HTMLElement && (target.isContentEditable || ['INPUT', 'TEXTAREA', 'SELECT'].includes(target.tagName))
}
