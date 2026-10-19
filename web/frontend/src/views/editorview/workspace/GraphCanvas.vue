/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import {VueFlow, useVueFlow, type Connection, type EdgeChange, type EdgeMouseEvent, type NodeChange, type NodeMouseEvent, type OnConnectStartParams} from '@vue-flow/core'
import '@vue-flow/core/dist/style.css'
import {useEventListener} from '@vueuse/core'
import GraphNode from '~/views/editorview/workspace/GraphNode.vue'
import CommentFrame from '~/views/editorview/workspace/CommentFrame.vue'
import {useCanvasFrames} from '~/views/editorview/workspace/canvasFrames'
import CanvasToolbar from '~/views/editorview/workspace/CanvasToolbar.vue'
import CanvasShortcuts from '~/views/editorview/workspace/CanvasShortcuts.vue'
import NodePicker from '~/views/editorview/workspace/NodePicker.vue'
import type {Catalog} from '~/api/generated/schema'
import type {Graph, Macro} from '~/api/generated/payload'
import {graphView, handles, type EdgeView, type NodeView} from '~/util/graph/graphView'
import {effectLabels, PLAY_EFFECT} from '~/util/particles/particleEffects'
import {addableNodes, nodeMatches, pulledPin, type NodeMatch, type PulledPin} from '~/util/graph/nodeMatches'
import {channelsEdit, configEdit} from '~/util/graph/nodeEdits'
import {arrange, type LayoutNode} from '~/util/graph/autoLayout'
import {executionOrder} from '~/util/graph/executionOrder'
import {graphPlayback} from '~/util/graph/graphPlayback'
import {storedPosition, type Position} from '~/util/graph/graphLayout'
import {COMMENT_NODE, type CommentView} from '~/util/graph/comments'
import type {GraphBindings} from '~/util/graph/graphBindings'

/**
 * A graph on a canvas: drawn from the payload, and every edit (moving, wiring, unwiring, deleting) sent as a change.
 * A wire let go on the empty canvas opens a picker of the nodes that can take it, and the node picked is added there,
 * wired; a double click on the empty canvas opens the same picker with every node, so nodes can be added without the
 * palette. A double click on a node opens its details. Dragging on the empty canvas selects the nodes in a box; holding
 * Space drags the view instead. A preview drags the view without Space. Every node keeps the position saved with it: a graph opened without any is arranged
 * (see {@link arrange}) and saved, or while read only just shown arranged, and the view fitted to it unless the user
 * moved the view in the meantime; unplaced nodes of a placed graph are saved
 * where they are shown, so moving one node never moves another. Arrange lays the whole graph out again. Read only while someone else holds the graph's
 * section. The play button of a node starting a chain lights up the nodes and wires it reaches, one after another in
 * the order they run (see {@link executionOrder}), each with its step number. Nodes snap to the grid the background dots draw; holding Alt while dragging places them freely. Settings and
 * values are edited on the nodes themselves. A double click on a wire puts a reroute on it there, a dot that only
 * gives the wire another path. A wire reaches Vue Flow only once it holds both of its nodes: Vue Flow takes new wires
 * before new nodes, so a node added with its wire would otherwise leave the wire pointing at nothing for a moment.
 * Comment frames sit behind the nodes (see {@link useCanvasFrames}); C or the toolbar frames the selected nodes, Delete
 * removes a frame only, and Arrange keeps frames with their nodes.
 */
const props = defineProps<{
  graph: Graph
  base: string
  catalog: Catalog
  macros: readonly Macro[]
  inMacro: boolean
  /** The graph's graph-wide types, such as the mob's own entity type for Self. */
  bindings: GraphBindings
  readOnly: boolean
  /** A preview only looked at, never edited, whose view is dragged without Space; fixed for the canvas's life. */
  preview?: boolean
  heldBy: string | null
  problemCount: number
}>()

const panOnDrag = props.preview === true
const selectionKey = props.preview ? null : true

const emit = defineEmits<{
  inspect: [node: string | null]
  problems: []
}>()

const flowId = useId()
const flow = useVueFlow(flowId)
const {editor, apply} = useGraphEdits(() => props)
const {t} = useI18n()
const pulled = ref<PulledPin | null>(null)
const wired = ref(false)
const picking = ref(false)
const pickAt = ref({x: 0, y: 0})
const dropAt = ref({x: 0, y: 0})
const matches = computed(() => pulled.value
  ? nodeMatches(props.catalog, pulled.value, props.inMacro)
  : addableNodes(props.catalog, props.macros, props.inMacro))

const GRID = 22
const freely = ref(false)
const viewport = computed(() => flow.viewport.value)
const grid = computed(() => ({
  backgroundSize: `${GRID * viewport.value.zoom}px ${GRID * viewport.value.zoom}px`,
  backgroundPosition: `${viewport.value.x}px ${viewport.value.y}px`,
}))

useEventListener('keydown', (event: KeyboardEvent) => {
  freely.value = event.altKey
})
useEventListener('keyup', (event: KeyboardEvent) => {
  freely.value = event.altKey
})

const session = useEditorSession()
const effects = computed(() => effectLabels(session.payload.value))
const sessionChoices = computed(() => ({[`${PLAY_EFFECT}|effect`]: {options: Object.keys(effects.value), labels: effects.value}}))
const view = computed(() => graphView(props.graph, props.catalog, props.macros, props.bindings, sessionChoices.value))
const edgesById = computed(() => new Map(view.value.edges.map(edge => [edge.id, edge])))
const playback = graphPlayback()
const shownPositions = ref<ReadonlyMap<string, Position>>(new Map())
watch(() => Object.keys(props.graph.nodes ?? {}).join(), () => {
  shownPositions.value = new Map()
})
const frames = useCanvasFrames({
  graph: () => props.graph,
  base: () => props.base,
  readOnly: () => props.readOnly,
  view,
  shownPositions,
  flow,
  playback,
  problems: () => session.data.value.problems,
  editor,
  apply,
  grid: GRID,
  viewCenter,
  keys: !props.preview,
})
const {nodes, edges} = frames

/** Plays the order a chain runs in, from the node that starts it. */
function played(start: string): void {
  playback.play(executionOrder(props.graph, props.catalog, start))
}

onUnmounted(() => playback.stop())

function onGrid(position: Position): Position {
  return {x: Math.round(position.x / GRID) * GRID, y: Math.round(position.y / GRID) * GRID}
}

function typed(node: string, port: string, value: unknown): void {
  if (!props.readOnly) void apply(editor().literal(node, port, value))
}

function configured(node: string, field: string, value: unknown): void {
  if (!props.readOnly) void apply(configEdit(props.base, props.graph, props.catalog, node, field, value))
}

function coloured(node: string, hex: string): void {
  if (!props.readOnly) void apply(channelsEdit(props.base, props.graph, node, hex))
}

/**
 * Puts a reroute on a wire where it was double clicked: the wire runs into the reroute and on from it.
 */
function rerouted({edge, event}: EdgeMouseEvent): void {
  const wire = edgesById.value.get(edge.id)
  if (props.readOnly || !wire || !('clientX' in event)) return
  const at = flow.screenToFlowCoordinate({x: event.clientX, y: event.clientY})
  const position = {x: at.x - 8, y: at.y - 8}
  const changes = editor()
  if (wire.targetHandle === handles.execIn) {
    const port = wire.sourceHandle.slice(5)
    const {id, operations} = changes.add('reroute.execution', position, {executions: {next: [wire.target]}})
    void apply([...operations, ...changes.redirectExecution(wire.source, port, wire.target, id)])
    return
  }
  const {id, operations} = changes.add('reroute', position, {inputs: {value: {node: wire.source, port: wire.sourceHandle.slice(4)}}})
  void apply([...operations, ...changes.connectData(wire.target, wire.targetHandle.slice(3), id, 'value')])
}

function connected(connection: Connection): void {
  wired.value = true
  if (props.readOnly) return
  const {source, sourceHandle, target, targetHandle} = connection
  if (sourceHandle?.startsWith('exec:') && targetHandle === handles.execIn) {
    void apply(editor().connectExecution(source, sourceHandle.slice(5), target))
  } else if (sourceHandle?.startsWith('out:') && targetHandle?.startsWith('in:')) {
    void apply(editor().connectData(target, targetHandle.slice(3), source, sourceHandle.slice(4)))
  }
}

function validConnection(connection: Connection): boolean {
  const execution = connection.sourceHandle?.startsWith('exec:') && connection.targetHandle === handles.execIn
  const data = connection.sourceHandle?.startsWith('out:') && connection.targetHandle?.startsWith('in:')
  const framed = connection.sourceHandle?.startsWith('cout:') || connection.targetHandle?.startsWith('cin:')
  return Boolean(execution || data || framed) && connection.source !== connection.target
}

const removedEdges = new Set<string>()
const removedNodes = new Set<string>()
const removedFrames = new Set<string>()
let removing = false

/**
 * Collects what one delete removes and sends it as one change on the next tick: deleting a node also reports its
 * wires as removed, and sending those first would leave the node removal working on a graph that has moved on.
 * Wires of removed nodes go with the nodes.
 */
function removeLater(): void {
  if (removing) return
  removing = true
  void nextTick(() => {
    removing = false
    const nodes = [...removedNodes]
    const comments = [...removedFrames]
    const wires = [...removedEdges]
      .map(id => edgesById.value.get(id))
      .filter((edge): edge is EdgeView => edge !== undefined && !removedNodes.has(edge.source) && !removedNodes.has(edge.target))
    removedNodes.clear()
    removedEdges.clear()
    removedFrames.clear()
    if (props.readOnly) return
    void apply([...unwire(nodes, wires), ...comments.flatMap(id => editor().removeComment(id))])
  })
}

function edgesChanged(edgeChanges: EdgeChange[]): void {
  edgeChanges.forEach(change => {
    if (change.type === 'remove') removedEdges.add(change.id)
  })
  removeLater()
}

/**
 * Removes nodes and wires in one change: data wires by their input, execution wires by their position, all of one
 * output's list together and the later ones first.
 */
function unwire(nodes: readonly string[], wires: readonly EdgeView[]) {
  const executions = wires
    .filter(wire => wire.targetHandle === handles.execIn)
    .map(wire => ({source: wire.source, port: wire.sourceHandle.slice(5), position: wire.position ?? 0}))
  const data = wires.filter(wire => wire.targetHandle !== handles.execIn)
  return [...data.flatMap(wire => editor().disconnectData(wire.target, wire.targetHandle.slice(3))), ...editor().remove(nodes, executions)]
}

function nodesChanged(nodeChanges: NodeChange[]): void {
  nodeChanges.forEach(change => {
    if (change.type !== 'remove') return
    if (change.id.startsWith(COMMENT_NODE)) removedFrames.add(change.id.slice(COMMENT_NODE.length))
    else removedNodes.add(change.id)
  })
  if (removedNodes.size > 0 || removedFrames.size > 0) removeLater()
}

/**
 * Opens a node's details on a double click, unless the click was meant for one of its fields, such as the arrows of
 * a number.
 */
function inspected({node, event}: NodeMouseEvent): void {
  if (event.target instanceof Element && event.target.closest('input, select, textarea, button')) return
  if (frames.doubleClicked(node.id)) return
  emit('inspect', node.id)
}

function pullStarted({nodeId, handleId}: OnConnectStartParams): void {
  wired.value = false
  pulled.value = nodeId && handleId ? pulledPin(props.graph, props.catalog, nodeId, handleId, props.bindings) : null
}

function pullEnded(event?: MouseEvent | TouchEvent): void {
  if (props.readOnly || wired.value || !pulled.value || !event) return
  const point = 'changedTouches' in event ? event.changedTouches[0] : event
  const bounds = flow.vueFlowRef.value?.getBoundingClientRect()
  if (!point || !bounds) return
  pickAt.value = {x: point.clientX - bounds.left, y: point.clientY - bounds.top}
  dropAt.value = flow.screenToFlowCoordinate({x: point.clientX, y: point.clientY})
  picking.value = true
}

function picked(match: NodeMatch): void {
  if (props.readOnly) return
  if (!pulled.value) {
    void apply(editor().add(match.type, dropAt.value).operations)
    return
  }
  void apply(editor().addWired(match.type, dropAt.value, pulled.value, match.port, match.config).operations)
}

/**
 * Opens the picker with every node where the empty canvas was double clicked.
 */
function paneDoubleClicked(event: MouseEvent): void {
  if (props.readOnly || !(event.target instanceof Element) || !event.target.classList.contains('vue-flow__pane')) return
  const bounds = flow.vueFlowRef.value?.getBoundingClientRect()
  if (!bounds) return
  pulled.value = null
  pickAt.value = {x: event.clientX - bounds.left, y: event.clientY - bounds.top}
  dropAt.value = flow.screenToFlowCoordinate({x: event.clientX, y: event.clientY})
  picking.value = true
}

function measured(): LayoutNode[] {
  return flow.getNodes.value.filter(node => frames.arranged(node.id)).map(node => ({
    id: node.id,
    width: node.dimensions.width,
    height: node.dimensions.height,
    ports: [
      ...(node.handleBounds.target ?? []).map(handle => ({
        id: handle.id ?? '', x: handle.x + handle.width / 2, y: handle.y + handle.height / 2, side: 'WEST' as const,
      })),
      ...(node.handleBounds.source ?? []).map(handle => ({
        id: handle.id ?? '', x: handle.x + handle.width / 2, y: handle.y + handle.height / 2, side: 'EAST' as const,
      })),
    ],
  }))
}

function save(positions: ReadonlyMap<string, Position>): Promise<boolean> {
  return apply([...positions].flatMap(([id, position]) => editor().move(id, position)))
}

/** Lays the whole graph out with ELK and saves every position. */
async function arrangeAll(): Promise<void> {
  if (props.readOnly || flow.getNodes.value.length === 0) return
  const before = frames.frames.value
  const sizes = new Map(flow.getNodes.value.map(node => [node.id, node.dimensions]))
  const arranged = await arrange(measured(), frames.routed.value.edges)
  const positions = new Map([...arranged].map(([id, position]) => [id, onGrid(position)]))
  const moves = [...positions].filter(([id]) => !id.startsWith(COMMENT_NODE)).flatMap(([id, position]) => editor().move(id, position))
  if (await apply([...moves, ...frames.afterArrange(before, positions, sizes)])) void nextTick(fitUnlessMoved)
}

/** Lays out a graph that may not be changed, for this view only, so a read-only graph without positions is readable. */
async function arrangeShown(): Promise<void> {
  if (flow.getNodes.value.length === 0) return
  const arranged = await arrange(measured(), frames.routed.value.edges)
  shownPositions.value = new Map([...arranged].map(([id, position]) => [id, onGrid(position)]))
  void nextTick(fitUnlessMoved)
}

/** Whether every node has its place and the view was fitted to them; the canvas carries it as `data-arranged`. */
const arranged = ref(false)
let movedWhileArranging = false

flow.onMoveStart(({event}) => {
  if (!arranged.value && (event as {sourceEvent?: unknown}).sourceEvent) movedWhileArranging = true
})

/** Fits the view to the arranged graph, unless the user moved it while the graph was being arranged. */
function fitUnlessMoved(): void {
  if (!movedWhileArranging) void flow.fitView()
}

/**
 * Saves a position for every node that has none: the whole graph arranged when no node has one, else the unplaced
 * nodes where they are shown.
 */
async function placeUnplaced(): Promise<void> {
  const nodes = props.graph.nodes ?? {}
  const unplaced = Object.keys(nodes).filter(id => !storedPosition(nodes[id]!))
  if (unplaced.length === 0) return
  if (props.readOnly) {
    if (unplaced.length === Object.keys(nodes).length) await arrangeShown()
    return
  }
  if (unplaced.length === Object.keys(nodes).length) {
    await arrangeAll()
    return
  }
  const shown = new Map(view.value.nodes.map(node => [node.id, node.position]))
  await save(new Map(unplaced.flatMap(id => shown.has(id) ? [[id, shown.get(id)!] as const] : [])))
}

flow.onNodesInitialized(() => {
  arranged.value = false
  movedWhileArranging = false
  void placeUnplaced().then(() => nextTick()).then(() => {
    arranged.value = true
  })
})

/** Adds a node of a type in the middle of what the canvas shows. */
function add(type: string): void {
  if (props.readOnly) return
  void apply(editor().add(type, viewCenter()).operations)
}

function viewCenter(): Position {
  const bounds = flow.vueFlowRef.value?.getBoundingClientRect()
  return flow.screenToFlowCoordinate({x: (bounds?.left ?? 0) + (bounds?.width ?? 0) / 2, y: (bounds?.top ?? 0) + (bounds?.height ?? 0) / 2})
}

defineExpose({add})
</script>

<template>
  <div class="absolute inset-0">
    <VueFlow
        :id="flowId"
        :delete-key-code="readOnly ? null : ['Delete', 'Backspace']"
        :edges="edges"
        :edges-updatable="false"
        :elements-selectable="true"
        :is-valid-connection="validConnection"
        :max-zoom="2"
        :min-zoom="0.2"
        :nodes="nodes"
        :nodes-connectable="!readOnly"
        :nodes-draggable="!readOnly"
        :pan-on-drag="panOnDrag"
        :snap-grid="[GRID, GRID]"
        :snap-to-grid="!freely"
        :selection-key-code="selectionKey"
        pan-activation-key-code="Space"
        :zoom-on-double-click="false"
        :style="grid"
        :data-arranged="arranged"
        class="bn-canvas"
        fit-view-on-init
        @connect="connected"
        @connect-end="pullEnded"
        @connect-start="pullStarted"
        @dblclick="paneDoubleClicked"
        @edge-double-click="rerouted"
        @edges-change="edgesChanged"
        @nodes-change="nodesChanged"
        @node-double-click="inspected"
        @pane-click="emit('inspect', null)"
    >
      <template #node-comment="frameProps">
        <CommentFrame v-bind="frames.frameBindings(frameProps.data as CommentView)" :selected="frameProps.selected" @edit="edit => frames.edit((frameProps.data as CommentView).id, edit)" @named="frames.naming.value = null" @toggle="frames.toggle((frameProps.data as CommentView).id)"/>
      </template>
      <template #node-blueprint="nodeProps">
        <GraphNode
            :data="nodeProps.data as NodeView"
            :held-by="heldBy"
            :read-only="readOnly"
            :selected="nodeProps.selected"
            :step="playback.order.value.get(nodeProps.id)"
            @play="played(nodeProps.id)"
            @channels="hex => coloured(nodeProps.id, hex)"
            @config="(field, value) => configured(nodeProps.id, field, value)"
            @literal="(port, value) => typed(nodeProps.id, port, value)"
        />
      </template>
    </VueFlow>
    <NodePicker
        v-model="picking"
        :at="pickAt"
        :empty="pulled ? t('editor.picker.none') : t('editor.picker.noMatch')"
        :matches="matches"
        :title="pulled ? t('editor.picker.title') : t('editor.picker.add')"
        @pick="picked"
    />
    <CanvasShortcuts/>
    <CanvasToolbar
        :problem-count="problemCount"
        :read-only="readOnly"
        @arrange="arrangeAll"
        @comment="frames.frameSelection"
        @fit="flow.fitView()"
        @problems="emit('problems')"
        @zoom-in="flow.zoomIn()"
        @zoom-out="flow.zoomOut()"
    />
  </div>
</template>

<style scoped>
.bn-canvas {
  background-color: var(--color-ground);
  background-image: radial-gradient(var(--color-line-soft) 1px, transparent 1px);
  background-size: 22px 22px;
}

:deep(.vue-flow__node) {
  padding: 0;
  border: 0;
  background: transparent;
}

:deep(.vue-flow__node.bn-lit) {
  filter: drop-shadow(0 0 6px color-mix(in srgb, var(--color-focus) 70%, transparent));
  transition: filter 200ms ease-out;
}

:deep(.vue-flow__node.bn-current) {
  filter: drop-shadow(0 0 14px var(--color-focus));
}

:deep(.vue-flow__edge.bn-lit-wire path) {
  filter: drop-shadow(0 0 4px currentColor);
}

:deep(.vue-flow__selection),
:deep(.vue-flow__nodesselection-rect) {
  border: 1px dashed var(--color-focus);
  background: color-mix(in srgb, var(--color-focus) 12%, transparent);
}
</style>
