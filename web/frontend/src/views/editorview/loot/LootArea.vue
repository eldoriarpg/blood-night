/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import InfoBadge from '@/components/badge/InfoBadge.vue'
import BareButton from '@/components/button/BareButton.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import SectionLabel from '@/components/typography/SectionLabel.vue'
import EmptyState from '@/components/feedback/EmptyState.vue'
import SearchInput from '@/components/input/text/SearchInput.vue'
import LootTableForm from '~/views/editorview/loot/LootTableForm.vue'
import NewLootTableDialog from '~/views/editorview/loot/NewLootTableDialog.vue'
import {section} from '~/util/session/sections'
import {tableEntries} from '~/util/loot/lootTables'

/**
 * The loot tables mobs, worlds and the server assign: the tables in a sidebar, searchable by name, and the picked one's
 * form. Opening a table takes its section; a table someone else holds is shown read only.
 */
const session = useEditorSession()
const locks = useEditorLocks()
const {place, openLootTable} = useEditorNavigation()
const {t} = useI18n()
const creating = ref(false)
const query = ref('')

const tables = computed(() => tableEntries(session.payload.value))
const found = computed(() => {
  const words = query.value.toLowerCase().split(/\s+/).filter(word => word !== '')
  return tables.value.filter(candidate => words.every(word => candidate.id.includes(word) || candidate.table.name.toLowerCase().includes(word)))
})
const entry = computed(() => tables.value.find(candidate => candidate.id === place.value.lootTable) ?? tables.value[0])

watch(() => entry.value?.id, (now, before) => {
  if (before) void locks.release(section.lootTable(before))
  if (now) void locks.take(section.lootTable(now))
}, {immediate: true})

onBeforeUnmount(() => {
  if (entry.value) void locks.release(section.lootTable(entry.value.id))
})
</script>

<template>
  <div class="flex min-h-0 flex-1">
    <nav :aria-label="t('editor.loot.list')" class="flex w-60 shrink-0 flex-col gap-1 overflow-y-auto border-r border-line-soft bg-surface p-3">
      <SectionLabel class="px-2 pb-1">{{ t('editor.loot.list') }}</SectionLabel>
      <SearchInput v-model="query" :aria-label="t('editor.loot.search')" :placeholder="t('editor.loot.search')" class="mb-1"/>
      <BareButton
          v-for="candidate in found"
          :key="candidate.id"
          :class="candidate.id === entry?.id ? 'bg-selected' : 'hover:bg-raised'"
          class="flex min-h-10 items-center gap-2 rounded-control px-2 text-left"
          @click="openLootTable(candidate.id)"
      >
        <span class="min-w-0 flex-1">
          <span class="block truncate text-sm">{{ candidate.table.name || candidate.id }}</span>
          <span class="block truncate text-xs text-ink-faint">{{ t('editor.loot.entryCount', {count: candidate.table.entries.length}) }}</span>
        </span>
        <InfoBadge v-if="locks.heldByOther(section.lootTable(candidate.id))">{{ locks.heldByOther(section.lootTable(candidate.id)) }}</InfoBadge>
      </BareButton>
      <SecondaryButton class="mt-2" compact icon="plus" @click="creating = true">{{ t('editor.loot.new') }}</SecondaryButton>
    </nav>
    <LootTableForm v-if="entry" :key="entry.id" :entry="entry" class="min-w-0 flex-1" @deleted="openLootTable('')"/>
    <EmptyState v-else :message="t('editor.loot.empty')" class="flex-1"/>
    <NewLootTableDialog v-model="creating" @created="openLootTable"/>
  </div>
</template>
