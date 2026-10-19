/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import LabelledField from '@/components/input/LabelledField.vue'
import DecimalInput from '@/components/input/number/DecimalInput.vue'

/**
 * A number of a particle effect, typed and saved when the field is left or Enter is pressed. Whole numbers round, and
 * the value is kept within its limits.
 */
const props = withDefaults(defineProps<{
  label: string
  value: number | undefined
  readOnly: boolean
  whole?: boolean
  min?: number
  max?: number
  help?: string
}>(), {
  min: Number.NEGATIVE_INFINITY,
  max: Number.POSITIVE_INFINITY,
  help: undefined,
})

const emit = defineEmits<{
  save: [value: number]
}>()

const draft = ref<number | undefined>(undefined)

watch(() => props.value, value => {
  draft.value = value
}, {immediate: true})

function save(): void {
  if (draft.value === undefined || Number.isNaN(draft.value)) {
    draft.value = props.value
    return
  }
  const rounded = props.whole ? Math.round(draft.value) : draft.value
  const kept = Math.min(Math.max(rounded, props.min), props.max)
  draft.value = kept
  if (kept !== props.value) emit('save', kept)
}
</script>

<template>
  <LabelledField v-slot="{id}" :help="help" :label="label">
    <DecimalInput :id="id" v-model="draft" :disabled="readOnly" :step="whole ? '1' : '0.05'" @focusout="save" @keydown.enter.prevent="save"/>
  </LabelledField>
</template>
