/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import BaseInput from '../BaseInput.vue'

/** A number with a fractional part, stepping by hundredths unless told otherwise. */
const model = defineModel<number>()

const props = withDefaults(defineProps<{
  placeholder?: string
  disabled?: boolean
  step?: string
  /** The small variant drawn inside graph nodes. */
  compact?: boolean
}>(), {
  placeholder: undefined,
  step: '0.01',
})
</script>

<template>
  <BaseInput
      v-model="model"
      :compact="compact"
      :disabled="disabled"
      :placeholder="placeholder"
      :step="props.step"
      type="number"
  />
</template>
