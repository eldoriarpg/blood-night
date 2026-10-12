/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useId} from 'vue'
import {BORDERED_INPUT_CLASSES, BORDERLESS_INPUT_CLASSES, COMPACT_INPUT_CLASSES} from './inputClasses'

/**
 * The field every typed input is built from: full width, bordered unless told otherwise, and
 * carrying an id a label can point at, generated where the caller gives none.
 */
const model = defineModel<string | number>()

const props = defineProps<{
  type?: 'text' | 'number' | 'password' | 'email'
  placeholder?: string
  disabled?: boolean
  step?: string
  borderless?: boolean
  /** The small variant drawn inside graph nodes. */
  compact?: boolean
  /** The standard autocomplete token(s) for the field, such as "off" or "username". */
  autocomplete?: string
  /** What a label's `for` names. Every field carries one, generated where the caller gives none. */
  id?: string
}>()

const generatedId = useId()
</script>

<template>
  <input
      :id="id ?? generatedId"
      v-model="model"
      :disabled="disabled"
      :placeholder="placeholder"
      :step="step"
      :type="type ?? 'text'"
      :autocomplete="autocomplete"
      :class="['w-full', props.compact ? COMPACT_INPUT_CLASSES : props.borderless ? BORDERLESS_INPUT_CLASSES : BORDERED_INPUT_CLASSES]"
  />
</template>
