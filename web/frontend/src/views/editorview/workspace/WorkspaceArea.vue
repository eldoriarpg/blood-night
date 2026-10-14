/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import InfoBadge from '@/components/badge/InfoBadge.vue'
import GraphCanvas from '~/views/editorview/workspace/GraphCanvas.vue'
import EntrySwitcher from '~/views/editorview/workspace/EntrySwitcher.vue'
import NodePalette from '~/views/editorview/workspace/NodePalette.vue'
import NodeInspector from '~/views/editorview/workspace/NodeInspector.vue'
import EntrySheet from '~/views/editorview/workspace/EntrySheet.vue'
import ProblemsPanel from '~/views/editorview/workspace/ProblemsPanel.vue'
import NewEntryDialog from '~/views/editorview/workspace/NewEntryDialog.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import PrimaryButton from '@/components/button/PrimaryButton.vue'
import EmptyState from '@/components/feedback/EmptyState.vue'
import {workspaceEntries, type WorkspaceKind} from '~/views/editorview/workspace/workspaceEntries'
import {mobBindings} from '~/util/graph/graphBindings'
import {withRoutines} from '~/util/graph/routineCalls'
import {withVariants} from '~/util/graph/mutate'

/**
 * The workspace of mobs or macros: the open entry's graph filling the area, with the switcher, the node palette, the
 * inspector or the entry's settings, and the problems floating over it. Opening an entry takes its section, leaving it
 * releases the section; an entry someone else holds is shown read only. Mutate nodes whose type no longer fits what is
 * wired into them, e.g. after the mob's entity type changed, are corrected as soon as the entry can be edited. Each
 * entry gets a canvas of its own: Vue Flow takes in a swapped graph's wires before its nodes and drops every wire whose
 * ends it does not know yet.
 */
const props = defineProps<{
  kind: WorkspaceKind
}>()

const session = useEditorSession()
const locks = useEditorLocks()
const selection = useWorkspaceSelection()
const {t} = useI18n()
const canvas = ref<InstanceType<typeof GraphCanvas> | null>(null)
const selectedNode = ref<string | null>(null)
const paletteOpen = ref(true)
const sheetOpen = ref(false)
const problemsOpen = ref(false)
const creating = ref(false)

const entries = computed(() => session.payload.value ? workspaceEntries(session.payload.value, props.kind) : [])
const entry = computed(() => entries.value.find(candidate => candidate.id === selection.value[props.kind]) ?? entries.value[0])
const bindings = computed(() => props.kind === 'mobs' && entry.value ? mobBindings(session.payload.value, entry.value.id) : {})
const heldBy = computed(() => entry.value ? locks.heldByOther(entry.value.section) : null)
const denied = computed(() => entry.value ? !locks.mayChange(entry.value.section) : false)
const readOnly = computed(() => heldBy.value !== null || denied.value)
const changes = useEditorChanges()

watch([() => entry.value?.graph, () => session.catalog.value, bindings], ([graph, catalog]) => {
  if (!graph || !catalog || !entry.value || readOnly.value) return
  const fixes = withVariants(entry.value.base, graph, withRoutines(catalog, graph), [], bindings.value)
  if (fixes.length > 0) void changes.apply(fixes)
}, {immediate: true})
const problems = computed(() => session.data.value.problems.filter(problem => problem.path.startsWith(`/${props.kind}/`)))
const problemsByEntry = computed(() => {
  const counts: Record<string, number> = {}
  for (const entry of entries.value) counts[entry.id] = problems.value.filter(problem => problem.path.startsWith(entry.path + '/')).length
  return counts
})

watch(() => entry.value?.section, (now, before) => {
  if (before) void locks.release(before)
  if (now) void locks.take(now)
  selectedNode.value = null
}, {immediate: true})

onBeforeUnmount(() => {
  if (entry.value) void locks.release(entry.value.section)
})

/** The server's catalog with the call nodes of the routines the open graph defines. */
const graphCatalog = computed(() => session.catalog.value && entry.value ? withRoutines(session.catalog.value, entry.value.graph) : undefined)

function pick(id: string): void {
  selection.value[props.kind] = id
}
</script>

<template>
  <div class="relative flex-1 overflow-hidden">
    <template v-if="entry && session.catalog.value">
      <GraphCanvas
          :key="`${kind}:${entry.id}`"
          ref="canvas"
          :base="entry.base"
          :bindings="bindings"
          :catalog="graphCatalog ?? session.catalog.value"
          :graph="entry.graph"
          :held-by="heldBy"
          :in-macro="kind === 'macros'"
          :macros="session.payload.value?.macros ?? []"
          :problem-count="problems.length"
          :read-only="readOnly"
          @problems="problemsOpen = !problemsOpen"
          @inspect="node => selectedNode = node"
      />
      <div class="pointer-events-none absolute inset-x-4 top-4 flex flex-wrap items-start gap-2">
        <div class="pointer-events-auto flex flex-wrap items-center gap-2">
          <EntrySwitcher :current="entry.id" :entries="entries" :kind="kind" :problems="problemsByEntry" @create="creating = true" @pick="pick"/>
          <SecondaryButton icon="sliders" @click="sheetOpen = !sheetOpen; selectedNode = null">{{ t(`editor.sheet.open.${kind}`) }}</SecondaryButton>
          <InfoBadge v-if="heldBy">{{ t('editor.workspace.readOnly', {name: heldBy}) }}</InfoBadge>
          <InfoBadge v-else-if="denied">{{ t('editor.permission.readOnly') }}</InfoBadge>
        </div>
      </div>
      <div class="pointer-events-none absolute bottom-20 left-4 top-16 flex">
        <NodePalette
            v-if="paletteOpen"
            :catalog="graphCatalog ?? session.catalog.value"
            :in-macro="kind === 'macros'"
            :macros="kind === 'mobs' ? session.payload.value?.macros ?? [] : []"
            :read-only="readOnly"
            class="pointer-events-auto"
            @add="type => canvas?.add(type)"
            @close="paletteOpen = false"
        />
        <div v-else class="pointer-events-auto"><SecondaryButton icon="plus" @click="paletteOpen = true">{{ t('editor.palette.open') }}</SecondaryButton></div>
      </div>
      <div class="pointer-events-none absolute bottom-20 right-4 top-4 flex">
        <EntrySheet v-if="sheetOpen" :entry="entry" :kind="kind" :read-only="readOnly" class="pointer-events-auto" @close="sheetOpen = false" @deleted="sheetOpen = false"/>
        <NodeInspector
            v-else-if="selectedNode"
            :base="entry.base"
            :bindings="bindings"
            :catalog="graphCatalog ?? session.catalog.value"
            :graph="entry.graph"
            :node="selectedNode"
            :read-only="readOnly"
            class="pointer-events-auto"
            @close="selectedNode = null"
        />
      </div>
      <ProblemsPanel v-if="problemsOpen" :problems="problems" @close="problemsOpen = false"/>
    </template>
    <div v-else class="flex h-full flex-col items-center justify-center gap-4">
      <EmptyState :message="t(`editor.workspace.empty.${kind}`)"/>
      <PrimaryButton icon="plus" @click="creating = true">{{ t(`editor.switcher.new.${kind}`) }}</PrimaryButton>
    </div>
    <NewEntryDialog v-model="creating" :kind="kind" :taken="entries.map(candidate => candidate.id)" @created="pick"/>
  </div>
</template>
