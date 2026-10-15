/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import LabelledField from '@/components/input/LabelledField.vue'
import NumberInput from '@/components/input/number/NumberInput.vue'
import SliderInput from '@/components/input/range/SliderInput.vue'

/**
 * A chance from 0 to 100 %: a slider, with the number beside it for an exact value. Saved once the slider is let go
 * or the number left.
 */
const props = defineProps<{
  label: string
  value: number | undefined
  help?: string
  readOnly: boolean
}>()

const emit = defineEmits<{
  save: [value: number]
}>()

const draft = ref(0)

watch(() => props.value, value => {
  draft.value = value ?? 0
}, {immediate: true})

function save(): void {
  emit('save', Math.min(100, Math.max(0, Math.round(draft.value))))
}
</script>

<template>
  <LabelledField v-slot="{id}" :help="help" :label="label">
    <div class="flex items-center gap-3">
      <SliderInput v-model="draft" :disabled="readOnly" :label="label" :max="100" :min="0" :step="1" class="min-w-0 flex-1" @commit="save"/>
      <div class="flex w-24 shrink-0 items-center gap-1">
        <NumberInput :id="id" v-model="draft" :disabled="readOnly" @focusout="save"/>
        <span class="text-sm text-ink-muted">%</span>
      </div>
    </div>
  </LabelledField>
</template>
