/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
/**
 * One handle on a track for a number in a range, e.g. a chance from 0 to 100. Reports the value while it is dragged and
 * `commit` once it is let go.
 */
const model = defineModel<number>({required: true})

defineProps<{
  min: number
  max: number
  step: number
  /** Names the slider for screen readers. */
  label: string
  /** How screen readers say the value, e.g. a clock time; the number itself otherwise. */
  valueText?: string
  disabled?: boolean
}>()

const emit = defineEmits<{
  commit: [value: number]
}>()
</script>

<template>
  <input
      v-model.number="model"
      :aria-label="label"
      :aria-valuetext="valueText"
      :disabled="disabled"
      :max="max"
      :min="min"
      :step="step"
      class="w-full accent-accent disabled:cursor-not-allowed disabled:opacity-50"
      type="range"
      @change="emit('commit', model)"
  />
</template>
