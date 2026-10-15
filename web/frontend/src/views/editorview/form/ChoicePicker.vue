/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import BareButton from '@/components/button/BareButton.vue'
import Popover from '@/components/feedback/Popover.vue'
import SearchInput from '@/components/input/text/SearchInput.vue'
import MutedText from '@/components/typography/MutedText.vue'
import AppIcon from '@/components/display/AppIcon.vue'
import {BORDERED_INPUT_CLASSES, COMPACT_INPUT_CLASSES} from '@/components/input/inputClasses'
import {searchLabelled} from '~/util/graph/choiceLabels'

/**
 * The editor's dropdown: a field showing the chosen one's name, opening a list of names. A long list is searchable and
 * shows each choice's key beside its name; every word searched for must match a name or a key (see
 * {@link searchLabelled}), and only the first matches are listed, so typing narrows the rest down. A short list is
 * just the names. Arrow keys and enter pick in both. The compact variant is drawn inside graph nodes, where a press on
 * it must neither drag the node nor scroll the canvas.
 */
const model = defineModel<string | undefined>({required: true})

const props = defineProps<{
  label: string
  options: readonly string[]
  /** The choices' names by key; a key without one is shown as it is. */
  labels?: Readonly<Record<string, string>>
  readOnly?: boolean
  /** Whether the choice can be emptied. */
  clearable?: boolean
  /** The small variant drawn inside graph nodes. */
  compact?: boolean
  /** What the field says while nothing is picked, e.g. what picking does. */
  placeholder?: string
}>()

const {t} = useI18n()
const open = ref(false)
const query = ref('')
const active = ref(0)
const LIMIT = 200
const SEARCHED_FROM = 13
const list = ref<HTMLElement | null>(null)
const searchable = computed(() => props.options.length >= SEARCHED_FROM)

const names = computed(() => props.labels ?? {})
const matches = computed(() => searchLabelled(props.options, names.value, query.value))
const shown = computed(() => matches.value.slice(0, LIMIT))

watch(query, () => {
  active.value = 0
})

watch(open, now => {
  if (now) {
    query.value = ''
    active.value = Math.max(0, shown.value.indexOf(model.value ?? ''))
    if (!searchable.value) void nextTick(() => list.value?.focus())
  }
})

function nameOf(option: string): string {
  return names.value[option] ?? option
}

function step(by: number): void {
  if (shown.value.length > 0) active.value = (active.value + by + shown.value.length) % shown.value.length
}

function pick(option: string | undefined): void {
  if (option === undefined) return
  model.value = option
  open.value = false
}

function clear(): void {
  model.value = undefined
  open.value = false
}
</script>

<template>
  <Popover v-model:open="open" :label="label" align="start" :panel-class="searchable ? 'flex w-80 max-w-[90vw] flex-col gap-2 p-2' : 'flex min-w-48 max-w-[90vw] flex-col p-1'" role="dialog">
    <template #trigger="{toggle, triggerAttrs}">
      <BareButton
          :aria-label="label"
          :class="compact ? [COMPACT_INPUT_CLASSES, 'nodrag nowheel w-full'] : [BORDERED_INPUT_CLASSES, 'w-full']"
          :disabled="readOnly"
          class="flex items-center gap-2 text-left"
          v-bind="triggerAttrs"
          @click="toggle"
      >
        <span class="min-w-0 flex-1 truncate">{{ model === undefined ? placeholder ?? t('editor.choicePicker.none') : nameOf(model) }}</span>
        <span v-if="model !== undefined && !compact && searchable" class="truncate font-mono text-xs text-ink-faint">{{ model }}</span>
        <AppIcon :class="compact ? 'text-[10px]' : 'text-xs'" aria-hidden="true" class="shrink-0 text-ink-faint" icon="chevron-down"/>
      </BareButton>
    </template>
    <SearchInput
        v-if="searchable"
        v-model="query"
        :aria-label="t('editor.choicePicker.search')"
        :placeholder="t('editor.choicePicker.search')"
        @keydown.down.prevent="step(1)"
        @keydown.enter.prevent="pick(shown[active])"
        @keydown.up.prevent="step(-1)"
    />
    <div
        ref="list"
        :aria-label="label"
        class="flex max-h-80 flex-col overflow-y-auto outline-none"
        role="listbox"
        tabindex="-1"
        @keydown.down.prevent="step(1)"
        @keydown.enter.prevent="pick(shown[active])"
        @keydown.up.prevent="step(-1)"
    >
      <BareButton v-if="clearable" class="flex min-h-8 items-center rounded-control px-2 text-left text-sm text-ink-muted hover:bg-raised" @click="clear">
        {{ t('editor.choicePicker.clear') }}
      </BareButton>
      <BareButton
          v-for="(option, index) in shown"
          :key="option"
          :aria-selected="option === model"
          :class="index === active ? 'bg-selected' : 'hover:bg-raised'"
          class="flex min-h-8 items-center gap-2 rounded-control px-2 text-left text-sm"
          role="option"
          @click="pick(option)"
          @focus="active = index"
          @mouseenter="active = index"
      >
        <span class="min-w-0 flex-1 truncate">{{ nameOf(option) }}</span>
        <span v-if="searchable" class="max-w-36 shrink-0 truncate font-mono text-xs text-ink-faint">{{ option }}</span>
      </BareButton>
      <MutedText v-if="matches.length === 0" class="px-2 py-1" size="sm" tag="p">{{ t('editor.choicePicker.noMatch') }}</MutedText>
      <MutedText v-else-if="matches.length > shown.length" class="px-2 py-1" size="sm" tag="p">
        {{ t('editor.choicePicker.more', {count: matches.length - shown.length}) }}
      </MutedText>
    </div>
  </Popover>
</template>
