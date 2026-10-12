/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import AppIcon from '@/components/display/AppIcon.vue'

/**
 * A banner saying how something stands. An error is an `alert`, which a screen reader announces
 * the moment it appears; anything else is a `status`, announced once the reader is free. `held` is
 * the warning colour, for something another person currently holds.
 */
type Variant = 'info' | 'success' | 'held' | 'error'

defineProps<{
  variant: Variant
}>()

const BOX_CLASSES: Record<Variant, string> = {
  info: 'border-line bg-raised text-ink',
  success: 'border-pin-number/50 bg-raised text-ink',
  held: 'border-held/50 bg-held-bg text-held',
  error: 'border-problem/50 bg-problem-bg text-problem',
}

const ICONS: Record<Variant, string[]> = {
  info: ['fas', 'circle-info'],
  success: ['fas', 'circle-check'],
  held: ['fas', 'triangle-exclamation'],
  error: ['fas', 'triangle-exclamation'],
}
</script>

<template>
  <div
      :role="variant === 'error' ? 'alert' : 'status'"
      :class="BOX_CLASSES[variant]"
      class="flex items-start gap-3 rounded-panel border p-4"
  >
    <AppIcon :icon="ICONS[variant]" :class="variant === 'success' ? 'text-pin-number' : ''" class="mt-0.5 shrink-0"/>
    <div class="text-sm">
      <slot/>
    </div>
  </div>
</template>
