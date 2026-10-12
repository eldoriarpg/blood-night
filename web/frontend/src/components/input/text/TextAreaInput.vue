/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {BORDERED_INPUT_CLASSES} from '../inputClasses'

/** Several lines of text, e.g. one command per line or a value written as JSON. */
const model = defineModel<string>()

defineProps<{
  label: string
  rows?: number
  disabled?: boolean
  mono?: boolean
}>()
</script>

<template>
  <textarea
      v-model="model"
      :aria-label="label"
      :class="[BORDERED_INPUT_CLASSES, mono && 'font-mono text-xs']"
      :disabled="disabled"
      :rows="rows ?? 3"
      class="min-h-20"
  />
</template>
