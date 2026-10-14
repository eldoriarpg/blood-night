/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import LabelledField from '@/components/input/LabelledField.vue'
import TextInput from '@/components/input/text/TextInput.vue'
import MutedIconButton from '@/components/button/MutedIconButton.vue'

/**
 * A list of texts the server suggests values for, e.g. world names or biomes: each entry with a remove button, and a
 * field that suggests while typing and adds its text on Enter. Texts it does not suggest are kept as typed.
 */
const props = defineProps<{
  label: string
  value: readonly string[] | undefined
  suggestions: readonly string[]
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [value: string[]]
}>()

const {t} = useI18n()
const draft = ref('')
const listId = useId()
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
        {{ entry }}
        <MutedIconButton v-if="!readOnly" :label="t('editor.form.remove', {entry})" hover="error" icon="xmark" @click="remove(entry)"/>
      </li>
    </ul>
    <TextInput
        v-if="!readOnly"
        :id="id"
        v-model="draft"
        :list="listId"
        :placeholder="t('editor.form.addSuggested')"
        @change="add"
        @keydown.enter.prevent="add"
    />
    <datalist :id="listId">
      <option v-for="suggestion in offered" :key="suggestion" :value="suggestion"/>
    </datalist>
  </LabelledField>
</template>
