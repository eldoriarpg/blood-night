/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import TextInput from '@/components/input/text/TextInput.vue'
import MiniMessageHelp from '~/views/editorview/form/MiniMessageHelp.vue'
import {completeTag, tagCandidates} from '~/util/minimessage/completion'

/**
 * A line of MiniMessage with help typing it: Tab completes the tag being typed (see {@link completeTag}), the tags it
 * could become are listed under the field, and the question mark beside it opens a quick reference whose entries
 * are inserted where the caret was. Attributes such as `id`, `aria-label` and `@focusout` go to the text field.
 */
defineOptions({inheritAttrs: false})

const model = defineModel<string>({required: true})

const props = defineProps<{
  disabled?: boolean
  placeholder?: string
  /** Placeholders the text may use and the sample shown for them, e.g. `{name: 'Blood Night'}`. */
  samples?: Readonly<Record<string, string>>
}>()

const {t} = useI18n()
const wrapper = ref<HTMLElement | null>(null)
const caret = ref(0)
const focused = ref(false)
const placeholderNames = computed(() => Object.keys(props.samples ?? {}))
const candidates = computed(() => focused.value ? tagCandidates(model.value, caret.value, placeholderNames.value)?.candidates.slice(0, 6) ?? [] : [])

function input(): HTMLInputElement | null {
  return wrapper.value?.querySelector('input') ?? null
}

function track(): void {
  caret.value = input()?.selectionStart ?? model.value.length
}

function moveCaret(to: number): void {
  caret.value = to
  void nextTick(() => {
    const field = input()
    field?.focus()
    field?.setSelectionRange(to, to)
  })
}

function complete(event: KeyboardEvent): void {
  if (event.shiftKey) return
  track()
  const completed = completeTag(model.value, caret.value, placeholderNames.value)
  if (!completed) return
  event.preventDefault()
  model.value = completed.text
  moveCaret(completed.caret)
}

function insert(tag: string): void {
  if (props.disabled) return
  const at = Math.min(caret.value, model.value.length)
  model.value = model.value.slice(0, at) + tag + model.value.slice(at)
  moveCaret(at + tag.length)
}
</script>

<template>
  <div ref="wrapper" class="flex min-w-0 flex-1 flex-col gap-1">
    <div class="flex items-center gap-1">
      <TextInput
          v-model="model"
          :disabled="disabled"
          :placeholder="placeholder"
          class="font-mono"
          v-bind="$attrs"
          @blur="focused = false"
          @click="track"
          @focus="focused = true"
          @input="track"
          @keydown.tab="complete"
          @keyup="track"
      />
      <MiniMessageHelp v-if="!disabled" :samples="samples" @insert="insert"/>
    </div>
    <p v-if="candidates.length" class="font-mono text-xs text-ink-muted" aria-live="polite">
      {{ t('editor.miniMessage.tab', {tags: candidates.join(', ')}) }}
    </p>
  </div>
</template>
