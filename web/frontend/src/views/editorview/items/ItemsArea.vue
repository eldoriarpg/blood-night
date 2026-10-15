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
import ItemForm from '~/views/editorview/items/ItemForm.vue'
import NewItemDialog from '~/views/editorview/items/NewItemDialog.vue'
import {section} from '~/util/session/sections'
import {itemEntries} from '~/util/session/itemEntries'
import {searchChoices} from '~/util/items/itemChoices'

/**
 * The items mobs wear and drop: the items in a sidebar and the picked one's form. Opening an item takes its section;
 * an item someone else holds is shown read only.
 */
const session = useEditorSession()
const locks = useEditorLocks()
const {t} = useI18n()
const assets = useItemAssets()
const picked = ref('')
const creating = ref(false)
const query = ref('')
const searchable = assets.choices(true, false)

const items = computed(() => itemEntries(session.payload.value))
const found = computed(() => {
  const keys = new Set(searchChoices(searchable.value, query.value).map(choice => choice.key))
  return items.value.filter(candidate => keys.has(candidate.id))
})
const details = computed(() => new Map(searchable.value.map(choice => [choice.key, choice.details])))

onMounted(() => {
  void assets.load()
})
const entry = computed(() => items.value.find(candidate => candidate.id === picked.value) ?? items.value[0])

watch(() => entry.value?.id, (now, before) => {
  if (before) void locks.release(section.item(before))
  if (now) void locks.take(section.item(now))
}, {immediate: true})

onBeforeUnmount(() => {
  if (entry.value) void locks.release(section.item(entry.value.id))
})
</script>

<template>
  <div class="flex min-h-0 flex-1">
    <nav :aria-label="t('editor.items.list')" class="flex w-60 shrink-0 flex-col gap-1 overflow-y-auto border-r border-line-soft bg-surface p-3">
      <SectionLabel class="px-2 pb-1">{{ t('editor.items.list') }}</SectionLabel>
      <SearchInput v-model="query" :aria-label="t('editor.itemPicker.search')" :placeholder="t('editor.itemPicker.search')" class="mb-1"/>
      <BareButton
          v-for="candidate in found"
          :key="candidate.id"
          :class="candidate.id === entry?.id ? 'bg-selected' : 'hover:bg-raised'"
          class="flex min-h-10 items-center gap-2 rounded-control px-2 text-left"
          @click="picked = candidate.id"
      >
        <span class="min-w-0 flex-1">
          <span class="block truncate font-mono text-sm">{{ candidate.id }}</span>
          <span class="block truncate text-xs text-ink-faint">{{ details.get(candidate.id) || t(`editor.items.kind.${candidate.item.kind}`) }}</span>
        </span>
        <InfoBadge v-if="locks.heldByOther(section.item(candidate.id))">{{ locks.heldByOther(section.item(candidate.id)) }}</InfoBadge>
      </BareButton>
      <SecondaryButton class="mt-2" compact icon="plus" @click="creating = true">{{ t('editor.items.new') }}</SecondaryButton>
    </nav>
    <ItemForm v-if="entry" :key="entry.id" :entry="entry" class="min-w-0 flex-1" @deleted="picked = ''"/>
    <EmptyState v-else :message="t('editor.items.empty')" class="flex-1"/>
    <NewItemDialog v-model="creating" :taken="items.map(candidate => candidate.id)" @created="id => picked = id"/>
  </div>
</template>
