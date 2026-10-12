/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useId} from 'vue'

/** A colour as `#rrggbb`, picked with the browser's colour picker from a swatch. */
const model = defineModel<string>()

defineProps<{
  disabled?: boolean
  /** The small swatch drawn inside graph nodes. */
  compact?: boolean
  /** What a label's `for` names. Every field carries one, generated where the caller gives none. */
  id?: string
  /** The name read out when no label points at the swatch. */
  label?: string
}>()

const generatedId = useId()
</script>

<template>
  <input
      :id="id ?? generatedId"
      v-model="model"
      :aria-label="label"
      :class="compact ? 'h-6 w-10' : 'h-9 w-16'"
      :disabled="disabled"
      class="cursor-pointer appearance-none rounded-control bg-surface p-0.5 ring-1 ring-inset ring-line disabled:cursor-not-allowed disabled:opacity-50"
      type="color"
  />
</template>
