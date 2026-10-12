/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import BareButton from '@/components/button/BareButton.vue'
import MutedIconButton from '@/components/button/MutedIconButton.vue'
import SearchInput from '@/components/input/text/SearchInput.vue'
import SectionLabel from '@/components/typography/SectionLabel.vue'
import type {Catalog} from '~/api/generated/schema'
import type {Macro} from '~/api/generated/payload'
import {addableNodes} from '~/util/graph/nodeMatches'

/**
 * The nodes that can be added: the catalog's, grouped by category, and the session's macros. Nodes only macros
 * may hold are offered in macros only.
 */
const props = defineProps<{
  catalog: Catalog
  macros: readonly Macro[]
  inMacro: boolean
  readOnly: boolean
}>()

const emit = defineEmits<{
  add: [type: string]
  close: []
}>()

const {t} = useI18n()
const query = ref('')

const groups = computed(() => {
  const needle = query.value.trim().toLowerCase()
  const entries = addableNodes(props.catalog, props.macros, props.inMacro)
    .filter(entry => needle === '' || entry.name.toLowerCase().includes(needle) || entry.type.includes(needle))
  const byCategory = new Map<string, typeof entries>()
  for (const entry of entries) byCategory.set(entry.category, [...(byCategory.get(entry.category) ?? []), entry])
  return [...byCategory.entries()].sort(([a], [b]) => a.localeCompare(b))
})

const tones = {event: 'bg-node-event', action: 'bg-node-action', value: 'bg-node-value', flow: 'bg-node-flow', macro: 'bg-node-macro'}
</script>

<template>
  <aside :aria-label="t('editor.palette.title')" class="flex max-h-full w-64 flex-col gap-2 rounded-panel border border-line bg-surface p-3">
    <div class="flex items-center justify-between">
      <SectionLabel>{{ t('editor.palette.title') }}</SectionLabel>
      <MutedIconButton :label="t('editor.palette.close')" icon="xmark" @click="emit('close')"/>
    </div>
    <SearchInput v-model="query" :placeholder="t('editor.palette.search', {count: catalog.nodes?.length ?? 0})"/>
    <div class="flex min-h-0 flex-col gap-3 overflow-y-auto">
      <div v-for="[category, entries] in groups" :key="category" class="flex flex-col">
        <span class="px-2 pb-1 text-xs uppercase tracking-wider text-ink-faint">{{ category }}</span>
        <BareButton
            v-for="entry in entries"
            :key="entry.type"
            :disabled="readOnly"
            class="flex min-h-9 items-center gap-2 rounded-control px-2 text-left text-sm hover:bg-raised"
            @click="emit('add', entry.type)"
        >
          <span :class="tones[entry.tone]" aria-hidden="true" class="size-2.5 shrink-0 rounded-sm"/>
          <span class="truncate">{{ entry.name }}</span>
        </BareButton>
      </div>
    </div>
  </aside>
</template>
