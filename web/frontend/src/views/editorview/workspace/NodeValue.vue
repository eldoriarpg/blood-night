/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import LabelledField from '@/components/input/LabelledField.vue'
import TextInput from '@/components/input/text/TextInput.vue'
import DecimalInput from '@/components/input/number/DecimalInput.vue'
import ColorInput from '@/components/input/color/ColorInput.vue'
import CheckboxInput from '@/components/input/toggle/CheckboxInput.vue'
import ToggleSetting from '@/components/input/toggle/ToggleSetting.vue'
import ChoicePicker from '~/views/editorview/form/ChoicePicker.vue'
import SuggestInput from '~/views/editorview/workspace/SuggestInput.vue'
import type {PinKind} from '~/util/graph/pins'
import type {Suggestion} from '~/util/graph/graphView'

/**
 * A value typed into a node: a switch for booleans, a number, a dropdown for enums (searchable when there are many), a
 * colour picker for colours, text otherwise. An empty field clears the value. Compact, it is the small field drawn on
 * the node itself, labelled for screen readers only.
 */
const props = defineProps<{
  label: string
  pin: PinKind
  options: readonly string[]
  /** Names shown for options, e.g. `Living Entity` for `living_entity`; an option without one shows itself. */
  labels?: Readonly<Record<string, string>>
  /** How the value is typed in when not by its shape, e.g. `color`. */
  format?: string
  value: unknown
  readOnly: boolean
  compact?: boolean
  /** Values offered while typing text, e.g. the variable names already in use. */
  suggestions?: readonly Suggestion[]
}>()

const emit = defineEmits<{
  save: [value: unknown]
}>()

const draft = ref<string | number | undefined>(undefined)
const numberDraft = computed<number | undefined>({
  get: () => typeof draft.value === 'number' ? draft.value : undefined,
  set: value => {
    draft.value = value
  },
})
const textDraft = computed<string | undefined>({
  get: () => typeof draft.value === 'string' ? draft.value : undefined,
  set: value => {
    draft.value = value
  },
})
const choice = computed(() => typeof props.value === 'string' ? props.value : null)
const colour = computed(() => typeof props.value === 'string' ? props.value : '#ffffff')

watch(() => props.value, value => {
  draft.value = typeof value === 'number' || typeof value === 'string' ? value : value === undefined || value === null ? undefined : JSON.stringify(value)
}, {immediate: true})

function save(): void {
  const value = draft.value === '' ? undefined : draft.value
  if (value !== props.value) emit('save', value)
}
</script>

<template>
  <template v-if="compact">
    <CheckboxInput v-if="pin === 'boolean'" :aria-label="label" :disabled="readOnly" :model-value="Boolean(value)" class="nodrag" @update:model-value="on => emit('save', on)"/>
    <ColorInput v-else-if="format === 'color'" :disabled="readOnly" :label="label" :model-value="colour" class="nodrag" compact @update:model-value="hex => emit('save', hex)"/>
    <ChoicePicker v-else-if="options.length > 0" :label="label" :labels="labels" :model-value="choice ?? undefined" :options="options" :read-only="readOnly" compact @update:model-value="picked => emit('save', picked)"/>
    <DecimalInput v-else-if="pin === 'number'" v-model="numberDraft" :aria-label="label" :disabled="readOnly" class="nodrag nowheel" compact @focusout="save" @keydown.enter="save"/>
    <SuggestInput v-else-if="suggestions?.length" :label="label" :read-only="readOnly" :suggestions="suggestions" :value="choice ?? undefined" @save="text => emit('save', text)"/>
    <TextInput v-else v-model="textDraft" :aria-label="label" :disabled="readOnly" class="nodrag" compact @focusout="save" @keydown.enter="save"/>
  </template>
  <ToggleSetting v-else-if="pin === 'boolean'" :disabled="readOnly" :label="label" :model-value="Boolean(value)" @update:model-value="on => emit('save', on)"/>
  <LabelledField v-else-if="format === 'color'" v-slot="{id}" :label="label">
    <ColorInput :id="id" :disabled="readOnly" :model-value="colour" @update:model-value="hex => emit('save', hex)"/>
  </LabelledField>
  <LabelledField v-else-if="options.length > 0" :label="label">
    <ChoicePicker :label="label" :labels="labels" :model-value="choice ?? undefined" :options="options" :read-only="readOnly" @update:model-value="picked => emit('save', picked)"/>
  </LabelledField>
  <LabelledField v-else-if="pin === 'number'" v-slot="{id}" :label="label">
    <DecimalInput :id="id" v-model="numberDraft" :disabled="readOnly" @focusout="save"/>
  </LabelledField>
  <LabelledField v-else v-slot="{id}" :label="label">
    <TextInput :id="id" v-model="textDraft" :disabled="readOnly" @focusout="save"/>
  </LabelledField>
</template>
