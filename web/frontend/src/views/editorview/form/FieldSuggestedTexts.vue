/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import LabelledField from '@/components/input/LabelledField.vue'
import SuggestInput from '@/components/input/text/SuggestInput.vue'
import MutedIconButton from '@/components/button/MutedIconButton.vue'

/**
 * A list of texts the server suggests values for, e.g. world names or biomes: each entry with a remove button, and a
 * field that suggests while typing and adds a suggestion when it is taken, or its text on Enter. Texts it does not
 * suggest are kept as typed.
 */
const props = defineProps<{
  label: string
  value: readonly string[] | undefined
  suggestions: readonly string[]
  /** Names to show instead of the texts, by text, e.g. a biome's name; the text is shown beside it then. */
  labels?: Readonly<Record<string, string>>
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [value: string[]]
}>()

const {t} = useI18n()
const draft = ref('')
const entries = computed(() => props.value ?? [])
const offered = computed(() => props.suggestions.filter(suggestion => !entries.value.includes(suggestion)))

function add(): void {
  const text = draft.value.trim()
  draft.value = ''
  if (text === '' || entries.value.includes(text)) return
  emit('save', [...entries.value, text])
}

function remove(text: string): void {
  emit('save', entries.value.filter(entry => entry !== text))
}
</script>

<template>
  <LabelledField v-slot="{id}" :label="label">
    <ul v-if="entries.length > 0" class="flex flex-wrap gap-1">
      <li v-for="entry in entries" :key="entry" class="flex items-center gap-1 rounded-control border border-line-soft pl-2 font-mono text-sm">
        <template v-if="labels?.[entry]"><span class="font-sans">{{ labels[entry] }}</span> <span class="text-xs text-ink-faint">{{ entry }}</span></template>
        <template v-else>{{ entry }}</template>
        <MutedIconButton v-if="!readOnly" :label="t('editor.form.remove', {entry: labels?.[entry] ?? entry})" hover="error" icon="xmark" @click="remove(entry)"/>
      </li>
    </ul>
    <SuggestInput
        v-if="!readOnly"
        :id="id"
        v-model="draft"
        :placeholder="t('editor.form.addSuggested')"
        :suggestions="offered.map(value => ({value, label: labels?.[value]}))"
        class="font-mono"
        @enter="add"
        @pick="add"
    />
  </LabelledField>
</template>
