/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script setup lang="ts">
import BareButton from '@/components/button/BareButton.vue'
import MutedText from '@/components/typography/MutedText.vue'

/** One answer of a wizard step to pick: its name and what it means, marked while picked. */
defineProps<{
  label: string
  hint: string
  picked: boolean
}>()
</script>

<template>
  <BareButton
      :aria-pressed="picked"
      :class="picked ? 'border-primary bg-hover' : 'border-line-soft hover:bg-hover'"
      class="flex w-full flex-col items-start rounded-control border px-3 py-2 text-left">
    <span class="font-medium">{{ label }}</span>
    <MutedText size="sm">{{ hint }}</MutedText>
  </BareButton>
</template>
