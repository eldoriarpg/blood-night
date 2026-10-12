/*
 *     SPDX-License-Identifier: AGPL-3.0-only
 *
 *     Copyright (C) RainbowDashLabs and Contributor
 */
<script lang="ts" setup>
import AppIcon from '@/components/display/AppIcon.vue'

/**
 * The shape every labelled button shares: padding, the control radius, a label that never wraps and
 * the disabled state. The colour variants put their own fill on top of it.
 */
defineProps<{
  disabled?: boolean
  fullWidth?: boolean
  icon?: string | string[] | null
  compact?: boolean
}>()

defineEmits<{
  click: [event: MouseEvent]
}>()
</script>

<template>
  <button
      type="button"
      :disabled="disabled"
      :class="[
        fullWidth ? 'w-full justify-center' : '',
        compact ? 'px-2 py-1 text-xs' : 'px-3 py-1.5 text-sm',
      ]"
      class="inline-flex items-center gap-1.5 whitespace-nowrap rounded-control border font-medium transition-colors duration-150 disabled:opacity-50 disabled:cursor-not-allowed"
      @click="$emit('click', $event)"
  >
    <AppIcon v-if="icon" :icon="icon"/>
    <slot/>
  </button>
</template>
