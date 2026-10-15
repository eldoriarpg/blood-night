/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import InfoBadge from '@/components/badge/InfoBadge.vue'
import CountBadge from '@/components/badge/CountBadge.vue'
import BareButton from '@/components/button/BareButton.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import SectionLabel from '@/components/typography/SectionLabel.vue'
import SearchInput from '@/components/input/text/SearchInput.vue'
import type {WorkspaceEntry} from '~/views/editorview/workspace/workspaceEntries'
import {mobLabel} from '~/util/mobs/mobName'

/**
 * The mobs of the session in a sidebar, each by its name in the editor's language with its id below, searchable by
 * both, with how many problems each has, who else holds it and whether the store has an update for it; Add asks for
 * a new mob or one from the store.
 */
const props = defineProps<{
  entries: readonly WorkspaceEntry[]
  current: string | undefined
  problems: Readonly<Record<string, number>>
  updates: ReadonlySet<string>
}>()

const emit = defineEmits<{
  pick: [id: string]
  add: []
}>()

const locks = useEditorLocks()
const {t, locale} = useI18n()
const query = ref('')
const label = (entry: WorkspaceEntry) => mobLabel(entry.name, locale.value)
const found = computed(() => {
  const typed = query.value.trim().toLowerCase()
  return props.entries.filter(entry => entry.id.includes(typed) || label(entry).toLowerCase().includes(typed))
})
</script>

<template>
  <nav :aria-label="t('editor.mobs.list')" class="flex w-60 shrink-0 flex-col gap-1 overflow-y-auto border-r border-line-soft bg-surface p-3">
    <SectionLabel class="px-2 pb-1">{{ t('editor.mobs.list') }}</SectionLabel>
    <SearchInput v-model="query" :aria-label="t('editor.mobs.search')" :placeholder="t('editor.mobs.search')" class="mb-1"/>
    <BareButton
        v-for="entry in found"
        :key="entry.id"
        :class="entry.id === current ? 'bg-selected' : 'hover:bg-raised'"
        class="flex min-h-9 items-center gap-2 rounded-control px-2 text-left"
        @click="emit('pick', entry.id)"
    >
      <span class="min-w-0 flex-1">
        <span class="block truncate text-sm">{{ label(entry) || entry.id }}</span>
        <span v-if="label(entry)" class="block truncate font-mono text-xs text-ink-faint">{{ entry.id }}</span>
      </span>
      <InfoBadge v-if="updates.has(entry.id)">{{ t('editor.store.state.update') }}</InfoBadge>
      <InfoBadge v-if="locks.heldByOther(entry.section)">{{ locks.heldByOther(entry.section) }}</InfoBadge>
      <CountBadge v-if="(problems[entry.id] ?? 0) > 0" :count="problems[entry.id] ?? 0"/>
    </BareButton>
    <SecondaryButton class="mt-2" compact icon="plus" @click="emit('add')">{{ t('editor.mobs.add') }}</SecondaryButton>
  </nav>
</template>
