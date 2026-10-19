/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import ActionsMenu from '@/components/button/ActionsMenu.vue'
import BareButton from '@/components/button/BareButton.vue'
import DropdownMenuItem from '@/components/button/DropdownMenuItem.vue'
import NewIdDialog from '~/views/editorview/form/NewIdDialog.vue'
import type {Graph} from '~/api/generated/payload'
import {StateChanges, removable, stateRows} from '~/util/graph/graphStates'

/**
 * The states of a mob's graph as tabs: the part that always runs first, then every state, the ones inside another
 * indented below it and the initial one of each level starred. The menu adds a state at the top or inside the open
 * one, renames the open state everywhere it is named, makes it the initial one, and removes it once nothing lies in it.
 */
type Naming = 'add' | 'inside' | 'rename'

const state = defineModel<string | undefined>('state', {required: true})

const props = defineProps<{
  graph: Graph
  base: string
  readOnly: boolean
}>()

const emit = defineEmits<{
  overview: []
}>()

const {t} = useI18n()
const changes = useEditorChanges()
const rows = computed(() => stateRows(props.graph))
const names = computed(() => rows.value.map(row => row.name))
const current = computed(() => rows.value.find(row => row.name === state.value))
const naming = ref<Naming | null>(null)
const dialogOpen = computed({
  get: () => naming.value !== null,
  set: open => {
    if (!open) naming.value = null
  },
})

function edits(): StateChanges {
  return new StateChanges(props.base, props.graph)
}

function operations(_: unknown, name: string) {
  const mode = naming.value
  if (mode === 'rename' && state.value) return edits().rename(state.value, name)
  return edits().add(name, mode === 'inside' ? state.value : undefined)
}

function named(name: string): void {
  naming.value = null
  state.value = name
}

async function makeInitial(): Promise<void> {
  if (state.value) await changes.apply(edits().initial(state.value))
}

async function remove(): Promise<void> {
  const removed = state.value
  if (!removed || !removable(props.graph, removed)) return
  state.value = current.value?.parent
  await changes.apply(edits().remove(removed))
}
</script>

<template>
  <div :aria-label="t('editor.states.bar')" class="flex max-w-full flex-wrap items-center gap-1 rounded-panel border border-line bg-surface p-1" role="tablist">
    <BareButton
        :aria-selected="state === undefined"
        :class="state === undefined ? 'bg-selected' : 'hover:bg-raised'"
        class="min-h-8 rounded-control px-2 text-sm"
        role="tab"
        @click="state = undefined"
    >{{ t('editor.states.general') }}</BareButton>
    <BareButton
        v-for="row in rows"
        :key="row.name"
        :aria-selected="state === row.name"
        :class="state === row.name ? 'bg-selected' : 'hover:bg-raised'"
        :style="{marginLeft: `${row.depth * 0.75}rem`}"
        :title="row.initial ? t('editor.states.initial') : undefined"
        class="min-h-8 rounded-control px-2 font-mono text-sm"
        role="tab"
        @click="state = row.name"
    >{{ row.initial ? `★ ${row.name}` : row.name }}</BareButton>
    <ActionsMenu :label="t('editor.states.menu')" test-id="state-menu">
      <DropdownMenuItem :disabled="readOnly" icon="plus" @click="naming = 'add'">{{ t('editor.states.add') }}</DropdownMenuItem>
      <DropdownMenuItem :disabled="readOnly || !current" icon="plus" @click="naming = 'inside'">{{ t('editor.states.addInside') }}</DropdownMenuItem>
      <DropdownMenuItem :disabled="readOnly || !current" icon="pen" @click="naming = 'rename'">{{ t('editor.states.rename') }}</DropdownMenuItem>
      <DropdownMenuItem :disabled="readOnly || !current || current.initial" icon="star" @click="makeInitial">{{ t('editor.states.makeInitial') }}</DropdownMenuItem>
      <DropdownMenuItem :disabled="rows.length === 0" icon="diagram-project" @click="emit('overview')">{{ t('editor.states.overview') }}</DropdownMenuItem>
      <DropdownMenuItem :disabled="readOnly || !current || !removable(graph, current.name)" destructive icon="trash" @click="remove">{{ t('editor.states.remove') }}</DropdownMenuItem>
    </ActionsMenu>
    <NewIdDialog
        v-model="dialogOpen"
        :operations="operations"
        :ready="true"
        :taken="names"
        :title="t(`editor.states.title.${naming ?? 'add'}`)"
        @created="named"
    />
  </div>
</template>
