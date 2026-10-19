/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import BareButton from '@/components/button/BareButton.vue'
import Popover from '@/components/feedback/Popover.vue'
import SearchInput from '@/components/input/text/SearchInput.vue'
import InfoBadge from '@/components/badge/InfoBadge.vue'
import MutedText from '@/components/typography/MutedText.vue'
import {BORDERED_INPUT_CLASSES} from '@/components/input/inputClasses'
import {searchChoices, type ItemChoice} from '~/util/items/itemChoices'

/**
 * Picks an item or material: a field showing the chosen one's icon and name, opening a searchable list of icons and
 * names in the editor's language. Every word searched for must match the key, a name, the type, the lore or an
 * enchantment (see {@link searchChoices}); arrow keys and enter pick. Only the first matches are listed, so typing
 * narrows the rest down.
 */
const model = defineModel<string | undefined>({required: true})

const props = defineProps<{
  label: string
  choices: readonly ItemChoice[]
  readOnly?: boolean
  /** Whether the field can be emptied, e.g. an equipment slot left as it is. */
  clearable?: boolean
}>()

const {t} = useI18n()
const assets = useItemAssets()
const open = ref(false)
const LIMIT = 120

const chosen = computed(() => props.choices.find(choice => choice.key === model.value))
const {query, active, matches, shown, step} = usePickerSearch(open, query => searchChoices(props.choices, query), LIMIT, choice => choice.key === model.value)

onMounted(() => {
  void assets.load()
})

function pick(choice: ItemChoice | undefined): void {
  if (!choice) return
  model.value = choice.key
  open.value = false
}

function clear(): void {
  model.value = undefined
  open.value = false
}
</script>

<template>
  <Popover v-model:open="open" :label="label" align="start" panel-class="flex w-96 max-w-[90vw] flex-col gap-2 p-2" role="dialog">
    <template #trigger="{toggle, triggerAttrs}">
      <BareButton
          :aria-label="label"
          :class="BORDERED_INPUT_CLASSES"
          :disabled="readOnly"
          class="flex w-full items-center gap-2 text-left"
          v-bind="triggerAttrs"
          @click="toggle"
      >
        <img v-if="assets.icon(chosen?.texture)" :src="assets.icon(chosen?.texture)" alt="" class="bn-icon size-6 shrink-0"/>
        <span v-else aria-hidden="true" class="size-6 shrink-0 rounded-sm bg-raised"/>
        <span class="min-w-0 flex-1 truncate">{{ chosen?.name ?? model ?? t('editor.itemPicker.none') }}</span>
        <span v-if="model" class="truncate font-mono text-xs text-ink-faint">{{ model }}</span>
      </BareButton>
    </template>
    <SearchInput
        v-model="query"
        :aria-label="t('editor.itemPicker.search')"
        :placeholder="t('editor.itemPicker.search')"
        @keydown.down.prevent="step(1)"
        @keydown.enter.prevent="pick(shown[active])"
        @keydown.up.prevent="step(-1)"
    />
    <MutedText v-if="assets.status.value === 'loading'" class="px-2" size="sm" tag="p">{{ t('editor.itemPicker.loading') }}</MutedText>
    <MutedText v-else-if="assets.status.value === 'failed'" class="px-2" size="sm" tag="p">{{ t('editor.itemPicker.failed') }}</MutedText>
    <div class="flex max-h-80 flex-col overflow-y-auto" role="listbox" :aria-label="label">
      <BareButton v-if="clearable" class="flex min-h-9 items-center gap-2 rounded-control px-2 text-left text-sm text-ink-muted hover:bg-raised" @click="clear">
        {{ t('editor.itemPicker.clear') }}
      </BareButton>
      <BareButton
          v-for="(choice, index) in shown"
          :key="choice.key"
          :aria-selected="choice.key === model"
          :class="index === active ? 'bg-selected' : 'hover:bg-raised'"
          class="flex min-h-9 items-center gap-2 rounded-control px-2 text-left text-sm"
          role="option"
          @click="pick(choice)"
          @focus="active = index"
          @mouseenter="active = index"
      >
        <img v-if="assets.icon(choice.texture)" :src="assets.icon(choice.texture)" alt="" class="bn-icon size-6 shrink-0" loading="lazy"/>
        <span v-else aria-hidden="true" class="size-6 shrink-0 rounded-sm bg-raised"/>
        <span class="min-w-0 flex-1">
          <span class="block truncate">{{ choice.name }}</span>
          <span v-if="choice.details" class="block truncate text-xs text-ink-faint">{{ choice.details }}</span>
        </span>
        <InfoBadge v-if="choice.custom">{{ t('editor.itemPicker.custom') }}</InfoBadge>
        <span class="max-w-36 shrink-0 truncate font-mono text-xs text-ink-faint">{{ choice.key }}</span>
      </BareButton>
      <MutedText v-if="matches.length === 0" class="px-2 py-1" size="sm" tag="p">{{ t('editor.itemPicker.noMatch') }}</MutedText>
      <MutedText v-else-if="matches.length > shown.length" class="px-2 py-1" size="sm" tag="p">
        {{ t('editor.itemPicker.more', {count: matches.length - shown.length}) }}
      </MutedText>
    </div>
  </Popover>
</template>

<style scoped>
.bn-icon {
  image-rendering: pixelated;
  object-fit: cover;
  object-position: top;
}
</style>
