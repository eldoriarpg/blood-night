/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import InfoBadge from '@/components/badge/InfoBadge.vue'
import Popover from '@/components/feedback/Popover.vue'
import SecondaryButton from '@/components/button/SecondaryButton.vue'
import BareButton from '@/components/button/BareButton.vue'
import SearchInput from '@/components/input/text/SearchInput.vue'
import CountBadge from '@/components/badge/CountBadge.vue'
import type {WorkspaceEntry, WorkspaceKind} from '~/views/editorview/workspace/workspaceEntries'

/** Picks the mob or macro the workspace shows; marks entries others hold and entries with problems. */
const props = defineProps<{
  kind: WorkspaceKind
  entries: readonly WorkspaceEntry[]
  current: string
  problems: Record<string, number>
}>()

const emit = defineEmits<{
  pick: [id: string]
  create: []
}>()

const locks = useEditorLocks()
const {t} = useI18n()
const query = ref('')

const shown = computed(() => props.entries.filter(entry => entry.id.includes(query.value.trim().toLowerCase())))
</script>

<template>
  <Popover :label="t(`editor.switcher.${kind}`)" align="start" panel-class="w-80 p-2" role="dialog">
    <template #trigger="{toggle, triggerAttrs}">
      <SecondaryButton v-bind="triggerAttrs" icon="chevron-down" @click="toggle">
        <span class="font-mono">{{ current || t(`editor.switcher.${kind}`) }}</span>
      </SecondaryButton>
    </template>
    <template #default="{close}">
      <div class="flex flex-col gap-1">
        <SearchInput v-model="query" :placeholder="t(`editor.switcher.find.${kind}`)"/>
        <div class="flex max-h-80 flex-col overflow-y-auto">
          <BareButton
              v-for="entry in shown"
              :key="entry.id"
              :class="entry.id === current ? 'bg-selected' : 'hover:bg-raised'"
              class="flex min-h-10 items-center gap-2 rounded-control px-2 text-left"
              @click="emit('pick', entry.id); close()"
          >
            <span class="flex-1 truncate font-mono text-sm">{{ entry.id }}</span>
            <span class="truncate text-xs text-ink-faint">{{ entry.detail }}</span>
            <InfoBadge v-if="locks.heldByOther(entry.section)">{{ locks.heldByOther(entry.section) }}</InfoBadge>
            <CountBadge v-if="problems[entry.id]" :count="problems[entry.id] ?? 0"/>
          </BareButton>
        </div>
        <BareButton class="min-h-10 rounded-control px-2 text-left text-sm text-accent-text hover:bg-raised" @click="emit('create'); close()">
          {{ t(`editor.switcher.new.${kind}`) }}
        </BareButton>
      </div>
    </template>
  </Popover>
</template>
