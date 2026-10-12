/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import BareButton from '@/components/button/BareButton.vue'
import FloatingPanel from '@/components/feedback/FloatingPanel.vue'
import SearchInput from '@/components/input/text/SearchInput.vue'
import MutedText from '@/components/typography/MutedText.vue'
import type {NodeMatch} from '~/util/graph/nodeMatches'

/**
 * Nodes to add, opened where they go: the nodes a pulled wire can go to, where the wire was let go, or every node,
 * where the empty canvas was double clicked. Searchable, grouped by category, picked by click or by the arrow keys and
 * enter.
 */
const open = defineModel<boolean>({required: true})

const props = defineProps<{
  matches: readonly NodeMatch[]
  at: {x: number, y: number}
  /** What the picker is named for screen readers and its empty list, e.g. the nodes for a wire. */
  title: string
  empty: string
}>()

const emit = defineEmits<{
  pick: [match: NodeMatch]
}>()

const {t} = useI18n()
const query = ref('')
const active = ref(0)
const search = ref<InstanceType<typeof SearchInput> | null>(null)

const shown = computed(() => {
  const needle = query.value.trim().toLowerCase()
  return props.matches.filter(match => needle === '' || match.name.toLowerCase().includes(needle) || match.type.includes(needle))
})
const groups = computed(() => {
  const byCategory = new Map<string, {match: NodeMatch, index: number}[]>()
  shown.value.forEach((match, index) => byCategory.set(match.category, [...(byCategory.get(match.category) ?? []), {match, index}]))
  return [...byCategory.entries()]
})

watch(query, () => {
  active.value = 0
})

watch(open, now => {
  if (now) query.value = ''
})

function step(by: number): void {
  if (shown.value.length > 0) active.value = (active.value + by + shown.value.length) % shown.value.length
}

function pick(match: NodeMatch | undefined): void {
  if (!match) return
  open.value = false
  emit('pick', match)
}

const tones = {event: 'bg-node-event', action: 'bg-node-action', value: 'bg-node-value', flow: 'bg-node-flow', macro: 'bg-node-macro'}
</script>

<template>
  <div :style="{left: `${at.x}px`, top: `${at.y}px`}" class="absolute size-0">
    <FloatingPanel v-model:open="open" :label="title" align="start" panel-class="flex w-72 flex-col gap-2 p-2" role="dialog" @opened="search?.focus()">
      <SearchInput
          ref="search"
          v-model="query"
          :aria-label="t('editor.picker.search')"
          :placeholder="t('editor.picker.search')"
          @keydown.down.prevent="step(1)"
          @keydown.enter.prevent="pick(shown[active])"
          @keydown.up.prevent="step(-1)"
      />
      <MutedText v-if="shown.length === 0" class="px-2 py-1" size="sm" tag="p">{{ empty }}</MutedText>
      <div v-for="[category, entries] in groups" :key="category" class="flex flex-col">
        <span class="px-2 pb-1 text-xs uppercase tracking-wider text-ink-faint">{{ category }}</span>
        <BareButton
            v-for="entry in entries"
            :key="entry.match.type"
            :class="entry.index === active ? 'bg-selected' : 'hover:bg-raised'"
            class="flex min-h-8 items-center gap-2 rounded-control px-2 text-left text-sm"
            @click="pick(entry.match)"
            @focus="active = entry.index"
            @mouseenter="active = entry.index"
        >
          <span :class="tones[entry.match.tone]" aria-hidden="true" class="size-2.5 shrink-0 rounded-sm"/>
          <span class="truncate">{{ entry.match.name }}</span>
        </BareButton>
      </div>
    </FloatingPanel>
  </div>
</template>
