/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import AppIcon from '@/components/display/AppIcon.vue'

/** An on and off switch: the accent fill with a check while on, the raised surface while off. */
const model = defineModel<boolean>({default: false})

defineProps<{
  disabled?: boolean
}>()
</script>

<template>
  <button
      :aria-checked="model"
      :class="model ? 'border-accent bg-accent' : 'border-line bg-raised'"
      :disabled="disabled"
      class="relative inline-flex h-6 w-11 shrink-0 cursor-pointer items-center rounded-full border transition-colors duration-150 disabled:opacity-50 disabled:cursor-not-allowed"
      role="switch"
      type="button"
      @click="model = !model"
  >
    <span
        :class="model ? 'translate-x-5.5 bg-accent-contrast text-accent' : 'translate-x-0.5 bg-ink-muted text-raised'"
        class="pointer-events-none inline-flex h-4.5 w-4.5 items-center justify-center rounded-full transition-transform duration-150"
    >
      <AppIcon :icon="model ? ['fas', 'check'] : ['fas', 'xmark']" class="h-2.5 w-2.5"/>
    </span>
  </button>
</template>
