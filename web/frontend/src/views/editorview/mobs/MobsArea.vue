/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import EmptyState from '@/components/feedback/EmptyState.vue'
import WorkspaceArea from '~/views/editorview/workspace/WorkspaceArea.vue'
import EntrySheet from '~/views/editorview/workspace/EntrySheet.vue'
import NewEntryDialog from '~/views/editorview/workspace/NewEntryDialog.vue'
import MobList from '~/views/editorview/mobs/MobList.vue'
import MobPreview from '~/views/editorview/mobs/MobPreview.vue'
import AddMobDialog from '~/views/editorview/mobs/AddMobDialog.vue'
import StoreDialog from '~/views/editorview/mobs/StoreDialog.vue'
import {workspaceEntries} from '~/views/editorview/workspace/workspaceEntries'
import {offerState} from '~/util/session/storeOffers'

/**
 * The mobs: the list on the left, the picked mob's attributes in the middle and its behaviour as a read-only graph on
 * the right. Edit opens the full graph editor for the mob, which leads back here. Add makes a new mob or takes one from
 * the store. The picked mob's section is held while it is open here, as in the graph editor.
 */
const session = useEditorSession()
const locks = useEditorLocks()
const selection = useWorkspaceSelection()
const {t} = useI18n()
const editing = ref(false)
const adding = ref(false)
const creating = ref(false)
const shopping = ref(false)

const entries = computed(() => session.payload.value ? workspaceEntries(session.payload.value, 'mobs') : [])
const entry = computed(() => entries.value.find(candidate => candidate.id === selection.value.mobs) ?? entries.value[0])
const readOnly = computed(() => entry.value ? locks.heldByOther(entry.value.section) !== null || !locks.mayChange(entry.value.section) : true)
const problems = computed(() => Object.fromEntries(entries.value.map(candidate => [
  candidate.id,
  session.data.value.problems.filter(problem => problem.path.startsWith(candidate.path + '/')).length,
])))
const updates = computed<ReadonlySet<string>>(() => {
  const payload = session.payload.value
  const offers = payload?.store?.offers ?? []
  return new Set(payload ? offers.filter(offer => offerState(offer, payload) === 'update').map(offer => offer.id) : [])
})

watch([() => entry.value?.section, editing], ([now, inEditor], [before]) => {
  if (before && before !== now) void locks.release(before)
  if (now && !inEditor) void locks.take(now)
}, {immediate: true})

onBeforeUnmount(() => {
  if (entry.value) void locks.release(entry.value.section)
})

function pick(id: string): void {
  selection.value.mobs = id
}

function choose(way: 'new' | 'store'): void {
  if (way === 'new') creating.value = true
  else shopping.value = true
}
</script>

<template>
  <WorkspaceArea v-if="editing" back kind="mobs" @back="editing = false"/>
  <div v-else class="flex min-h-0 flex-1">
    <MobList :current="entry?.id" :entries="entries" :problems="problems" :updates="updates" @add="adding = true" @pick="pick"/>
    <template v-if="entry">
      <div class="w-[26rem] shrink-0 border-r border-line-soft"><EntrySheet :key="entry.id" :entry="entry" :read-only="readOnly" inline kind="mobs"/></div>
      <MobPreview :entry="entry" @edit="editing = true"/>
    </template>
    <EmptyState v-else :message="t('editor.workspace.empty.mobs')" class="flex-1"/>
    <AddMobDialog v-model="adding" @choose="choose"/>
    <NewEntryDialog v-model="creating" :taken="entries.map(candidate => candidate.id)" kind="mobs" @created="pick"/>
    <StoreDialog v-model="shopping" @installed="pick"/>
  </div>
</template>
