/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import {useId} from 'vue'
import FieldLabel from '@/components/typography/FieldLabel.vue'
import MutedText from '@/components/typography/MutedText.vue'
import ToggleInput from '@/components/input/toggle/ToggleInput.vue'

/** One setting that is on or off: its label and an optional hint on the left, the switch on the right. */
const model = defineModel<boolean>({required: true})

defineProps<{
  label: string
  hint?: string
  disabled?: boolean
}>()

const labelId = useId()
</script>

<template>
  <div class="flex items-center justify-between gap-4">
    <div>
      <FieldLabel :id="labelId">{{ label }}</FieldLabel>
      <MutedText v-if="hint" tag="p">{{ hint }}</MutedText>
    </div>
    <ToggleInput v-model="model" :aria-labelledby="labelId" :disabled="disabled"/>
  </div>
</template>
