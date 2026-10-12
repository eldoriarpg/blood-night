/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import BareButton from '@/components/button/BareButton.vue'
import FloatingPanel from '@/components/feedback/FloatingPanel.vue'
import TextInput from '@/components/input/text/TextInput.vue'
import type {Suggestion} from '~/util/graph/graphView'

/**
 * A text field on a node that suggests values while typing, such as the variables or routines a graph already uses,
 * each with what it holds. The suggestions open below the field, narrowed by what is typed; arrow keys and enter pick
 * one, escape closes them, and any other text is kept as typed. The value is saved on enter, on leaving the field or
 * on picking.
 */
const props = defineProps<{
  value: string | undefined
  label: string
  suggestions: readonly Suggestion[]
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [value: string | undefined]
}>()

const draft = ref<string | undefined>(props.value)
const focused = ref(false)
const dismissed = ref(false)
const active = ref(-1)

watch(() => props.value, value => {
  draft.value = value
})

const matches = computed(() => {
  const needle = (draft.value ?? '').trim().toLowerCase()
  return props.suggestions.filter(suggestion => needle === '' || suggestion.value.toLowerCase().includes(needle))
})
const open = computed({
  get: () => focused.value && !dismissed.value && !props.readOnly && matches.value.length > 0,
  set: now => {
    if (!now) dismissed.value = true
  },
})

watch(draft, () => {
  active.value = -1
  dismissed.value = false
})

function step(by: number): void {
  if (matches.value.length === 0) return
  dismissed.value = false
  active.value = (active.value + by + matches.value.length) % matches.value.length
}

function save(): void {
  const value = draft.value === '' ? undefined : draft.value
  if (value !== props.value) emit('save', value)
}

function pick(suggestion: Suggestion): void {
  draft.value = suggestion.value
  dismissed.value = true
  save()
}

function enter(): void {
  const suggestion = open.value ? matches.value[active.value] : undefined
  if (suggestion) pick(suggestion)
  else save()
}

function blur(): void {
  focused.value = false
  save()
}
</script>

<template>
  <FloatingPanel v-model:open="open" :label="label" align="start" panel-class="flex min-w-40 flex-col p-1" role="dialog">
    <template #trigger>
      <TextInput
          v-model="draft"
          :aria-expanded="open"
          :aria-label="label"
          :disabled="readOnly"
          aria-autocomplete="list"
          class="nodrag"
          compact
          role="combobox"
          @focus="focused = true; dismissed = false"
          @focusout="blur"
          @keydown.down.prevent="step(1)"
          @keydown.enter.prevent="enter"
          @keydown.esc="dismissed = true"
          @keydown.up.prevent="step(-1)"
      />
    </template>
    <div :aria-label="label" class="flex flex-col" role="listbox">
      <BareButton
          v-for="(suggestion, index) in matches"
          :key="suggestion.value"
          :aria-selected="index === active"
          :class="index === active ? 'bg-selected' : 'hover:bg-hover'"
          class="flex min-h-7 items-center gap-3 rounded-control px-2 text-left text-xs"
          role="option"
          tabindex="-1"
          @focus="active = index"
          @mousedown.prevent="pick(suggestion)"
          @mouseenter="active = index"
      >
        <span class="min-w-0 flex-1 truncate text-ink">{{ suggestion.value }}</span>
        <span v-if="suggestion.hint" class="shrink-0 text-ink-faint">{{ suggestion.hint }}</span>
      </BareButton>
    </div>
  </FloatingPanel>
</template>
