/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import TextInput from './TextInput.vue'
import {useFloatingPanel} from '@/composables/useFloatingPanel'

/** One suggestion: the text it puts into the field, and how it is shown. */
export interface Suggestion {
  value: string
  /** Shown instead of the value, e.g. an enchantment's name; the value is shown beside it then. */
  label?: string
}

/**
 * A line of text with suggestions in a list under it, in the editor's own style rather than the browser's. The list
 * shows the suggestions whose text or name contains what is typed, the typed part marked; arrow keys move through it,
 * Enter or a click takes one, Escape closes it. Enter with nothing marked is passed on, e.g. to add what was typed.
 * The list scrolls through every match and floats over the page under the field (above it where there is no room),
 * so opening or closing it never changes how far the page scrolls.
 */
defineOptions({inheritAttrs: false})

const model = defineModel<string>({required: true})

const props = withDefaults(defineProps<{
  suggestions: readonly Suggestion[]
  placeholder?: string
  disabled?: boolean
  /** How many suggestions are listed at most; all by default. */
  limit?: number
}>(), {limit: undefined, placeholder: undefined})

const emit = defineEmits<{
  /** Enter was pressed with no suggestion marked. */
  enter: []
  /** A suggestion was taken. */
  pick: [value: string]
}>()

const open = ref(false)
const active = ref(-1)
const listId = useId()
const field = ref<HTMLElement | null>(null)

const matches = computed(() => {
  const typed = model.value.trim().toLowerCase()
  return props.suggestions
    .filter(entry => typed === '' || entry.value.toLowerCase().includes(typed) || entry.label?.toLowerCase().includes(typed))
    .slice(0, props.limit)
})
const shown = computed(() => open.value && matches.value.length > 0)
const {panel, style} = useFloatingPanel(field, shown, 'start')
const width = ref('0')

watch(shown, now => {
  if (now) width.value = `${field.value?.offsetWidth ?? 0}px`
})

watch(model, () => {
  active.value = -1
})

function parts(text: string): {text: string, marked: boolean}[] {
  const typed = model.value.trim()
  const at = typed === '' ? -1 : text.toLowerCase().indexOf(typed.toLowerCase())
  if (at < 0) return [{text, marked: false}]
  return [
    {text: text.slice(0, at), marked: false},
    {text: text.slice(at, at + typed.length), marked: true},
    {text: text.slice(at + typed.length), marked: false},
  ].filter(part => part.text !== '')
}

function step(by: number): void {
  open.value = true
  if (matches.value.length === 0) return
  active.value = (active.value + by + matches.value.length) % matches.value.length
}

function take(entry: Suggestion | undefined): void {
  if (!entry) return
  model.value = entry.value
  open.value = false
  active.value = -1
  emit('pick', entry.value)
}

function enter(): void {
  if (shown.value && active.value >= 0) take(matches.value[active.value])
  else {
    open.value = false
    emit('enter')
  }
}
</script>

<template>
  <div ref="field" class="min-w-0 flex-1">
    <TextInput
        v-model="model"
        :aria-activedescendant="shown && active >= 0 ? `${listId}-${active}` : undefined"
        :aria-autocomplete="'list'"
        :aria-controls="listId"
        :aria-expanded="shown"
        :disabled="disabled"
        :placeholder="placeholder"
        autocomplete="off"
        role="combobox"
        v-bind="$attrs"
        @blur="open = false"
        @focus="open = true"
        @input="open = true"
        @keydown.down.prevent="step(1)"
        @keydown.enter.prevent="enter"
        @keydown.esc="open = false"
        @keydown.up.prevent="step(-1)"
    />
    <Teleport to="body">
      <ul
          v-show="shown"
          :id="listId"
          ref="panel"
          :style="[style, {width}]"
          class="z-50 flex max-h-72 flex-col overflow-y-auto rounded-panel border border-line bg-surface p-1 shadow-lg"
          role="listbox"
      >
        <li
            v-for="(entry, index) in matches"
            :id="`${listId}-${index}`"
            :key="entry.value"
            :aria-selected="index === active"
            :class="index === active ? 'bg-selected text-ink' : 'text-ink-muted'"
            class="flex cursor-pointer flex-col rounded-control px-2 py-1.5 text-sm hover:bg-raised hover:text-ink"
            role="option"
            tabindex="-1"
            @mousedown.prevent="take(entry)"
        >
          <span class="min-w-0 break-words">
            <span v-for="(part, at) in parts(entry.label ?? entry.value)" :key="at" :class="part.marked ? 'font-semibold text-accent-text' : ''">{{ part.text }}</span>
          </span>
          <span v-if="entry.label" class="min-w-0 break-all font-mono text-xs text-ink-faint">{{ entry.value }}</span>
        </li>
      </ul>
    </Teleport>
  </div>
</template>
