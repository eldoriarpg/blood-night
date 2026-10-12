/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import BaseInput from '../BaseInput.vue'

/** A single line of text. */
const model = defineModel<string>()

defineProps<{
  placeholder?: string
  disabled?: boolean
  borderless?: boolean
  /** The small variant drawn inside graph nodes. */
  compact?: boolean
  /** Standard autocomplete token(s), e.g. "username webauthn" or "one-time-code". */
  autocomplete?: string
}>()
</script>

<template>
  <BaseInput
      v-model="model"
      :disabled="disabled"
      :placeholder="placeholder"
      :borderless="borderless"
      :compact="compact"
      :autocomplete="autocomplete"
      type="text"
  />
</template>
